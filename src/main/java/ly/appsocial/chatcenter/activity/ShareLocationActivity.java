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
import ly.appsocial.chatcenter.util.PreferenceUtil;

public class ShareLocationActivity extends BaseActivity implements OnMapReadyCallback, WidgetPreviewDialog.WidgetPreviewListener{

	private GoogleMap mMap;
	private Location mLocation;
	private TextView mShareTimeLabel;
	private int mShareTime = 60;
	private boolean mDontShowPreview = false;

	final static private int SHARE_TIME_MIN = 5;
	final static private int SHARE_TIME_MAX = 60;
	final static private int SHARE_TIME_VAL = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_location);

		Intent intent = getIntent();
		mDontShowPreview = !intent.getBooleanExtra("show_preview", true);

		SharedPreferences pref = PreferenceUtil.getPreferences(this);

		String json = pref.getString(ChatCenterConstants.Preference.LAST_LOCATION, null);
		mLocation = json == null ? null : new Gson().fromJson(json, Location.class);

		MapFragment mapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setTitle(R.string.location_live_title);

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
				showDialogWidgetPreview(widgetContent);
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

				final SharedPreferences.Editor editor = PreferenceUtil.getPreferences(ShareLocationActivity.this).edit();
				String json = new Gson().toJson(mLocation);
				editor.putString(ChatCenterConstants.Preference.LAST_LOCATION, json).apply();
			}
		});
	}

	private void updateShareTime(){
		String form = getString(R.string.share_time_format);
		mShareTimeLabel.setText(String.format(form, mShareTime));
	}

	private void increaseTime(){
		int time = mShareTime + SHARE_TIME_VAL;
		if ( time <= SHARE_TIME_MAX ){
			mShareTime = time;
		}
		updateShareTime();
	}

	private void decreaseTime(){
		int time = mShareTime - SHARE_TIME_VAL;
		if ( time >= SHARE_TIME_MIN ){
			mShareTime = time;
		}
		updateShareTime();
	}

}
