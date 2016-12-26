/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ly.appsocial.chatcenter.activity.ChatActivity;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ws.request.PostStickerRequestDto;
import ly.appsocial.chatcenter.dto.ws.response.LiveLocationResponseDto;
import ly.appsocial.chatcenter.util.AuthUtil;
import ly.appsocial.chatcenter.util.NetworkQueueHelper;
import ly.appsocial.chatcenter.widgets.BasicWidget;
import ly.appsocial.chatcenter.ws.ApiRequest;
import ly.appsocial.chatcenter.ws.OkHttpApiRequest;

public class CSLocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		LocationListener {

	private static final String REQUEST_TAG = CSLocationService.class.getCanonicalName();

	private GoogleApiClient mGoogleApiClient;
	private boolean mInProgress;
	private OkHttpApiRequest<LiveLocationResponseDto> mLiveLocationRequest;

	private int mUpdateInterval = 15;
	private String mChannelUid;
	private String mAppToken;
	private String mWidgetId;
	private int mShareTime = 60;
	private Timer mTimer;
	private int mTimerCount = 0;
	final int INTERVAL_PERIOD = 1000;

	private static boolean gStarted = false;

	public static boolean isStarted() {
		return gStarted;
	}

	public CSLocationService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mInProgress = false;
	}

	private LocationRequest createLocationRequest() {
		return new LocationRequest()
				.setInterval(mUpdateInterval * 1000)
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	public int onStartCommand (Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);

		gStarted = true;
		mUpdateInterval = intent.getIntExtra("interval", mUpdateInterval);
		int icon_id = intent.getIntExtra("icon_id", -1);
		String appTitle = intent.getStringExtra("app_name");

		if ( intent.hasExtra("channel_uid")) {
			mChannelUid = intent.getStringExtra("channel_uid");
		}
		if ( intent.hasExtra("app_token")) {
			mAppToken = intent.getStringExtra("app_token");
		}
		if ( intent.hasExtra("widget_id") ){
			mWidgetId = intent.getStringExtra("widget_id");
		}
		if ( intent.hasExtra("share_time") ) {
			// share time is minute
			mShareTime = intent.getIntExtra("share_time", mShareTime);
			if (mTimer != null) {
				mTimer.cancel();
			}
			mTimerCount = 0;
			mTimer = new Timer();
			mTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					mTimerCount++;
					if ( mTimerCount >= mShareTime * 60){
						mTimer.cancel();
						mTimer = null;
						requestStopLiveLocation();
					} else {
						sendBroadCast(true, mShareTime, mTimerCount);
					}
				}
			}, 0, INTERVAL_PERIOD);
		}

		if ( intent.hasExtra("command") ){
			String command = intent.getStringExtra("command");
			if (ChatCenterConstants.LocationService.STOP.equals(command)){
				requestStopLiveLocation();
			}
		}

		if( mGoogleApiClient == null ) {
			synchronized (this) {
				mGoogleApiClient = new GoogleApiClient.Builder(this)
						.addConnectionCallbacks(this)
						.addOnConnectionFailedListener(this)
						.addApi(LocationServices.API)
						.build();
			}
		}

		if ( icon_id != -1 ) {
			Intent activityIntent = new Intent(this, ChatActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);
			Notification notification = new Notification.Builder(this)
					.setContentTitle(appTitle)
					.setContentText(getString(R.string.sending_location))
					.setContentIntent(pendingIntent)
					.setSmallIcon(icon_id)
					.build();
			startForeground(startId, notification);
		}

		if( mGoogleApiClient.isConnected() || mInProgress )
			return START_STICKY;

		if( !mGoogleApiClient.isConnecting() ){
			mInProgress = true;
			mGoogleApiClient.connect();
		}


		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(ChatCenterConstants.BroadcastAction.RELOAD_CHAT);
				getBaseContext().sendBroadcast(broadcastIntent);
			}
		}, 1000);

		return START_STICKY;
	}

	@Override
	public void onLocationChanged(Location location) {
		requestUpdateLocation(location);

		Log.d(REQUEST_TAG, "Location Changed:lon=" + location.getLongitude() + " lat=" + location.getLatitude() );
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		mInProgress = false;

		if (mGoogleApiClient != null) {
			LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
			mGoogleApiClient.unregisterConnectionCallbacks(this);
			mGoogleApiClient.unregisterConnectionFailedListener(this);
			mGoogleApiClient.disconnect();
			mGoogleApiClient = null;
		}

		if ( mTimer != null ){
			mTimer.cancel();
			mTimer = null;
		}

		sendBroadCast(false, mShareTime, mTimerCount);

		gStarted = false;
		super.onDestroy();
	}

	@Override
	public void onConnected(Bundle bundle)
	{
		checkLocationPreference();
	}

	@Override
	public void onConnectionSuspended(int i) {
		mInProgress = false;
		mGoogleApiClient = null;
		CSLocationService.this.stopSelf();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		mInProgress = false;
		mGoogleApiClient = null;
		CSLocationService.this.stopSelf();
	}

	protected void sendBroadCast(boolean sharing, int timer_org, int timer_count) {

		Intent broadcastIntent = new Intent();
		broadcastIntent.putExtra("sharing", sharing);
		broadcastIntent.putExtra("timer_count", timer_count);
		broadcastIntent.putExtra("timer_org", timer_org);
		broadcastIntent.setAction(ChatCenterConstants.BroadcastAction.UPDATE_STATUS);
		getBaseContext().sendBroadcast(broadcastIntent);
	}

	private void requestUpdateLocation(Location location) {
		if (mLiveLocationRequest != null ) {
			return;
		}

		String path = "channels/" + mChannelUid + "/messages";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mLiveLocationRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers, headers, new ApiRequest.Callback<LiveLocationResponseDto>() {
			@Override
			public void onSuccess(LiveLocationResponseDto responseDto) {
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

		mLiveLocationRequest.setApiToken(mAppToken);

		BasicWidget widget = new BasicWidget();
		widget.stickerContent = new BasicWidget.StickerContent();
		widget.stickerContent.stickerData = new BasicWidget.StickerContent.StickerData();
		widget.stickerContent.stickerData.location = new BasicWidget.StickerContent.StickerData.Location();
		widget.stickerContent.stickerData.location.latitude = location.getLatitude();
		widget.stickerContent.stickerData.location.longitude = location.getLongitude();
		widget.replyTo = mWidgetId;
		widget.stickerType = ChatCenterConstants.StickerName.STICKER_TYPE_CO_LOCATION;

		PostStickerRequestDto requestDto = new PostStickerRequestDto();
		requestDto.content = new Gson().toJson(widget).toString();
		requestDto.type = "response";

		mLiveLocationRequest.setJsonBody(requestDto.toJson());
		NetworkQueueHelper.enqueue(mLiveLocationRequest, REQUEST_TAG);
	}

	private void requestStopLiveLocation() {
		if (mLiveLocationRequest != null ) {
			return;
		}

		String path = "channels/" + mChannelUid + "/messages";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mLiveLocationRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers, headers, new ApiRequest.Callback<LiveLocationResponseDto>() {
			@Override
			public void onSuccess(LiveLocationResponseDto responseDto) {
				mLiveLocationRequest = null;
				mWidgetId = null;
				CSLocationService.this.stopSelf();
			}

			@Override
			public void onError(ApiRequest.Error error) {
				mLiveLocationRequest = null;
				mWidgetId = null;
				CSLocationService.this.stopSelf();
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

		mLiveLocationRequest.setApiToken(mAppToken);

		BasicWidget widget = new BasicWidget();
		widget.stickerContent = new BasicWidget.StickerContent();
		widget.stickerContent.stickerData = new BasicWidget.StickerContent.StickerData();
		widget.stickerContent.stickerData.type = "stop";
		widget.stickerType = ChatCenterConstants.StickerName.STICKER_TYPE_CO_LOCATION;
		widget.replyTo = mWidgetId;

		PostStickerRequestDto requestDto = new PostStickerRequestDto();
		requestDto.content = new Gson().toJson(widget).toString();
		requestDto.type = "response";

		mLiveLocationRequest.setJsonBody(requestDto.toJson());
		NetworkQueueHelper.enqueue(mLiveLocationRequest, REQUEST_TAG);
	}

	private static final int REQUEST_LOCATION_SET = 1013;

	private void checkLocationPreference() {
		PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(
				mGoogleApiClient,
				new LocationSettingsRequest.Builder().addLocationRequest(createLocationRequest()).build());
		result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
			@Override
			public void onResult(LocationSettingsResult locationSettingsResult) {
				final Status status = locationSettingsResult.getStatus();
				switch (status.getStatusCode()) {
					case LocationSettingsStatusCodes.SUCCESS:
						LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
								createLocationRequest(), CSLocationService.this);
						break;
					case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
						try {
							status.startResolutionForResult(ChatActivity.getInstance(), REQUEST_LOCATION_SET);
						} catch (IntentSender.SendIntentException e) {
							CSLocationService.this.stopSelf();
						}
						break;
					case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
						CSLocationService.this.stopSelf();
						break;
				}
			}
		});
	}
}