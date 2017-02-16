/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.fragment;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.VideoChatActivity;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.util.ViewUtil;

/**
 * VideoCallFragment.
 */
public class VideoCallFragment extends Fragment {

	private VideoChatActivity mVideoChatActivity;

	private TextView mNameLabel;

	private Ringtone mRingtone;

	public VideoCallFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mVideoChatActivity = (VideoChatActivity)getActivity();

		View layout = inflater.inflate(R.layout.fragment_video_call, container, false);
		mNameLabel = (TextView)layout.findViewById(R.id.name);
		ImageView imvUserAva = (ImageView) layout.findViewById(R.id.imv_left_menu_user_ava);
		TextView tvUserAva = (TextView) layout.findViewById(R.id.tv_left_menu_user_ava);

		Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		mRingtone = RingtoneManager.getRingtone(mVideoChatActivity, uri);
		if ( mRingtone == null ){
			RingtoneManager manager = new RingtoneManager(mVideoChatActivity);
			manager.setType(RingtoneManager.TYPE_RINGTONE);
			Cursor cursor = manager.getCursor();
			while (cursor.moveToNext()) {
				String uriPrefix = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
				String index = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
				uri = Uri.parse(uriPrefix + "/" + index);
				mRingtone = RingtoneManager.getRingtone(mVideoChatActivity, uri);
				break;
			}
		}

		Intent intent = getActivity().getIntent();

		String thumbUrl = intent.getStringExtra("thumbnail");
		String userName = intent.getStringExtra("name");

		// Avatar setup
		if (StringUtil.isNotBlank(thumbUrl)) {
			tvUserAva.setVisibility(View.GONE);
			imvUserAva.setVisibility(View.VISIBLE);
			ViewUtil.loadImageCircle(imvUserAva, thumbUrl);
		} else {
			tvUserAva.setVisibility(View.VISIBLE);
			imvUserAva.setVisibility(View.GONE);

			tvUserAva.setText(userName.toUpperCase().substring(0, 1));

			GradientDrawable gradientDrawable = (GradientDrawable) tvUserAva.getBackground();
			gradientDrawable.setColor(ViewUtil.getIconColor(userName));
		}

		if ( StringUtil.isNotBlank(userName)){
			mNameLabel.setText(userName);
		} else {
			// TODO place holder
		}

		View hangup = layout.findViewById(R.id.hangup);
		hangup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mVideoChatActivity.rejectCall();
			}
		});

		View answerVideo = layout.findViewById(R.id.answer_video);

		answerVideo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mVideoChatActivity.acceptCall(false);
			}
		});

		View answerAudio = layout.findViewById(R.id.answer_audio);
		answerAudio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mVideoChatActivity.acceptCall(true);
			}
		});

		return layout;
	}

	@Override
	public void onResume(){
		super.onResume();
		if ( mRingtone != null ){
			mRingtone.play();
		}
	}

	@Override
	public void onPause(){
		super.onPause();
		if ( mRingtone != null ) {
			mRingtone.stop();
		}
	}

}
