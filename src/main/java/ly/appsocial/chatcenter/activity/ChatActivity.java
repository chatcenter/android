package ly.appsocial.chatcenter.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ly.appsocial.chatcenter.BuildConfig;
import ly.appsocial.chatcenter.CSLocationService;
import ly.appsocial.chatcenter.ChatCenter;
import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.adapter.ChatAdapter;
import ly.appsocial.chatcenter.activity.adapter.WidgetMenuGridAdapter;
import ly.appsocial.chatcenter.activity.receivers.NetworkStateReceiver;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.database.tables.TbApp;
import ly.appsocial.chatcenter.database.tables.TbMessage;
import ly.appsocial.chatcenter.dto.ChannelItem;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.dto.OrgItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.dto.UserItem;
import ly.appsocial.chatcenter.dto.WidgetAction;
import ly.appsocial.chatcenter.dto.param.ChatParamDto;
import ly.appsocial.chatcenter.dto.param.PhotoParamDto;
import ly.appsocial.chatcenter.dto.ws.request.GetMessagesRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostChannelsRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostMessageReadRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostMessageRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostStickerRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostStickerResponseRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostUsersRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.StartVideoChatRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.WsChannelActiveRequest;
import ly.appsocial.chatcenter.dto.ws.request.WsConnectChannelRequest;
import ly.appsocial.chatcenter.dto.ws.request.WsTypingRequest;
import ly.appsocial.chatcenter.dto.ws.response.GetAppsResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.GetFixedPhraseResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.GetMessagesResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.GetUsersResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.LiveLocationResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostChannelsResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostMessagesReadResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostMessagesResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostUsersResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.StartVideoChatResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.WsChannelResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.WsMessagesResponseDto;
import ly.appsocial.chatcenter.fragment.AlertDialogFragment;
import ly.appsocial.chatcenter.fragment.ProgressDialogFragment;
import ly.appsocial.chatcenter.fragment.WidgetPreviewDialog;
import ly.appsocial.chatcenter.util.ApiUtil;
import ly.appsocial.chatcenter.util.CCAuthUtil;
import ly.appsocial.chatcenter.util.CCLog;
import ly.appsocial.chatcenter.util.DialogUtil;
import ly.appsocial.chatcenter.util.NetworkQueueHelper;
import ly.appsocial.chatcenter.util.RealPathUtil;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.util.ViewUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;
import ly.appsocial.chatcenter.widgets.VideoCallWidget;
import ly.appsocial.chatcenter.widgets.views.WidgetView;
import ly.appsocial.chatcenter.ws.ApiRequest;
import ly.appsocial.chatcenter.ws.CCWebSocketClientListener;
import ly.appsocial.chatcenter.ws.OkHttpApiRequest;
import ly.appsocial.chatcenter.ws.WebSocketHelper;
import ly.appsocial.chatcenter.ws.parser.GetAppsParser;
import ly.appsocial.chatcenter.ws.parser.GetFixedPhraseParser;
import ly.appsocial.chatcenter.ws.parser.GetMessagesParser;
import ly.appsocial.chatcenter.ws.parser.PostChannelsParser;
import ly.appsocial.chatcenter.ws.parser.PostMessagesParser;
import ly.appsocial.chatcenter.ws.parser.PostMessagesReadParser;
import ly.appsocial.chatcenter.ws.parser.PostUsersParser;
import ly.appsocial.chatcenter.ws.parser.StartVideoChatParser;

import static ly.appsocial.chatcenter.widgets.VideoCallWidget.VIDEO_CALL_ACTION_ACCEPT;
import static ly.appsocial.chatcenter.widgets.VideoCallWidget.VIDEO_CALL_ACTION_HANGUP;
import static ly.appsocial.chatcenter.widgets.VideoCallWidget.VIDEO_CALL_ACTION_REJECT;

/**
 * Chat Activity
 * 「チャット」アクティビティ。
 *
 */
public class ChatActivity extends BaseActivity implements View.OnClickListener, AlertDialogFragment.DialogListener,
		ProgressDialogFragment.DialogListener, ViewUtil.OnSoftKeyBoardVisibleListener,
		WidgetView.StickerActionListener, WidgetMenuGridAdapter.WidgetMenuClickListener,
		TbMessage.SaveMessagesCallback, TbMessage.GetMessagesCallback, TbApp.InsertAppCallback, TbMessage.GetFailedMessagesCallback {

	private static final String TAG = ChatActivity.class.getSimpleName();

	private static final int PERMISSIONS_REQUEST_GALLERY = 3000;
	private static final int PERMISSIONS_REQUEST_CAMERA = 3001;
	private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3002;

	private static final int REQUEST_FIXED_PHRASES = 1000;
	private static final int REQUEST_CAMERA_ROLL = 10001;
	private static final int REQUEST_CAMERA = 1002;
	private static final int REQUEST_LOCATION_PICKER = 1003;
	private static final int REQUEST_SCHEDULE_WIDGET = 1006;
	private static final int REQUEST_QUESTION = 1007;
	private static final int REQUEST_LIVE_LOCATION = 1008;

	private static final int MAX_MESSAGE_TEXT_LENGTH = 2000;

	private static final int ACTIVITY_CHANNEL_SETUP_RESULT = 5000;


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
	/** 新着メッセージ追加された際にListViewが最終項目が表示されていない場合はラベルを表示する*/
	private TextView mTvNotiNewMessage;

	/** プログレスバー */
	private ProgressBar mProgressBar;
	/** ネットワークエラー */
	private TextView mNetworkErrorTextView;
	/** ListView */
	private ListView mListView;
	/** テキストボックス */
	private EditText mEdtMsgInput;
	/** 送信ボタン */
	private Button mSendButton;
	/** Adapter */
	private ChatAdapter mAdapter;
	/** The list of all messages in this channel */
	private List<ChatItem> mMessagesList;

	/** Place to show the fixed phrase on the Guest app*/
	private LinearLayout mFixedPhrasePlaceHolder;
	private LinearLayout mFixedPhraseHorizontalView;

	private boolean mNoPreviousMessage = false;
	private boolean mLoadFromFirst;
	private boolean mMessagesLoading = false;

	/** スチッカーを送信ボタン*/
	private ImageButton mIbtSendSticker;

	private ArrayList<JSONObject> userVideoChat = new ArrayList<>();


	private String mSharingLocationId;

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
	/** GET /api/apps */
	private OkHttpApiRequest<GetAppsResponseDto> mGetAppsRequest;
	/** TokBox */
	private ApiRequest<StartVideoChatResponseDto> mStartVideoChatRequest;
	/** Live Location **/
	private ApiRequest<LiveLocationResponseDto> mLiveLocationRequest;

	/** WebSocket */
//	private CCWebSocketClient mWebSocketClient;
	/** ネットワークエラー表示タスク */
	private final Runnable mShowNWErrorTask = new Runnable() {
		@Override
		public void run() {
			showNWErrorLabel(isInternetConnecting);
		}
	};
	/** ネットワークエラー非表示タスク */
	private final Runnable mHideNWErrorTask = new Runnable() {
		@Override
		public void run() {
			mNetworkErrorTextView.setVisibility(View.GONE);
		}
	};
	/** 入力中非表示タスク */
	private final Runnable mHideTypingStatusTask = new Runnable() {
		@Override
		public void run() {
			if (mTvUserTyping != null) {
				mTvUserTyping.setVisibility(View.GONE);
			}
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
//	private int mClientUserId;
	/** ダイアル可能かどうか */
//	private boolean mCanDial;

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

	// private Uri mImageUri;
	//private String mSendingContent;

	private VideoCallWidget mVideoCallItem;

	/** Video call button*/
	private ImageButton mBtVideoCall;

	/** Phone call button*/
	private ImageButton mBtPhoneCall;

	private static ChatActivity instance;
	private ChannelItem mCurrentChannelItem;
	private GuestsListDialogFragment mGuestListDialog;

	private UpdateReceiver mReceiver;
	private NetworkStateReceiver mNetworkStateReceiver;
	private boolean isInternetConnecting = true;
	private boolean isVideoCallEnabledForApp = false;
	private boolean isNeedToRefresh = true;
	private List<ChatItem> mListFixedPhrases;

	/** To check is user calling other?*/
	private boolean isCalling;

	public static ChatActivity getInstance() {
		Log.i("ChatActivity", "getInstance");
		return instance;
	}

	private boolean isNeedToSendTyping = true;
	private TextView mTvUserTyping;

	private WebSocketClientListener mWSListener = new WebSocketClientListener();

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		instance = this;

		mIsAgent = getIntent().getBooleanExtra(ChatCenterConstants.Extra.IS_AGENT, false);
		mCurrentOrgItem = getIntent().getParcelableExtra(ChatCenterConstants.Extra.ORG);


		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("");

		// パラメータの取得
		mParamDto = (ChatParamDto) getIntent().getExtras().get(ChatCenterConstants.Extra.CHAT_PARAM);

		// ルートレイアウト
		mRootLayout = (ViewGroup) findViewById(R.id.chat_root_layout);

		// 他のユーザーがメッセージを入力している
		mTvUserTyping = (TextView) findViewById(R.id.tv_user_typing);

		// タイトル
		mTitleTextView = (TextView) findViewById(R.id.toolbar_title);
		mTitleTextView.setOnClickListener(this);
		mTitleTextView.setVisibility(View.GONE);
		mTitleTextView.setCompoundDrawablesWithIntrinsicBounds(null, null,
				setDrawableTint(R.drawable.icon_right_triangle_small, R.color.color_chatcenter_title_text), null);

		// プログレスバー
		mProgressBar = (ProgressBar) findViewById(R.id.chat_progressbar);

		// ネットワークエラー
		mNetworkErrorTextView = (TextView) findViewById(R.id.chat_network_error_textview);
		mNetworkErrorTextView.setVisibility(View.GONE);

		// 通知ラベル
		mTvNotiNewMessage = (TextView) findViewById(R.id.tv_notification_new_message);
		mTvNotiNewMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				scrollToLast(true, false);
				showSuggestionForLastMsg(mIsAgent);
			}
		});

		// ListView
		mListView = (ListView) findViewById(R.id.chat_listview);
		mListView.addFooterView(ViewUtil.getSpaceView(this, ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.chat_list_space_top_padding)), null, false);
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				int lastVisiblePosition = firstVisibleItem + visibleItemCount;
				if ((totalItemCount - 1) == lastVisiblePosition) {
					// If the last item is been displaying, hide notification
					mTvNotiNewMessage.setVisibility(View.GONE);
				}

				// Load old message
				if ( !mNoPreviousMessage && firstVisibleItem <= 5 && !mMessagesLoading
						&& mAdapter != null && mAdapter.getCount() > 0 && mAdapter.getItem(0) != null){
					ChatItem item = mAdapter.getItem(0);
					if (isInternetConnecting) {
						requestGetMessages(item.id);
					} else {
						mTbMessage.getListMessageInChannel(mChannelUid, getOrgUid(), String.valueOf(item.created), ChatActivity.this);
						// mTbMessage.getListFailedMessageInChannel(mChannelUid, getOrgUid(), this);
					}
				}
			}
		});

		mMessagesList = new ArrayList<>();
		mAdapter = new ChatAdapter(this, mMessagesList, this, mIsAgent, mCurrentApp);
		mListView.setAdapter(mAdapter);
		final GestureDetector gestureDetector = new GestureDetector(this, new MyGestureDetector());
		mListView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});

		// テキストボックス
		mEdtMsgInput = (EditText) findViewById(R.id.chat_edittext);
		mEdtMsgInput.addTextChangedListener(new SendTextWatcher());
		mEdtMsgInput.setOnClickListener(this);

		// 送信ボタン
		mSendButton = (Button) findViewById(R.id.chat_send_button);
		mSendButton.setOnClickListener(this);

		// Video call
		mBtVideoCall = (ImageButton) findViewById(R.id.menu_video_call);
		mBtVideoCall.setOnClickListener(this);

		// Phone Call
		mBtPhoneCall = (ImageButton) findViewById(R.id.menu_phone_call);
		mBtPhoneCall.setOnClickListener(this);

		// Fixed phrase view
		mFixedPhrasePlaceHolder = (LinearLayout) findViewById(R.id.fixed_phrase_place_holder);
		mFixedPhraseHorizontalView = (LinearLayout) findViewById(R.id.fixed_phrase_horizontal_view);

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

		mReceiver = new UpdateReceiver();
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(ChatCenterConstants.BroadcastAction.UPDATE_CHAT);
		mIntentFilter.addAction(ChatCenterConstants.BroadcastAction.RELOAD_CHAT);
		registerReceiver(mReceiver, mIntentFilter);

		mNetworkStateReceiver = new NetworkStateReceiver() {
			@Override
			public void onNetWorkConnected() {
				isInternetConnecting = true;
				mHandler.post(mShowNWErrorTask);
				if (mReconnectTask != null) {
					mHandler.post(mReconnectTask);
				}
			}

			@Override
			public void onNetWorkLost() {
				isInternetConnecting = false;
				// We need to load message from start after connecting to internet
				isNeedToRefresh = true;
				mHandler.post(mShowNWErrorTask);
				WebSocketHelper.disconnect();
			}
		};
		IntentFilter netWorkStateFilter = new IntentFilter();
		netWorkStateFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		registerReceiver(mNetworkStateReceiver, netWorkStateFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Enable call function again
		isCalling = false;

		mCurrentApp = mTbApp.getAppByToken(getAppToken());
		mAdapter.setApp(mCurrentApp);
		if (!mIsAgent && StringUtil.isNotBlank(getOrgUid())) {
			mCurrentChannelItem = mTbChannel.getChannelForGuest(getOrgUid());
		}

		if (mCurrentApp != null
				&& mCurrentApp.stickers != null
				&& mCurrentApp.stickers.size() > 0) {
			mIbtSendSticker.setVisibility(View.VISIBLE);
			setupWidgetMenu();
		} else if (mCurrentApp == null && mParamDto.providerTokenCreatedAt == CCAuthUtil.getProviderTokenTimestamp(this)
				&& StringUtil.isNotBlank(CCAuthUtil.getUserToken(this))) {
			requestGetApps();
		}

		checkNetworkToStart();
		WebSocketHelper.setListener(mWSListener);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		unregisterReceiver(mReceiver);
		unregisterReceiver(mNetworkStateReceiver);

		WebSocketHelper.removeListener(mWSListener);

		if (mIsWebSocketInited) {
			disconnectWS();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		String draftTextMessage = mEdtMsgInput.getText().toString();
		saveDraftTextMessage(draftTextMessage);
	}

	private void setupWidgetMenu(){
		changeKeyboardHeight((int) getResources().getDimension(R.dimen.keyboard_height));
		enablePopUpView();
		checkKeyboardHeight(mRootLayout);
	}

	public String getChannelUid(){
		return mChannelUid;
	}

	public String getOrgUid(){
		return mParamDto.kissCd;
	}

	@Override
	protected void onStart() {
		super.onStart();

		mEdtMsgInput.requestFocus();
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

//		disconnectWS();

		// API のキャンセル
		mOkHttpClient.cancel(REQUEST_TAG);
		mPostUsersRequest = null;
		mPostChannelsRequest = null;
		mGetMessagesRequest = null;
		mPostMessagesRequest = null;
		mPostMessagesReadRequest = null;
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
				if (mIsAgent) {
					if (isTaskRoot()) {
						ChatCenter.showMessages(this, null, null, null);
						finish();
					} else {
						finish();
					}
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
			// TODO Uncomment
//			if (mWebSocketClient == null) {
//				// メッセージ送信エラーダイアログの表示
//				DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.chatcenter_dialog_message_send_error_body));
//				return;
//			}
			String message =  mEdtMsgInput.getText().toString();
			mEdtMsgInput.setText("");
			requestPostMessages(message);
		} else if (id == R.id.toolbar_title) {

			// Information button
			InfoActivity.startActivityForResult(this, getOrgUid(),
					mChannelUid, mParamDto,ACTIVITY_CHANNEL_SETUP_RESULT);

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
			startVideoChat(true);
		} else if (id == R.id.menu_video_call) {
			startVideoChat(false);
		}
	}

	@Override
	public void onDialogCancel(String tag) {
		if (DialogUtil.Tag.ERROR_401.equals(tag)) { // 401エラー
			finish();
		}
	}

	/**
	 * ダイアログの肯定ボタンを押下した際のコールバック。
	 *
	 * @param tag このフラグメントのタグ
	 */
	@Override
	public void onPositiveButtonClick(String tag) {

	}

	@Override
	public void onSoftKeyBoardVisible(boolean visible) {
		if (visible && mAdapter != null) {
			/*
			 * ソフトキーボードが表示されたらメッセージの最後にスクロールします。
			 */
			scrollToLast(true, false);
			mFixedPhrasePlaceHolder.setVisibility(View.GONE);
		} else {
			showFixedPhraseView(mIsAgent);
		}
	}

	public GetAppsResponseDto.App getCurrentApp(){
		return mCurrentApp;
	}

	// //////////////////////////////////////////////////////////////////////////
	// プライベートメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Send a request to the API
	 * API にリクエストします。
	 */
	private void requestApis() {

		if (mIsInit && isInternetConnecting) {
			/*
			 * 初回のみプログレスを表示します。
			 */
			mProgressBar.setVisibility(View.VISIBLE);
			mIsInit = false;
		}

		if (StringUtil.isNotBlank(mParamDto.channelUid)) { // 履歴画面から
			mChannelUid = mParamDto.channelUid;

			// Restore draft message
			restoreDraftTextMessage();

			// channels->message
			if ((mCurrentChannelItem == null || isNeedToRefresh) && isInternetConnecting) {
				isNeedToRefresh = false;
				requestGetChannel(mChannelUid);
			}
		} else if (isInternetConnecting){ // 詳細から
			if (mParamDto.providerTokenCreatedAt != CCAuthUtil.getProviderTokenTimestamp(this)
					|| StringUtil.isBlank(CCAuthUtil.getUserToken(this))) { // Userトークンが無い
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
		if (!isInternetConnecting || mPostUsersRequest != null) {
			return;
		}

		String path = "users";

		PostUsersRequestDto postUsersRequestDto = new PostUsersRequestDto();
		postUsersRequestDto.provider = mParamDto.provider;
		postUsersRequestDto.providerToken = mParamDto.providerToken;
		postUsersRequestDto.providerTokenSecret = mParamDto.providerTokenSecret;
		postUsersRequestDto.providerRefreshToken = mParamDto.providerRefreshToken;
		postUsersRequestDto.setProviderTokenCreateAt(this, mParamDto.providerTokenCreatedAt);
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
		if (!isInternetConnecting || mGetChannelsRequest != null) {
			return;
		}

		String path = "channels/" + channelUid;

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

		mGetChannelsRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.GET, path, null, headers,
				new PostChannelsCallback(), new PostChannelsParser());
		if (mParamDto.appToken != null) {
			mGetChannelsRequest.setApiToken(mParamDto.appToken);
		}
		NetworkQueueHelper.enqueue(mGetChannelsRequest, REQUEST_TAG);
	}

	/**
	 * POST /api/channels
	 */
	private void requestPostChannels() {
		if (!isInternetConnecting || mPostChannelsRequest != null) {
			return;
		}

		String path = "channels/";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

		PostChannelsRequestDto postChannelsRequestDto = new PostChannelsRequestDto();
		postChannelsRequestDto.orgUID = mParamDto.kissCd;
		postChannelsRequestDto.channelInformation = mParamDto.channelInformations;
		Map<String, String> params = postChannelsRequestDto.toParams();

		mPostChannelsRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, params, headers,
				new PostChannelsCallback(), new PostChannelsParser());
		if (mParamDto.appToken != null) {
			mPostChannelsRequest.setApiToken(mParamDto.appToken);
		}
		NetworkQueueHelper.enqueue(mPostChannelsRequest, REQUEST_TAG);
	}

	/**
	 * GET /api/channels/:channel_uid/messages
	 */
	private void requestGetMessages(Integer lastId) {
		if (!isInternetConnecting || mGetMessagesRequest != null) {
			return;
		}

		mMessagesLoading = true;

		String path = "channels/" + mChannelUid + "/messages";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

		GetMessagesRequestDto getMessagesRequestDto = new GetMessagesRequestDto();
		getMessagesRequestDto.maxLoadNum = ChatCenterConstants.MAX_MESSAGE_ON_LOAD;
		if ( lastId != null ){
			mLoadFromFirst = false;
			getMessagesRequestDto.lastId = lastId;
		} else {
			mLoadFromFirst = true;
		}

		mGetMessagesRequest = new OkHttpApiRequest<>(getApplicationContext(), ApiRequest.Method.GET,
				path, getMessagesRequestDto.toParams(), headers, new GetMessagesCallback(), new GetMessagesParser());
		if (mParamDto.appToken != null) {
			mGetMessagesRequest.setApiToken(mParamDto.appToken);
		}
		NetworkQueueHelper.enqueue(mGetMessagesRequest, REQUEST_TAG);
	}

	/**
	 * GET /api/fixed_phrases?org_uid
	 */
	private void requestGetFixedPhrase() {

		String path = "fixed_phrases?org_uid=" + getOrgUid();

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

		ApiRequest<GetFixedPhraseResponseDto> mGetFixedPhraseRequest = new OkHttpApiRequest<>(getApplicationContext(),
				ApiRequest.Method.GET, path, null, headers, new GetFixedPhraseCallback(),
				new GetFixedPhraseParser());

		if (mParamDto.appToken != null) {
			mGetFixedPhraseRequest.setApiToken(mParamDto.appToken);
		}

		NetworkQueueHelper.enqueue(mGetFixedPhraseRequest, REQUEST_TAG);
	}

	/**
	 * POST /api/channels/:channel_uid/messages
	 */
	private void requestPostMessages(String message) {

		int currentUserId = CCAuthUtil.getUserId(this);
		String tmpUid = mChannelUid + "-" + currentUserId + "" + System.currentTimeMillis();

		// Getting data Add the temporary message
		ChatItem item = ChatItem.createTemporaryMessage(message, tmpUid, CCAuthUtil.getCurrentUser(this));
		item.channelUid = mChannelUid;
		item.orgUid = getOrgUid();
		item.type = ResponseType.MESSAGE;
		item.rawContent = message;

		if (!isInternetConnecting) {
			item.localStatus = ChatItem.ChatItemStatus.SEND_FAILED;
		}

		// Save message into database
		mTbMessage.updateOrInsert(item.rebuildChatItem());

		item = mTbMessage.getMessage(item.widget.uid, item.channelUid, item.orgUid);
		mAdapter.add(item);
		mAdapter.notifyDataSetChanged();
		mListView.post(new Runnable() {
			@Override
			public void run() {
				scrollToLast(true, false);
			}
		});

		if (isInternetConnecting) {
			String path = "channels/" + mChannelUid + "/messages";

			Map<String, String> headers = new HashMap<>();
			headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

			mPostMessagesRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers, headers,
					new PostMessagesCallback(item), new PostMessagesParser());
			if (mParamDto.appToken != null) {
				mPostMessagesRequest.setApiToken(mParamDto.appToken);
			}
			PostMessageRequestDto postMessagesRequestDto = new PostMessageRequestDto();
			postMessagesRequestDto.text = message;
			postMessagesRequestDto.tmpUid = tmpUid;

			mPostMessagesRequest.setJsonBody(postMessagesRequestDto.toJson());
			NetworkQueueHelper.enqueue(mPostMessagesRequest, REQUEST_TAG);
		}
	}

	/**
	 * POST /api/channels/:channel_uid/messages
	 */
	private void requestPostSticker(String content) {
		if (content == null) {
			return;
		}

		String type = ResponseType.STICKER;

		// Getting data
		int currentUserId = CCAuthUtil.getUserId(this);
		String tmpUid = mChannelUid + "-" + currentUserId + "" + System.currentTimeMillis();
		// Add the temporary message
		ChatItem item = ChatItem.createTemporarySticker(content, tmpUid, CCAuthUtil.getCurrentUser(this));
		item.channelUid = mChannelUid;
		item.orgUid = getOrgUid();
		item.type = type;
		item.rawContent = content;

		if (!isInternetConnecting) {
			item.localStatus = ChatItem.ChatItemStatus.SEND_FAILED;
		}

		// Save message into database
		mTbMessage.updateOrInsert(item.rebuildChatItem());

		item = mTbMessage.getMessage(item.widget.uid, item.channelUid, item.orgUid);
		mAdapter.add(item);
		mAdapter.notifyDataSetChanged();
		mListView.post(new Runnable() {
			@Override
			public void run() {
				scrollToLast(true, false);
			}
		});

		if (isInternetConnecting) {
			String path = "channels/" + mChannelUid + "/messages";

			Map<String, String> headers = new HashMap<>();
			headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

			mPostMessagesRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers,
					headers, new PostMessagesCallback(item), new PostMessagesParser());
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
	}

	/**
	 * ビデオチャットの開始要求
	 *
	 * POST /api/channels/:channel_uid/call
	 */
	private void startVideoChat(boolean audioOnly) {
		if (mIsAgent && mCurrentChannelItem != null
				&& mCurrentChannelItem.getGuests() != null
				&& mCurrentChannelItem.getGuests().size() > 1) {
			selectUserToCall(audioOnly, mCurrentChannelItem);
		} else {
			requestStartVideoChat(audioOnly, null);
		}
	}

	private void requestStartVideoChat(boolean audioOnly, List<String> userIds) {
		if (isCalling) {
			return;
		}

		if (!isInternetConnecting) {
			showNWErrorDialog();
			return;
		}

		// Disable call
		isCalling = true;

		String progMsg = getResources().getString(R.string.processing);
		DialogUtil.showProgressDialog(getSupportFragmentManager(), progMsg, DialogUtil.Tag.PROGRESS);

		String path = "channels/" + mChannelUid + "/calls";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

		mStartVideoChatRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers, headers,
				new StartVideoChatCallback(), new StartVideoChatParser());
		if (mParamDto.appToken != null) {
			mStartVideoChatRequest.setApiToken(mParamDto.appToken);
		}
		StartVideoChatRequestDto startVideoChatRequestDto = new StartVideoChatRequestDto();
		startVideoChatRequestDto.action = audioOnly ? ChatCenterConstants.CallType.VOICE : ChatCenterConstants.CallType.VIDEO;
		if (userIds != null && userIds.size() > 0) {
			List<StartVideoChatRequestDto.Receiver> receivers = new ArrayList<>();
			for (String userId: userIds) {
				if (StringUtil.isNotBlank(userId)) {
					receivers.add(new StartVideoChatRequestDto.Receiver(userId));
				}
			}
			startVideoChatRequestDto.receivers = receivers;
		}

		String jsonBody = startVideoChatRequestDto.toJson();
		mStartVideoChatRequest.setJsonBody(jsonBody);
		NetworkQueueHelper.enqueue(mStartVideoChatRequest, REQUEST_TAG);
	}


	/**
	 * Live Locationの開始要求
	 *
	 */
	private void requestStartLiveLocation(Location location, final int share_time) {
		if (mLiveLocationRequest != null) {
			return;
		}

//		String progMsg = getResources().getString(R.string.processing);
//		DialogUtil.showProgressDialog(getSupportFragmentManager(), progMsg, DialogUtil.Tag.PROGRESS);

		String content = ChatItem.createLiveLocationStickerContent(location, this);

		// Getting data
		int currentUserId = CCAuthUtil.getUserId(getApplicationContext());
		String tmpUid = mChannelUid + "-" + currentUserId + "" + System.currentTimeMillis();
		// Add the temporary message
		ChatItem item = ChatItem.createTemporarySticker(content,
				tmpUid, CCAuthUtil.getCurrentUser(this));
		item.channelUid = StringUtil.isBlank(mChannelUid) ? "" : mChannelUid;
		item.orgUid = getOrgUid();
		item.type = ResponseType.STICKER;
		item.stickerType = ChatCenterConstants.StickerName.STICKER_TYPE_CO_LOCATION;
		item.rawContent = "" + share_time;
		item.localStatus = ChatItem.ChatItemStatus.SENDING;

		if (!isInternetConnecting) {
			item.localStatus = ChatItem.ChatItemStatus.SEND_FAILED;
		}

		mTbMessage.updateOrInsert(item.rebuildChatItem());

		item = mTbMessage.getMessage(item.widget.uid, item.channelUid, item.orgUid);
		mAdapter.add(item);
		mAdapter.notifyDataSetChanged();
		mListView.post(new Runnable() {
			@Override
			public void run() {
				scrollToLast(true, false);
			}
		});

		if (isInternetConnecting) {
			String path = "channels/" + mChannelUid + "/messages";

			Map<String, String> headers = new HashMap<>();
			headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

			mLiveLocationRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path,
					headers, headers, new StartLiveLocationCallback(item, share_time),
					new ApiRequest.Parser<LiveLocationResponseDto>() {
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

			if (mParamDto.appToken != null) {
				mLiveLocationRequest.setApiToken(mParamDto.appToken);
			}
			
			item.widget.stickerContent.stickerData.type = "start";

			PostStickerRequestDto requestDto = new PostStickerRequestDto();
			requestDto.content = new Gson().toJson(item.widget).toString();
			requestDto.type = ResponseType.STICKER;
			requestDto.tmpUid = tmpUid;

			mLiveLocationRequest.setJsonBody(requestDto.toJson());
			NetworkQueueHelper.enqueue(mLiveLocationRequest, REQUEST_TAG);
		}
	}

	/**
	 * POST /api/channels/:channel_uid/messages Reply sticker
	 */
	private void sendMessageResponseForChannel(BasicWidget.StickerAction.ActionData answer, String replyTo) {
		if (!isInternetConnecting) {
			showNWErrorDialog();
			return;
		}

		String path = "channels/" + mChannelUid + "/messages";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

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
		if (!isInternetConnecting) {
			showNWErrorDialog();
			return;
		}

		String path = "channels/" + mChannelUid + "/messages";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

		mPostMessagesRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers,
				headers, new SendCheckboxReplyCallback(answers, replyTo), new PostMessagesParser());
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
		if (!isInternetConnecting) {
			return;
		}

		String path = "channels/" + mChannelUid + "/messages";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

		mPostMessagesReadRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, headers,
				headers, new PostMessagesReadCallback(), new PostMessagesReadParser());
		if (mParamDto.appToken != null) {
			mPostMessagesReadRequest.setApiToken(mParamDto.appToken);
		}
		PostMessageReadRequestDto postMessagesReadRequestDto = new PostMessageReadRequestDto();
		postMessagesReadRequestDto.messageIds = messageIds;

		mPostMessagesReadRequest.setJsonBody(postMessagesReadRequestDto.toJson());
		NetworkQueueHelper.enqueue(mPostMessagesReadRequest, REQUEST_TAG);
	}

	/**
	 * GET /api/apps
	 *
	 * To get current app.
	 */
	private void requestGetApps() {
		if (!isInternetConnecting || mGetAppsRequest != null) {
			return;
		}

		String path = "apps";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

		mGetAppsRequest = new OkHttpApiRequest<>(getApplicationContext(), OkHttpApiRequest.Method.GET,
				path, null, headers, new GetAppsCallback(), new GetAppsParser());

		NetworkQueueHelper.enqueue(mGetAppsRequest, REQUEST_TAG);
	}

	/**
	 * POST /api/channels/:channel_uid/messages
	 */

	private static String getMimeType(String url) {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}
		return type;
	}

	private void requestUploadFile(String picturePath, MediaType mediaType) {

		int currentUserId = CCAuthUtil.getUserId(this);
		String tmpUid = mChannelUid + "-" + currentUserId + "" + System.currentTimeMillis();
		// Add the temporary message
		ChatItem item = ChatItem.createTemporaryImageSticker(picturePath, tmpUid, CCAuthUtil.getCurrentUser(this));
		item.channelUid = mChannelUid;
		item.orgUid = getOrgUid();
		item.type = ResponseType.STICKER;
		item.rawContent = picturePath;
		item.stickerType = ChatCenterConstants.StickerName.STICKER_TYPE_TYPE_FILE;

		if (!isInternetConnecting) {
			item.localStatus = ChatItem.ChatItemStatus.SEND_FAILED;
		}

		// Save message into database
		mTbMessage.updateOrInsert(item.rebuildChatItem());

		item = mTbMessage.getMessage(item.widget.uid, item.channelUid, item.orgUid);
		mAdapter.add(item);
		mAdapter.notifyDataSetChanged();
		scrollToLast(true, false);

		if (isInternetConnecting) {
			File fileToUpload = new File(picturePath);
			if (fileToUpload == null || !fileToUpload.exists()) {
				return;
			}

			String path = "channels/" + mChannelUid + "/messages/upload_files";

			Map<String, String> headers = new HashMap<>();
			headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

			mPostMessagesRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path,
					headers,
					headers,
					new PostMessagesCallback(item),
					new PostMessagesParser());

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
	}

//	/**
//	 * ネットワークエラー表示し、再接続を行います。
//	 */
//	private void errorWithReconnect() {
//		// ネットワークエラー表示
//		mHandler.post(mShowNWErrorTask);
//		// 再接続
//		mHandler.postDelayed(mReconnectTask, 3000);
//	}

	/**
	 * API が認証エラーか判定し、認証エラーの場合は認証エラーダイアログを表示します。
	 *
	 * @param error OkHttp's error response
	 * @return 認証エラーの場合は true、そうでない場合は false
	 */
	private boolean isAuthErrorWithAlert(OkHttpApiRequest.Error error) {
		// Hide loading dialog
		mProgressBar.setVisibility(View.GONE);

		// Check if is Authentication Token error -> login again
		if (error.getNetworkResponse() == null
				|| error.getNetworkResponse() != null && error.getNetworkResponse().code() != 401) {
			return false;
		}
		DialogUtil.showAlertDialog(getSupportFragmentManager(),
				DialogUtil.Tag.ERROR_401, null, getString(R.string.dialog_auth_error_body));
		return true;
	}

	/**
	 *
	 * リストを最後までスクロールします。
	 *
	 * @param force 強制にリストを最後までスクロール
	 * @param isSmooth
     */
	private void scrollToLast(boolean force, boolean isSmooth) {
		int lastPosition = mAdapter.getCount() + mListView.getHeaderViewsCount() + mListView.getFooterViewsCount() - 1;
		int lastVisiblePosition = mListView.getLastVisiblePosition();
		if (lastVisiblePosition >= lastPosition - 1 || force) {
			mTvNotiNewMessage.setVisibility(View.GONE);
			if (isSmooth) {
				mListView.smoothScrollToPosition(lastPosition);
			} else {
				mListView.setSelectionFromTop(lastPosition, 0);
			}
		}
	}

	private void showNotifiNewMessage(ChatItem chatItem) {
		if (chatItem == null || chatItem.user.id == CCAuthUtil.getUserId(this)) {
			return;
		}

		int lastPosition = mAdapter.getCount() + mListView.getHeaderViewsCount() + mListView.getFooterViewsCount() - 1;
		int lastVisiblePosition = mListView.getLastVisiblePosition();
		if (lastVisiblePosition < lastPosition - 1) {
			// Show a notification instead of scrolling to last position
			mTvNotiNewMessage.setVisibility(View.VISIBLE);

			StringBuilder latestMessageBuilder = new StringBuilder();
			if (chatItem != null && chatItem.user != null) {
				latestMessageBuilder.append(chatItem.user.displayName);
				latestMessageBuilder.append(getString(R.string.person_name_suffix));
				latestMessageBuilder.append(": ");
			}

			if (ResponseType.SUGGESTION.equals(chatItem.type)) {
				latestMessageBuilder.append(getString(R.string.new_suggestion));
			} else if (ResponseType.STICKER.equals(chatItem.type)) {
				latestMessageBuilder.append(getString(R.string.sent_a_widget));
			} else if (ResponseType.CALL.equals(chatItem.type)) {
				latestMessageBuilder.append(getString(R.string.called));
			} else {
				latestMessageBuilder.append(chatItem.widget == null || StringUtil.isBlank(chatItem.widget.text) ?
						getString(R.string.no_message) : chatItem.widget.text);
			}

			mTvNotiNewMessage.setText(latestMessageBuilder.toString());
		} else {
			mTvNotiNewMessage.setVisibility(View.GONE);
		}
	}

//	/**
//	 * 電話起動のインテントを生成します。
//	 * <p>
//	 * 電話機能がない、もしくは電話アプリがシステム内に存在しない場合は null を返します。
//	 * </p>
//	 *
//	 * @param context コンテキスト
//	 * @param phoneNumber 電話番号
//	 * @return 生成したインテント。電話発信できない状況では null
//	 */
//	public static Intent newDialIntent(final Context context, final String phoneNumber) {
//
//		final Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//		// 電話アプリがインストールされているか確認
//		final List<ResolveInfo> appInfo = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//		if (appInfo == null || appInfo.size() == 0) {
//			return null;
//		}
//		return intent;
//	}

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
		if (StringUtil.isBlank(actionData.type) && actionData != null) {
			for (String actionString : actionData.action) {
				if (actionString.startsWith(WidgetAction.REPLY_SUGGESTION_MSG)) {
					mEdtMsgInput.setText(actionData.message);
					break;
				} else if (actionString.startsWith(WidgetAction.OPEN_CALENDAR)) {
					onDateTimeAvailabilityClicked();
					break;
				} else if (actionString.startsWith("http")) {
					Intent intent = new Intent(this, WebViewActivity.class);
					intent.putExtra(ChatCenterConstants.Extra.URL, actionData.action.get(0));
					startActivity(intent);
					break;
				}
			}
		} else if (StringUtil.isNotBlank(actionData.type) && actionData != null) {

			for (String actionString : actionData.action) {
				if (actionString.startsWith(WidgetAction.OPEN_CALENDAR)) {
					/** "open calendar" is specified */
					onDateTimeAvailabilityClicked();
					break;
				} else if (actionString.startsWith(WidgetAction.OPEN_IMAGE)) {
					/** "open image" is specified*/
					try {
						String imageUrl = actionString.substring(actionString.indexOf("http"));
						URL url = new URL(imageUrl);
						PhotoParamDto photoParam = new PhotoParamDto(imageUrl);
						photoParam.fileName = StringUtil.getFileNameFromUrl(url);
						Intent intent = new Intent(this, PhotoActivity.class);
						intent.putExtra(PhotoActivity.PHOTO_DATA, photoParam);
						this.startActivity(intent);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
					break;
				} else if (actionString.startsWith(WidgetAction.REPLY_SUGGESTION_MSG)) {
					mEdtMsgInput.setText(actionData.message);
					break;
				} else if (actionString.startsWith(WidgetAction.REPLY_SUGGESTION_STICKER)
						|| actionData.type.equals(ResponseType.LOCATION)
						|| actionData.type.equals(ResponseType.SAVED)
						|| actionData.type.equals(ResponseType.FILE)) {
					Gson gson = new Gson();
					openWidgetPreview(gson.toJson(actionData.suggestionSticker).toString());
					break;
				} else if (actionString.startsWith(WidgetAction.OPEN_LOCATION) && actionData.action.size() > 1) {
					String mapUrl = actionData.action.get(1);
					Intent intent = new Intent(this, WebViewActivity.class);
					intent.putExtra(ChatCenterConstants.Extra.URL, mapUrl);
					intent.putExtra(ChatCenterConstants.Extra.ACTIVITY_TITLE, getString(R.string.google_map));
					startActivity(intent);
					break;
				} else {
					if (actionData.type.equals("url") && actionString.startsWith("http")) {
						Intent intent = new Intent(this, WebViewActivity.class);
						intent.putExtra(ChatCenterConstants.Extra.URL, actionData.action.get(0));
						startActivity(intent);
						break;
					}
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
		mSuggestionPlaceHolder.post(new Runnable() {
			@Override
			public void run() {
				scrollToLast(true, false);
			}
		});
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

	private View getFixedPhraseView(final ChatItem chatItem) {
		if (chatItem == null) {
			return null;
		}

		View suggestionView = LayoutInflater.from(this).inflate(R.layout.view_suggestion_title_item, null);
		TextView contentView = (TextView) suggestionView.findViewById(R.id.suggestion_title);
		ImageView icon = (ImageView) suggestionView.findViewById(R.id.suggestion_icon);

		if (chatItem.widget != null) {
			if(chatItem.widget.message != null
					&& StringUtil.isNotBlank(chatItem.widget.message.text)) {
				contentView.setText(chatItem.widget.message.text);
			}

			if (chatItem.widget.stickerAction != null
					&& StringUtil.isNotBlank(chatItem.widget.stickerAction.actionType)) {
				icon.setImageResource(chatItem.widget.getWidgetIcon(chatItem.widget.stickerAction.actionType));
				icon.setVisibility(View.VISIBLE);
			} else {
				icon.setVisibility(View.GONE);
			}
		}

		// contentView.setCompoundDrawables(null, null, null, null);
		suggestionView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				openWidgetPreview(chatItem.widget.content.toString());
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

		mFixedPhrasePlaceHolder.setVisibility(View.GONE);
	}

	@Override
	public void onMenuButtonClicked(String type){

		hideKeyboardAndWidgetMenu();

		switch (type){
			case ChatCenterConstants.StickerName.STICKER_TYPE_FIXED_PHRASE:
				if (!isInternetConnecting) {
					showNWErrorDialog();
				} else {
					onFixedPhraseClicked();
				}
				break;
			case ChatCenterConstants.StickerName.STICKER_TYPE_DATE_TIME_AVAILABILITY:
				onDateTimeAvailabilityClicked();
				break;
			case ChatCenterConstants.StickerName.STICKER_TYPE_LOCATION:
				if (!isInternetConnecting) {
					showNWErrorDialog();
				} else {
					onLocationClicked();
				}
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
				if (!isInternetConnecting) {
					showNWErrorDialog();
				} else {
					startVideoChat(false);
				}
				//videoChatCreateSession();
				break;
			case ChatCenterConstants.StickerName.STICKER_TYPE_TYPE_PHONE_CALL:
				if (!isInternetConnecting) {
					showNWErrorDialog();
				} else {
					startVideoChat(true);
				}
				//requestGetUsers();
				break;
		}
	}

	/*
	 * Fixed Phrase Widget
	 */
	private void onFixedPhraseClicked() {
		Intent intent = new Intent(this, FixedPhraseActivity.class);
		if (mParamDto != null) {
			intent.putExtra(FixedPhraseActivity.ORG_UID, mParamDto.kissCd);
			intent.putExtra(FixedPhraseActivity.API_TOKEN, mParamDto.appToken);
		}

		intent.putExtra(FixedPhraseActivity.IS_AGENT, mIsAgent);
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
		final String[] items = {getString(R.string.location_select_map), getString(R.string.location_select_live), getString(R.string.cancel)};
		new AlertDialog.Builder(this)
				.setTitle(R.string.location_dialog_title)
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if ( which == 0 ){
							GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
							int status = googleApiAvailability.isGooglePlayServicesAvailable(ChatActivity.this);
							try {
								PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
								startActivityForResult(builder.build(ChatActivity.this), REQUEST_LOCATION_PICKER);
							} catch (GooglePlayServicesRepairableException e) {
								GooglePlayServicesUtil.getErrorDialog(status, ChatActivity.this,
										0).show();
							} catch (GooglePlayServicesNotAvailableException e) {
								GooglePlayServicesUtil.getErrorDialog(status, ChatActivity.this,
										0).show();
							}
						} else if ( which == 1 ){
							Intent intent = new Intent(ChatActivity.this, ShareLocationActivity.class);
							intent.putExtra("show_preview", true);
							startActivityForResult(intent, REQUEST_LIVE_LOCATION);
						}
					}
				})
				.show();
	}

	private void startLiveLocationSharing(int interval, String widgetId, int share_time){
		Intent intent = new Intent(this, CSLocationService.class);
		intent.putExtra("command", ChatCenterConstants.LocationService.START);
		intent.putExtra("icon_id", ChatCenter.mAppIconId);
		intent.putExtra("app_name", ChatCenter.mAppName);
		intent.putExtra("app_token", ChatCenter.mAppToken);
		intent.putExtra("org_uid", mParamDto.kissCd);
		intent.putExtra("channel_uid", mChannelUid);
		intent.putExtra("interval", interval);
		intent.putExtra("widget_id", widgetId);
		intent.putExtra("share_time", share_time);

		startService(intent);
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
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			// Create the File where the photo should go
			try {
				mCurrentPhoto = createImageFile();
			} catch (IOException ex) {
				// Error occurred while creating the File
				ex.printStackTrace();
			}
			// Continue only if the File was successfully created
			if (mCurrentPhoto != null) {
				String provider = getPackageName() + ".provider";
				Uri photoURI;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					photoURI = FileProvider.getUriForFile(this,
							provider,
							mCurrentPhoto);
				} else {
					photoURI = Uri.fromFile(mCurrentPhoto);
				}
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
				startActivityForResult(takePictureIntent, REQUEST_CAMERA);
			}
		}
	}

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        return image;
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
		Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, REQUEST_CAMERA_ROLL);
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
				if (grantResults.length >= 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
						&& grantResults[1] == PackageManager.PERMISSION_GRANTED) {
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
								.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
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
								.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
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
						String mineType = getMimeType(filePath);
						final MediaType mediaType = MediaType.parse(mineType);
						previewImageWidget(filePath, mediaType);
					}
					break;
				case REQUEST_CAMERA_ROLL:
					if (data != null) {
						Uri uri = data.getData();
						final String picturePath = RealPathUtil.getRealPath(this, uri);
						String mineType = getMimeType(picturePath);
						final MediaType mediaType = MediaType.parse(mineType);
						previewImageWidget(picturePath, mediaType);
					}
					break;
				case REQUEST_LOCATION_PICKER:
					Place place = PlacePicker.getPlace(data, this);
					if (place != null) {
						String content = ChatItem.createLocationStickerContent(place, this);
						requestPostSticker(content);
					}
					break;
				case REQUEST_SCHEDULE_WIDGET:
					String scheduleContent = data.getStringExtra(ScheduleActivity.SELECTED_TIME);
					if (StringUtil.isNotBlank(scheduleContent)) {
						requestPostSticker(scheduleContent);
					}
					break;
				case REQUEST_QUESTION:
					onQuestionSelected(data);
					break;
				case REQUEST_LIVE_LOCATION:
					Location location = data.getParcelableExtra("location");
					int share_time = data.getIntExtra("share_time", 15);
					requestStartLiveLocation(location, share_time);
					break;
				case ACTIVITY_CHANNEL_SETUP_RESULT:
					boolean channelDeleted = data.getBooleanExtra(InfoActivity.CHANNEL_DELETED, false);
					if (channelDeleted) {
						finish();
					}
					break;
			}
		}
	}

	/** 選択した画像を送る*/
	private void previewImageWidget(final String picturePath, final MediaType mediaType) {
		int currentUserId = CCAuthUtil.getUserId(this);
		ChatItem item = ChatItem.createTemporaryImageSticker(picturePath, null, CCAuthUtil.getCurrentUser(this));
		String previewContent = new Gson().toJson(item.widget).toString();
		showDialogWidgetPreview(previewContent, new WidgetPreviewDialog.WidgetPreviewListener() {
			@Override
			public void onSendButtonClicked() {
				widgetPreviewDialog.dismiss();
				requestUploadFile(picturePath, mediaType);
			}
		});
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
			// requestPostSticker(content);
			openWidgetPreview(content);
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

	private void openWidgetPreview(final String content){
		showDialogWidgetPreview(content, new WidgetPreviewDialog.WidgetPreviewListener() {
			@Override
			public void onSendButtonClicked() {
				widgetPreviewDialog.dismiss();
				requestPostSticker(content);
			}
		});
	}

	private int getSuggestionIcon(String actionType) {
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
			int currentUserId = CCAuthUtil.getUserId(getApplicationContext());
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
						} else if (VIDEO_CALL_ACTION_ACCEPT.equals(event.content.action) ||
								VIDEO_CALL_ACTION_REJECT.equals(event.content.action)) {
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

	@Override
	public void saveMessagesSuccess() {
		String lastMessageCreated = null;
		if (mMessagesList != null && mMessagesList.size() > 0) {
			ChatItem lastItem = mMessagesList.get(0);
			lastMessageCreated = String.valueOf(lastItem.created);
		}

		mTbMessage.getListMessageInChannel(mChannelUid, getOrgUid(), lastMessageCreated, this);
	}

	@Override
	public void onGetMessagesSuccess(List<ChatItem> chatItems) {

		// メッセージ設定
		int nAdd = 0;
		List<Integer> readMessageIds = new ArrayList<>();

		for (ChatItem chatItem : chatItems) {

			if (chatItem.type.equals(ResponseType.SUGGESTION)) {
				if (mAdapter.getCount() > 0 && ResponseType.SUGGESTION.equals(mAdapter.getItem(0).type)) {
					continue;
				}
			} else if (chatItem.type.equals(ResponseType.CALL)) {

			}

			if (chatItem.widget != null) {
				chatItem.widget.setupWithUsers(mChannelUsers);
			}
			mAdapter.insert(chatItem, 0);
			nAdd++;

			// クライアントユーザーの更新
			readMessageIds.add(chatItem.id);
		}

		// アダプタに項目設定
		mAdapter.setChatInfo(mChannelUid);
		mAdapter.notifyDataSetChanged();

		// スクロール位置を末尾に
		if (mLoadFromFirst) {
			if (isInternetConnecting) {
				connectWS();
			}
			scrollToLast(true, false /*!isScroll*/);
		} else {
			if (mListView.getChildCount() > 0) {
				int position = mListView.getFirstVisiblePosition();
				int yOffset = mListView.getChildAt(0).getTop();
				mListView.setSelectionFromTop(position + nAdd, yOffset);
			}
		}

		// 表示したメッセージを既読化
		if (readMessageIds.size() > 0 && isInternetConnecting) {
			requestPostMessagesRead(readMessageIds);
		}

		if (chatItems == null || chatItems.size() == 0 ){
			mNoPreviousMessage = true;
		} else {
			mNoPreviousMessage = false;
		}

		mMessagesLoading = false;

		mProgressBar.setVisibility(View.GONE);

		if (mLoadFromFirst) {
			mTbMessage.getListFailedMessageInChannel(mChannelUid, getOrgUid(), this);
		}

		// Show suggestion view if need
		showSuggestionForLastMsg(mIsAgent);
	}

	private void resendFailedMessage(ChatItem chatItem) {
		// Remove from list of message
		if (mMessagesList != null && mMessagesList.size() > 0) {
			for (ChatItem item: mMessagesList) {
				if (item.localId == chatItem.localId) {
					mMessagesList.remove(item);
					break;
				}
			}
		}
		mMessagesList.remove(chatItem);

		// Remove from database
		mTbMessage.delete(chatItem);

		// Check to resend
		if (chatItem.type.equals(ResponseType.MESSAGE)) {
			requestPostMessages(chatItem.rawContent);
		} else if (chatItem.type.equals(ResponseType.STICKER)) {
			// Resend image
			if (chatItem.stickerType.equals(ChatCenterConstants.StickerName.STICKER_TYPE_TYPE_FILE)) {
				String path = chatItem.rawContent;
				String mineType = getMimeType(path);
				MediaType mediaType = MediaType.parse(mineType);
				requestUploadFile(path, mediaType);
			} else if (chatItem.stickerType.equals(ChatCenterConstants.StickerName.STICKER_TYPE_CO_LOCATION)) {
				if (chatItem != null && chatItem.widget != null && chatItem.widget.stickerContent != null
						&& chatItem.widget.stickerContent.stickerData != null) {
					Location location = new Location("");
					location.setLatitude(chatItem.widget.stickerContent.stickerData.location.latitude);
					location.setLongitude(chatItem.widget.stickerContent.stickerData.location.longitude);

					int shareTime = Integer.valueOf(chatItem.rawContent);

					requestStartLiveLocation(location, shareTime);
				}
			} else {
				requestPostSticker(chatItem.rawContent);
			}
		}
	}


	/**
	 * Finish inserting a list of App into Database
	 */
	@Override
	public void onInsertAppSuccess() {

	}

	@Override
	public void onGetFailedMessagesSuccess(List<ChatItem> chatItems) {
		// Resend failed message
		if (chatItems != null && chatItems.size() > 0) {
			for (ChatItem chatItem:chatItems) {
				resendFailedMessage(chatItem);
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
				// errorWithReconnect();
			}
		}

		@Override
		public void onSuccess(PostUsersResponseDto responseDto) {
			mPostUsersRequest = null;
			if (responseDto == null) {
				return;
			}

			mCurrentChannelItem = responseDto;
			mChannelUid = responseDto.uid;
			mTbChannel.deleteChannelInOrg(getOrgUid());
			mTbChannel.updateOrInsert(mCurrentChannelItem);

			// Restore draft message
			restoreDraftTextMessage();

			// トークンの保存
			CCAuthUtil.saveTokens(ChatActivity.this, mParamDto.providerTokenCreatedAt, responseDto.users.get(0));
			requestGetMessages(null);
			if (mCurrentApp == null) {
				requestGetApps();
			}
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
				// errorWithReconnect();
			}
		}

		@Override
		public void onSuccess(PostChannelsResponseDto responseDto) {
			mPostChannelsRequest = null;
			mGetChannelsRequest = null;

			if (responseDto == null)
				return;

			mCurrentChannelItem = responseDto;
			mChannelUid = responseDto.uid;

			mTbChannel.deleteChannelInOrg(getOrgUid());
			mTbChannel.updateOrInsert(mCurrentChannelItem);

			// Restore draft message
			restoreDraftTextMessage();

			mChannelUsers.addAll(responseDto.users);

			// タイトル設定
			mTitleTextView.setText(responseDto.getDisplayName(ChatActivity.this, mIsAgent));
			mTitleTextView.setVisibility(View.VISIBLE);

			requestGetMessages(null);

			// Set enable for video call
			if (isVideoCallEnabledForApp && mCurrentChannelItem != null && mCurrentChannelItem.canUseVideoCall(mIsAgent)) {
				mBtVideoCall.setVisibility(View.VISIBLE);
				mBtPhoneCall.setVisibility(View.VISIBLE);
			}

			if (!mIsAgent) {
				requestGetFixedPhrase();
			}
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
				// errorWithReconnect();
			}
			mMessagesLoading = false;
		}

		@Override
		public void onSuccess(GetMessagesResponseDto responseDto) {
			mGetMessagesRequest = null;

			if (responseDto == null || responseDto.items == null) {
				// エラー表示＆再接続
				// errorWithReconnect();
				return;
			}

			if ( mLoadFromFirst ){
				mAdapter.clear();
			}

			mTbMessage.saveListMessages(responseDto.items, ChatActivity.this);
//			List<ChatItem> chatItems = mTbMessage.getListMessageInChannel(mChannelUid, mCurrentOrgItem.uid);
		}
	}

	/**
	 * GET /api/users/:id のコールバック
	 */
	private class GetFixedPhraseCallback implements OkHttpApiRequest.Callback<GetFixedPhraseResponseDto> {
		@Override
		public void onError(OkHttpApiRequest.Error error) {

		}

		@Override
		public void onSuccess(GetFixedPhraseResponseDto responseDto) {
			if (responseDto == null) {
				return;
			}

			mListFixedPhrases = responseDto.getAllFixedPhrases();

			if (mListFixedPhrases != null && mListFixedPhrases.size() > 0) {
				mFixedPhraseHorizontalView.removeAllViews();
				for (ChatItem chatItem : mListFixedPhrases) {
					View view = getFixedPhraseView(chatItem);
					mFixedPhraseHorizontalView.addView(view);
					view.getLayoutParams().width = mSuggestionWidth;
				}
			}

			int paddingHorizontal = (getScreenWidth() - mSuggestionWidth) / 2;
			mFixedPhraseHorizontalView.setPadding(paddingHorizontal, 0, paddingHorizontal, 0);
			showFixedPhraseView(mIsAgent);
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

			if (!isAuthErrorWithAlert(error)) {
				// メッセージ送信エラーダイアログの表示
				// DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.chatcenter_dialog_message_send_error_body));
			}

			if (mFakeChatItem != null) {
				mFakeChatItem.localStatus = ChatItem.ChatItemStatus.SEND_FAILED;
				mTbMessage.updateOrInsert(mFakeChatItem.rebuildChatItem());

				// Update list message
				int index = indexOnList(mFakeChatItem);
				if (index >= 0) {
					mMessagesList.remove(index);
					mMessagesList.add(index, mFakeChatItem);
					mAdapter.notifyDataSetChanged();
				}
			}
		}

		@Override
		public void onSuccess(PostMessagesResponseDto responseDto) {
			DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
			// テキストボックスのクリア
			// mEdtMsgInput.setText("");

			int index = indexOnList(mFakeChatItem);
			// if is an image widget, remove it from list and database. Waiting for data from websocket
			if(ChatCenterConstants.StickerName.STICKER_TYPE_TYPE_FILE.equals(mFakeChatItem.stickerType)) {
				if (index >= 0) {
					mMessagesList.remove(index);
				}
				mAdapter.notifyDataSetChanged();
				mTbMessage.delete(mFakeChatItem);
			} else {
				// If is other type of message, update status sent successfully
				mFakeChatItem.localStatus = ChatItem.ChatItemStatus.SENT;
				mTbMessage.updateOrInsert(mFakeChatItem.rebuildChatItem());
				if (index >= 0) {
					mMessagesList.remove(index);
					mMessagesList.add(index, mFakeChatItem);
					mAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	private class StartLiveLocationCallback implements ApiRequest.Callback<LiveLocationResponseDto> {
		private ChatItem mFakeChatItem;
		private int mShareTime;

		public StartLiveLocationCallback(ChatItem fakeItem, int share_time) {
			mFakeChatItem = fakeItem;
			mShareTime = share_time;
		}

		@Override
		public void onError(OkHttpApiRequest.Error error) {
			mLiveLocationRequest = null;

			if (!isAuthErrorWithAlert(error)) {
				// メッセージ送信エラーダイアログの表示
				// DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.chatcenter_dialog_message_send_error_body));
			}

			if (mFakeChatItem != null) {
				mFakeChatItem.localStatus = ChatItem.ChatItemStatus.SEND_FAILED;
				mTbMessage.updateOrInsert(mFakeChatItem.rebuildChatItem());

				// Update list message
				int index = indexOnList(mFakeChatItem);
				if (index >= 0) {
					mMessagesList.remove(index);
					mMessagesList.add(index, mFakeChatItem);
					mAdapter.notifyDataSetChanged();
				}
			}
		}

		@Override
		public void onSuccess(LiveLocationResponseDto responseDto) {
			int interval = 10;
			mSharingLocationId = "";

			if (responseDto != null) {
				if (responseDto.content != null
						&& responseDto.content.stickerContent != null && responseDto.content.stickerContent.stickerData != null
						&& responseDto.content.stickerContent.stickerData.preferredInterval != null) {
					interval = Integer.valueOf(responseDto.content.stickerContent.stickerData.preferredInterval);
				}

				if (responseDto.id != null && !responseDto.id.isEmpty()) {
					mSharingLocationId = responseDto.id;
				}
			}

			startLiveLocationSharing(interval, mSharingLocationId, mShareTime);
			mLiveLocationRequest = null;

			// Update list message
			int index = indexOnList(mFakeChatItem);

			// If is other type of message, update status sent successfully
			mFakeChatItem.localStatus = ChatItem.ChatItemStatus.SENT;
			mTbMessage.updateOrInsert(mFakeChatItem.rebuildChatItem());
			if (index >= 0) {
				mMessagesList.remove(index);
				mMessagesList.add(index, mFakeChatItem);
				mAdapter.notifyDataSetChanged();
			}

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
				// DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.dialog_error_body));
			}
		}

		@Override
		public void onSuccess(PostMessagesReadResponseDto responseDto) {
			mPostMessagesReadRequest = null;
			// empty
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
				// DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.chatcenter_dialog_message_error_body));
			}

			isCalling = false;
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

			mTbApp.saveListApps(responseDto.items, ChatActivity.this);

			String appToken = getAppToken();
			if ( appToken != null ) {
				for (GetAppsResponseDto.App item : responseDto.items) {
					if (appToken.equals(item.token)) {
						mCurrentApp = item;
						mAdapter.setApp(mCurrentApp);
						if (mCurrentApp.stickers != null && mCurrentApp.stickers.size() > 0) {
							mIbtSendSticker.setVisibility(View.VISIBLE);
							setupWidgetMenu();
						}
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
				// errorWithReconnect();
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

			for ( int i = 0; i < mAdapter.getCount(); i++ ){
				ChatItem item = mAdapter.getItem(i);
				if ( item.widget != null && item.id == Integer.parseInt(msgId)) {
					item.widget.onSendMessageReplySuccess(mAnswer);
					mTbMessage.updateOrInsert(item.rebuildChatItem());
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

			if (!isAuthErrorWithAlert(error)) {
				// 共通エラーダイアログの表示
				// DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.dialog_error_body));
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

			for ( int i = 0; i < mAdapter.getCount(); i++ ){
				ChatItem item = mAdapter.getItem(i);
				if ( item.widget != null && item.id == Integer.parseInt(msgId)) {
					item.widget.onSendCheckboxReplySuccess(mAnswers);
					mTbMessage.updateOrInsert(item.rebuildChatItem());
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

			if (!isAuthErrorWithAlert(error)) {
				// 共通エラーダイアログの表示
				// DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.dialog_error_body));
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
			Editable editable = mEdtMsgInput.getText();

			// span の削除
			CharacterStyle[] spans = editable.getSpans(0, editable.length(), CharacterStyle.class);
			for (CharacterStyle span : spans) {
				editable.removeSpan(span);
			}

			// 送信ボタンの有効設定
			String text = mEdtMsgInput.getText().toString();
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

			// 入力中というステータスをサーバーに報告する
			if (charSequence.length() > 0) {
				sendMessageTyping(mChannelUid, CCAuthUtil.getUserId(ChatActivity.this));
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
	boolean mIsWebSocketInited;
	private void connectWS() {
		String appToken = getAppToken();
		mIsWebSocketInited = WebSocketHelper.connectWithAppToken(getApplicationContext(), appToken,
				mWSListener);
		if (WebSocketHelper.isConnected()) {
			// ネットワークエラーの非表示
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mNetworkErrorTextView.setBackgroundColor(getResources().getColor(R.color.color_green));
					mNetworkErrorTextView.setText(R.string.internet_connected);
				}
			});
			mHandler.postDelayed(mHideNWErrorTask, 1000);

			WsConnectChannelRequest wsConnectChannelRequest = new WsConnectChannelRequest();

			wsConnectChannelRequest.channelUid = mChannelUid;
			WebSocketHelper.send(wsConnectChannelRequest.toJson());

			// The channel come to active
			sendChannelActive(mChannelUid);

		}
	}

	private void disconnectWS() {
		WebSocketHelper.disconnect();
	}

	private class WebSocketClientListener extends CCWebSocketClientListener {

		@Override
		public void onWSConnect() {
			// Request join channel
			WsConnectChannelRequest wsConnectChannelRequest = new WsConnectChannelRequest();
			wsConnectChannelRequest.channelUid = mChannelUid;

			WebSocketHelper.send(wsConnectChannelRequest.toJson());

			// The channel come to active
			sendChannelActive(mChannelUid);

			// ネットワークエラーの非表示
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mNetworkErrorTextView.setBackgroundColor(getResources().getColor(R.color.color_green));
					mNetworkErrorTextView.setText(R.string.internet_connected);
				}
			});
			mHandler.postDelayed(mHideNWErrorTask, 1000);
		}

		@Override
		public void onWSDisconnect(int code, String reason) {
			// Do nothing
		}

		@Override
		public void onWSError(Exception exception) {
			// errorWithReconnect();
		}

		private Boolean updateMessage(ChatItem item){
			boolean found = false;

			if (item.widget != null && StringUtil.isNotBlank(item.widget.uid)) {
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

		private Boolean updateMessage(Integer id, ChatItem item){
			if (item.id != null ) {
				for (int i = 0; i < mAdapter.getCount(); i++) {
					ChatItem oldItem = mAdapter.getItem(i);
					if (oldItem == null || oldItem.id == null) {
						continue;
					}

					if (oldItem.id.equals(id)) {
						if (oldItem.updateWithResponse(ChatActivity.this, item) ){
							mAdapter.remove(oldItem);
							if (item != null
									&& item.widget != null
									&& item.widget.content != null) {
								try {

									Gson gson = new Gson();
									BasicWidget.StickerAction.ActionData answer = gson.fromJson(item.widget.content.getJSONObject("answer").toString(),
											BasicWidget.StickerAction.ActionData.class);
									ArrayList<BasicWidget.StickerAction.ActionData> answers = new ArrayList<>();
									JSONArray jsonArray = item.widget.content.getJSONArray("answers");
									if (jsonArray != null && jsonArray.length() > 0) {
										for (int j = 0; j < jsonArray.length(); j++) {
											JSONObject jsonObject = jsonArray.getJSONObject(j);
											BasicWidget.StickerAction.ActionData ans = gson.fromJson(jsonObject.toString(),
													BasicWidget.StickerAction.ActionData.class);
											answers.add(ans);
										}
									}

									BasicWidget.StickerAction.ResponseAction responseAction = new BasicWidget.StickerAction.ResponseAction(answer);
									responseAction.actionObj = answers;
									responseAction.actionOld = answer;
									responseAction.actionDataList = answers;

									if (oldItem.widget.stickerAction != null) {
										if (oldItem.widget.stickerAction.responseActions != null) {
											oldItem.widget.stickerAction.responseActions.clear();
											oldItem.widget.stickerAction.responseActions.add(responseAction);
										} else {
											oldItem.widget.stickerAction.responseActions = new ArrayList<>();
											oldItem.widget.stickerAction.responseActions.add(responseAction);
										}
									}

								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
							mTbMessage.updateOrInsert(oldItem.rebuildChatItem());
							mAdapter.insert(oldItem, i);
							mAdapter.notifyDataSetChanged();
							return true;
						}
					}
				}
			}
			return false;
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

					if (item.widget != null && StringUtil.isBlank(item.widget.uid)) {
						String tmpUid = mChannelUid + "-" + CCAuthUtil.getUserId(ChatActivity.this)
								+ "" + System.currentTimeMillis();
						item.widget.uid = tmpUid;
					}

					if (messageType.equals(ResponseType.SUGGESTION)) {
						// showOrHideSuggestionView(item);
						if (mAdapter.getCount() > 0) {
							ChatItem lastItem = mAdapter.getItem(mAdapter.getCount() - 1);
							if (!ResponseType.SUGGESTION.equals(lastItem.type)) {
								item.localStatus = ChatItem.ChatItemStatus.DEFAULT;
								item.channelUid = StringUtil.isBlank(mChannelUid) ? "" : mChannelUid;
								item.orgUid = getOrgUid();
								mTbMessage.updateOrInsert(item.rebuildChatItem());

								mAdapter.add(mTbMessage.getMessage(item.widget.uid, item.channelUid, item.orgUid));
								mAdapter.notifyDataSetChanged();
								scrollToLast(false, false);
								showNotifiNewMessage(item);

								// Display suggestion view if last displayed message is Suggestion
								if (mTvNotiNewMessage.getVisibility() != View.VISIBLE) {
									showSuggestionForLastMsg(mIsAgent);
								}
							}
						}
					} else if (messageType.equals(ResponseType.CALLINVITE)) {
						if ( mVideoCallItem != null && mVideoCallItem.messageId != null &&
								Integer.valueOf(mVideoCallItem.messageId).equals(item.id) ){
							// already on chat
							return;
						}

						item.type = ResponseType.CALL;
						item.widget.setupWithUsers(mChannelUsers);
						mTbMessage.updateOrInsert(item);

						VideoCallWidget videoCallWidget = (VideoCallWidget) item.widget;
						int currentUserId = CCAuthUtil.getUserId(ChatActivity.this);
						boolean showVideoChat = false;

						if (videoCallWidget != null && videoCallWidget.receivers != null && videoCallWidget.receivers.size() > 0) {
							for (VideoCallWidget.VideoCallUser receiver : videoCallWidget.receivers) {
								if (receiver.userId == currentUserId) {
									showVideoChat = true;
									break;
								}
							}
						}

						if (showVideoChat) {
							if (item.channelUid.equals(mChannelUid)) {
								openVideoChatActivity(videoCallWidget, String.valueOf(item.id));
							}
						}
					} else if (messageType.equals(ResponseType.TYPING)) {

						if (item.user != null && !item.user.id.equals(CCAuthUtil.getUserId(ChatActivity.this))) {

							mTvUserTyping.setText(Html.fromHtml(String.format(getString(R.string.user_typing), item.user.displayName)));

							// If typing label is hided then show and hide it after 5 seconds
							mTvUserTyping.setVisibility(View.VISIBLE);
							mHandler.removeCallbacks(mHideTypingStatusTask);
							mHandler.postDelayed(mHideTypingStatusTask, ChatCenterConstants.TIME_DISPLAYING_TYPING);
						}

                    } else {
						if (messageType.equals(ResponseType.CALL) && item.widget != null) {
							VideoCallWidget videoCallWidget = (VideoCallWidget) item.widget;
							if (videoCallWidget.caller == null || videoCallWidget.receivers == null || videoCallWidget.receivers.size() == 0) {
								return;
							}

							item.widget.setupWithUsers(mChannelUsers);

							ChatItem oldItem = mTbMessage.getMessage(item.id, item.channelUid, item.orgUid);
							if (oldItem != null) {
								((VideoCallWidget) oldItem.widget).events = videoCallWidget.events;
								mTbMessage.updateOrInsert(oldItem);
								int index = indexOnList(oldItem);
								if (index >= 0) {
									mMessagesList.remove(index);
									mMessagesList.add(index, oldItem);
								} else {
									mMessagesList.add(oldItem);
								}
								mAdapter.notifyDataSetChanged();
								scrollToLast(false, true);
								return;
							}
						}
						// Add or update the item
						boolean found = updateMessage(item);
						boolean processed = false;
						if (!found) {
							Boolean isResponse = messageType.equals(ResponseType.RESPONSE);
							if (isResponse && item.widget != null) {
								Integer replyTo = item.widget.getInt("reply_to");
								if (replyTo != null) {
									processed = updateMessage(replyTo, item);
									if ( !processed ){
										if ( "co-location".equals(item.widget.stickerType) ){
											// something wrong
											processed = true;
											// requestGetMessages(null);
										}
									}

								} else {
									item.updateWithResponse(ChatActivity.this, item);
								}
							}

						}

						if (CCAuthUtil.getUserId(ChatActivity.this) != item.user.id) {
							// クライアントユーザーの更新
//							mClientUserId = item.user.id;

							// 表示したメッセージを既読化
							List<Integer> unreadMessages = new ArrayList<>();
							unreadMessages.add(item.id);
							requestPostMessagesRead(unreadMessages);

						}

						// Update message if need
						if (!processed) {
							item.channelUid = StringUtil.isBlank(mChannelUid) ? "" : mChannelUid;
							item.orgUid = getOrgUid();
							mTbMessage.updateOrInsert(item.rebuildChatItem());
						}


						if (!processed && !found){
							mAdapter.add(mTbMessage.getMessage(item.widget.uid, item.channelUid, item.orgUid));
						}

						mAdapter.notifyDataSetChanged();

						if (!processed) {
							scrollToLast(false, false);
							showNotifiNewMessage(item);
						}

						// Hide the Suggestion view
						mSuggestionPlaceHolder.setVisibility(View.GONE);
					}

				}
			});
		}

		@Override
		public void onWSChannelJoin(WsChannelResponseDto response) {
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
					if (!mChannelUid.equals(channelUid) || messages == null || messages.length() == 0
							|| mMessagesList == null || mMessagesList.size() == 0) {
					/*
					 * 自身の他のチャネルも送られるのでスキップします。
					 */
						return;
					}

					// Get list of message that user read
					List<Integer> readMessageIds = new ArrayList<>();
					for (int i = 0; i < messages.length(); i++) {
						try {
							int messageId = messages.getInt(i);
							readMessageIds.add(messageId);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					// User: Who read these message
					Gson gson = new Gson();
					UserItem userItem = gson.fromJson(user.toString(), UserItem.class);

					if (userItem == null) {
						return;
					}

					// Update list of message
					for (int i = 0; (readMessageIds != null) && (i < readMessageIds.size()); i++) {
						for (int j = mMessagesList.size() - 1; j >= 0; j--) {
							ChatItem chatItem = mMessagesList.get(j);
							if (chatItem.id != null && chatItem.id.equals(readMessageIds.get(i))) {
								if (chatItem.usersReadMessage == null) {
									chatItem.usersReadMessage = new ArrayList<>();
								}
								chatItem.usersReadMessage.add(userItem);
								chatItem.channelUid = mChannelUid;
								chatItem.orgUid = getOrgUid();
								mTbMessage.updateOrInsert(chatItem.rebuildChatItem());
								break;
							}
						}
					}

					// Reload listview
					mAdapter.notifyDataSetChanged();
				}
			});
		}

		@Override
		public void onWSChannelClosed(WsChannelResponseDto response) {
			Log.e("ChatActivity", "onWSChannelClosed");
			mTbChannel.deleteChannel(response);
		}

		@Override
		public void onWSChannelDeleted(WsChannelResponseDto response) {
			Log.e("ChatActivity", "onWSChannelDeleted");
			mTbChannel.deleteChannel(response);
		}

		@Override
		public void onWSRecieveAnswer(Integer messageId, Integer answerType){
		}

	}

	/**
	 * Send active status of channel to server when the channel come active
	 * @param channelUid
	 */
	private void sendChannelActive(final String channelUid) {
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
			@Override
			public void run() {
				WsChannelActiveRequest request = new WsChannelActiveRequest();
				request.channelUid = channelUid;

				WebSocketHelper.send(request.toJson());
			}
		}, 1000);

	}

	/**
	 *
	 */
	private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// ソフトキーボードを非表示
			hideKeyboardAndWidgetMenu();

			if (!isSuggestionAtBottom()) {
				mSuggestionPlaceHolder.setVisibility(View.GONE);
			}
			// フォーカスを移す
//			mCloseButton.requestFocus();
			// WidgetMenu を非表示
			return false;
		}
	}

	private ArrayList<WidgetMenuGridAdapter.MenuButton> getWidgetMenuButtons(List<String> stickers) {
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

					mBtVideoCall.setVisibility(View.VISIBLE);
					mBtPhoneCall.setVisibility(View.VISIBLE);
					*/

					isVideoCallEnabledForApp = true;

					if (isVideoCallEnabledForApp && mCurrentChannelItem != null && mCurrentChannelItem.canUseVideoCall(mIsAgent)) {
						mBtVideoCall.setVisibility(View.VISIBLE);
						mBtPhoneCall.setVisibility(View.VISIBLE);
					}

				} else if (sticker.equals(ChatCenterConstants.StickerName.STICKER_TYPE_TYPE_FILE)) {
					mWidgetMenuButtons.add(new WidgetMenuGridAdapter.MenuButton(ChatCenterConstants.StickerName.STICKER_TYPE_TYPE_FILE,
							R.drawable.icon_widget_image,
							getString(R.string.sticker_label_image)));

					mWidgetMenuButtons.add(new WidgetMenuGridAdapter.MenuButton(ChatCenterConstants.StickerName.STICKER_TYPE_CAMERA,
							R.drawable.icon_widget_camera,
							getString(R.string.sticker_label_camera)));
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
							if (!isKeyBoardVisible) {
								changeKeyboardHeight(heightDifference);
								mFixedPhrasePlaceHolder.setVisibility(View.GONE);
								mSuggestionPlaceHolder.setVisibility(View.GONE);

								scrollToLast(true, false);
								isKeyBoardVisible = true;
							}
						} else {
							if (isKeyBoardVisible) {

								if (mWidgetMenuPopupWidow == null || !mWidgetMenuPopupWidow.isShowing()) {
									if (mFixedPhrasePlaceHolder.getVisibility() != View.VISIBLE) {
										showFixedPhraseView(mIsAgent);
									}
									if (mSuggestionPlaceHolder.getVisibility() != View.VISIBLE) {
										showSuggestionForLastMsg(mIsAgent);
									}
								}

								isKeyBoardVisible = false;
							}
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

		showFixedPhraseView(mIsAgent);
	}

	private void openVideoChatActivity(VideoCallWidget widget, String messageId){
		mVideoCallItem = widget;
		mVideoCallItem.setupWithUsers(mChannelUsers);
		mVideoCallItem.messageId = messageId;

		if (mVideoCallItem == null ||  mVideoCallItem.caller == null || mVideoCallItem.receivers == null )
			return;

		int currentUserId = CCAuthUtil.getUserId(getApplicationContext());
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
		intent.putExtra(ChatCenterConstants.Extra.CHAT_PARAM, mParamDto);
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

	/** If there are many guests in channel, Show dialog to select one*/
	private void selectUserToCall(boolean audioOnly, ChannelItem channelItem) {
		if (mGuestListDialog == null) {
			mGuestListDialog = new GuestsListDialogFragment();
			mGuestListDialog.mActivity = this;
			mGuestListDialog.mAudioOnly = audioOnly;
			mGuestListDialog.mAdapter = new GuestsAdapter(this, 0, channelItem.getGuests());
		}
		mGuestListDialog.show(getSupportFragmentManager(), GuestsListDialogFragment.TAG);
	}

	public static class GuestsListDialogFragment extends DialogFragment {
		public static final String TAG = "GuestsListDialogFragment";

		public ChatActivity mActivity;
		public GuestsAdapter mAdapter;
		public boolean mAudioOnly;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			// Get the layout inflater
			LayoutInflater inflater = getActivity().getLayoutInflater();

			View dialogCustomView = inflater.inflate(R.layout.dialog_list_view, null);

			TextView tvTitle = (TextView) dialogCustomView.findViewById(R.id.dialog_title);
			ListView lvApps = (ListView) dialogCustomView.findViewById(R.id.lv_apps);

			tvTitle.setText(R.string.select_user_to_call);
			lvApps.setAdapter(mAdapter);
			lvApps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					mActivity.mGuestListDialog.dismiss();
					UserItem user = (UserItem) view.getTag();
					if (user != null) {
						List<String> ids = new ArrayList<>();
						ids.add(user.id + "");
						mActivity.requestStartVideoChat(mAudioOnly, ids);
					}
				}
			});

			Button cancel = (Button) dialogCustomView.findViewById(R.id.btn_cancel);
			cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mActivity.mGuestListDialog.dismiss();
				}
			});

			builder.setView(dialogCustomView);
			Dialog dialog = builder.create();

			return dialog;
		}

	}

	private class GuestsAdapter extends ArrayAdapter<UserItem> {

		private List<UserItem> mGuests;

		public GuestsAdapter(Context context, int resource, List<UserItem> objects) {
			super(context, resource, objects);

			mGuests = objects;
		}

		@NonNull
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = new TextView(ChatActivity.this);
			ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					(int) convertDpToPixel(40, getContext()));

			textView.setLayoutParams(layoutParams);

			UserItem guest = getItem(position);

			textView.setText(guest.displayName);
			textView.setGravity(Gravity.CENTER);
			textView.setTextColor(getContext().getResources().getColor(R.color.color_chatcenter_text));

			textView.setTag(getItem(position));

			return textView;
		}

		@Override
		public int getCount() {
			return mGuests.size();
		}

		@Override
		public UserItem getItem(int position) {
			return mGuests.get(position);
		}
	}

	private class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if ( ChatCenterConstants.BroadcastAction.UPDATE_CHAT.equals(action) ) {
				mAdapter.notifyDataSetChanged();
			} else if (ChatCenterConstants.BroadcastAction.RELOAD_CHAT.equals(action)){
				requestGetMessages(null);
			}
		}
	}

	private void checkNetworkToStart() {
		if (!isNetworkAvailable()) {
			isInternetConnecting = false;
			if (mHandler != null) {
				mHandler.post(mShowNWErrorTask);
			}

			if (mMessagesList.size() == 0) {
				if (mIsAgent) {
					mChannelUid = mParamDto.channelUid;
					mTbMessage.getListMessageInChannel(mChannelUid, getOrgUid(), null, this);
					mCurrentChannelItem = mTbChannel.getChannel(getOrgUid(), mChannelUid);

					// タイトル設定
					if(mCurrentChannelItem != null) {
						mTitleTextView.setText(mCurrentChannelItem.getDisplayName(ChatActivity.this, !mIsAgent));
					}

					mTitleTextView.setVisibility(View.VISIBLE);
				} else {
					// タイトル設定
					if (mCurrentChannelItem != null) {
						mChannelUid = mCurrentChannelItem.uid;
						mTitleTextView.setText(mCurrentChannelItem.getDisplayName(ChatActivity.this, mIsAgent));
						mTitleTextView.setVisibility(View.VISIBLE);

						mTbMessage.getListMessageInChannel(mChannelUid, getOrgUid(), null, ChatActivity.this);
					}
				}

				restoreDraftTextMessage();
			}
		}
	}

	private void showNWErrorLabel(boolean isConnecting) {
		if (isConnecting) {
			mNetworkErrorTextView.setBackgroundColor(getResources().getColor(R.color.color_orange));
			mNetworkErrorTextView.setText(R.string.connecting);
		} else {
			mNetworkErrorTextView.setBackgroundColor(getResources().getColor(R.color.color_chatcenter_text_red));
			mNetworkErrorTextView.setText(R.string.chat_network_error_message);
		}

		mNetworkErrorTextView.setVisibility(View.VISIBLE);
	}

	private void showNWErrorDialog() {
		DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ALERT, "", getString(R.string.alert_can_not_send_without_message));
	}

	/**
	 * ユーザーはメッセージを入力している状態をサーバーに報告します。
	 *
	 * @param channelUid
	 * @param userId
	 */
	private void sendMessageTyping(String channelUid, int userId) {
		if (isNeedToSendTyping) {
			isNeedToSendTyping = false;

			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					isNeedToSendTyping = true;
				}
			}, ChatCenterConstants.SEND_TYPING_TIME_OUT);

			WsTypingRequest typingRequest = new WsTypingRequest();
			typingRequest.channelUid = channelUid;
			typingRequest.userId = userId;

			String param = typingRequest.toJson();
			WebSocketHelper.send(param);
		}
	}

	/**
	 * Check if message is added to list of messages already.
	 * @param chatItem
	 * @return index of message on the list
	 */
	private int indexOnList(ChatItem chatItem) {
		int index = -1;

		if (mMessagesList != null) {
			for (int i = mMessagesList.size() - 1; i >= 0; i--) {
				ChatItem oldItem = mMessagesList.get(i);
				if (oldItem != null && oldItem.id != null && oldItem.id.equals(chatItem.id)) {
					index = i;
					break;
				}
			}
		}

		return index;
	}

	/**
	 * Save the text message you are typing
	 * @param message
	 * @return
	 */
	private long saveDraftTextMessage(String message) {

		CCLog.d(TAG, "saveDraftTextMessage");

		if (StringUtil.isBlank(message)) {
			return -1;
		}
		int currentUserId = CCAuthUtil.getUserId(this);
		String tmpUid = mChannelUid + "-" + currentUserId + "" + System.currentTimeMillis();

		// Getting data Add the temporary message
		ChatItem item = ChatItem.createTemporaryMessage(message, tmpUid, CCAuthUtil.getCurrentUser(this));
		item.channelUid = mChannelUid;
		item.orgUid = getOrgUid();
		item.type = ResponseType.MESSAGE;
		item.rawContent = message;
		item.localStatus = ChatItem.ChatItemStatus.DRAFT;

		// Save message into database
		return mTbMessage.updateOrInsert(item.rebuildChatItem());
	}

	/**
	 * Get draft message and fill Message Input by old message
	 */
	private void restoreDraftTextMessage() {
		CCLog.d(TAG, "restoreDraftTextMessage");

		if (StringUtil.isBlank(mChannelUid)) {
			return;
		}

		// Get message from database
		ChatItem draftMessage = mTbMessage.getDraftMessage(mChannelUid, getOrgUid());

		// Fill content to EditText
		if (draftMessage != null && StringUtil.isNotBlank(draftMessage.rawContent)) {
			mEdtMsgInput.setText(draftMessage.rawContent);
			mEdtMsgInput.setSelection(draftMessage.rawContent.length());

			// Remove old draft from database
			mTbMessage.delete(draftMessage);
		}
	}

	/**
	 * Show fixed phrase if user is guest
	 * @param isAgent
	 */
	private void showFixedPhraseView(boolean isAgent) {
		if (mListFixedPhrases != null && mListFixedPhrases.size() > 0) {
			mFixedPhrasePlaceHolder.setVisibility(isAgent ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * If last message on the list is suggestion
	 */
	private boolean isSuggestionAtBottom() {
		if (mMessagesList != null && mMessagesList.size() > 0) {
			ChatItem lastItem = mMessagesList.get(mMessagesList.size() - 1);
			if (lastItem != null && ResponseType.SUGGESTION.equals(lastItem.type)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Show the suggestion view if last message is suggestion
	 */
	private void showSuggestionForLastMsg(boolean isAgent) {
		if (!isAgent) {
			return;
		}
		if (isSuggestionAtBottom()) {
			ChatItem lastItem = mMessagesList.get(mMessagesList.size() - 1);
			showOrHideSuggestionView(lastItem);
		}
	}
}
