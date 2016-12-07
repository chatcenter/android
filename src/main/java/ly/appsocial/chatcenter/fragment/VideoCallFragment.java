/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.fragment;


import android.content.Intent;
import android.database.Cursor;
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

import com.squareup.picasso.Picasso;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.VideoChatActivity;
import ly.appsocial.chatcenter.util.CircleTransformation;

import static com.opentok.client.DeviceInfo.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoCallFragment extends Fragment {

	private VideoChatActivity mVideoChatActivity;

	private ImageView mThumbImage;
	private TextView mNameLabel;

	private Ringtone mRingtone;

	public VideoCallFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mVideoChatActivity = (VideoChatActivity)getActivity();

		View layout = inflater.inflate(R.layout.fragment_video_call, container, false);

		mThumbImage = (ImageView)layout.findViewById(R.id.thumb);
		mNameLabel = (TextView)layout.findViewById(R.id.name);

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
		if ( thumbUrl != null && !thumbUrl.isEmpty() ) {
			Picasso.with(getActivity()).load(thumbUrl).transform(new CircleTransformation()).into(mThumbImage, null);
		} else {
			// TODO place holder
		}

		String userName = intent.getStringExtra("name");
		if ( userName != null && !userName.isEmpty() ){
			mNameLabel.setText(userName);
		} else {
			// TODO place holder
		}

		boolean isAudioOnly = intent.getBooleanExtra("audioOnly", false);

		View hangup = layout.findViewById(R.id.hangup);
		hangup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mVideoChatActivity.rejectCall();
			}
		});

		View answerVideo = layout.findViewById(R.id.answer_video);
		if ( isAudioOnly ){
			answerVideo.setVisibility(View.INVISIBLE);
		} else {
			answerVideo.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mVideoChatActivity.acceptCall(false);
				}
			});
		}

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
