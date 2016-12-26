/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import ly.appsocial.chatcenter.CSLocationService;
import ly.appsocial.chatcenter.ChatCenter;
import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ws.request.PostStickerRequestDto;
import ly.appsocial.chatcenter.dto.ws.response.LiveLocationResponseDto;
import ly.appsocial.chatcenter.util.AuthUtil;
import ly.appsocial.chatcenter.util.NetworkQueueHelper;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;
import ly.appsocial.chatcenter.ws.ApiRequest;
import ly.appsocial.chatcenter.ws.OkHttpApiRequest;

public class LiveLocationActivity extends WebViewActivity {
	private static final String TAG = LiveLocationActivity.class.getSimpleName();

	private View mCommandButton;

	private TextView mButtonLabel;
	private TextView mTimerLabel;
	private WebView mWebView;

	private UpdateReceiver mReceiver;
	private IntentFilter mIntentFilter;

	private boolean mSharingLocation = false;
	private String mSharingLocationId;

	private String mChannelUid;
	private String mAppToken;
	private ApiRequest<LiveLocationResponseDto> mLiveLocationRequest;

	private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3000;
	private static final int REQUEST_LIVE_LOCATION = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_live_location);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setTitle(R.string.location_title);

		mWebView = (WebView)findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		mWebView.setScrollbarFadingEnabled(true);

		Intent intent = getIntent();
		if ( intent.hasExtra("channel_uid")) {
			mChannelUid = intent.getStringExtra("channel_uid");
		}
		if ( intent.hasExtra("app_token")) {
			mAppToken = intent.getStringExtra("app_token");
		}
		if ( intent.hasExtra(ChatCenterConstants.Extra.URL)){
			String url = intent.getStringExtra(ChatCenterConstants.Extra.URL);
			if ( !StringUtil.isBlank(url) ){
				mWebView.loadUrl(url);
			}
		}

		mButtonLabel = (TextView)findViewById(R.id.title);
		mTimerLabel = (TextView)findViewById(R.id.timer);
		mTimerLabel.setVisibility(View.GONE);

		mCommandButton = findViewById(R.id.button);
		mCommandButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if ( mSharingLocation ){
					stopShareLocation();
				} else if (!CSLocationService.isStarted()) {
					if (ContextCompat.checkSelfPermission(LiveLocationActivity.this,
								Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
							ActivityCompat.requestPermissions(LiveLocationActivity.this,
									new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
									PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
					} else {
						openPlacePicker();
					}
				}
			}
		});

		updateStatus(false, 0, 0);

		mReceiver = new UpdateReceiver();
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(ChatCenterConstants.BroadcastAction.UPDATE_STATUS);
		registerReceiver(mReceiver, mIntentFilter);
	}

	@Override
	public void onDestroy(){
		unregisterReceiver(mReceiver);

		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode){
				case REQUEST_LIVE_LOCATION:
					Location location = data.getParcelableExtra("location");
					int share_time = data.getIntExtra("share_time", 15);
					requestStartLiveLocation(location, share_time);
					break;
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// Permission was granted
					openPlacePicker();
				} else {
					// Permission was denied. Possibly show a message indicating the user what happens if the app does have this permission
					new AlertDialog.Builder(this)
							.setTitle(R.string.alert)
							.setMessage(R.string.location_permission_denied)
							.setCancelable(false)
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							})
							.show();
				}
				break;
		}
	}

	private void openPlacePicker() {
		Intent intent = new Intent(LiveLocationActivity.this, ShareLocationActivity.class);
		intent.putExtra("show_preview", false);
		startActivityForResult(intent, REQUEST_LIVE_LOCATION);
	}

	/**
	 * Live Locationの開始要求
	 *
	 */
	private void requestStartLiveLocation(Location location, final int share_time) {
		if (mLiveLocationRequest != null ) {
			return;
		}

//		String progMsg = getResources().getString(R.string.processing);
//		DialogUtil.showProgressDialog(getSupportFragmentManager(), progMsg, DialogUtil.Tag.PROGRESS);

		String path = "channels/" + mChannelUid + "/messages";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mLiveLocationRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers, headers, new ApiRequest.Callback<LiveLocationResponseDto>() {
			@Override
			public void onSuccess(LiveLocationResponseDto responseDto) {
				int interval = 10;
				mSharingLocationId = "";

				if ( responseDto != null ) {
					if (responseDto.content != null
							&& responseDto.content.stickerContent != null && responseDto.content.stickerContent.stickerData != null
							&& responseDto.content.stickerContent.stickerData.preferredInterval != null) {
						interval = Integer.valueOf(responseDto.content.stickerContent.stickerData.preferredInterval);
					}

					if (responseDto.id != null && !responseDto.id.isEmpty()) {
						mSharingLocationId = responseDto.id;
					}
				}

				startLiveLocationSharing(interval, mSharingLocationId, share_time);
				mLiveLocationRequest = null;
			}

			@Override
			public void onError(ApiRequest.Error error) {
				mLiveLocationRequest = null;
			}
		}, new ApiRequest.Parser<LiveLocationResponseDto>() {
			@Override
			public int getErrorCode() {
				return 0;
			}

			@Override
			public LiveLocationResponseDto parser(String response) {
				try {
					return new Gson().fromJson(response, LiveLocationResponseDto.class);
				} catch (Exception e) {
				}
				return null;
			}
		});

		if (mAppToken != null) {
			mLiveLocationRequest.setApiToken(mAppToken);
		}

		BasicWidget widget = new BasicWidget();
		widget.stickerContent = new BasicWidget.StickerContent();
		widget.stickerContent.stickerData = new BasicWidget.StickerContent.StickerData();
		widget.stickerContent.stickerData.type = "start";
		widget.stickerContent.stickerData.location = new BasicWidget.StickerContent.StickerData.Location();
		widget.stickerContent.stickerData.location.longitude = location.getLongitude();
		widget.stickerContent.stickerData.location.latitude = location.getLatitude();
		widget.stickerType = ChatCenterConstants.StickerName.STICKER_TYPE_CO_LOCATION;

		PostStickerRequestDto requestDto = new PostStickerRequestDto();
		requestDto.content = new Gson().toJson(widget).toString();
		requestDto.type = "sticker";

		mLiveLocationRequest.setJsonBody(requestDto.toJson());
		NetworkQueueHelper.enqueue(mLiveLocationRequest, TAG);
	}

	private void startLiveLocationSharing(int interval, String widgetId, int share_time){
		if (!CSLocationService.isStarted()) {
			Intent intent = new Intent(this, CSLocationService.class);
			intent.putExtra("command", ChatCenterConstants.LocationService.START);
			intent.putExtra("icon_id", ChatCenter.mAppIconId);
			intent.putExtra("app_name", ChatCenter.mAppName);
			intent.putExtra("app_token", ChatCenter.mAppToken);
			intent.putExtra("channel_uid", mChannelUid);
			intent.putExtra("interval", interval);
			intent.putExtra("widget_id", widgetId);
			intent.putExtra("share_time", share_time);

			startService(intent);
			mSharingLocation = true;
		}
	}

	private void stopShareLocation(){
		if (CSLocationService.isStarted()) {
			Intent intent = new Intent(LiveLocationActivity.this, CSLocationService.class);
			intent.putExtra("command", ChatCenterConstants.LocationService.STOP);
			startService(intent);
		}
		mSharingLocation = false;
	}

	/**
	 * @param sharing
	 * @param timer_org		minute
	 * @param timer_count	second
	 */
	private void updateStatus(boolean sharing, int timer_org, int timer_count){
		if ( sharing ){
			int remainSec = timer_org*60 - timer_count;
			int min = remainSec / 60;
			int sec = remainSec % 60;
			String secStr = String.format("%02d:%02d", min, sec);

			mTimerLabel.setVisibility(View.VISIBLE);
			mTimerLabel.setText(secStr);
			mButtonLabel.setText(R.string.location_stop);
		} else {
			mTimerLabel.setVisibility(View.GONE);
			mButtonLabel.setText(R.string.location_share_mylive);
		}
		mSharingLocation = sharing;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if ( id == android.R.id.home ){
			Intent intent = new Intent();
			setResult(RESULT_CANCELED, intent);
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	public class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			int timer_org = bundle.getInt("timer_org");
			int timer_count = bundle.getInt("timer_count");
			boolean sharing = bundle.getBoolean("sharing");

			updateStatus(sharing, timer_org, timer_count);
		}
	}
}
