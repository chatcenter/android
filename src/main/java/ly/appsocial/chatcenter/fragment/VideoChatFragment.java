/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
import com.squareup.picasso.Picasso;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.VideoChatActivity;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.util.CircleTransformation;

/**
 * VideoChatFragment.
 */
public class VideoChatFragment extends Fragment implements Session.SessionListener,
		Publisher.PublisherListener, Subscriber.VideoListener{

	private VideoChatActivity mVideoChatActivity;

	private Session mSession;
	private Publisher mPublisher;
	private Subscriber mSubscriber;

	/* 呼び出した側かどうか*/
	private boolean mIsCalling;

	/* ボイスチャットかどうか */
	private boolean mIsAudioOnly;

	/* 通話が開始されたかどうか */
	private boolean mIsStartChatting;

	private LinearLayout mPublisherViewContainer;
	private LinearLayout mSubscriberViewContainer;

	private View mRingingLabel;

	private ImageView mVideoButton;
	private ImageView mAudioButton;

	private View mOtherMicOffImage;
	private View mOtherVideoOffLabel;

	public boolean mMuteVideo = false;
	public boolean mMuteAudio = false;
	private View mCameraSwitch;

	public VideoChatFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mVideoChatActivity = (VideoChatActivity)getActivity();

		// Inflate the layout for this fragment
		View layout = inflater.inflate(R.layout.fragment_video_chat, container, false);

		mPublisherViewContainer = (LinearLayout) layout.findViewById(R.id.publisherview);
		mSubscriberViewContainer = (LinearLayout) layout.findViewById(R.id.subscriberview);

		mOtherMicOffImage = layout.findViewById(R.id.other_mic_off);
		mOtherVideoOffLabel = layout.findViewById(R.id.video_off_label);

		mOtherMicOffImage.setVisibility(View.INVISIBLE);
		mOtherVideoOffLabel.setVisibility(View.INVISIBLE);

		Intent intent = getActivity().getIntent();
		String apiKey = intent.getStringExtra("api_key");
		String sessionId = intent.getStringExtra("session_id");
		String token = intent.getStringExtra("token");
		mIsCalling = intent.getBooleanExtra("isCalling", false);
		mIsAudioOnly = intent.getBooleanExtra("audioOnly", false);

		mRingingLabel = layout.findViewById(R.id.ringing);

		if ( !mIsCalling ){
			mRingingLabel.setVisibility(View.INVISIBLE);
		}

		mCameraSwitch = layout.findViewById(R.id.switch_camera);
		if (mIsAudioOnly) {
			mCameraSwitch.setVisibility(View.INVISIBLE);
			mPublisherViewContainer.setVisibility(View.INVISIBLE);
		}
		mCameraSwitch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleCamera();
			}
		});

		mVideoButton = (ImageView)layout.findViewById(R.id.mute_video);
		mVideoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMuteVideo = !mMuteVideo;
				if ( mPublisher != null ) {
					mPublisher.setPublishVideo(!mMuteVideo);
				}
				updateButtonState();
			}
		});

		if ( mIsAudioOnly ){
			mMuteVideo = true;
			mVideoButton.setVisibility(View.INVISIBLE);
		}

		mAudioButton = (ImageView)layout.findViewById(R.id.mute_audio);
		mAudioButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMuteAudio = !mMuteAudio;
				if ( mPublisher != null ) {
					mPublisher.setPublishAudio(!mMuteVideo);
				}
				updateButtonState();
			}
		});

		View hangButton = layout.findViewById(R.id.hangup);
		hangButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disconnectSession();
				if ( mIsStartChatting ){
					mVideoChatActivity.hangUpCall();
				} else {
					mVideoChatActivity.rejectCall();
				}
			}
		});

		updateButtonState();

		mSession = new Session(getActivity(), apiKey, sessionId);
		mSession.setSessionListener(this);
		mSession.connect(token);

		return layout;
	}

	private void toggleCamera(){
		if ( mPublisher != null ){
			mPublisher.cycleCamera();
		}
	}

	private void updateButtonState(){
		if ( !mMuteVideo ){
			mVideoButton.setImageResource(R.drawable.camera_on_btn);
			if (mPublisher != null && mPublisher.getView() != null) {
				mPublisher.getView().setVisibility(View.VISIBLE);
			}
			mCameraSwitch.setVisibility(View.VISIBLE);
		} else {
			mVideoButton.setImageResource(R.drawable.camera_off_btn);
			mCameraSwitch.setVisibility(View.INVISIBLE);
			if (mPublisher != null && mPublisher.getView() != null) {
				mPublisher.getView().setVisibility(View.INVISIBLE);
			}
		}

		if ( !mMuteAudio ){
			mAudioButton.setImageResource(R.drawable.mic_on_btn);
		} else {
			mAudioButton.setImageResource(R.drawable.mic_off_btn);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mSession == null) {
			return;
		}
		mSession.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();

		if (mSession == null) {
			return;
		}
		mSession.onPause();

		if (getActivity().isFinishing()) {
			disconnectSession();
		}
	}

	@Override
	public void onDestroy() {
		disconnectSession();

		super.onDestroy();
	}

	@Override
	public void onConnected(Session session) {
		mPublisher = new Publisher(getActivity(), "publisher", true,  !mIsAudioOnly);
		mPublisher.setPublisherListener(this);
		mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
		mPublisherViewContainer.addView(mPublisher.getView());
		mSession.publish(mPublisher);
	}

	@Override
	public void onDisconnected(Session session) {
		mSession = null;
	}

	@Override
	public void onError(Session session, OpentokError opentokError) {
		//Toast.makeText(this, "Session error. See the logcat please.", Toast.LENGTH_LONG).show();
		getActivity().finish();
	}

	@Override
	public void onStreamReceived(Session session, Stream stream) {
		if (mSubscriber != null) {
			return;
		}
		subscribeToStream(stream);
	}

	@Override
	public void onStreamDropped(Session session, Stream stream) {
		if (mSubscriber == null) {
			return;
		}

		if (mSubscriber.getStream().equals(stream)) {
			mSubscriberViewContainer.removeView(mSubscriber.getView());
			mSubscriber.destroy();
			mSubscriber = null;
		}
	}

	@Override
	public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
		//subscribeToStream(stream);
		mRingingLabel.setVisibility(View.INVISIBLE);
		if (mIsAudioOnly) {
			mOtherVideoOffLabel.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

	}

	@Override
	public void onError(PublisherKit publisherKit, OpentokError opentokError) {
//        Toast.makeText(this, "Session error. See the logcat please.", Toast.LENGTH_LONG).show();
		getActivity().finish();
	}

	@Override
	public void onVideoDataReceived(SubscriberKit subscriberKit) {
		mIsStartChatting = true;
		mSubscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
		mSubscriberViewContainer.addView(mSubscriber.getView());
	}

	@Override
	public void onVideoDisabled(SubscriberKit subscriberKit, String s) {
		mOtherVideoOffLabel.setVisibility(View.VISIBLE);
		if (mSubscriber != null && mSubscriber.getView() != null) {
			mSubscriber.getView().setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onVideoEnabled(SubscriberKit subscriberKit, String s) {
		mOtherVideoOffLabel.setVisibility(View.INVISIBLE);
		if (mSubscriber != null && mSubscriber.getView() != null) {
			mSubscriber.getView().setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onVideoDisableWarning(SubscriberKit subscriberKit) {

	}

	@Override
	public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {

	}

	private void subscribeToStream(Stream stream) {
		mSubscriber = new Subscriber(getActivity(), stream);
		mSubscriber.setVideoListener(this);
		mSession.subscribe(mSubscriber);
	}

	private void disconnectSession() {
		if (mSession == null) {
			return;
		}

		if (mSubscriber != null) {
			mSubscriberViewContainer.removeView(mSubscriber.getView());
			mSession.unsubscribe(mSubscriber);
			mSubscriber.destroy();
			mSubscriber = null;
		}

		if (mPublisher != null) {
			mPublisherViewContainer.removeView(mPublisher.getView());
			mSession.unpublish(mPublisher);
			mPublisher.destroy();
			mPublisher = null;
		}
		mSession.disconnect();
	}
}
