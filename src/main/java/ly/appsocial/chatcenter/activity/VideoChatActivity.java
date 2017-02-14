/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ly.appsocial.chatcenter.BuildConfig;
import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.dto.UserItem;
import ly.appsocial.chatcenter.dto.param.ChatParamDto;
import ly.appsocial.chatcenter.dto.ws.request.VideoChatRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.WsConnectChannelRequest;
import ly.appsocial.chatcenter.dto.ws.response.VideoChatResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.WsChannelJoinMessageDto;
import ly.appsocial.chatcenter.dto.ws.response.WsMessagesResponseDto;
import ly.appsocial.chatcenter.fragment.VideoCallFragment;
import ly.appsocial.chatcenter.fragment.VideoChatFragment;
import ly.appsocial.chatcenter.util.ApiUtil;
import ly.appsocial.chatcenter.util.AuthUtil;
import ly.appsocial.chatcenter.util.NetworkQueueHelper;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.widgets.VideoCallWidget;
import ly.appsocial.chatcenter.ws.ApiRequest;
import ly.appsocial.chatcenter.ws.CCWebSocketClient;
import ly.appsocial.chatcenter.ws.CCWebSocketClientListener;
import ly.appsocial.chatcenter.ws.OkHttpApiRequest;
import ly.appsocial.chatcenter.ws.WebSocketHelper;

import static ly.appsocial.chatcenter.widgets.VideoCallWidget.VIDEO_CALL_ACTION_ACCEPT;
import static ly.appsocial.chatcenter.widgets.VideoCallWidget.VIDEO_CALL_ACTION_HANGUP;
import static ly.appsocial.chatcenter.widgets.VideoCallWidget.VIDEO_CALL_ACTION_REJECT;


public class VideoChatActivity extends BaseActivity {
	/**
	 * リクエストタグ
	 */
	private static final String REQUEST_TAG = VideoChatActivity.class.getCanonicalName();

	private static final int PERMISSIONS_REQUEST_VIDEOCHAT = 3003;
	private boolean mOnlyVoice = false;
	/** チャネルUID */
	private String mChannelUid;
	private String mMessageId;
	private ChatParamDto mParamDto;

	private Boolean mIsCalling;

	private boolean _isAlreadyReject = false;
	private boolean _isAlreadyAccept = false;
	private boolean _isAlreadyHangup = false;

	private ApiRequest<VideoChatResponseDto> mVideoChatRequest;
	ProgressDialog progressDialog;

	/** WebSocket */
	private Handler mHandler = new Handler();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_chat);

		Intent intent = getIntent();
		mIsCalling = intent.getBooleanExtra("isCalling", false);
		mChannelUid = intent.getStringExtra(ChatCenterConstants.Extra.CHANNEL_UID);
		mParamDto = getIntent().getParcelableExtra(ChatCenterConstants.Extra.CHAT_PARAM);
		mMessageId = intent.getStringExtra("message_id");
		mOnlyVoice = intent.getBooleanExtra("audioOnly", false);

		// WebSocket 接続
		String appToken = ApiUtil.getAppToken(this);
		appToken = (appToken == null || appToken.isEmpty() ) ? mParamDto.appToken : appToken;

		WebSocketHelper.connectWithAppToken(getApplicationContext(), appToken, new WebSocketClientListener());


		if ( mIsCalling ){
			openVideoChat();
		} else {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.container, new VideoCallFragment());
			ft.commit();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		// API のキャンセル
		mOkHttpClient.cancel(REQUEST_TAG);
	}

	private void openVideoChat(){
		if ( ( ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
				!= PackageManager.PERMISSION_GRANTED) ||
				ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
						!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
					PERMISSIONS_REQUEST_VIDEOCHAT);
		} else {
			doOpenVideoChat();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (grantResults.length >= 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
			// Permission was granted
			doOpenVideoChat();
		} else {
			// Permission was denied. Possibly show a message indicating the user what happens if the app does have this permission
			if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
				// Camera permission was denied
				new AlertDialog.Builder(this)
						.setTitle(R.string.alert)
						.setMessage(R.string.video_permission_denied)
						.setCancelable(false)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								if ( !mIsCalling ){
									rejectCall();
								} else {
									closeActivity();
								}
							}
						})
						.show();
			}

			if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
				// Write external storage permission was denied
				new AlertDialog.Builder(this)
						.setTitle("Alert")
						.setMessage(R.string.audio_permission_denied)
						.setCancelable(false)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								if ( !mIsCalling ){
									rejectCall();
								} else {
									closeActivity();
								}
							}
						})
						.show();
			}
		}
	}

	private void closeProgress(){
		if (progressDialog != null) {
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		}
	}

	private void doOpenVideoChat(){
		VideoChatFragment fragment = new VideoChatFragment();
		fragment.mMuteVideo = mOnlyVoice;

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.container, fragment);
		ft.commitAllowingStateLoss();
	}

	private void closeActivity(){
		closeProgress();
		mVideoChatRequest = null;
		finish();
	}

	public void rejectCall() {
		// prevent double hangup action
		if(_isAlreadyReject || mVideoChatRequest != null) {
			return;
		}
		_isAlreadyReject = true;

		String progMsg = getResources().getString(R.string.processing);
		progressDialog = ProgressDialog.show(this, null, progMsg, false, true);

		String path = "channels/" + mChannelUid + "/calls/" + mMessageId + "/reject";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mVideoChatRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers, headers, new ApiRequest.Callback<VideoChatResponseDto>() {
			@Override
			public void onSuccess(VideoChatResponseDto responseDto) {
				closeActivity();
			}

			@Override
			public void onError(ApiRequest.Error error) {
				closeActivity();
			}
		}, new ApiRequest.Parser<VideoChatResponseDto>() {
			@Override
			public int getErrorCode() {
				return 0;
			}

			@Override
			public VideoChatResponseDto parser(String response) {
				return null;
			}
		});

		if (mParamDto.appToken != null) {
			mVideoChatRequest.setApiToken(mParamDto.appToken);
		}
		VideoChatRequestDto videoChatRequestDto = new VideoChatRequestDto();
		videoChatRequestDto.callId = mMessageId;
		videoChatRequestDto.user = new UserItem();
		videoChatRequestDto.user.id = AuthUtil.getUserId(getApplicationContext());
		videoChatRequestDto.reason = new VideoChatRequestDto.Reason();
		videoChatRequestDto.reason.type = "error";
		videoChatRequestDto.reason.message = "Invite to Participant was canceled";

		mVideoChatRequest.setJsonBody(videoChatRequestDto.toJson());
		NetworkQueueHelper.enqueue(mVideoChatRequest, REQUEST_TAG);
	}

	public void hangUpCall() {
		//if ( mIsCalling ){
		//	finish();
		//	return;
		//}

		// prevent double hangup action
		if(_isAlreadyHangup || mVideoChatRequest != null) {
			return;
		}
		_isAlreadyHangup = true;

		String progMsg = getResources().getString(R.string.processing);
		progressDialog = ProgressDialog.show(this, null, progMsg, false, true);

		String path = "channels/" + mChannelUid + "/calls/" + mMessageId + "/hangup";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mVideoChatRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers, headers, new ApiRequest.Callback<VideoChatResponseDto>() {
			@Override
			public void onSuccess(VideoChatResponseDto responseDto) {
				closeActivity();
			}

			@Override
			public void onError(ApiRequest.Error error) {
				closeActivity();
			}
		}, new ApiRequest.Parser<VideoChatResponseDto>() {
			@Override
			public int getErrorCode() {
				return 0;
			}

			@Override
			public VideoChatResponseDto parser(String response) {
				return null;
			}
		});

		if (mParamDto.appToken != null) {
			mVideoChatRequest.setApiToken(mParamDto.appToken);
		}
		VideoChatRequestDto videoChatRequestDto = new VideoChatRequestDto();
		videoChatRequestDto.callId = mMessageId;
		videoChatRequestDto.user = new UserItem();
		videoChatRequestDto.user.id = AuthUtil.getUserId(getApplicationContext());

		mVideoChatRequest.setJsonBody(videoChatRequestDto.toJson());
		NetworkQueueHelper.enqueue(mVideoChatRequest, REQUEST_TAG);
	}

	public void acceptCall(boolean bOnlyVoice) {
		// prevent double hangup action
		if(_isAlreadyAccept || mVideoChatRequest != null) {
			return;
		}
		_isAlreadyAccept = true;
		mOnlyVoice = bOnlyVoice;

		String progMsg = getResources().getString(R.string.processing);
		progressDialog = ProgressDialog.show(this, null, progMsg, false, true);

		String path = "channels/" + mChannelUid + "/calls/" + mMessageId + "/accept";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mVideoChatRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers, headers, new ApiRequest.Callback<VideoChatResponseDto>() {
			@Override
			public void onSuccess(VideoChatResponseDto responseDto) {
				closeProgress();
				mVideoChatRequest = null;
				openVideoChat();
			}

			@Override
			public void onError(ApiRequest.Error error) {
				closeActivity();
			}
		}, new ApiRequest.Parser<VideoChatResponseDto>() {
			@Override
			public int getErrorCode() {
				return 0;
			}

			@Override
			public VideoChatResponseDto parser(String response) {
				return null;
			}
		});

		if (mParamDto.appToken != null) {
			mVideoChatRequest.setApiToken(mParamDto.appToken);
		}
		VideoChatRequestDto videoChatRequestDto = new VideoChatRequestDto();
		videoChatRequestDto.callId = mMessageId;
		videoChatRequestDto.user = new UserItem();
		videoChatRequestDto.user.id = AuthUtil.getUserId(getApplicationContext());

		mVideoChatRequest.setJsonBody(videoChatRequestDto.toJson());
		NetworkQueueHelper.enqueue(mVideoChatRequest, REQUEST_TAG);
	}

	private class WebSocketClientListener extends CCWebSocketClientListener {

		@Override
		public void onWSConnect() {
			// Request join channel
			WsConnectChannelRequest wsConnectChannelRequest = new WsConnectChannelRequest();
			wsConnectChannelRequest.channelUid = mChannelUid;
			WebSocketHelper.send(wsConnectChannelRequest.toJson());
		}

		@Override
		public void onWSDisconnect(int code, String reason) {
			// Do nothing
		}

		@Override
		public void onWSError(Exception exception) {
		}

		@Override
		public void onWSMessage(final WsMessagesResponseDto response, final String messageType) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (response == null || !mChannelUid.equals(response.channelUid)
							|| response.widget == null || !(response.widget instanceof VideoCallWidget)) {
						/*
						 * 自身の他のチャネルも送られるのでスキップします。
						 */
						return;
					}

					ChatItem item = response;
					VideoCallWidget widget = (VideoCallWidget)item.widget;
					if ( widget.events != null ){
						VideoCallWidget.VideoCallUser caller = widget.caller;
						int currentUserId = AuthUtil.getUserId(getApplicationContext());

						for ( VideoCallWidget.VideoCallEvent event : widget.events){
							if ( event.content != null && event.content.action != null ){
								if (event.content.action.equals(VIDEO_CALL_ACTION_REJECT)){
									// Caller canceled call
									handleReject(widget, event);
								} else if (event.content.action.equals(VIDEO_CALL_ACTION_HANGUP)) {
									closeActivity();
									break;
								} else if ( caller.userId != currentUserId
										&& event.content.action.equals(VIDEO_CALL_ACTION_ACCEPT)
										&& event.content.user.userId != currentUserId ){
									// Other people accepted
									closeActivity();
									break;
								}
							}
						}
					}
				}
			});
		}

		@Override
		public void onWSChannelJoin(WsChannelJoinMessageDto response) {
			// Do nothing
		}

		@Override
		public void onWSRecieveOnline(final String channelUid, final JSONObject user, final String orgUid, final Boolean online){
			// Do nothing
		}

		@Override
		public void onWSReceiveReceipt(final String channelUid, final JSONArray messages, final JSONObject user){
			// Do nothing
		}

		@Override
		public void onWSRecieveAnswer(Integer messageId, Integer answerType){
			// Do nothing
		}

	}

	private void handleReject(VideoCallWidget widget, VideoCallWidget.VideoCallEvent event) {
		boolean needToCloseActivity = false;
		int currentUserId = AuthUtil.getUserId(getApplicationContext());
		VideoCallWidget.VideoCallUser caller = widget.caller;
		VideoCallWidget.VideoCallUser rejectedUser = event.content.user;
		List<VideoCallWidget.VideoCallUser> receivers = widget.receivers;

		// case 1: if reject event from caller or current user -> close activity
		if (caller == null || rejectedUser.userId == caller.userId
				|| rejectedUser.userId == currentUserId || receivers == null) {
			needToCloseActivity = true;
		} else {
			// Case 2: all receiver rejected
			for (VideoCallWidget.VideoCallUser receiver: receivers) {
				if (receiver.userId == rejectedUser.userId) {
					receivers.remove(receiver);
					break;
				}
			}

			if (receivers.size() == 0) {
				needToCloseActivity = true;
			}
		}

		if (needToCloseActivity) {
			closeActivity();
		}
	}

}
