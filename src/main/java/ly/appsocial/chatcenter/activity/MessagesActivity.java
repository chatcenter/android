/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import ly.appsocial.chatcenter.ChatCenter;
import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.adapter.MessagesAdapter;
import ly.appsocial.chatcenter.activity.adapter.MessagesAgentMenuAdapter;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ChannelItem;
import ly.appsocial.chatcenter.dto.FunnelItem;
import ly.appsocial.chatcenter.dto.OrgItem;
import ly.appsocial.chatcenter.dto.param.ChatParamDto;
import ly.appsocial.chatcenter.dto.param.MessagesParamDto;
import ly.appsocial.chatcenter.dto.ws.request.GetChannelsMineRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.GetChannelsRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostChannelsCloseRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostUsersAuthRequestDto;
import ly.appsocial.chatcenter.dto.ws.response.GetAppsResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.GetChannelsCountResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.GetChannelsMineResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.GetFunnelResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.GetOrgsResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostChannelsCloseResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostDevicesSignInResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostUsersAuthResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.WsChannelJoinMessageDto;
import ly.appsocial.chatcenter.dto.ws.response.WsMessagesResponseDto;
import ly.appsocial.chatcenter.fragment.AlertDialogFragment;
import ly.appsocial.chatcenter.gcm.ChatCenterDeviceTokenRequest;
import ly.appsocial.chatcenter.ui.ChannelFilterView;
import ly.appsocial.chatcenter.util.ApiUtil;
import ly.appsocial.chatcenter.util.AuthUtil;
import ly.appsocial.chatcenter.util.DialogUtil;
import ly.appsocial.chatcenter.util.NetworkQueueHelper;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.util.ViewUtil;
import ly.appsocial.chatcenter.ws.ApiRequest;
import ly.appsocial.chatcenter.ws.OkHttpApiRequest;
import ly.appsocial.chatcenter.ws.CCWebSocketClient;
import ly.appsocial.chatcenter.ws.CCWebSocketClientListener;
import ly.appsocial.chatcenter.ws.parser.GetAppsParser;
import ly.appsocial.chatcenter.ws.parser.GetChannelsCountParser;
import ly.appsocial.chatcenter.ws.parser.GetChannelsMineParser;
import ly.appsocial.chatcenter.ws.parser.GetFunnelsParser;
import ly.appsocial.chatcenter.ws.parser.GetOrgsParser;
import ly.appsocial.chatcenter.ws.parser.PostChannelsCloseParser;
import ly.appsocial.chatcenter.ws.parser.PostUsersAuthParser;

/**
 * 「履歴」アクティビティ。
 */
public class MessagesActivity extends ly.appsocial.chatcenter.activity.BaseActivity implements View.OnClickListener,
		AdapterView.OnItemClickListener, AlertDialogFragment.DialogListener,
		AbsListView.OnScrollListener, ChannelFilterView.ChannelFilterDialogListener, SwipeRefreshLayout.OnRefreshListener {

	private static final String TAG = MessagesActivity.class.getSimpleName();

	// //////////////////////////////////////////////////////////////////////////
	// staticフィールド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * リクエストタグ
	 */
	private static final String REQUEST_TAG = MessagesActivity.class.getCanonicalName();

	/** チャネル並び順のコンパレータ */
	public static final Comparator<GetChannelsMineResponseDto.Item> COMPARATOR = new Comparator<GetChannelsMineResponseDto.Item>() {
		@Override
		public int compare(final GetChannelsMineResponseDto.Item o1, final GetChannelsMineResponseDto.Item o2) {

			long t1 = (o1.latestMessage == null) ? o1.created : o1.latestMessage.created;
			long t2 = (o2.latestMessage == null) ? o2.created : o2.latestMessage.created;

			if (t1 == t2) {
				return 0;
			}
			return t1 > t2 ? -1 : 1;
		}
	};

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** ParamDto */
	private MessagesParamDto mParamDto;

	/** 削除ボタン */
	private Button mDeleteButton;
	/** 編集/キャンセルボタン */
	private Button mEditButton;
	/** 0件メッセージ */
	private TextView mEmptyTextView;
	/** ListView */
	private ListView mListView;
	/** Adapter */
	private MessagesAdapter mAdapter;
	/** プログレスバー */
	private LinearLayout mProgressBar;
	/** ネットワークエラー */
	private TextView mNetoworkErrorTextView;
	/** Header title*/
	private TextView mTvOrgName;
	/** Header funnel*/
	private TextView mTvFunnel;
	/** SwipeRefreshLayout*/
	private SwipeRefreshLayout mSwipeRefreshLayout;

	// タスク
	/** POST /api/users/auth */
	private OkHttpApiRequest<PostUsersAuthResponseDto> mPostUsersAuthRequest;
	/** GET /api/channels/mine */
	private OkHttpApiRequest<GetChannelsMineResponseDto> mPostChannelsMineRequest;
	/** POST /api/channels/:channel_uid/close */
	private OkHttpApiRequest<PostChannelsCloseResponseDto> mPostChannelsCloseRequest;
	/** 再接続タスク */
	private final Runnable mReconnectTask = new Runnable() {
		@Override
		public void run() {
			requestApis();
		}
	};
	/** List of Channel items */
	private List<GetChannelsMineResponseDto.Item> mChannelItems;

	// etc
	/** Handler */
	private Handler mHandler = new Handler();
	/** 初回ロードかどうか */
	private boolean mIsInit = true;

	// For agents
	/** True if is agent, false otherwise */
	private boolean mIsAgent = false;
	/** List of all menu items */
	private List<MessagesAgentMenuItem> mMenuItems;
	/** Adapter for agent menu */
	private MessagesAgentMenuAdapter mMenuAdapter;
	/** Current Org */
	private OrgItem mCurrentOrgItem;
	/** Current App ID */
	private String mCurrentAppId;
	/** The menu list view */
	private ListView mMenuListView;
	/** The menu view */
	private DrawerLayout mMenu;
	/** Request device tokens from GCM */
	private ChatCenterDeviceTokenRequest mDeviceTokenRequest;
	/** Current App */
	private GetAppsResponseDto.App mCurrentApp;

	/** App actionbar */
	private Toolbar mToolbar;
	/** Channel filter popup window*/
	private PopupWindow mChannelFilterWindow;

	private ArrayList<FunnelItem> mFunnelItems = new ArrayList<>();
	private ChannelFilterView.MessageStatusItem mCurrentStatus;
	private ChannelFilterView.MessageFunnelItem mCurrentFunnel;

	private AdapterView.OnItemClickListener mMenuClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
			MessagesAgentMenuItem item = mMenuItems.get(position);

			if (item.getType() == MessagesAgentMenuItem.ITEM_TYPE_ORGS) {

				if (mCurrentOrgItem.uid.equals(item.getValue())) {
					return;
				}

				// Load new channel
				mCurrentOrgItem = item.getOrg();
				mTvOrgName.setText(mCurrentOrgItem.name);
				mTvFunnel.setEnabled(true);
				requestGetChannels();

				// Update the view to loading
				mMenu.closeDrawers();
				mListView.setVisibility(View.GONE);
				mProgressBar.setVisibility(View.VISIBLE);
				mEmptyTextView.setVisibility(View.GONE);
				mMenuAdapter.setSelectedOrg(mCurrentOrgItem.uid);
				mMenuAdapter.notifyDataSetChanged();
			} else if (item.getType() == MessagesAgentMenuItem.ITEM_TYPE_SIGNOUT) {
				ChatCenter.signOutAgent(MessagesActivity.this);
				Intent intent = new Intent(MessagesActivity.this, ChatCenter.getTopActivity().getClass());
				startActivity(intent);
				finish();
			} else if (item.getType() == MessagesAgentMenuItem.ITEM_TYPE_APPS) {
				if (mGetOrgsRequest != null || mGetChannelsRequest != null) {
					return;
				}

				mCurrentAppId = item.getValue();
				requestGetOrgs(true);
				mMenuAdapter.setSelectedApp(mCurrentAppId);

				mCurrentApp = item.getApp();

				// Update UI
				mMenu.closeDrawers();
				mProgressBar.setVisibility(View.VISIBLE);
				mListView.setVisibility(View.GONE);
				mEmptyTextView.setVisibility(View.GONE);

				// Now, connect to this app
				reconnectWithAppToken(mCurrentAppId);

				// Now we register this device token
				String token = AuthUtil.getDeviceToken(getApplicationContext());
				ChatCenter.registerDeviceToken(getApplicationContext(), token, mCurrentAppId, new PostDevicesCallback());
			}
		}
	};

	/** POST /api/orgs */
	private OkHttpApiRequest<GetOrgsResponseDto> mGetOrgsRequest;
	/** GET /api/apps */
	private OkHttpApiRequest<GetAppsResponseDto> mGetAppsRequest;
	/** POST /api/channels */
	private OkHttpApiRequest<GetChannelsMineResponseDto> mGetChannelsRequest;
	private OkHttpApiRequest<GetChannelsCountResponseDto> mGetChannelsCountRequest;
	/**
	 * GET /api/funnels
	 */
	private ApiRequest<GetFunnelResponseDto> mGetFunnelsRequest;;

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// パラメータの取得
		mParamDto = getIntent().getExtras().getParcelable(MessagesParamDto.class.getCanonicalName());
		mIsAgent = mParamDto.isAgent;

		// Set the content view accordingly
		setContentView(mIsAgent ? R.layout.messages_agent : R.layout.messages);

		// 削除ボタン
		mDeleteButton = (Button) findViewById(R.id.messages_delete_button);
		mDeleteButton.setOnClickListener(this);

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setNavigationIcon(mIsAgent ? R.drawable.btn_menu : R.drawable.bt_close);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("");

		// 編集/キャンセルボタン
		mEditButton = (Button) findViewById(R.id.messages_edit_button);
		mEditButton.setOnClickListener(this);
		mEditButton.setVisibility(View.GONE);

		// 0件メッセージ
		mEmptyTextView = (TextView) findViewById(R.id.messages_empty_textview);

		// プログレスバー
		mProgressBar = (LinearLayout) findViewById(R.id.messages_progressbar);

		// ネットワークエラー
		mNetoworkErrorTextView = (TextView) findViewById(R.id.messages_network_error_textview);
		mNetoworkErrorTextView.setVisibility(View.GONE);

		// ListView
		mListView = (ListView) findViewById(R.id.messages_listview);
		mChannelItems = new ArrayList<>();
		mAdapter = new MessagesAdapter(MessagesActivity.this, mChannelItems, AuthUtil.getUserId(MessagesActivity.this));
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnScrollListener(this);

		mTvFunnel = (TextView) findViewById(R.id.tv_header_funnel);
		mTvOrgName = (TextView) findViewById(R.id.tv_org_name);
		mTvFunnel.setEnabled(false);

		if (mIsAgent) {
			prepareAgentMenu();
			getDeviceToken(new ChatCenterDeviceTokenRequest.DeviceTokenRequestCallback() {
				@Override
				public void onTokenReceived(String token) {
					mDeviceTokenRequest = null;
				}
			});
		}

		mTvFunnel.setOnClickListener(this);

		// Pull down to refresh
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
		mSwipeRefreshLayout.setColorSchemeResources(R.color.color_orange, R.color.color_green, R.color.color_blue);
		mSwipeRefreshLayout.setOnRefreshListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// 再接続
		mHandler.post(mReconnectTask);
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Handler のキャンセル
		mHandler.removeCallbacks(mReconnectTask);

		// API のキャンセル
		mOkHttpClient.cancel(REQUEST_TAG);
		mPostUsersAuthRequest = null;
		mPostChannelsMineRequest = null;
		mPostChannelsCloseRequest = null;
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
					mMenu.openDrawer(mMenuListView);
				} else {
					finish();
				}

				if (mChannelFilterWindow != null && mChannelFilterWindow.isShowing()) {
					mChannelFilterWindow.dismiss();
				}
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		if (view.equals(mDeleteButton)) { // 削除ボタン

			List<String> channelsUids = new ArrayList<>();
			for (GetChannelsMineResponseDto.Item item : ViewUtil.<GetChannelsMineResponseDto.Item> getCheckedItems(mListView)) {
				channelsUids.add(item.uid);
			}
			if (channelsUids.size() == 0) {
				DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ALERT, null, getString(R.string.dialog_no_select_chat_body));
				return;
			}
			requestPostChannelsClose(channelsUids);

		} else if (view.equals(mEditButton)) { // 編集/キャンセルボタン

			// モードの切り替え
			toggleMode();
		} else if (view.equals(mTvFunnel)) {
			if (mChannelFilterWindow == null) {
				mChannelFilterWindow = new PopupWindow(this);

				ChannelFilterView contentView = new ChannelFilterView(this, mFunnelItems);
				contentView.setFilterDialogListener(this);

				mChannelFilterWindow.setContentView(contentView);
				mChannelFilterWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

				mChannelFilterWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
				mChannelFilterWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
			}

			if (mChannelFilterWindow.isShowing()) {
				mChannelFilterWindow.dismiss();
			} else {
				if (mCurrentOrgItem != null) {
					requestGetChannelCount(mCurrentOrgItem.uid);
				}
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

		GetChannelsMineResponseDto.Item item = (GetChannelsMineResponseDto.Item) adapterView.getAdapter().getItem(position);

		if (mListView.getChoiceMode() != ListView.CHOICE_MODE_MULTIPLE) { // 通常モード
			disconnect();

            ChatParamDto chatParamDto = new ChatParamDto();
            chatParamDto.providerToken = mParamDto.providerToken;
            chatParamDto.providerTokenTimestamp = mParamDto.providerTokenTimestamp;
            chatParamDto.kissCd = item.orgUid;
            chatParamDto.channelUid = item.uid;
            chatParamDto.channelName = item.orgName;
			if (mIsAgent) {
				chatParamDto.appToken = mCurrentAppId;
			}

			// 「チャット」アクティビティの起動
			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtra(ChatParamDto.class.getCanonicalName(), chatParamDto);
			intent.putExtra(ChatCenterConstants.Extra.IS_AGENT, mIsAgent);
			intent.putExtra(ChatCenterConstants.Extra.ORG, mCurrentOrgItem);
			intent.putExtra(ChatCenterConstants.Extra.APP, mCurrentApp);

			startActivity(intent);
		}

	}

	@Override
	public void onDialogCancel(String tag) {
		if (DialogUtil.Tag.ERROR_401.equals(tag)) { // 401エラー
			finish();

			// This means the agent has to login again
			if (mIsAgent) {
				ChatCenter.signOutAgent(this);
			}
		}
	}

	@Override
	public void onPositiveButtonClick(String tag) {
		if (DialogUtil.Tag.ERROR_401.equals(tag)) { // 401エラー
			finish();

			// This means the agent has to login again
			if (mIsAgent) {
				ChatCenter.signOutAgent(this);
			}
		}
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

        if (mParamDto.providerTokenTimestamp != AuthUtil.getProviderTokenTimestamp(getApplicationContext()) // トークン生成タイムスタンプが変わっている
                || StringUtil.isBlank(AuthUtil.getUserToken(getApplicationContext()))) { // Userトークンが無い
            // auth->mine
            requestPostUsersAuth();
        } else if (mIsAgent) {
			if (mCurrentAppId == null && mCurrentOrgItem == null) {
				requestGetApps();
			} else {
				requestGetChannels();
			}
			requestGetFunnels();
		} else {
			requestGetChannelsMine();
        }
	}

	/**
	 * POST /api/users/auth
	 */
	private void requestPostUsersAuth() {
		if (mPostUsersAuthRequest != null) {
			return;
		}

		String path = "users/auth";

        PostUsersAuthRequestDto postUsersAuthRequestDto = new PostUsersAuthRequestDto();
        postUsersAuthRequestDto.provider = mParamDto.provider;
        postUsersAuthRequestDto.providerToken = mParamDto.providerToken;
        postUsersAuthRequestDto.setProviderTokenCreateAt(mParamDto.providerTokenTimestamp);
        postUsersAuthRequestDto.setProviderTokenExpires(mParamDto.providerTokenExpires);
        postUsersAuthRequestDto.deviceToken = mParamDto.deviceToken;
		postUsersAuthRequestDto.email = mParamDto.email;
		postUsersAuthRequestDto.password = mParamDto.password;

		mPostUsersAuthRequest = new OkHttpApiRequest<>(getApplicationContext(), OkHttpApiRequest.Method.POST, path, postUsersAuthRequestDto.toParams(), null, new PostUsersAuthCallback(), new PostUsersAuthParser());
		if (mIsAgent) {
			mPostUsersAuthRequest.setApiToken(null);
		}
		NetworkQueueHelper.enqueue(mPostUsersAuthRequest, REQUEST_TAG);
	}

	private void requestGetChannelsMine() {
		requestGetChannelsMine(0);
	}

	/**
	 * GET /api/channels/mine
	 *
	 * @param lastUpdatedDate last updated date in epoch timestamp
	 */
	private void requestGetChannelsMine(int lastUpdatedDate) {
		if (mPostChannelsMineRequest != null) {
			return;
		}

		String path = "channels/mine";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		GetChannelsMineRequestDto request = new GetChannelsMineRequestDto();
		request.setStatus(mParamDto.channelStatus);
		request.setType(mParamDto.channelType);
		request.setLastUpdatedAt(lastUpdatedDate);
		Map<String, String> params = request.toParams();

		mPostChannelsMineRequest = new OkHttpApiRequest<>(getApplicationContext(), OkHttpApiRequest.Method.GET,
				path, params, headers, new GetChannelsMineCallback(lastUpdatedDate > 0),
				new GetChannelsMineParser());

		NetworkQueueHelper.enqueue(mPostChannelsMineRequest, REQUEST_TAG);
	}

	/**
	 * GET /api/channels/:channel_uid/close
	 *
	 * @param channelUids 削除対象のチャネルUID
	 */
	private void requestPostChannelsClose(List<String> channelUids) {
		if (mPostChannelsCloseRequest != null) {
			return;
		}

		String path = "channels/close";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		PostChannelsCloseRequestDto postChannelsCloseRequestDto = new PostChannelsCloseRequestDto();
		postChannelsCloseRequestDto.channelUids = channelUids;

		mPostChannelsCloseRequest = new OkHttpApiRequest<>(getApplicationContext(), OkHttpApiRequest.Method.POST, path, null, headers, new PostChannelsCloseCallback(), new PostChannelsCloseParser());
		mPostChannelsCloseRequest.setJsonBody(postChannelsCloseRequestDto.toJson());

		NetworkQueueHelper.enqueue(mPostChannelsCloseRequest, REQUEST_TAG);
	}

	/**
	 * API が認証エラーか判定し、認証エラーの場合は認証エラーダイアログを表示します。
	 *
	 * @param error Volleyエラー
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
	 * ネットワークエラー表示し、再接続を行います。
	 */
	private void errorWithReconnect() {
		// ネットワークエラー表示
		mNetoworkErrorTextView.setVisibility(View.VISIBLE);
		// 再接続
		mHandler.postDelayed(mReconnectTask, 3000);
	}

	/**
	 * 編集モード/通常モードをトルグします。
	 */
	private void toggleMode() {
		editMode(mListView.getChoiceMode() != ListView.CHOICE_MODE_MULTIPLE);
	}

	/**
	 * 編集モード/通常モードにシフトします。
	 * 
	 * @param editMode 編集モードの場合は true、通常モードの場合は false
	 */
	private void editMode(boolean editMode) {
		if (editMode) {
			mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			mAdapter.setEdit(true);
			mEditButton.setText("キャンセル");
			mDeleteButton.setVisibility(View.VISIBLE);
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		} else {
			mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
			mListView.clearChoices();
			mAdapter.setEdit(false);
			mEditButton.setText("編集");
			mDeleteButton.setVisibility(View.GONE);
		}
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onFunnelItemSelected(ChannelFilterView.MessageFunnelItem item) {
		mCurrentFunnel = item;
		mProgressBar.setVisibility(View.VISIBLE);
		mChannelFilterWindow.dismiss();
		mParamDto.funnelId = item.funnel.id;
		requestGetChannels();
		setUpFilterLabel();
	}

	@Override
	public void onStatusItemSelected(ChannelFilterView.MessageStatusItem item) {
		mCurrentStatus = item;
		mProgressBar.setVisibility(View.VISIBLE);
		mChannelFilterWindow.dismiss();
		mParamDto.channelStatus = item.value;
		requestGetChannels();
		setUpFilterLabel();
	}

	@Override
	public void onDialogOutsideTouched() {
		mChannelFilterWindow.dismiss();
	}

	private void setUpFilterLabel () {
		if ((mCurrentFunnel == null || mCurrentFunnel.funnel.id < 0)
				&& (mCurrentStatus == null || mCurrentStatus.value == ChannelItem.ChannelStatus.CHANNEL_ALL)) {
			mTvFunnel.setText(R.string.all);
		} else if (mCurrentFunnel != null && mCurrentFunnel.funnel.id >= 0
				&& (mCurrentStatus == null || mCurrentStatus.value == ChannelItem.ChannelStatus.CHANNEL_ALL)) {
			mTvFunnel.setText(mCurrentFunnel.funnel.name);
		} else if ((mCurrentFunnel == null || mCurrentFunnel.funnel.id < 0)
				&& mCurrentStatus!= null && mCurrentStatus.value != ChannelItem.ChannelStatus.CHANNEL_ALL) {
			mTvFunnel.setText(mCurrentStatus.name);
		} else {
			mTvFunnel.setText(mCurrentFunnel.funnel.name + ", " + mCurrentStatus.name);
		}
	}

	@Override
	public void onRefresh() {
		requestGetChannels();
	}

	// //////////////////////////////////////////////////////////////////////////
	// コールバック
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * POST /api/users/auth のコールバック
	 */
	private class PostUsersAuthCallback implements OkHttpApiRequest.Callback<PostUsersAuthResponseDto> {
		@Override
		public void onError(OkHttpApiRequest.Error error) {
			mPostUsersAuthRequest = null;

			mProgressBar.setVisibility(View.GONE);

			if (!isAuthErrorWithAlert(error)) {
				if (error.getNetworkResponse() != null && error.getNetworkResponse().code() == 405) {
					mEmptyTextView.setVisibility(View.VISIBLE);
				} else {
					// エラー表示＆再接続
					errorWithReconnect();
				}
			}

			if (mIsAgent) {
				ChatCenter.signOutAgent(MessagesActivity.this);
			}
		}

		@Override
		public void onSuccess(PostUsersAuthResponseDto responseDto) {
			mPostUsersAuthRequest = null;

			// 認証情報の保存
			AuthUtil.saveTokens(MessagesActivity.this, mParamDto.providerTokenTimestamp, responseDto.token, responseDto.id);

			if (mIsAgent) {
				requestGetApps();
			} else {
				requestGetChannelsMine();
			}
		}
	}

	/**
	 * GET /api/channels/mine のコールバック
	 */
	private class GetChannelsMineCallback implements OkHttpApiRequest.Callback<GetChannelsMineResponseDto> {
		private boolean mIsLoadMore = false;
		public GetChannelsMineCallback(boolean more) {
			mIsLoadMore = more;
		}
		@Override
		public void onError(OkHttpApiRequest.Error error) {
			mPostChannelsMineRequest = null;
			mGetChannelsRequest = null;

			mProgressBar.setVisibility(View.GONE);

			if (!isAuthErrorWithAlert(error)) {
				// エラー表示＆再接続
				errorWithReconnect();
			}
			if (mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(false);
			}
		}

		@Override
		public void onSuccess(GetChannelsMineResponseDto responseDto) {
			mPostChannelsMineRequest = null;
			mGetChannelsRequest = null;

			// リスト作成
			List<GetChannelsMineResponseDto.Item> items = new ArrayList<>();
			if (responseDto!= null) {
				if (responseDto.items == null || responseDto.items.size() == 0) {
					mMessagesLastPage = true;
				} else {
					for (GetChannelsMineResponseDto.Item item : responseDto.items) {
//						if (item.isClosed()) {
//							continue;
//						}
						items.add(item);
					}
				}
			} else {
				mMessagesLastPage = true;
			}
			// ソート
			Collections.sort(items, COMPARATOR);

			// Make the channel list

			if (mChannelItems == null) {
				mChannelItems = new ArrayList<>();
			} else if (!mIsLoadMore) {
				mChannelItems.clear();
			}
			mChannelItems.addAll(items);

			boolean isEdit;
			isEdit = mAdapter.isEdit();

			mAdapter.setEdit(isEdit);
			mListView.clearChoices();

			if (mAdapter.getCount() == 0) {
				// 0件メッセージ
				mEmptyTextView.setVisibility(View.VISIBLE);
			} else {
				mEmptyTextView.setVisibility(View.GONE);
				mEditButton.setVisibility(mIsAgent ? View.GONE : View.VISIBLE);
			}

			mProgressBar.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
			// ネットワークエラーの非表示
			mNetoworkErrorTextView.setVisibility(View.GONE);

			if (mIsAgent) {
				// Connect to WebSocket
				reconnectWithAppToken(mCurrentAppId);
			}

			mAdapter.notifyDataSetChanged();

			if (mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(false);
			}
		}
	}

	/**
	 * POST /api/channels/close のコールバック
	 */
	private class PostChannelsCloseCallback implements OkHttpApiRequest.Callback<PostChannelsCloseResponseDto> {
		@Override
		public void onError(OkHttpApiRequest.Error error) {
			mPostChannelsCloseRequest = null;

			if (!isAuthErrorWithAlert(error)) {
				// チャット削除エラーダイアログの表示
				DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.dialog_chat_delete_error_body));
			}
		}

		@Override
		public void onSuccess(PostChannelsCloseResponseDto responseDto) {
			mPostChannelsCloseRequest = null;

			// 削除チャネルIDの取得
			List<String> closeChannelUids = new ArrayList<>();
			for (PostChannelsCloseResponseDto.Item item : responseDto.items) {
				closeChannelUids.add(item.uid);
			}

			// リストから削除
			int count = mAdapter.getCount();
			for (int i = count - 1; i >= 0; i--) {
				GetChannelsMineResponseDto.Item item = mAdapter.getItem(i);
				if (closeChannelUids.contains(item.uid)) {
					mAdapter.remove(item);
				}
			}
			mListView.clearChoices();

			if (mAdapter.getCount() == 0) {
				mEmptyTextView.setVisibility(View.VISIBLE);
				mEditButton.setVisibility(View.GONE);

				// 通常モードに切り替え
				editMode(false);
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// ListView
	// //////////////////////////////////////////////////////////////////////////
	private int mLastItem = -1;
	private boolean mMessagesLastPage = false;
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		final int lastItem = firstVisibleItem + visibleItemCount;
		if (lastItem == totalItemCount && mAdapter != null && totalItemCount > 0) {
			if (mLastItem != lastItem) { // Avoid multiple calls for last item
				mLastItem = lastItem;
			}

			// At the end of scroll
			if (!mMessagesLastPage && lastItem > 0) {
				if (mIsAgent) {
					requestGetChannels((int) Math.floor(mAdapter.getItem(lastItem - 1).lastUpdatedAt));
				} else {
					requestGetChannelsMine((int) Math.floor(mAdapter.getItem(lastItem - 1).lastUpdatedAt));
				}
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Agents View
	// //////////////////////////////////////////////////////////////////////////
	private void prepareAgentMenu() {
		mMenuItems = new ArrayList<>();
		mMenuAdapter = new MessagesAgentMenuAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, android.R.id.text1, mMenuItems);

		mMenuListView = (ListView) findViewById(R.id.messages_drawer_menu);
		if (mMenuListView == null) {
			return;
		}
		mMenuListView.setAdapter(mMenuAdapter);
		mMenuListView.setOnItemClickListener(mMenuClickListener);

		mMenu = (DrawerLayout) findViewById(R.id.messages_drawer);
	}

	/**
	 * GET /api/orgs
	 */
	private void requestGetOrgs(boolean cleanOldOrgs) {
		if (mGetOrgsRequest != null) {
			return;
		}

		String path = "orgs";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mGetOrgsRequest = new OkHttpApiRequest<>(getApplicationContext(), OkHttpApiRequest.Method.GET, path, null, headers, new GetOrgsCallback(cleanOldOrgs), new GetOrgsParser());
		mGetOrgsRequest.setApiToken(mCurrentAppId);

		NetworkQueueHelper.enqueue(mGetOrgsRequest, REQUEST_TAG);
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
	 * GET /api/channels
	 */
	private void requestGetChannels(int lastUpdatedDate) {
		if (mGetChannelsRequest != null || mCurrentOrgItem == null) {
			return;
		}

		String path = "channels";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		GetChannelsRequestDto request = new GetChannelsRequestDto();
		request.setStatus(mParamDto.channelStatus);
		request.setType(mParamDto.channelType);
		request.setLastUpdatedAt(lastUpdatedDate);
		request.setOrgUid(mCurrentOrgItem.uid);
		request.setFunnelID(mParamDto.funnelId);

		Map<String, String> params = request.toParams();

		mGetChannelsRequest = new OkHttpApiRequest<>(getApplicationContext(), OkHttpApiRequest.Method.GET, path, params, headers, new GetChannelsMineCallback(lastUpdatedDate > 0), new GetChannelsMineParser());
		mGetChannelsRequest.setApiToken(mCurrentAppId);

		NetworkQueueHelper.enqueue(mGetChannelsRequest, REQUEST_TAG);
	}

	/**
	 * GET /api/channels
	 */
	private void requestGetChannels() {
		requestGetChannels(0);
	}


	/** GET /api/channels/count*/

	private void requestGetChannelCount(String orgUid) {
		if (mGetChannelsCountRequest != null) {
			return;
		}

		String path = "channels/count";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		Map<String, String> params = new HashMap<>();
		params.put("org_uid", orgUid);

		mGetChannelsCountRequest = new OkHttpApiRequest<>(getApplicationContext(), OkHttpApiRequest.Method.GET, path, params, headers, new GetChannelsCountCallback(), new GetChannelsCountParser());
		mGetChannelsCountRequest.setApiToken(mCurrentAppId);

		NetworkQueueHelper.enqueue(mGetChannelsCountRequest, REQUEST_TAG);
	}

	/**
	 * GET /api/funnels
	 */
	private void requestGetFunnels() {
		if (mGetFunnelsRequest != null) {
			return;
		}

		String path = "funnels/";

		Map<String, String> headers = new HashMap<>();
		headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

		mGetFunnelsRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.GET, path, null, headers,
				new GetFunnelsCallback(), new GetFunnelsParser());
		NetworkQueueHelper.enqueue(mGetFunnelsRequest, REQUEST_TAG);
	}

	public static class MessagesAgentMenuItem {
		private int mType;
		private String mValue;
		private String mDisplayText;
		private OrgItem mOrg;
		private GetAppsResponseDto.App mApp;

		public static final int ITEM_TYPE_ORGS 		= 0;
		public static final int ITEM_TYPE_APPS 		= 1;
		public static final int ITEM_TYPE_SIGNOUT 	= 2;
		public static final int ITEM_TYPE_SKIP 		= 3;

		public MessagesAgentMenuItem(int type, String displaText, String value, OrgItem orgItem, GetAppsResponseDto.App app) {
			this.mType = type;
			this.mValue = value;
			this.mDisplayText = displaText;
			this.mOrg = orgItem;
			this.mApp = app;
		}

		public int getType() {
			return mType;
		}

		public void setType(int type) {
			mType = type;
		}

		public String getValue() {
			return mValue;
		}

		public void setValue(String value) {
			mValue = value;
		}

		public String getDisplayText() {
			return mDisplayText;
		}

		public void setDisplayText(String displayText) {
			mDisplayText = displayText;
		}

		public OrgItem getOrg() {
			return mOrg;
		}

		public void setOrg(OrgItem org) {
			this.mOrg = org;
		}

		public GetAppsResponseDto.App getApp() {
			return mApp;
		}
	}

	private int mOrgsCount = 0;

	private class GetOrgsCallback implements OkHttpApiRequest.Callback<GetOrgsResponseDto> {
		private boolean mCleanOldOrgs;

		public GetOrgsCallback(boolean cleanOldOrgs) {
			mCleanOldOrgs = cleanOldOrgs;
		}

		@Override
		public void onSuccess(GetOrgsResponseDto responseDto) {
			// Clean up old Orgs
			for (int i = 0; mCleanOldOrgs && i < mOrgsCount + 1; i++) {
				mMenuItems.remove(0);
			}

			// Add the Orgs before apps
			int currentSize = mMenuItems.size();
			for (OrgItem item : responseDto.items) {
				mMenuItems.add(mMenuItems.size() - currentSize, new MessagesAgentMenuItem(MessagesAgentMenuItem.ITEM_TYPE_ORGS, item.name, item.uid, item, null));
			}

			// Add others items
			mMenuItems.add(mMenuItems.size() - currentSize, new MessagesAgentMenuItem(MessagesAgentMenuItem.ITEM_TYPE_SKIP, "", "", null, null));
			if (!mCleanOldOrgs) {
				mMenuItems.add(new MessagesAgentMenuItem(MessagesAgentMenuItem.ITEM_TYPE_SKIP, "", "", null, null));
				mMenuItems.add(new MessagesAgentMenuItem(MessagesAgentMenuItem.ITEM_TYPE_SIGNOUT, getString(R.string.chatcenter_messages_signout), "", null, null));
			}

			// Keeping book on the org list
			mOrgsCount = responseDto.items.size();

			// Update data
			mMenuAdapter.notifyDataSetChanged();
			if (mCurrentOrgItem == null || mCleanOldOrgs) {
				mCurrentOrgItem = responseDto.items.get(0);
				mTvOrgName.setText(mCurrentOrgItem.name);
				mTvFunnel.setEnabled(true);
				mMenuAdapter.setSelectedOrg(mCurrentOrgItem.uid);
			}

			requestGetChannels();

			mGetOrgsRequest = null;
		}

		@Override
		public void onError(OkHttpApiRequest.Error error) {
			mGetOrgsRequest = null;
			if (!isAuthErrorWithAlert(error)) {
				// エラー表示＆再接続
				errorWithReconnect();
			}
		}
	}

	private class GetAppsCallback implements OkHttpApiRequest.Callback<GetAppsResponseDto> {
		@Override
		public void onSuccess(GetAppsResponseDto responseDto) {
			mGetAppsRequest = null;

			if (mMenuItems.size() > 0) {
				mMenuItems.clear();
			}

			for (GetAppsResponseDto.App item : responseDto.items) {
				mMenuItems.add(new MessagesAgentMenuItem(MessagesAgentMenuItem.ITEM_TYPE_APPS, item.name, item.token, null, item));
			}

			// Update current application token
			if (mCurrentAppId == null) {
				mCurrentAppId = responseDto.items.get(0).token;
			}

			if (mCurrentApp == null) {
				mCurrentApp = responseDto.items.get(0);
			}

			// Update data
			mMenuAdapter.setSelectedApp(mCurrentAppId);
			mMenuAdapter.notifyDataSetChanged();

			requestGetOrgs(false);

			// Now we register this device token
			String token = AuthUtil.getDeviceToken(getApplicationContext());
			ChatCenter.registerDeviceToken(getApplicationContext(), token, mCurrentAppId, new PostDevicesCallback());

			// Connect with the newly acquired app token
			reconnectWithAppToken(mCurrentAppId);
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

	private void getDeviceToken(ChatCenterDeviceTokenRequest.DeviceTokenRequestCallback listener) {
		// We have already had the device token, no need to do anything
		if (AuthUtil.getDeviceToken(this) != null) {
			return;
		}
		if (mDeviceTokenRequest == null) {
			mDeviceTokenRequest = new ChatCenterDeviceTokenRequest(getApplicationContext(), listener);
			mDeviceTokenRequest.execute();
		} else {
			mDeviceTokenRequest.setCallback(listener);
		}
	}

	private class PostDevicesCallback implements OkHttpApiRequest.Callback<PostDevicesSignInResponseDto> {
		@Override
		public void onSuccess(PostDevicesSignInResponseDto responseDto) {
			// Do nothing
			Log.d("###", "Token registration success");
		}

		@Override
		public void onError(OkHttpApiRequest.Error error) {
			// Do nothing
			Log.e("###", "Token registration failed");
		}
	}

	// ===================================================================================
	// WebSocket support
	// ===================================================================================
	private CCWebSocketClient mWebSocketClient;
	public void connectWithAppToken(String appToken) {
		String url = ApiUtil.getWsUrl(this) + "/?authentication="
				+ AuthUtil.getUserToken(getApplicationContext()) + "&app_token=" + appToken; //;
		mWebSocketClient = new CCWebSocketClient(this, url, new WebSocketClientListener());
		mWebSocketClient.connect();
	}

	public void reconnectWithAppToken(String appToken) {
		if (mWebSocketClient != null && mWebSocketClient.isConnected()) {
			mWebSocketClient.disconnect();
		}
		connectWithAppToken(appToken);
	}

	public void disconnect() {
		if (mWebSocketClient != null && mWebSocketClient.isConnected()) {
			mWebSocketClient.disconnect();
		}
	}

	private class WebSocketClientListener extends CCWebSocketClientListener {

		@Override
		public void onWSConnect() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// ネットワークエラーの非表示
					mNetoworkErrorTextView.setVisibility(View.GONE);
				}
			});
		}

		@Override
		public void onWSDisconnect(int code, String reason) {
			// Do nothing
		}

		@Override
		public void onWSError(final Exception exception) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.e("---", "Exception: " + exception.getMessage(), exception);
					// Do nothing here
				}
			});
		}

		@Override
		public void onWSMessage(final WsMessagesResponseDto response, final String messageType) {
			// Do nothing
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (mCurrentOrgItem == null || (mCurrentOrgItem != null && !response.orgUid.equals(mCurrentOrgItem.uid))) {
						// Not current OrgUid, skip
						return;
					}

					GetChannelsMineResponseDto.Item found = null;
					for (GetChannelsMineResponseDto.Item item : mChannelItems) {
						if (!item.uid.equals(response.channelUid)) {
							continue;
						}

						mChannelItems.remove(item);
						mChannelItems.add(0, item);
						found = item;
						break;
					}

					if (found == null || found.latestMessage.id == response.id) {
						return;
					}

					found.latestMessage = new GetChannelsMineResponseDto.Item.LatestMessage();
					found.latestMessage.id = response.id;
					found.latestMessage.type = response.type;
					found.latestMessage.widget.text = response.widget.text;
					found.latestMessage.created = response.created;
					found.latestMessage.user = response.user;
					found.unreadMessages += 1;
					mAdapter.notifyDataSetChanged();
				}
			});
		}

		@Override
		public void onWSRecieveAnswer(Integer messageId, Integer answerType){
		}

		@Override
		public void onWSChannelJoin(final WsChannelJoinMessageDto response) {
			if (mCurrentOrgItem == null || (mCurrentOrgItem != null && !response.orgUid.equals(mCurrentOrgItem.uid))) {
				// Not current OrgUid, skip
				return;
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					boolean old = false;
					for (GetChannelsMineResponseDto.Item item : mChannelItems) {
						if (!item.uid.equals(response.uid)) {
							continue;
						}
						old = true;
						break;
					}

					if (!old) {
						mChannelItems.add(0, response);
					}
					mAdapter.notifyDataSetChanged();
				}
			});
		}

		@Override
		public void onWSRecieveOnline(String channelUid, JSONObject user, String orgUid, Boolean online){
		}

		@Override
		public void onWSReceiveReceipt(String channelUid, JSONArray messages, JSONObject user){
		}
	}

	private class GetChannelsCountCallback implements ApiRequest.Callback<GetChannelsCountResponseDto> {
		/**
		 * レスポンスが成功の場合のコールバック
		 *
		 * @param responseDto レスポンスDTO
		 */
		@Override
		public void onSuccess(GetChannelsCountResponseDto responseDto) {
			mGetChannelsCountRequest = null;
			((ChannelFilterView)mChannelFilterWindow.getContentView()).updateData(responseDto);
			PopupWindowCompat.showAsDropDown(mChannelFilterWindow, mToolbar, 0, 0, Gravity.CENTER_HORIZONTAL);
		}

		/**
		 * レスポンスが失敗の場合のコールバック
		 *
		 * @param error エラー
		 */
		@Override
		public void onError(ApiRequest.Error error) {
			mGetChannelsCountRequest = null;

		}
	}

	/**
	 * GET /api/funnels のコールバック
	 */
	private class GetFunnelsCallback implements OkHttpApiRequest.Callback<GetFunnelResponseDto> {
		@Override
		public void onError(OkHttpApiRequest.Error error) {
			mGetFunnelsRequest = null;
		}

		@Override
		public void onSuccess(GetFunnelResponseDto responseDto) {
			mGetFunnelsRequest = null;

			if (responseDto != null && responseDto.funnels != null) {
				mFunnelItems.clear();
				mFunnelItems.addAll(responseDto.funnels);
			}

		}
	}
}