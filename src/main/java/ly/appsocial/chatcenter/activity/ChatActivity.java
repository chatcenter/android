package ly.appsocial.chatcenter.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ly.appsocial.chatcenter.BuildConfig;
import ly.appsocial.chatcenter.ChatCenter;
import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.adapter.ChatAdapter;
import ly.appsocial.chatcenter.activity.adapter.WidgetMenuGridAdapter;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.dto.OrgItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.dto.UserItem;
import ly.appsocial.chatcenter.dto.WidgetAction;
import ly.appsocial.chatcenter.dto.param.ChatParamDto;
import ly.appsocial.chatcenter.dto.ws.request.GetMessagesRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostChannelsRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostMessageReadRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostMessageRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostStickerRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostStickerResponseRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostUsersRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.StartVideoChatRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.WsConnectChannelRequest;
import ly.appsocial.chatcenter.dto.ws.response.GetAppsResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.GetMessagesResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.GetUsersResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostChannelsResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostMessagesReadResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostMessagesResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostUsersResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.StartVideoChatResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.WsChannelJoinMessageDto;
import ly.appsocial.chatcenter.dto.ws.response.WsMessagesResponseDto;
import ly.appsocial.chatcenter.fragment.AlertDialogFragment;
import ly.appsocial.chatcenter.fragment.ConfirmDialogFragment;
import ly.appsocial.chatcenter.fragment.ProgressDialogFragment;
import ly.appsocial.chatcenter.util.ApiUtil;
import ly.appsocial.chatcenter.util.AuthUtil;
import ly.appsocial.chatcenter.util.DialogUtil;
import ly.appsocial.chatcenter.util.NetworkQueueHelper;
import ly.appsocial.chatcenter.util.RealPathUtil;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.util.ViewUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;
import ly.appsocial.chatcenter.widgets.VideoCallWidget;
import ly.appsocial.chatcenter.widgets.views.WidgetView;
import ly.appsocial.chatcenter.ws.ApiRequest;
import ly.appsocial.chatcenter.ws.CCWebSocketClient;
import ly.appsocial.chatcenter.ws.CCWebSocketClientListener;
import ly.appsocial.chatcenter.ws.OkHttpApiRequest;
import ly.appsocial.chatcenter.ws.parser.GetAppsParser;
import ly.appsocial.chatcenter.ws.parser.GetMessagesParser;
import ly.appsocial.chatcenter.ws.parser.GetUsersParser;
import ly.appsocial.chatcenter.ws.parser.PostChannelsParser;
import ly.appsocial.chatcenter.ws.parser.PostMessagesParser;
import ly.appsocial.chatcenter.ws.parser.PostMessagesReadParser;
import ly.appsocial.chatcenter.ws.parser.PostUsersParser;
import ly.appsocial.chatcenter.ws.parser.StartVideoChatParser;

import static ly.appsocial.chatcenter.widgets.VideoCallWidget.VIDEO_CALL_ACTION_ACCEPT;
import static ly.appsocial.chatcenter.widgets.VideoCallWidget.VIDEO_CALL_ACTION_HANGUP;
import static ly.appsocial.chatcenter.widgets.VideoCallWidget.VIDEO_CALL_ACTION_REJECT;

/**
 * 「チャット」アクティビティ。
 */
public class ChatActivity extends BaseActivity implements View.OnClickListener,
		AlertDialogFragment.DialogListener, ProgressDialogFragment.DialogListener,
		ConfirmDialogFragment.DialogListener, ViewUtil.OnSoftKeyBoardVisibleListener,
		WidgetView.StickerActionListener, WidgetMenuGridAdapter.WidgetMenuClickListener{

	private static final String TAG = ChatActivity.class.getSimpleName();

	private static final Integer MAX_LOADITEM = 10;

	private static final int PERMISSIONS_REQUEST_GALLERY = 3000;
	private static final int PERMISSIONS_REQUEST_CAMERA = 3001;
	private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3002;

	public static final int REQUEST_FIXED_PHRASES = 1000;
	private static final int REQUEST_CAMERA_ROLL = 10001;
	private static final int REQUEST_CAMERA = 1002;
	private static final int REQUEST_LOCATION_PICKER = 1003;
	private static final int REQUEST_PREVIEW_IMAGE = 1004;
	private static final int REQUEST_PREVIEW_WIDGET = 1005;
	private static final int REQUEST_SCHEDULE_WIDGET = 1006;
	private static final int REQUEST_QUESTION = 1007;

	private static final int MAX_MESSAGE_TEXT_LENGTH = 2000;

	// //////////////////////////////////////////////////////////////////////////
	// static フィールド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * リクエストタグ
	 */
	private static final String REQUEST_TAG = ChatActivity.class.getCanonicalName();

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** ParamDto */
	private ChatParamDto mParamDto;

	// View
	/** ルートレイアウト */
	private ViewGroup mRootLayout;
	/** タイトル */
	private TextView mTitleTextView;

	/** プログレスバー */
	private ProgressBar mProgressBar;
	/** ネットワークエラー */
	private TextView mNetoworkErrorTextView;
	/** ListView */
	private ListView mListView;
	/** テキストボックス */
	private EditText mEditText;
	/** 送信ボタン */
	private Button mSendButton;
	/** Adapter */
	private ChatAdapter mAdapter;
	/** The list of all messages in this channel */
	private List<ChatItem> mMessagesList;

	private boolean mNoPreviousMessage = false;
	private boolean mLoadFromFirst;

	/** スチッカーを送信ボタン*/
	private ImageButton mIbtSendSticker;

	private ArrayList<JSONObject> userVideoChat = new ArrayList<>();


	// タスク
	/** POST /api/users */
	private ApiRequest<PostUsersResponseDto> mPostUsersRequest;
	/** POST /api/channels */
	private ApiRequest<PostChannelsResponseDto> mPostChannelsRequest;
	/** GET /api/channels/:channel_uid */
	private ApiRequest<PostChannelsResponseDto> mGetChannelsRequest;
	/** GET /api/channels/:channel_uid/messages */
	private ApiRequest<GetMessagesResponseDto> mGetMessagesRequest;
	/** POST /api/channels/:channel_uid/messages */
	private OkHttpApiRequest<PostMessagesResponseDto> mPostMessagesRequest;
	/** POST /api/channels/:channel_uid/messages read */
	private ApiRequest<PostMessagesReadResponseDto> mPostMessagesReadRequest;
	/** GET /api/users/:id */
	private ApiRequest<GetUsersResponseDto> mGetUsersRequest;
	/** GET /api/apps */
	private OkHttpApiRequest<GetAppsResponseDto> mGetAppsRequest;
	/** TokBox */
	private ApiRequest<StartVideoChatResponseDto> mStartVideoChatRequest;

	/** WebSocket */
	private CCWebSocketClient mWebSocketClient;
	/** ネットワークエラー表示タスク */
	private final Runnable mShowNWErrorTask = new Runnable() {
		@Override
		public void run() {
			mNetoworkErrorTextView.setVisibility(View.VISIBLE);
		}
	};
	/** ネットワークエラー非表示タスク */
	private final Runnable mHideNWErrorTask = new Runnable() {
		@Override
		public void run() {
			mNetoworkErrorTextView.setVisibility(View.GONE);
		}
	};
	/** 再接続タスク */
	private final Runnable mReconnectTask = new Runnable() {
		@Override
		public void run() {
			requestApis();
		}
	};

	// レスポンス
	/** GET /api/users/:id */
	private GetUsersResponseDto mGetUsersResponseDto;

	// etc
	/** Handler */
	private Handler mHandler = new Handler();
	/** 初回ロードかどうか */
	private boolean mIsInit = true;
	/** チャネルUID */
	private String mChannelUid;
	/** 最も新しいクライアントメッセージのユーザーID */
	private int mClientUserId;
	/** ダイアル可能かどうか */
	private boolean mCanDial;

	private boolean mIsAgent;

	private OrgItem mCurrentOrgItem;

	private List<UserItem> mChannelUsers;

	private GetAppsResponseDto.App mCurrentApp;

	/** メッセージの数字＝２００１時にアラートダイアログを表示*/
	private boolean isCanShowAlertTooLongTextMsg = true;

	private File mCurrentPhoto;

	/** Widget menu*/
	private View mWidgetMenuView;
	private LinearLayout mWidgetMenuPlaceHolder;
	private PopupWindow mWidgetMenuPopupWidow;
	private int mKeyboardHeight;
	private boolean isKeyBoardVisible;
	private ArrayList<WidgetMenuGridAdapter.MenuButton> mWidgetMenuButtons;

	/** Suggestion UI*/
	private LinearLayout mSuggestionPlaceHolder;
	private LinearLayout mSuggestionHorizontalView;
	private int mSuggestionWidth = 0;

	private Uri mImageUri;
	//private String mSendingContent;

	private VideoCallWidget mVideoCallItem;

	/** Video call button*/
	private ImageButton mBtVideoCall;

	/** Phone call button*/
	private ImageButton mBtPhoneCall;
	private String mSendingContent;

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("");

		// パラメータの取得
		mParamDto = getIntent().getExtras().getParcelable(ChatParamDto.class.getCanonicalName());

		// ルートレイアウト
		mRootLayout = (ViewGroup) findViewById(R.id.chat_root_layout);

		// タイトル
		mTitleTextView = (TextView) findViewById(R.id.toolbar_title);
		mTitleTextView.setOnClickListener(this);
		mTitleTextView.setVisibility(View.GONE);

		// プログレスバー
		mProgressBar = (ProgressBar) findViewById(R.id.chat_progressbar);

		// ネットワークエラー
		mNetoworkErrorTextView = (TextView) findViewById(R.id.chat_network_error_textview);
		mNetoworkErrorTextView.setVisibility(View.GONE);

		// ListView
		mListView = (ListView) findViewById(R.id.chat_listview);
		mListView.addHeaderView(ViewUtil.getSpaceView(this, ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.chat_list_space_top_padding)), null, false);
		mListView.addFooterView(ViewUtil.getSpaceView(this, ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.chat_list_space_top_padding)), null, false);

		mMessagesList = new ArrayList<>();
		mAdapter = new ChatAdapter(this, mMessagesList, this, new ChatAdapter.ViewPositionChangedListener() {
			@Override
			public void onChanged(int itemViewType, int position) {
				if ( !mNoPreviousMessage && position <= 5 ){
					ChatItem item = mAdapter.getItem(0);
					requestGetMessages(item.id);
				}
			}
		});
		mListView.setAdapter(mAdapter);
		final GestureDetector gestureDetector = new GestureDetector(this, new MyGestureDetector());
		mListView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});

		// テキストボックス
		mEditText = (EditText) findViewById(R.id.chat_edittext);
		mEditText.addTextChangedListener(new SendTextWatcher());
		mEditText.setOnClickListener(this);

		// 送信ボタン
		mSendButton = (Button) findViewById(R.id.chat_send_button);
		mSendButton.setOnClickListener(this);

		// Video call
		mBtVideoCall = (ImageButton) findViewById(R.id.menu_video_call);
		mBtVideoCall.setOnClickListener(this);

		// Phone Call
		mBtPhoneCall = (ImageButton) findViewById(R.id.menu_phone_call);
		mBtPhoneCall.setOnClickListener(this);

		// ソフトキーボード表示・非表示検知
		// ViewUtil.observeSoftKeyBoards(this, this);

		// ダイアル可能かどうか
		mCanDial = newDialIntent(getApplicationContext(), null) != null;

		// setupParentAutoHideSoftKeyboard(mRootLayout);

		mIsAgent = getIntent().getBooleanExtra(ChatCenterConstants.Extra.IS_AGENT, false);
		mCurrentOrgItem = getIntent().getParcelableExtra(ChatCenterConstants.Extra.ORG);
		mCurrentApp = (GetAppsResponseDto.App) getIntent().getSerializableExtra(ChatCenterConstants.Extra.APP);

		// List users in this channel
		mChannelUsers = new ArrayList<>();
		if (mCurrentOrgItem != null && mCurrentOrgItem.users != null) {
			mChannelUsers.addAll(mCurrentOrgItem.users);
		}

		// Widget menu
		mWidgetMenuPlaceHolder = (LinearLayout) findViewById(R.id.widget_menu_place_holder);
		mWidgetMenuView = getLayoutInflater().inflate(R.layout.view_widget_menu, null);

		// Suggestion
		mSuggestionPlaceHolder = (LinearLayout) findViewById(R.id.suggestion_place_holder);
		mSuggestionHorizontalView = (LinearLayout) findViewById(R.id.suggestion_horizontal_view);
		mSuggestionWidth = (int) (0.8 * getScreenWidth());

		// スチッカーを送信ボタン
		mIbtSendSticker = (ImageButton) findViewById(R.id.ibt_add_sticker);
		mIbtSendSticker.setOnClickListener(this);

		if (mCurrentApp != null
				&& mCurrentApp.stickers != null
				&& mCurrentApp.stickers.size() > 0) {
			mIbtSendSticker.setVisibility(View.VISIBLE);
			setupWidgetMenu();
		} else if (mCurrentApp == null) {
			requestGetApps();
		}
	}

	private void setupWidgetMenu(){
		changeKeyboardHeight((int) getResources().getDimension(R.dimen.keyboard_height));
		enablePopUpView();
		checkKeyboardHeight(mRootLayout);
	}

	@Override
	protected void onStart() {
		super.onStart();

		mEditText.requestFocus();
		mAdapter.notifyDataSetChanged();
		/*
		 * メーラーを無効にされた場合にリンクをクリックされるとクラッシュするため、それだけのために表示を更新します。
		 */

		// 再接続
		mHandler.post(mReconnectTask);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mRootLayout != null) {
			mRootLayout.requestFocus();
		}

		// Handler のキャンセル
		mHandler.removeCallbacks(mReconnectTask);


		disconnectWS();

		// API のキャンセル
		mOkHttpClient.cancel(REQUEST_TAG);
		mPostUsersRequest = null;
		mPostChannelsRequest = null;
		mGetMessagesRequest = null;
		mPostMessagesRequest = null;
		mPostMessagesReadRequest = null;
		mGetUsersRequest = null;
	}

	@Override
	public void finish() {
		super.finish();

		// アニメーションの設定
		final TypedValue outValue = new TypedValue();
		getTheme().resolveAttribute(android.R.attr.windowAnimationStyle, outValue, true);
		if (outValue.resourceId == R.style.PullAnimation) {
			/*
			 * テーマでアクティビティクローズ時にプルダウンのアニメーションが設定されていても、
			 * 4系ではアニメーションしないためプログラム側で直接設定する。
			 */
			overridePendingTransition(0, R.anim.activity_close_exit);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (isTaskRoot()) {
					ChatCenter.showMessages(this, null, null);
					finish();
				} else {
					finish();
				}

				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (view.equals(mSendButton)) { // 送信ボタン
			if (mWebSocketClient == null) {
				// メッセージ送信エラーダイアログの表示
				DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.chatcenter_dialog_message_send_error_body));
				return;
			}
			String message =  mEditText.getText().toString();
			mEditText.setText("");
			requestPostMessages(message);
		} else if (id == R.id.toolbar_title) { // Information button
			Intent intent = new Intent(this, InfoActivity.class);
			intent.putExtra(ChatCenterConstants.Extra.CHANNEL_UID, mChannelUid);
			intent.putExtra(ChatParamDto.class.getSimpleName(), mParamDto);
			intent.putExtra(ChatCenterConstants.Extra.IS_AGENT, mIsAgent);
			intent.putExtra(ChatCenterConstants.Extra.ORG, mCurrentOrgItem);
			startActivity(intent);
		} else if (id == R.id.ibt_add_sticker) {
			openStickerMenu();
		} else if (id == R.id.chat_edittext) {
			if (mWidgetMenuPopupWidow != null && mWidgetMenuPopupWidow.isShowing()) {
				mWidgetMenuPopupWidow.dismiss();
			}
			if (mSuggestionPlaceHolder != null && mSuggestionPlaceHolder.getVisibility() == View.VISIBLE) {
				mSuggestionPlaceHolder.setVisibility(View.GONE);
			}
		} else if (id == R.id.menu_phone_call) {
			requestStartVideoChat(true);
		} else if (id == R.id.menu_video_call) {
			requestStartVideoChat(false);
		}
	}

	@Override
	public void onDialogCancel(String tag) {
		if (DialogUtil.Tag.ERROR_401.equals(tag)) { // 401エラー
			finish();
		}
	}

	@Override
	public void onPositiveButtonClick(String tag) {
		if (DialogUtil.Tag.ERROR_401.equals(tag)) { // 401エラー
			finish();
		} else if (DialogUtil.Tag.TEL.equals(tag)) { // 電話確認
			// ダイアラーの起動
			startActivity(newDialIntent(getApplicationContext(), mGetUsersResponseDto.mobileNumber));
		}
	}

	@Override
	public void onNegativeButtonClick(String tag) {
		// empty
	}

	@Override
	public void onSoftKeyBoardVisible(boolean visible) {
		if (visible && mAdapter != null) {
			/*
			 * ソフトキーボードが表示されたらメッセージの最後にスクロールします。
			 */
			scrollToLast(false);
		}
	}

	public GetAppsResponseDto.App getCurrentApp(){
		return mCurrentApp;
	}

	// //////////////////////////////////////////////////////////////////////////
	// プライベートメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * API にリクエストします。
	 */
	private void requestApis() {

		if (mIsInit) {
			/*
			 * 初回のみプログレスを表示します。
			 */
			mProgressBar.setVisibility(View.VISIBLE);
			mIsInit = false;
		}

		if (StringUtil.isNotBlank(mParamDto.channelUid)) { // 履歴画面から
			mChannelUid = mParamDto.channelUid;

			// channels->message
			requestGetChannel(mChannelUid);
		} else { // 詳細から
			if (mParamDto.providerTokenTimestamp != AuthUtil.getProviderTokenTimestamp(this)
					|| StringUtil.isBlank(AuthUtil.getUserToken(this))) { // Userトークンが無い
				// users->message
				requestPostUsers();
			} else {
				// channels->message
				requestPostChannels();
			}
		}
	}

	/**
	 * POST /api/users
	 */
	private void requestPostUsers() {
		if (mPostUsersRequest != null) {
			return;
		}

		String path = "users";

		PostUsersRequestDto postUsersRequestDto = new PostUsersRequestDto();
		postUsersRequestDto.provider = mParamDto.provider;
		postUsersRequestDto.providerToken = mParamDto.providerToken;
		postUsersRequestDto.setProviderTokenCreateAt(this, mParamDto.providerTokenTimestamp);
		postUsersRequestDto.setProviderExpires(this, mParamDto.providerTokenExpires);
		postUsersRequestDto.kissCd = mParamDto.kissCd;
		postUsersRequestDto.channelInformations = mParamDto.channelInformations;
		postUsersRequestDto.deviceToken = mParamDto.deviceToken;
		postUsersRequestDto.firstName = mParamDto.firstName;
		postUsersRequestDto.familyName = mParamDto.familyName;
		postUsersRequestDto.email = mParamDto.email;

		mPostUsersRequest = new OkHttpApiRequest<>(getApplicationContext(), ApiRequest.Method.POST,
				path, postUsersRequestDto.toParams(), null,
				new PostUsersCallback(), new PostUsersParser());
		if (mParamDto.appToken != null) {
			mPostUsersRequest.setApiToken(mParamDto.appToken);
		}

		NetworkQueueHelper.enqueue(mPostUsersRequest, REQUEST_TAG);
	}

	/**
	 * GET /api/channels/:channel_uid
	 */
	private void requestGetChannel(String channelUid) {
		if (mGetChannelsRequest != null) {
			return;
		}

		String path = "channels/" + channelUid;

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mGetChannelsRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.GET, path, null, headers, new PostChannelsCallback(), new PostChannelsParser());
		if (mParamDto.appToken != null) {
			mGetChannelsRequest.setApiToken(mParamDto.appToken);
		}
		NetworkQueueHelper.enqueue(mGetChannelsRequest, REQUEST_TAG);
	}

	/**
	 * POST /api/channels
	 */
	private void requestPostChannels() {
		if (mPostChannelsRequest != null) {
			return;
		}

		String path = "channels/";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		PostChannelsRequestDto postChannelsRequestDto = new PostChannelsRequestDto();
		postChannelsRequestDto.orgUID = mParamDto.kissCd;
		postChannelsRequestDto.channelInformation = mParamDto.channelInformations;
		Map<String, String> params = postChannelsRequestDto.toParams();

		mPostChannelsRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, params, headers, new PostChannelsCallback(), new PostChannelsParser());
		if (mParamDto.appToken != null) {
			mPostChannelsRequest.setApiToken(mParamDto.appToken);
		}
		NetworkQueueHelper.enqueue(mPostChannelsRequest, REQUEST_TAG);
	}

	/**
	 * GET /api/channels/:channel_uid/messages
	 */
	private void requestGetMessages(Integer lastId) {
		if (mGetMessagesRequest != null) {
			return;
		}

		String path = "channels/" + mChannelUid + "/messages";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		GetMessagesRequestDto getMessagesRequestDto = new GetMessagesRequestDto();
		getMessagesRequestDto.maxLoadNum = MAX_LOADITEM;
		if ( lastId != null ){
			mLoadFromFirst = false;
			getMessagesRequestDto.lastId = lastId;
		} else {
			mLoadFromFirst = true;
		}

		mGetMessagesRequest = new OkHttpApiRequest<>(getApplicationContext(), ApiRequest.Method.GET, path, getMessagesRequestDto.toParams(), headers, new GetMessagesCallback(), new GetMessagesParser());
		if (mParamDto.appToken != null) {
			mGetMessagesRequest.setApiToken(mParamDto.appToken);
		}
		NetworkQueueHelper.enqueue(mGetMessagesRequest, REQUEST_TAG);
	}

	/**
	 * POST /api/channels/:channel_uid/messages
	 */
	private void requestPostMessages(String message) {
		if (mPostMessagesRequest != null) {
			return;
		}

		int currentUserId = AuthUtil.getUserId(getApplicationContext());
		String tmpUid = mChannelUid + "-" + currentUserId + "" + System.currentTimeMillis();

		// Getting data
		// Add the temporary message
		ChatItem item = ChatItem.createTemporaryMessage(message, tmpUid, currentUserId);
		mAdapter.add(item);
		mAdapter.notifyDataSetChanged();
		scrollToLast(false);

		String path = "channels/" + mChannelUid + "/messages";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mPostMessagesRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers, headers, new PostMessagesCallback(item), new PostMessagesParser());
		if (mParamDto.appToken != null) {
			mPostMessagesRequest.setApiToken(mParamDto.appToken);
		}
		PostMessageRequestDto postMessagesRequestDto = new PostMessageRequestDto();
		postMessagesRequestDto.text = message;
		postMessagesRequestDto.tmpUid = tmpUid;

		mPostMessagesRequest.setJsonBody(postMessagesRequestDto.toJson());
		NetworkQueueHelper.enqueue(mPostMessagesRequest, REQUEST_TAG);
	}

	/**
	 * POST /api/channels/:channel_uid/messages
	 */
	private void requestPostSticker(String content) {
		if (mPostMessagesRequest != null || content == null) {
			return;
		}

		String type = ResponseType.STICKER;

		// Getting data
		int currentUserId = AuthUtil.getUserId(getApplicationContext());
		String tmpUid = mChannelUid + "-" + currentUserId + "" + System.currentTimeMillis();
		// Add the temporary message
		ChatItem item = ChatItem.createTemporarySticker(content, tmpUid, currentUserId);
		item.type = type;
		mAdapter.add(item);
		mAdapter.notifyDataSetChanged();
		scrollToLast(false);

		String path = "channels/" + mChannelUid + "/messages";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mPostMessagesRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers, headers, new PostMessagesCallback(item), new PostMessagesParser());
		if (mParamDto.appToken != null) {
			mPostMessagesRequest.setApiToken(mParamDto.appToken);
		}
		PostStickerRequestDto postStickerRequestDto = new PostStickerRequestDto();
		postStickerRequestDto.content = content;
		postStickerRequestDto.type = type;
		postStickerRequestDto.tmpUid = tmpUid;

		mPostMessagesRequest.setJsonBody(postStickerRequestDto.toJson());
		NetworkQueueHelper.enqueue(mPostMessagesRequest, REQUEST_TAG);
	}

	/**
	 * ビデオチャットの開始要求
	 *
	 * POST /api/channels/:channel_uid/call
	 */
	private void requestStartVideoChat(boolean audioOnly) {
		if (mStartVideoChatRequest != null) {
			return;
		}

		String progMsg = getResources().getString(R.string.processing);
		DialogUtil.showProgressDialog(getSupportFragmentManager(), progMsg, DialogUtil.Tag.PROGRESS);

		String path = "channels/" + mChannelUid + "/calls";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mStartVideoChatRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers, headers, new StartVideoChatCallback(), new StartVideoChatParser());
		if (mParamDto.appToken != null) {
			mStartVideoChatRequest.setApiToken(mParamDto.appToken);
		}
		StartVideoChatRequestDto startVideoChatRequestDto = new StartVideoChatRequestDto();
		startVideoChatRequestDto.action = audioOnly ? ChatCenterConstants.CallType.VOICE : ChatCenterConstants.CallType.VIDEO;

		mStartVideoChatRequest.setJsonBody(startVideoChatRequestDto.toJson());
		NetworkQueueHelper.enqueue(mStartVideoChatRequest, REQUEST_TAG);
	}




	/**
	 * POST /api/channels/:channel_uid/messages Reply sticker
	 */
	private void sendMessageResponseForChannel(BasicWidget.StickerAction.ActionData answer, String replyTo) {
		if (mPostMessagesRequest != null) {
			return;
		}

		String path = "channels/" + mChannelUid + "/messages";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		// mPostMessagesRequest = new ApiRequest<>(this, Request.Method.POST, path, headers, headers, new PostMessagesCallback(item), new PostMessagesParser());
		mPostMessagesRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers, headers, new SendMessageReplyCallback(answer, replyTo), new PostMessagesParser());
		if (mParamDto.appToken != null) {
			mPostMessagesRequest.setApiToken(mParamDto.appToken);
		}

		List<BasicWidget.StickerAction.ActionData> answers = new ArrayList<>();
		answers.add(answer);

		PostStickerResponseRequestDto stickerResponseRequestDto = new PostStickerResponseRequestDto();
		stickerResponseRequestDto.answer = answer;
		stickerResponseRequestDto.answers = answers;
		stickerResponseRequestDto.replyTo = replyTo;
		stickerResponseRequestDto.type = "response";
		stickerResponseRequestDto.answerLabel = answer.label;

		if (BuildConfig.DEBUG) {
			Log.d(TAG, "MessageResponse Params: " + stickerResponseRequestDto.toJson());
		}

		mPostMessagesRequest.setJsonBody(stickerResponseRequestDto.toJson());
		NetworkQueueHelper.enqueue(mPostMessagesRequest, REQUEST_TAG);
	}

	private void sendCheckboxResponseForChannel(List<BasicWidget.StickerAction.ActionData> answers, String replyTo) {
		if (mPostMessagesRequest != null) {
			return;
		}

		String path = "channels/" + mChannelUid + "/messages";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mPostMessagesRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers, headers, new SendCheckboxReplyCallback(answers, replyTo), new PostMessagesParser());
		if (mParamDto.appToken != null) {
			mPostMessagesRequest.setApiToken(mParamDto.appToken);
		}

		BasicWidget.StickerAction.ActionData answer = new BasicWidget.StickerAction.ActionData();
		answer.label = "";

		PostStickerResponseRequestDto stickerResponseRequestDto = new PostStickerResponseRequestDto();
		stickerResponseRequestDto.answer = answer;
		stickerResponseRequestDto.answers = answers;
		stickerResponseRequestDto.replyTo = replyTo;
		stickerResponseRequestDto.type = "response";

		if (BuildConfig.DEBUG) {
			Log.d(TAG, "MessageResponse Params: " + stickerResponseRequestDto.toJson());
		}

		mPostMessagesRequest.setJsonBody(stickerResponseRequestDto.toJson());
		NetworkQueueHelper.enqueue(mPostMessagesRequest, REQUEST_TAG);
	}

	/**
	 * POST /api/channels/:channel_uid/messages read
	 *
	 * @param messageIds 既読にするメッセージIDリスト
	 */
	private void requestPostMessagesRead(List<Integer> messageIds) {
		if (mPostMessagesRequest != null) {
			return;
		}

		String path = "channels/" + mChannelUid + "/messages";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mPostMessagesReadRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers, headers, new PostMessagesReadCallback(), new PostMessagesReadParser());
		if (mParamDto.appToken != null) {
			mPostMessagesReadRequest.setApiToken(mParamDto.appToken);
		}
		PostMessageReadRequestDto postMessagesReadRequestDto = new PostMessageReadRequestDto();
		postMessagesReadRequestDto.messageIds = messageIds;

		mPostMessagesReadRequest.setJsonBody(postMessagesReadRequestDto.toJson());
		NetworkQueueHelper.enqueue(mPostMessagesReadRequest, REQUEST_TAG);
	}

	/**
	 * GET /api/users/:id
	 */
	private void requestGetUsers() {
		if (mGetUsersRequest != null) {
			return;
		}

		String path = "users/" + mClientUserId;

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mGetUsersRequest = new OkHttpApiRequest<>(getApplicationContext(), ApiRequest.Method.GET, path, null, headers, new GetUsersCallback(), new GetUsersParser());
		if (mParamDto.appToken != null) {
			mGetUsersRequest.setApiToken(mParamDto.appToken);
		}
		NetworkQueueHelper.enqueue(mGetUsersRequest, REQUEST_TAG);

		DialogUtil.showProgressDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
	}

	/**
	 * GET /api/apps
	 */
	private void requestGetApps() {
		if (mGetAppsRequest != null) {
			return;
		}

		String path = "apps";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mGetAppsRequest = new OkHttpApiRequest<>(getApplicationContext(), OkHttpApiRequest.Method.GET, path, null, headers, new GetAppsCallback(), new GetAppsParser());
		mGetAppsRequest.setApiToken(""); // Do not pass AppToken

		NetworkQueueHelper.enqueue(mGetAppsRequest, REQUEST_TAG);
	}

	/**
	 * POST /api/channels/:channel_uid/messages
	 */
	private void requestUploadFileWithUri(Uri uri) {
		String picturePath = RealPathUtil.getRealPath(this, uri);
		MediaType mediaType = MediaType.parse(getContentResolver().getType(uri));

		requestUploadFile(picturePath, mediaType);
	}

	private static String getMimeType(String url) {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}
		return type;
	}

	private void requestUploadFile(String picturePath, MediaType mediaType) {
		if (mPostMessagesRequest != null) {
			return;
		}

		File fileToUpload = new File(picturePath);
		if (fileToUpload == null || !fileToUpload.exists()) {
			return;
		}

		DialogUtil.showProgressDialog(getSupportFragmentManager(), getString(R.string.file_uploading),DialogUtil.Tag.PROGRESS);

		int currentUserId = AuthUtil.getUserId(getApplicationContext());
		String tmpUid = mChannelUid + "-" + currentUserId + "" + System.currentTimeMillis();
		// Add the temporary message
		ChatItem item = ChatItem.createTemporaryImageSticker(picturePath, tmpUid, currentUserId);
		item.type = ResponseType.STICKER;
		mAdapter.add(item);
		mAdapter.notifyDataSetChanged();
		scrollToLast(false);

		String path = "channels/" + mChannelUid + "/messages/upload_files";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mPostMessagesRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers, headers, new PostMessagesCallback(item), new PostMessagesParser());

		if (mParamDto.appToken != null) {
			mPostMessagesRequest.setApiToken(mParamDto.appToken);
		}
		RequestBody requestBody = new MultipartBuilder()
				.type(MultipartBuilder.FORM)
				.addFormDataPart("files[]", fileToUpload.getName(),
						RequestBody.create(mediaType, fileToUpload))
				.build();

		mPostMessagesRequest.setMultipartBody(requestBody);
		NetworkQueueHelper.enqueue(mPostMessagesRequest, REQUEST_TAG);
	}

	/**
	 * ネットワークエラー表示し、再接続を行います。
	 */
	private void errorWithReconnect() {
		// ネットワークエラー表示
		mHandler.post(mShowNWErrorTask);
		// 再接続
		mHandler.postDelayed(mReconnectTask, 3000);
	}

	/**
	 * API が認証エラーか判定し、認証エラーの場合は認証エラーダイアログを表示します。
	 *
	 * @param error OkHttp's error response
	 * @return 認証エラーの場合は true、そうでない場合は false
	 */
	private boolean isAuthErrorWithAlert(OkHttpApiRequest.Error error) {
		if (error.getNetworkResponse() != null && error.getNetworkResponse().code() != 401) {
			return false;
		}
		DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR_401, null, getString(R.string.dialog_auth_error_body));
		return true;
	}

	/**
	 * リストを最後までスクロールします。
	 *
	 * @param isSmooth スムースするかどうか
	 */
	private void scrollToLast(boolean isSmooth) {
		int scrollPosition = mAdapter.getCount() + mListView.getHeaderViewsCount() + mListView.getFooterViewsCount() - 1;
		if (isSmooth) {
			mListView.smoothScrollToPosition(scrollPosition);
		} else {
			mListView.setSelectionFromTop(scrollPosition, 0);
		}
	}

	/**
	 * 電話起動のインテントを生成します。
	 * <p>
	 * 電話機能がない、もしくは電話アプリがシステム内に存在しない場合は null を返します。
	 * </p>
	 *
	 * @param context コンテキスト
	 * @param phoneNumber 電話番号
	 * @return 生成したインテント。電話発信できない状況では null
	 */
	public static Intent newDialIntent(final Context context, final String phoneNumber) {

		final Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// 電話アプリがインストールされているか確認
		final List<ResolveInfo> appInfo = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if (appInfo == null || appInfo.size() == 0) {
			return null;
		}
		return intent;
	}

	@Override
	public void onActionClick(BasicWidget.StickerAction.ActionData action, String msgId) {

		if (action == null || StringUtil.isBlank(action.label)) {
			return;
		}

		if (action.action != null || action.value != null) {
			if (action.action != null) {
				for (String actionString : action.action) {
					if (actionString.startsWith("http")) {
						Intent intent = new Intent(this, WebViewActivity.class);
						intent.putExtra(ChatCenterConstants.Extra.URL, action.action.get(0));
						startActivity(intent);
						break;
					} else if (actionString.startsWith(WidgetAction.OPEN_CALENDAR)) {
						onDateTimeAvailabilityClicked();
						break;
					}
				}
			}

			if (action.value != null) {
				if (action.label.startsWith("http")) {
					Intent intent = new Intent(this, WebViewActivity.class);
					intent.putExtra(ChatCenterConstants.Extra.URL, action.label);
					startActivity(intent);
				} else {
					String message = String.format(getString(R.string.message_reply_choosed_value), action.label);
					showDialogConfirmReply(message, action, msgId);
				}
			}
		}
	}

	private void onSuggestionActionSelected(BasicWidget.StickerAction.ActionData actionData) {

		// Old Suggestion Structure
		if (StringUtil.isBlank(actionData.type)) {
			for (String actionString : actionData.action) {
				if (actionString.startsWith("http")) {
					Intent intent = new Intent(this, WebViewActivity.class);
					intent.putExtra(ChatCenterConstants.Extra.URL, actionData.action.get(0));
					startActivity(intent);
					break;
				} else if (actionString.startsWith(WidgetAction.REPLY_SUGGESTION_MSG)) {
					mEditText.setText(actionData.message);
					break;
				}
			}
		} else {
			// New Suggestion Structure
			if (actionData.type.equals(ResponseType.MESSAGE)) {
				mEditText.setText(actionData.message);
			} else if (actionData.type.equals(ResponseType.URL) && actionData.action != null && actionData.action.size() > 0) {
				Intent intent = new Intent(this, WebViewActivity.class);
				intent.putExtra(ChatCenterConstants.Extra.URL, actionData.action.get(0));
				startActivity(intent);
			} else {
				if (actionData != null
						&& actionData.suggestionSticker != null) {
					Gson gson = new Gson();
					openWidgetPreview(gson.toJson(actionData.suggestionSticker).toString());
				}
			}
		}
	}

	@Override
	public void onCheckBoxOK(List<String> labels, List<String> answers, String msgId) {
		List<BasicWidget.StickerAction.ActionData> actions = new ArrayList<>();

		if ( answers != null && answers.size() > 0 ) {
			String answerStr = "";
			for ( int i = 0; i < answers.size(); i++ ){
				if ( !answerStr.isEmpty() ){
					answerStr += ", ";
				}
				String label = labels.get(i);
				answerStr += label;

				BasicWidget.StickerAction.ActionData actionData = new BasicWidget.StickerAction.ActionData();
				actionData.label = label;
				actionData.value = new BasicWidget.StickerAction.ActionData.Value();
				actionData.value.answer = answers.get(i);
				actions.add(actionData);
			}

			String message = String.format(getString(R.string.message_reply_choosed_value), answerStr);
			showDialogCheckboxConfirmReply(message, actions, msgId);
		}
	}

	@Override
	public void onSuggestionBubbleClicked(ChatItem chatItem) {
		showOrHideSuggestionView(chatItem);
	}

	private void showOrHideSuggestionView(ChatItem chatItem) {

		// Hide keyboard and WidgetMenu if need
		hideKeyboardAndWidgetMenu();

		mSuggestionHorizontalView.removeAllViews();

		if (chatItem.widget.stickerAction != null && BasicWidget.WIDGET_TYPE_SELECT.equals(chatItem.widget.stickerAction.actionType)) {

			for (BasicWidget.StickerAction.ActionData actionData: chatItem.widget.stickerAction.actionData) {
				View view = getSuggestionViewForChatItem(actionData);
				mSuggestionHorizontalView.addView(view);
				view.getLayoutParams().width = mSuggestionWidth;
			}
		}


		int paddingHorizontal = (getScreenWidth() - mSuggestionWidth)/ 2;
		mSuggestionHorizontalView.setPadding(paddingHorizontal, 0, paddingHorizontal, 0);

		// Show or hide SuggestionView
		mSuggestionPlaceHolder.setVisibility(View.VISIBLE);
	}

	private View getSuggestionViewForChatItem(final BasicWidget.StickerAction.ActionData action) {
		if (action == null) {
			return null;
		}

		View suggestionView = LayoutInflater.from(this).inflate(R.layout.view_suggestion_title_item, null);
		TextView contentView = (TextView) suggestionView.findViewById(R.id.suggestion_title);
		ImageView icon = (ImageView) suggestionView.findViewById(R.id.suggestion_icon);

		// Older suggestion object do not contain type
		if (StringUtil.isBlank(action.type)) {
			contentView.setText(action.label);
			icon.setImageResource(R.drawable.icon_widget_schedule);
		} else {
			if (action.type.equals(ResponseType.MESSAGE) || action.type.equals(ResponseType.URL)) {
				contentView.setText(action.label);
				icon.setVisibility(View.GONE);
			} else {
				if (action.suggestionSticker != null
						&& action.suggestionSticker.message != null) {
					contentView.setText(action.suggestionSticker.message.text);
					int iconDrawable = getSuggestionIcon(action.suggestionSticker.stickerType);
					if (iconDrawable > 0) {
						icon.setImageResource(iconDrawable);
					}
				} else {
					contentView.setText(action.label);
					icon.setVisibility(View.GONE);
				}
			}
		}

		// contentView.setCompoundDrawables(null, null, null, null);
		suggestionView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSuggestionActionSelected(action);
			}
		});

		return suggestionView;
	}

	private void showDialogConfirmReply (final String message, final BasicWidget.StickerAction.ActionData answer, final String msgId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_confirm_reply_sticker);
		builder.setMessage(message);
		builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				sendMessageResponseForChannel(answer, msgId);
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create().show();
	}

	private void showDialogCheckboxConfirmReply (final String message, final List<BasicWidget.StickerAction.ActionData> answers, final String msgId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_confirm_reply_sticker);
		builder.setMessage(message);
		builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				sendCheckboxResponseForChannel(answers, msgId);
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create().show();
	}

	protected void updateLeftOfInputToolbar() {
		//TODO
	}


	// //////////////////////////////////////////////////////////////////////////
	// Widget作成
	// //////////////////////////////////////////////////////////////////////////

	private void openStickerMenu() {
		if (mSuggestionPlaceHolder.getVisibility() == View.VISIBLE) {
			mSuggestionPlaceHolder.setVisibility(View.GONE);
		}

		if (!mWidgetMenuPopupWidow.isShowing()) {
			mWidgetMenuPopupWidow.setHeight(mKeyboardHeight);
			if (isKeyBoardVisible) {
				mWidgetMenuPlaceHolder.setVisibility(LinearLayout.GONE);
			} else {
				mWidgetMenuPlaceHolder.setVisibility(LinearLayout.VISIBLE);
			}
			mWidgetMenuPopupWidow.showAtLocation(mRootLayout, Gravity.BOTTOM, 0, 0);
		} else {
			mWidgetMenuPopupWidow.dismiss();
			if (!isKeyBoardVisible) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			}
		}

		if (!mWidgetMenuPopupWidow.isShowing()) {
			mIbtSendSticker.setImageResource(R.drawable.btn_add_widget);
		} else {
			mIbtSendSticker.setImageResource(R.drawable.btn_keyboard);
		}
	}

	@Override
	public void onMenuButtonClicked(String type){

		hideKeyboardAndWidgetMenu();

		switch (type){
			case ChatCenterConstants.StickerName.STICKER_TYPE_FIXED_PHRASE:
				onFixedPhraseClicked();
				break;
			case ChatCenterConstants.StickerName.STICKER_TYPE_DATE_TIME_AVAILABILITY:
				onDateTimeAvailabilityClicked();
				break;
			case ChatCenterConstants.StickerName.STICKER_TYPE_LOCATION:
				onLocationClicked();
				break;
			case ChatCenterConstants.StickerName.STICKER_TYPE_CAMERA:
				onCameraClicked();
				break;
			case ChatCenterConstants.StickerName.STICKER_TYPE_TYPE_FILE:
				onFileClicked();
				break;
			case ChatCenterConstants.StickerName.STICKER_TYPE_QUESTION:
				onQuestionClicked();
				break;
			case ChatCenterConstants.StickerName.STICKER_TYPE_VIDEO_CHAT:
				requestStartVideoChat(false);
				//videoChatCreateSession();
				break;
			case ChatCenterConstants.StickerName.STICKER_TYPE_TYPE_PHONE_CALL:
				requestStartVideoChat(true);
				//requestGetUsers();
				break;
		}
	}

	/*
	 * Fixed Phrase Widget
	 */
	private void onFixedPhraseClicked() {
		Intent intent = new Intent(this, FixedPhraseActivity.class);
		intent.putExtra(FixedPhraseActivity.ORG_UID, mCurrentOrgItem.uid);
		if (mParamDto.appToken != null) {
			intent.putExtra(FixedPhraseActivity.API_TOKEN, mParamDto.appToken);
		}
		startActivityForResult(intent, REQUEST_FIXED_PHRASES);
	}

	/*
	 * DateTime Widget
	 */
	private void onDateTimeAvailabilityClicked(){
		Intent intent = new Intent(this, ScheduleActivity.class);
		startActivityForResult(intent, REQUEST_SCHEDULE_WIDGET);
	}

	/*
	 * Location Widget
	 */
	private void onLocationClicked(){
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
					PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
		} else {
			openPlacePicker();
		}
	}

	private void openPlacePicker() {
		try {
			PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
			startActivityForResult(builder.build(this), REQUEST_LOCATION_PICKER);
		} catch (GooglePlayServicesRepairableException e) {
			e.printStackTrace();
		} catch (GooglePlayServicesNotAvailableException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Image Widget from Camera
	 */
	private void onCameraClicked(){
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(this,
						Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
					PERMISSIONS_REQUEST_CAMERA);
		} else {
			startCameraIntent();
		}
	}

	private void startCameraIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			// キャッシュディレクトリのチェック
			File externalCacheDir = Environment.getExternalStorageDirectory();
			if (externalCacheDir == null) {
				return;
			}
			// 保存先パスの生成
			String saveDirPath = externalCacheDir.getAbsolutePath() + "/" + getPackageName() + "/Chat/";
			File saveDir = new File(saveDirPath);
			if (!saveDir.exists() && !saveDir.mkdirs()) {
				return;
			}
			for (File file : saveDir.listFiles()) {
				file.delete();
			}
			String saveFilePath = saveDirPath + new SimpleDateFormat("yyyyMMddHHmmss", Locale.JAPAN).format(new Date()) + ".jpg";
			mCurrentPhoto = new File(saveFilePath);
			// Continue only if the File was successfully created
			if (mCurrentPhoto != null) {
				Uri photoURI = Uri.fromFile(mCurrentPhoto);
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
				startActivityForResult(takePictureIntent, REQUEST_CAMERA);
			}
		}
	}

	/*
	 * Image Widget from Camera Roll
	 */
	private void onFileClicked(){
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
					PERMISSIONS_REQUEST_GALLERY);
		} else {
			selectImageFromCameraroll();
		}
	}

	private void selectImageFromCameraroll() {
		Intent intent = new Intent();
		intent.setType("image/*, application/pdf");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), REQUEST_CAMERA_ROLL);
	}

	/*
	 * Question Widget
	 */
	private void onQuestionClicked(){
		Intent intent = new Intent(this, QuestionActivity.class);
		startActivityForResult(intent, REQUEST_QUESTION);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case PERMISSIONS_REQUEST_CAMERA:
				if (grantResults.length >= 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
					// Permission was granted
					startCameraIntent();
				} else {
					// Permission was denied. Possibly show a message indicating the user what happens if the app does have this permission
					if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
						// Camera permission was denied
						new AlertDialog.Builder(this)
								.setTitle(R.string.alert)
								.setMessage(R.string.camera_permission_denied)
								.setCancelable(false)
								.setPositiveButton("OK", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								})
								.show();
					}

					if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
						// Write external storage permission was denied
						new AlertDialog.Builder(this)
								.setTitle("Alert")
								.setMessage(R.string.storage_permission_denied)
								.setCancelable(false)
								.setPositiveButton("OK", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								})
								.show();
					}
				}
				break;
			case PERMISSIONS_REQUEST_GALLERY:
				// Write external storage request
				// If the request is cancelled, grantResults array is empty
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// Permission was granted
					selectImageFromCameraroll();
				} else {
					// Permission was denied. Possibly show a message indicating the user what happens if the app does have this permission
					new AlertDialog.Builder(this)
							.setTitle(R.string.alert)
							.setMessage(R.string.storage_permission_denied)
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode){
				case REQUEST_FIXED_PHRASES:
					onFixedPhraseSelected(data);
					break;
				case REQUEST_CAMERA:
					if (mCurrentPhoto != null) {
						String filePath = mCurrentPhoto.getAbsolutePath();
						MediaType mediaType = MediaType.parse("image/jpeg");
						requestUploadFile(filePath, mediaType);
					}
					break;
				case REQUEST_CAMERA_ROLL:
					onCameraRollImageSelected(data);
					break;
				case REQUEST_LOCATION_PICKER:
					Place place = PlacePicker.getPlace(data, this);
					if (place != null) {
						String content = ChatItem.createLocationStickerContent(place);
						requestPostSticker(content);
					}
					break;
				case REQUEST_SCHEDULE_WIDGET:
					String scheduleContent = data.getStringExtra(ScheduleActivity.SELECTED_TIME);
					if (StringUtil.isNotBlank(scheduleContent)) {
						requestPostSticker(scheduleContent);
					}
					break;
				case REQUEST_PREVIEW_IMAGE:
					requestUploadFileWithUri(mImageUri);
					break;
				case REQUEST_PREVIEW_WIDGET:
					requestPostSticker(mSendingContent);
					break;
				case REQUEST_QUESTION:
					onQuestionSelected(data);
					break;
			}
		}
	}

	/** 選択した画像を送る*/
	private void onCameraRollImageSelected(Intent data) {
		if (data == null) {
			return;
		}

		mImageUri = data.getData();

		Intent intent = new Intent(this, PreviewActivity.class);
		intent.putExtra("type", "image");
		String picturePath = RealPathUtil.getRealPath(this, mImageUri);
		intent.putExtra("image_path", picturePath);
		startActivityForResult(intent, REQUEST_PREVIEW_IMAGE);
	}

	/** 選択したスチッカーを送る*/
	private void onFixedPhraseSelected(Intent data) {
		if(data == null) {
			return;
		}
		String selectedContentType = data.getStringExtra(FixedPhraseActivity.SELECTED_TYPE);
		String content = data.getStringExtra(FixedPhraseActivity.SELECTED_CONTENT);
		if (selectedContentType.equals(ResponseType.MESSAGE)) {
			requestPostMessages(content);
		} else if (selectedContentType.equals(ResponseType.STICKER)) {
			requestPostSticker(content);
		}
	}

	private void onQuestionSelected(Intent data){
		if(data == null) {
			return;
		}
		String content = data.getStringExtra(QuestionActivity.QUESTION_CONTENT);
		if ( StringUtil.isNotBlank(content) ){
			requestPostSticker(content);
		}
	}

	public void openWidgetPreview(String content){
		mSendingContent = content;
		Log.e(TAG, "Content: " + mSendingContent);

		Intent intent = new Intent(this, PreviewActivity.class);
		intent.putExtra("type", "widget");
		intent.putExtra("content", mSendingContent);
		startActivityForResult(intent, REQUEST_PREVIEW_WIDGET);
	}

	public int getSuggestionIcon(String actionType) {
		int drawable = 0;
		if (actionType.equals(BasicWidget.WIDGET_TYPE_CONFIRM)
				|| actionType.equals(ResponseType.QUESTION)) {
			drawable = R.drawable.icon_widget_question;
		} else if (actionType.equals(ResponseType.LIST)) {
			drawable = R.drawable.icon_widget_schedule;
		} else if (actionType.equals(ResponseType.LOCATION)
				|| actionType.equals(ResponseType.LOCATION)) {
			drawable = R.drawable.icon_widget_location;
		} else if (actionType.equals(ResponseType.FILE)) {
			drawable = R.drawable.icon_widget_attachment;
		}

		return drawable;
	}

	private void checkIsVideoChatActive(ChatItem chatItem){
		if ( chatItem.widget.getClass().equals(VideoCallWidget.class) ){
			int currentUserId = AuthUtil.getUserId(getApplicationContext());
			VideoCallWidget widget = (VideoCallWidget)chatItem.widget;
			if ( widget.caller.userId != currentUserId ){
				boolean bCallActiveAndNoOneAccepted = true;
				if ( widget.events != null ){
					for(VideoCallWidget.VideoCallEvent event : widget.events ){
						if ( event.content == null ) {
							bCallActiveAndNoOneAccepted = false;
							break;
						}
						if (event.content.user.userId == widget.caller.userId) {
							if (VIDEO_CALL_ACTION_REJECT.equals(event.content.action) ||
									VIDEO_CALL_ACTION_HANGUP.equals(event.content.action)) {
								bCallActiveAndNoOneAccepted = false;
								break;
							}
						} else if (VIDEO_CALL_ACTION_ACCEPT.equals(event.content.action)) {
							bCallActiveAndNoOneAccepted = false;
							break;
						}
					}
				}

				if ( bCallActiveAndNoOneAccepted ){
					openVideoChatActivity(widget, widget.messageId);
				}
			}
		}
	}


	// //////////////////////////////////////////////////////////////////////////
	// コールバック
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * POST /api/users のコールバック
	 */
	private class PostUsersCallback implements OkHttpApiRequest.Callback<PostUsersResponseDto> {
		@Override
		public void onError(OkHttpApiRequest.Error error) {
			mPostUsersRequest = null;

			mProgressBar.setVisibility(View.GONE);

			if (!isAuthErrorWithAlert(error)) {
				// エラー表示＆再接続
				errorWithReconnect();
			}
		}

		@Override
		public void onSuccess(PostUsersResponseDto responseDto) {
			mPostUsersRequest = null;
			mChannelUid = responseDto.uid;

			// タイトル設定
			if(!mIsAgent) {
				mTitleTextView.setText(responseDto.orgName);
				mTitleTextView.setVisibility(View.VISIBLE);
			}

			// トークンの保存
			AuthUtil.saveTokens(ChatActivity.this, mParamDto.providerTokenTimestamp, responseDto.users.get(0));
			requestGetMessages(null);
		}
	}

	/**
	 * POST /api/channels のコールバック
	 */
	private class PostChannelsCallback implements OkHttpApiRequest.Callback<PostChannelsResponseDto> {
		@Override
		public void onError(OkHttpApiRequest.Error error) {
			mPostChannelsRequest = null;
			mGetChannelsRequest = null;
			mProgressBar.setVisibility(View.GONE);

			if (!isAuthErrorWithAlert(error)) {
				// エラー表示＆再接続
				errorWithReconnect();
			}
		}

		@Override
		public void onSuccess(PostChannelsResponseDto responseDto) {
			mPostChannelsRequest = null;
			mGetChannelsRequest = null;
			mChannelUid = responseDto.uid;

			mChannelUsers.addAll(responseDto.users);

			// タイトル設定
			if(!mIsAgent) {
				mTitleTextView.setText(responseDto.orgName);
				mTitleTextView.setVisibility(View.VISIBLE);
			} else {
				if (responseDto.getGuest() != null) {
					mTitleTextView.setText(responseDto.getGuest().displayName);
					mTitleTextView.setVisibility(View.VISIBLE);
				} else {
					mTitleTextView.setText(responseDto.orgName);
					mTitleTextView.setVisibility(View.VISIBLE);
				}
			}

			requestGetMessages(null);
		}
	}

	/**
	 * GET /api/channels/:channel_uid/messages のコールバック
	 */
	private class GetMessagesCallback implements OkHttpApiRequest.Callback<GetMessagesResponseDto> {
		@Override
		public void onError(OkHttpApiRequest.Error error) {
			mGetMessagesRequest = null;

			mProgressBar.setVisibility(View.GONE);

			if (!isAuthErrorWithAlert(error)) {
				// エラー表示＆再接続
				errorWithReconnect();
			}
		}

		@Override
		public void onSuccess(GetMessagesResponseDto responseDto) {
			mGetMessagesRequest = null;

			if (responseDto == null || responseDto.items == null) {
				mProgressBar.setVisibility(View.GONE);
				// エラー表示＆再接続
				errorWithReconnect();
			}

			int userId = AuthUtil.getUserId(ChatActivity.this);
			boolean isScroll = mAdapter.getCount() == 0;

			if ( mLoadFromFirst ){
				mAdapter.clear();
			}

			if ( responseDto.items.size() < MAX_LOADITEM ){
				mNoPreviousMessage = true;
			} else {
				mNoPreviousMessage = false;
			}

			// メッセージ設定
			int nAdd = 0;
			List<Integer> readMessageIds = new ArrayList<>();
			for (ChatItem chatItem : responseDto.items) {

				if (chatItem.type.equals(ResponseType.SUGGESTION)) {
					if (mAdapter.getCount() > 0 && ResponseType.SUGGESTION.equals(mAdapter.getItem(0).type)) {
						continue;
					}
				} else if (chatItem.type.equals(ResponseType.CALL)) {
					checkIsVideoChatActive(chatItem);
				}

				if (chatItem.widget != null) {
					chatItem.widget.setupWithUsers(mChannelUsers);
				}
				mAdapter.insert(chatItem, 0);
				nAdd++;

				// クライアントユーザーの更新
				if (chatItem.user != null && userId != chatItem.user.id) {
					mClientUserId = chatItem.user.id;
				}
				readMessageIds.add(chatItem.id);
			}

			int position = mListView.getFirstVisiblePosition();
			int yOffset = mListView.getChildAt(0).getTop();

			// アダプタに項目設定
			mAdapter.setChatInfo(mChannelUid, userId);
			mAdapter.notifyDataSetChanged();

			// スクロール位置を末尾に
			if ( mLoadFromFirst ){
				connectWS();
				scrollToLast(false /*!isScroll*/);
			} else {
				mListView.setSelectionFromTop(position + nAdd, yOffset);
			}

			mProgressBar.setVisibility(View.GONE);

			// 表示したメッセージを既読化
			if (readMessageIds.size() > 0) {
				requestPostMessagesRead(readMessageIds);
			}
		}
	}

	/**
	 * POST /api/channels/:channel_uid/messages のコールバック
	 */
	private class PostMessagesCallback implements OkHttpApiRequest.Callback<PostMessagesResponseDto> {
		private ChatItem mFakeChatItem;

		public PostMessagesCallback(ChatItem fakeItem) {
			mFakeChatItem = fakeItem;
		}

		@Override
		public void onError(OkHttpApiRequest.Error error) {
			DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
			mPostMessagesRequest = null;

			if (!isAuthErrorWithAlert(error)) {
				// メッセージ送信エラーダイアログの表示
				DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.chatcenter_dialog_message_send_error_body));
			}

			if (mFakeChatItem != null) {
				mFakeChatItem.localStatus = ChatItem.ChatItemStatus.SEND_FAILED;
				mAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onSuccess(PostMessagesResponseDto responseDto) {
			mPostMessagesRequest = null;
			DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
			// テキストボックスのクリア
			mEditText.setText("");
		}
	}

	/**
	 * POST /api/channels/:channel_uid/messages read のコールバック
	 */
	private class PostMessagesReadCallback implements OkHttpApiRequest.Callback<PostMessagesReadResponseDto> {
		@Override
		public void onError(OkHttpApiRequest.Error error) {
			mPostMessagesReadRequest = null;

			if (!isAuthErrorWithAlert(error)) {
				// 共通エラーダイアログの表示
				DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.dialog_error_body));
			}
		}

		@Override
		public void onSuccess(PostMessagesReadResponseDto responseDto) {
			mPostMessagesReadRequest = null;
			// empty
		}
	}

	/**
	 * GET /api/users/:id のコールバック
	 */
	private class GetUsersCallback implements OkHttpApiRequest.Callback<GetUsersResponseDto> {
		@Override
		public void onError(OkHttpApiRequest.Error error) {
			mGetUsersRequest = null;
			DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);

			if (!isAuthErrorWithAlert(error)) {
				// 共通エラーダイアログの表示
				DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.dialog_error_body));
			}
		}

		@Override
		public void onSuccess(GetUsersResponseDto responseDto) {
			mGetUsersRequest = null;
			mGetUsersResponseDto = responseDto;
			DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);

			if (StringUtil.isBlank(responseDto.mobileNumber)) {
				// 電話番号未登録エラーダイアログの表示
				DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ALERT, null, getString(R.string.dialog_tel_number_not_register_body));
			} else {
				// ダイアル確認ダイアログの表示
				DialogUtil.showConfirmDialog(getSupportFragmentManager(), DialogUtil.Tag.TEL, getString(R.string.dialog_tel_title, responseDto.mobileNumber), getString(R.string.dialog_tel_body));
			}
		}
	}

	/*
	 * Video Chat 開始のコールバック
	 */
	private class StartVideoChatCallback implements OkHttpApiRequest.Callback<StartVideoChatResponseDto> {
		public StartVideoChatCallback() {
		}

		@Override
		public void onError(OkHttpApiRequest.Error error) {
			DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
			mStartVideoChatRequest = null;

			if (!isAuthErrorWithAlert(error)) {
				// メッセージ送信エラーダイアログの表示
				DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.chatcenter_dialog_message_error_body));
			}

		}

		@Override
		public void onSuccess(StartVideoChatResponseDto responseDto) {
			mStartVideoChatRequest = null;
			DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);

			if ( mVideoCallItem != null && mVideoCallItem.messageId != null &&
					Integer.valueOf(mVideoCallItem.messageId).equals(responseDto.id) ){
				// already on chat
				return;
			}

			openVideoChatActivity(responseDto.widget, String.valueOf(responseDto.id));
		}
	}



	private class GetAppsCallback implements OkHttpApiRequest.Callback<GetAppsResponseDto> {
		@Override
		public void onSuccess(GetAppsResponseDto responseDto) {

			if (responseDto == null || responseDto.items == null || responseDto.items.size() == 0) {
				return;
			}

			String appToken = getAppToken();
			if ( appToken != null ) {
				for (GetAppsResponseDto.App item : responseDto.items) {
					if (appToken.equals(item.token)) {
						mCurrentApp = item;
						mIbtSendSticker.setVisibility(View.VISIBLE);
						setupWidgetMenu();
						break;
					}
				}
			}
			mGetAppsRequest = null;
		}

		@Override
		public void onError(OkHttpApiRequest.Error error) {
			mGetAppsRequest = null;
			if (!isAuthErrorWithAlert(error)) {
				// エラー表示＆再接続
				errorWithReconnect();
			}
		}
	}

	private class SendMessageReplyCallback implements ApiRequest.Callback<PostMessagesResponseDto> {

		private BasicWidget.StickerAction.ActionData mAnswer;
		private String msgId;

		public SendMessageReplyCallback(BasicWidget.StickerAction.ActionData answer, String replyTo) {
			mAnswer = answer;
			msgId = replyTo;
		}

		/**
		 * レスポンスが成功の場合のコールバック
		 *
		 * @param responseDto レスポンスDTO
		 */
		@Override
		public void onSuccess(PostMessagesResponseDto responseDto) {
			mPostMessagesRequest = null;

			for ( int i = 0; i < mAdapter.getCount(); i++ ){
				ChatItem item = mAdapter.getItem(i);
				if ( item.widget != null && item.id == Integer.parseInt(msgId)) {
					item.widget.onSendMessageReplySuccess(mAnswer);
					break;
				}
			}
			mAdapter.notifyDataSetChanged();
		}

		/**
		 * レスポンスが失敗の場合のコールバック
		 *
		 * @param error エラー
		 */
		@Override
		public void onError(ApiRequest.Error error) {
			mPostMessagesRequest = null;

			if (!isAuthErrorWithAlert(error)) {
				// 共通エラーダイアログの表示
				DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.dialog_error_body));
			}
		}
	}

	private class SendCheckboxReplyCallback implements ApiRequest.Callback<PostMessagesResponseDto> {

		private List<BasicWidget.StickerAction.ActionData> mAnswers;
		private String msgId;

		public SendCheckboxReplyCallback(List<BasicWidget.StickerAction.ActionData> answers, String replyTo) {
			mAnswers = answers;
			msgId = replyTo;
		}

		/**
		 * レスポンスが成功の場合のコールバック
		 *
		 * @param responseDto レスポンスDTO
		 */
		@Override
		public void onSuccess(PostMessagesResponseDto responseDto) {
			mPostMessagesRequest = null;

			for ( int i = 0; i < mAdapter.getCount(); i++ ){
				ChatItem item = mAdapter.getItem(i);
				if ( item.widget != null && item.id == Integer.parseInt(msgId)) {
					item.widget.onSendCheckboxReplySuccess(mAnswers);
					break;
				}
			}
			mAdapter.notifyDataSetChanged();
		}

		/**
		 * レスポンスが失敗の場合のコールバック
		 *
		 * @param error エラー
		 */
		@Override
		public void onError(ApiRequest.Error error) {
			mPostMessagesRequest = null;

			if (!isAuthErrorWithAlert(error)) {
				// 共通エラーダイアログの表示
				DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.dialog_error_body));
			}
		}
	}

	/**
	 * 送信メッセージのテキストボックスのウォッチャー
	 */
	private class SendTextWatcher implements TextWatcher {
		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			// empty
		}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			Editable editable = mEditText.getText();

			// span の削除
			CharacterStyle[] spans = editable.getSpans(0, editable.length(), CharacterStyle.class);
			for (CharacterStyle span : spans) {
				editable.removeSpan(span);
			}

			// 送信ボタンの有効設定
			String text = mEditText.getText().toString();
			mSendButton.setEnabled(!text.matches("^[\\s　]*$") && text.length() <= MAX_MESSAGE_TEXT_LENGTH);

			if (text.length() <= MAX_MESSAGE_TEXT_LENGTH) {
				isCanShowAlertTooLongTextMsg = true;
			}

			if (text.length() > MAX_MESSAGE_TEXT_LENGTH && isCanShowAlertTooLongTextMsg) {

				DialogUtil.showAlertDialog(getSupportFragmentManager(),
						DialogUtil.Tag.ALERT,
						null,
						String.format(getString(R.string.alert_message_text_too_long), MAX_MESSAGE_TEXT_LENGTH));

				isCanShowAlertTooLongTextMsg = false;
			}
		}

		@Override
		public void afterTextChanged(Editable editable) {
			// empty
		}
	}

	private String getAppToken(){
		String appToken = ApiUtil.getAppToken(this);
		appToken = (appToken == null || appToken.isEmpty() ) ? mParamDto.appToken : appToken;
		return appToken;
	}

	// ===================================================================================
	// WebSocket support
	// ===================================================================================
	public void connectWS() {
		CCWebSocketClient.Listener listener;
		if (mWebSocketClient != null && (listener = mWebSocketClient.getListener()) != null) {
				/*
				 * onError() などのコールバックが呼ばれないようにキャンセルを設定。
				 */
			((WebSocketClientListener) listener).cancel();
		}

		String appToken = getAppToken();
		String url = ApiUtil.getWsUrl(this) + "/?authentication="
				+ AuthUtil.getUserToken(getApplicationContext()) + "&app_token=" + appToken; //;

		mWebSocketClient = new CCWebSocketClient(this, url, new WebSocketClientListener());
		mWebSocketClient.connect();
	}

	public void disconnectWS() {
		// WebSocket の切断
		CCWebSocketClient.Listener listener;
		if (mWebSocketClient != null && (listener = mWebSocketClient.getListener()) != null) {
			((WebSocketClientListener) listener).cancel();
			// TODO: onError: java.net.SocketException: Socket closed になる。
			mWebSocketClient.disconnect();
		}
	}

	private class WebSocketClientListener extends CCWebSocketClientListener {

		@Override
		public void onWSConnect() {
			// Request join channel
			WsConnectChannelRequest wsConnectChannelRequest = new WsConnectChannelRequest();
			wsConnectChannelRequest.channelUid = mChannelUid;

			mWebSocketClient.send(wsConnectChannelRequest.toJson());

			// ネットワークエラーの非表示
			mHandler.post(mHideNWErrorTask);
		}

		@Override
		public void onWSDisconnect(int code, String reason) {
			// Do nothing
		}

		@Override
		public void onWSError(Exception exception) {
			errorWithReconnect();
		}

		private Boolean updateMessage(ChatItem item){
			boolean found = false;

			if (item.widget != null && item.widget.uid != null) {
				for (int i = 0; i < mAdapter.getCount(); i++) {
					ChatItem oldItem = mAdapter.getItem(i);
					if (oldItem.widget != null && oldItem.widget.uid == null) {
						continue;
					}

					if (oldItem.widget.uid.equals(item.widget.uid)) {
						found = true;
						mAdapter.remove(mAdapter.getItem(i));
						mAdapter.insert(item, i);
						break;
					}
				}
			}
			return found;
		}

		private void updateMessage(Integer uid, ChatItem item){
			// TODO
		}

		private void updateMessageUsersReadMessage(String channelUid, Integer messageId, Integer userId, Boolean userAdmin){
			// TODO
		}

		@Override
		public void onWSMessage(final WsMessagesResponseDto response, final String messageType) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (!mChannelUid.equals(response.channelUid)) {
						/*
						 * 自身の他のチャネルも送られるのでスキップします。
						 */
						return;
					}

					ChatItem item = response;

					if (messageType.equals(ResponseType.SUGGESTION)) {
						if (mAdapter.getCount() > 0) {
							ChatItem lastItem = mAdapter.getItem(mAdapter.getCount() - 1);
							if (!ResponseType.SUGGESTION.equals(lastItem.type)) {
								mAdapter.add(item);
								mAdapter.notifyDataSetChanged();
								scrollToLast(false);
							}
						}
					} else if (messageType.equals(ResponseType.CALLINVITE)) {
						if ( mVideoCallItem != null && mVideoCallItem.messageId != null &&
								Integer.valueOf(mVideoCallItem.messageId).equals(item.id) ){
							// already on chat
							return;
						}
						openVideoChatActivity((VideoCallWidget)item.widget, String.valueOf(item.id));
					} else {
						// Add or update the item
						boolean found = updateMessage(item);
						if (!found) {
							mAdapter.add(item);

							Boolean isResponse = messageType.equals(ResponseType.RESPONSE);
							if (isResponse && item.widget != null) {
								Integer replyTo = item.widget.getInt("reply_to");
								if (replyTo != null) {
									updateMessage(replyTo, item);
								}
							}
						}
						mAdapter.notifyDataSetChanged();

						scrollToLast(false);

						if (AuthUtil.getUserId(ChatActivity.this) != item.user.id) {
							// クライアントユーザーの更新
							mClientUserId = item.user.id;

							// 表示したメッセージを既読化
							List<Integer> unreadMessages = new ArrayList<>();
							unreadMessages.add(response.id);
							requestPostMessagesRead(unreadMessages);
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
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (!mChannelUid.equals(channelUid)) {
					/*
					 * 自身の他のチャネルも送られるのでスキップします。
					 */
						return;
					}

					try {
						Integer currentUserId = user.getInt("id");
						Boolean bFound = false;
						for (UserItem userItem : mChannelUsers) {
							if (userItem.id.equals(currentUserId)) {
								bFound = true;
								break;
							}
						}
						if ( !bFound )
							return;

						ArrayList<JSONObject> tempUserVideoChat = new ArrayList<>(userVideoChat);
						Log.d(TAG, "tempUserVideoChat original: " + tempUserVideoChat.size());
						for (int i = 0; i < tempUserVideoChat.size(); i++) {
							JSONObject userTmp = tempUserVideoChat.get(i);
							Integer userId = userTmp.getInt("id");
							if(currentUserId.equals(userId)) {
								tempUserVideoChat.remove(i); // remove old object
								Log.d(TAG, "remove old object!");
							}
						}
						Log.d(TAG, "tempUserVideoChat modified: " + tempUserVideoChat.size());
						tempUserVideoChat.add(user); // add new object
						Log.d(TAG, "add new object!");
						// update user video chat info
						userVideoChat = tempUserVideoChat;
						updateLeftOfInputToolbar();
					} catch (JSONException e) {
					}
				}
			});
		}

		@Override
		public void onWSReceiveReceipt(final String channelUid, final JSONArray messages, final JSONObject user){
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (!mChannelUid.equals(channelUid)) {
					/*
					 * 自身の他のチャネルも送られるのでスキップします。
					 */
						return;
					}

					Boolean userAdmin = null;
					Integer userId = null;
					try{
						userAdmin = user.getBoolean("admin");
						userId = user.getInt("id");
					} catch (JSONException e){
						userAdmin = false;
					}

					//update message
					for (int i=0;i < messages.length() ; i++) {
						Integer messageId = null;
						try{
							messageId = messages.getInt(i);
						} catch (JSONException e){
						}
						if (messageId == null ) break;
						updateMessageUsersReadMessage(channelUid, messageId, userId, userAdmin);
					}

					if (messages != null && userId != null) {
						for (int i=0; i<messages.length(); i++) {
							for (int j=0; j<mAdapter.getCount(); j++) {
								ChatItem msg = mAdapter.getItem(j);
								Integer messageId = null;
								try{
									messageId = messages.getInt(i);
								} catch (JSONException e){
								}
								if ( messageId != null && msg != null
										&& msg.id != null && StringUtil.isNotBlank(msg.type)
										&& msg.id.equals(messageId) && !ResponseType.SUGGESTION.equals(msg.type)) {
									//OK
									JSONArray usersReadMessage;

									usersReadMessage = new JSONArray();
									try {
										JSONArray usersReadMessageData;
										usersReadMessageData = msg.widget.getJSONArray("usersReadMessage");
										if ( usersReadMessageData != null ) {
											for (int k = 0; k < usersReadMessageData.length(); ++k) {
												usersReadMessage.put(usersReadMessageData.get(k));
											}
										}

										JSONObject newData = new JSONObject();
										newData.put("id", userId);
										newData.put("admin", userAdmin ? 1 : 0 );
										usersReadMessage.put(newData);

										if ( msg != null && msg.widget != null && msg.widget.content != null ) {
											msg.widget.content.put("usersReadMessage", usersReadMessage);
										}
									} catch (JSONException e){
									}
									break;
								} else {

								}
							} // end for: self.messages
						} // end for: message

						// re-display data
						mAdapter.notifyDataSetChanged();
						//scrollToLast(true);
					}
					Log.d(TAG, "receiveReceiptFromWebScoket in ChatActivity");
				}
			});
		}

		@Override
		public void onWSRecieveAnswer(Integer messageId, Integer answerType){
		}

	}

	/**
	 *
	 */
	private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// ソフトキーボードを非表示
			hideKeyboardAndWidgetMenu();
			if (mSuggestionPlaceHolder.getVisibility() == View.VISIBLE) {
				mSuggestionPlaceHolder.setVisibility(View.GONE);
			}
			// フォーカスを移す
//			mCloseButton.requestFocus();
			// WidgetMenu を非表示
			return false;
		}
	}

	public ArrayList<WidgetMenuGridAdapter.MenuButton> getWidgetMenuButtons(List<String> stickers) {
		if (stickers == null || stickers.size() == 0) {
			return null;
		}

		if (mWidgetMenuButtons == null) {
			mWidgetMenuButtons = new ArrayList<>();

			for (String sticker : stickers) {
				if (sticker.equals(ChatCenterConstants.StickerName.STICKER_TYPE_QUESTION)) {
					mWidgetMenuButtons.add(new WidgetMenuGridAdapter.MenuButton(ChatCenterConstants.StickerName.STICKER_TYPE_QUESTION,
							R.drawable.icon_widget_question,
							getString(R.string.sticker_label_confirm)));
				} else if (sticker.equals(ChatCenterConstants.StickerName.STICKER_TYPE_DATE_TIME_AVAILABILITY)) {
					mWidgetMenuButtons.add(new WidgetMenuGridAdapter.MenuButton(ChatCenterConstants.StickerName.STICKER_TYPE_DATE_TIME_AVAILABILITY,
							R.drawable.icon_widget_schedule,
							getString(R.string.sticker_label_calendar)));
				} else if (sticker.equals(ChatCenterConstants.StickerName.STICKER_TYPE_FIXED_PHRASE)) {
					mWidgetMenuButtons.add(new WidgetMenuGridAdapter.MenuButton(ChatCenterConstants.StickerName.STICKER_TYPE_FIXED_PHRASE,
							R.drawable.icon_widget_custom,
							getString(R.string.sticker_label_phrase)));
				} else if (sticker.equals(ChatCenterConstants.StickerName.STICKER_TYPE_LOCATION)) {
					mWidgetMenuButtons.add(new WidgetMenuGridAdapter.MenuButton(ChatCenterConstants.StickerName.STICKER_TYPE_LOCATION,
							R.drawable.icon_widget_location,
							getString(R.string.sticker_label_location)));
				} else if (sticker.equals(ChatCenterConstants.StickerName.STICKER_TYPE_VIDEO_CHAT)) {
					/*
					mWidgetMenuButtons.add(new WidgetMenuGridAdapter.MenuButton(ChatCenterConstants.StickerName.STICKER_TYPE_VIDEO_CHAT,
							R.drawable.icon_widget_videochat,
							getString(R.string.sticker_label_video_call)));

					mWidgetMenuButtons.add(new WidgetMenuGridAdapter.MenuButton(ChatCenterConstants.StickerName.STICKER_TYPE_TYPE_PHONE_CALL,
							R.drawable.icon_widget_phonecall,
							getString(R.string.sticker_label_phone_call)));
					*/
					mBtVideoCall.setVisibility(View.VISIBLE);
					mBtPhoneCall.setVisibility(View.VISIBLE);
				} else if (sticker.equals(ChatCenterConstants.StickerName.STICKER_TYPE_TYPE_FILE)) {
					mWidgetMenuButtons.add(new WidgetMenuGridAdapter.MenuButton(ChatCenterConstants.StickerName.STICKER_TYPE_CAMERA,
							R.drawable.icon_widget_camera,
							getString(R.string.sticker_label_camera)));

					mWidgetMenuButtons.add(new WidgetMenuGridAdapter.MenuButton(ChatCenterConstants.StickerName.STICKER_TYPE_TYPE_FILE,
							R.drawable.icon_widget_image,
							getString(R.string.sticker_label_image)));
				}
			}

//			if (mCanDial) {
//				mWidgetMenuButtons.add(new WidgetMenuGridAdapter.MenuButton(ChatCenterConstants.StickerName.STICKER_TYPE_TYPE_PHONE_CALL,
//						R.drawable.icon_widget_phonecall,
//						getString(R.string.sticker_label_phone_call)));
//			}
		}

		return mWidgetMenuButtons;

	}

	/**
	 * Overriding onKeyDown for dismissing keyboard on key down
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mWidgetMenuPopupWidow != null && mWidgetMenuPopupWidow.isShowing()) {
			mWidgetMenuPopupWidow.dismiss();
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * Checking keyboard height and keyboard visibility
	 */
	int previousHeightDifferent = 0;
	private void checkKeyboardHeight(final View parentLayout) {

		parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						Rect r = new Rect();
						parentLayout.getWindowVisibleDisplayFrame(r);

						DisplayMetrics display = getResources().getDisplayMetrics();
						int heightDifference = display.heightPixels - (r.bottom);

						if (previousHeightDifferent - heightDifference > 50) {
							if ( mWidgetMenuPopupWidow != null ) {
								mWidgetMenuPopupWidow.dismiss();
							}
						}

						previousHeightDifferent = heightDifference;
						if (heightDifference > 100) {
							isKeyBoardVisible = true;
							changeKeyboardHeight(heightDifference);
						} else {
							isKeyBoardVisible = false;
						}
					}
				});
	}

	/**
	 * change height of emoticons keyboard according to height of actual
	 * keyboard
	 *
	 * @param height
	 *            minimum height by which we can make sure actual keyboard is
	 *            open or not
	 */
	private void changeKeyboardHeight(int height) {
		if (height > 100) {
			mKeyboardHeight = height;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT, mKeyboardHeight);
			mWidgetMenuPlaceHolder.setLayoutParams(params);
		}
	}

	/**
	 * Defining all components of emoticons keyboard
	 */
	private void enablePopUpView() {
		ArrayList<WidgetMenuGridAdapter.MenuButton> buttons = getWidgetMenuButtons(mCurrentApp.stickers);
		GridView grid = (GridView) mWidgetMenuView.findViewById(R.id.menu_grid);
		WidgetMenuGridAdapter adapter = new WidgetMenuGridAdapter(
				this, 0,buttons, this);
		grid.setAdapter(adapter);

		// Creating a pop window for emoticons keyboard
		mWidgetMenuPopupWidow = new PopupWindow(mWidgetMenuView, LinearLayout.LayoutParams.MATCH_PARENT, mKeyboardHeight, false);

		mWidgetMenuPopupWidow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				mWidgetMenuPlaceHolder.setVisibility(LinearLayout.GONE);
				mIbtSendSticker.setImageResource(R.drawable.btn_add_widget);
			}
		});
	}

	private void hideKeyboardAndWidgetMenu() {
		if(isKeyBoardVisible) {
			final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}

		if (mWidgetMenuPopupWidow != null && mWidgetMenuPopupWidow.isShowing()) {
			mWidgetMenuPopupWidow.dismiss();
		}
	}

	private void openVideoChatActivity(VideoCallWidget widget, String messageId){
		mVideoCallItem = widget;
		mVideoCallItem.setupWithUsers(mChannelUsers);
		mVideoCallItem.messageId = messageId;

		if (mVideoCallItem == null ||  mVideoCallItem.caller == null || mVideoCallItem.receivers == null )
			return;

		int currentUserId = AuthUtil.getUserId(getApplicationContext());
		boolean isCaller = (mVideoCallItem.caller.userId == currentUserId);

		Intent intent = new Intent(ChatActivity.this, VideoChatActivity.class);

		String token = null;
		if ( isCaller ){
			token = mVideoCallItem.caller.token;
		} else {
			intent.putExtra("name", mVideoCallItem.caller.displayName);
			intent.putExtra("thumb", mVideoCallItem.caller.iconUrl);

			for (VideoCallWidget.VideoCallUser user : mVideoCallItem.receivers ){
				if ( user.userId == currentUserId ){
					token = user.token;
					break;
				}
			}
		}

		if ( token == null )
			return;

		intent.putExtra(ChatCenterConstants.Extra.CHANNEL_UID, mChannelUid);
		intent.putExtra(ChatParamDto.class.getSimpleName(), mParamDto);
		intent.putExtra("api_key", mVideoCallItem.apiKey);
		intent.putExtra("session_id", mVideoCallItem.sessionId);
		intent.putExtra("token", token);
		intent.putExtra("isCalling", isCaller);
		intent.putExtra("message_id", mVideoCallItem.messageId);
		intent.putExtra("audioOnly", mVideoCallItem.action.equals(ChatCenterConstants.CallType.VOICE));
		startActivity(intent);
	}

	private int getScreenWidth() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.widthPixels;
	}
}
