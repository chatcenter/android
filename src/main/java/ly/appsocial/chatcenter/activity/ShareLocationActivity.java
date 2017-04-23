/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.fragment.WidgetPreviewDialog;
import ly.appsocial.chatcenter.util.CCPrefUtils;

public class ShareLocationActivity extends BaseActivity implements OnMapReadyCallback, WidgetPreviewDialog.WidgetPreviewListener{

	private final static int SHARE_TIME_MIN = 15; // 15 minutes
	private final static int SHARE_TIME_MAX = 60; // Max time for sharing location is 60 minutes
	private final static int SHARE_TIME_DEFAULT = SHARE_TIME_MIN; // 15 minutes
	private final static int ONE_HOUR = 60;
	private final static int QUARTER_HOUR = 15;

	private GoogleMap mMap;
	private Location mLocation;
	private TextView mShareTimeLabel;
	private int mShareTime = SHARE_TIME_DEFAULT;
	private boolean mDontShowPreview = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_location);

		Intent intent = getIntent();
		mDontShowPreview = !intent.getBooleanExtra("show_preview", true);

		SharedPreferences pref = CCPrefUtils.getPreferences(this);

		String json = pref.getString(ChatCenterConstants.Preference.LAST_LOCATION, null);
		mLocation = json == null ? null : new Gson().fromJson(json, Location.class);

		MapFragment mapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setHomeAsUpIndicator(R.drawable.bt_close);

		setTitle(R.string.share_location_live_title);

		View minusBtn = findViewById(R.id.minus_btn);
		minusBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				decreaseTime();
			}
		});

		View plusBtn = findViewById(R.id.plus_btn);
		plusBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				increaseTime();
			}
		});


		mShareTimeLabel = (TextView)findViewById(R.id.share_time);
		updateShareTime();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if ( mDontShowPreview ){
			getMenuInflater().inflate(R.menu.done_menu, menu);
		} else {
			getMenuInflater().inflate(R.menu.next_menu, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if ( id == R.id.next ){
			if ( mLocation != null ) {
				String widgetContent = ChatItem.createLiveLocationStickerContent(mLocation, this);
				showDialogWidgetPreview(widgetContent,this);
				return true;
			}
		} else if ( id == R.id.done ){
			onSendButtonClicked();
		} else if ( id == android.R.id.home ){
			Intent intent = new Intent();
			setResult(RESULT_CANCELED, intent);
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSendButtonClicked() {
		Intent intent = new Intent();
		intent.putExtra("location", mLocation);
		intent.putExtra("share_time", mShareTime);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		mMap.setMyLocationEnabled(true);
		if ( mLocation != null){
			LatLng lastPos = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
			mMap.moveCamera(CameraUpdateFactory.newLatLng(lastPos));
		}
		mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
			@Override
			public void onMyLocationChange(Location loc) {
				mLocation = loc;
				LatLng curr = new LatLng(loc.getLatitude(), loc.getLongitude());
				mMap.animateCamera(CameraUpdateFactory.newLatLng(curr));

				final SharedPreferences.Editor editor = CCPrefUtils.getPreferences(ShareLocationActivity.this).edit();
				String json = new Gson().toJson(mLocation);
				editor.putString(ChatCenterConstants.Preference.LAST_LOCATION, json).apply();
			}
		});
	}

	private void updateShareTime(){
		if (mShareTime >= ONE_HOUR) {
			if (mShareTime > 12 * ONE_HOUR) {
				mShareTimeLabel.setText(getString(R.string.infinite));
			} else {
				int shareTimeInHour = mShareTime / ONE_HOUR;
				String text = String.format(getString(R.string.share_time_format_hour), shareTimeInHour);

				/*if (shareTimeInHour == 1) {
					text = text.substring(0, text.length() - 1);
				}*/
				mShareTimeLabel.setText(text);
			}
		} else {
			mShareTimeLabel.setText(String.format(getString(R.string.share_time_format_min), mShareTime));
		}
	}

	private void increaseTime(){
		if (mShareTime >= SHARE_TIME_MAX) {
			return;
		}

		/*
		if (mShareTime >= SHARE_TIME_DEFAULT) {
			if (mShareTime >= 12 * ONE_HOUR) {
				mShareTime = Integer.MAX_VALUE;
			} else {
				mShareTime += ONE_HOUR;
			}
		} else {*/
		mShareTime += QUARTER_HOUR;
		/*}*/

		updateShareTime();
	}

	private void decreaseTime(){
		if ( mShareTime > SHARE_TIME_MIN ){
			/*if (mShareTime > SHARE_TIME_DEFAULT) {
				if (mShareTime > 12 * ONE_HOUR) {
					mShareTime = 12 * ONE_HOUR;
				} else {
					mShareTime -= ONE_HOUR;
				}
			} else {*/
			mShareTime -= QUARTER_HOUR;
			/*}*/
		}
		updateShareTime();
	}

	@Override
	public void finish() {
		super.finish();
		this.overridePendingTransition(0, R.anim.activity_close_exit);
	}
}
