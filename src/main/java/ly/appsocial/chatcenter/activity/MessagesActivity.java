/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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
import ly.appsocial.chatcenter.activity.adapter.LeftMenuAdapter;
import ly.appsocial.chatcenter.activity.adapter.MessagesAdapter;
import ly.appsocial.chatcenter.activity.model.LeftMenuChildItem;
import ly.appsocial.chatcenter.activity.model.LeftMenuGroupItem;
import ly.appsocial.chatcenter.activity.receivers.NetworkStateReceiver;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ChannelItem;
import ly.appsocial.chatcenter.dto.FunnelItem;
import ly.appsocial.chatcenter.dto.OrgItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.dto.UserItem;
import ly.appsocial.chatcenter.dto.param.ChatParamDto;
import ly.appsocial.chatcenter.dto.param.MessagesParamDto;
import ly.appsocial.chatcenter.dto.ws.request.GetChannelsMineRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.GetChannelsRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostChannelsCloseRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.PostUsersAuthRequestDto;
import ly.appsocial.chatcenter.dto.ws.request.WsConnectChannelRequest;
import ly.appsocial.chatcenter.dto.ws.response.GetAppsResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.GetChannelsCountResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.GetChannelsMineResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.GetFunnelResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.GetOrgsResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.GetUsersResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostChannelsCloseResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostDevicesSignInResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostUsersAuthResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.WsChannelJoinMessageDto;
import ly.appsocial.chatcenter.dto.ws.response.WsMessagesResponseDto;
import ly.appsocial.chatcenter.fragment.AlertDialogFragment;
import ly.appsocial.chatcenter.ui.ChannelFilterView;
import ly.appsocial.chatcenter.util.ApiUtil;
import ly.appsocial.chatcenter.util.AuthUtil;
import ly.appsocial.chatcenter.util.DialogUtil;
import ly.appsocial.chatcenter.util.NetworkQueueHelper;
import ly.appsocial.chatcenter.util.PreferenceUtil;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.util.ViewUtil;
import ly.appsocial.chatcenter.ws.ApiRequest;
import ly.appsocial.chatcenter.ws.CCWebSocketClientListener;
import ly.appsocial.chatcenter.ws.OkHttpApiRequest;
import ly.appsocial.chatcenter.ws.WebSocketHelper;
import ly.appsocial.chatcenter.ws.parser.GetAppsParser;
import ly.appsocial.chatcenter.ws.parser.GetChannelsCountParser;
import ly.appsocial.chatcenter.ws.parser.GetChannelsMineParser;
import ly.appsocial.chatcenter.ws.parser.GetFunnelsParser;
import ly.appsocial.chatcenter.ws.parser.GetOrgsParser;
import ly.appsocial.chatcenter.ws.parser.GetUsersParser;
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

    /**
     * チャネル並び順のコンパレータ
     */
    private static final Comparator<GetChannelsMineResponseDto.Channel> COMPARATOR = new Comparator<GetChannelsMineResponseDto.Channel>() {
        @Override
        public int compare(final GetChannelsMineResponseDto.Channel o1, final GetChannelsMineResponseDto.Channel o2) {

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

    /**
     * ParamDto
     */
    private MessagesParamDto mParamDto;

    /** 削除ボタン */
    // private Button mDeleteButton;
    /** 編集/キャンセルボタン */
    // private Button mEditButton;
    /**
     * 0件メッセージ
     */
    private TextView mEmptyTextView;
    /**
     * ListView
     */
    private ListView mListView;
    /**
     * Adapter
     */
    private MessagesAdapter mAdapter;
    /**
     * プログレスバー
     */
    private LinearLayout mProgressBar;
    /**
     * ネットワークエラー
     */
    private TextView mNetworkErrorTextView;
    /**
     * Header title
     */
    private TextView mTvOrgName;
    /**
     * Header funnel
     */
    private TextView mTvFunnel;
    /**
     * SwipeRefreshLayout
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // タスク
    /**
     * POST /api/users/auth
     */
    private OkHttpApiRequest<PostUsersAuthResponseDto> mPostUsersAuthRequest;
    /**
     * GET /api/channels/mine
     */
    private OkHttpApiRequest<GetChannelsMineResponseDto> mPostChannelsMineRequest;
    /**
     * POST /api/channels/:channel_uid/close
     */
    private OkHttpApiRequest<PostChannelsCloseResponseDto> mPostChannelsCloseRequest;
    /**
     * GET /api/users/:id
     */
    private ApiRequest<GetUsersResponseDto> mGetUsersRequest;

    /**
     * 再接続タスク
     */
    private final Runnable mReconnectTask = new Runnable() {
        @Override
        public void run() {
            requestApis();
        }
    };
    /**
     * List of Channel items
     */
    private List<GetChannelsMineResponseDto.Channel> mChannelItems;

    // etc
    /**
     * Handler
     */
    private Handler mHandler = new Handler();
    /** 初回ロードかどうか */
    // private boolean mIsInit = true;

    // For agents
    /**
     * True if is agent, false otherwise
     */
    private boolean mIsAgent = false;
    /**
     * List of all items
     */
    private List<LeftMenuChildItem> mMenuOrgItems;
    private List<LeftMenuChildItem> mMenuAppItems;
    /**
     * Adapter for agent menu
     */
    private LeftMenuAdapter mMenuAdapter;
    /**
     * Current Org
     */
    private OrgItem mCurrentOrgItem;
    /**
     * Current App ID
     */
    private String mCurrentAppId;
    private RelativeLayout mMenuLayout;
    /**
     * The menu list view
     */
    private ExpandableListView mMenuListView;
    /**
     * The menu view
     */
    private DrawerLayout mMenu;
    /**
     * Current App
     */
    private GetAppsResponseDto.App mCurrentApp;

    /**
     * App actionbar
     */
    private Toolbar mToolbar;
    /**
     * Channel filter popup window
     */
    private PopupWindow mChannelFilterWindow;

    private ArrayList<FunnelItem> mFunnelItems = new ArrayList<>();
    private ChannelFilterView.MessageStatusItem mCurrentStatus;
    private ChannelFilterView.MessageFunnelItem mCurrentFunnel;

    private SettingsDialogFragment mSettingDialog;
    private AppsListDialogFragment mAppsListDialog;

    /**
     * POST /api/orgs
     */
    private OkHttpApiRequest<GetOrgsResponseDto> mGetOrgsRequest;
    /**
     * GET /api/apps
     */
    private OkHttpApiRequest<GetAppsResponseDto> mGetAppsRequest;
    /**
     * POST /api/channels
     */
    private OkHttpApiRequest<GetChannelsMineResponseDto> mGetChannelsRequest;
    private OkHttpApiRequest<GetChannelsCountResponseDto> mGetChannelsCountRequest;
    /**
     * GET /api/funnels
     */
    private ApiRequest<GetFunnelResponseDto> mGetFunnelsRequest;

    /**
     * Left menu Footer
     */
    private View mMenuFooter;

    /**
     * Left menu Header
     */
    private View mMenuHeader;
    private NetworkStateReceiver mNetworkStateReceiver;
    private boolean isInternetConnecting = true;

    /**
     * ネットワークエラー表示タスク
     */
    private final Runnable mShowNWErrorTask = new Runnable() {
        @Override
        public void run() {
            showNWErrorLabel(isInternetConnecting);
        }
    };
    /**
     * ネットワークエラー非表示タスク
     */
    private final Runnable mHideNWErrorTask = new Runnable() {
        @Override
        public void run() {
            mNetworkErrorTextView.setVisibility(View.GONE);
        }
    };
    private boolean mIsInit = true;

    private boolean isChannelsLoading = false;
    private boolean isCanLoadMore = false;
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
        // mDeleteButton = (Button) findViewById(R.id.messages_delete_button);
        // mDeleteButton.setOnClickListener(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (mIsAgent) {
            mToolbar.setNavigationIcon(R.drawable.btn_menu);
        } else {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // 編集/キャンセルボタン
        // mEditButton = (Button) findViewById(R.id.messages_edit_button);
        // mEditButton.setOnClickListener(this);
        // mEditButton.setVisibility(View.GONE);

        // 0件メッセージ
        mEmptyTextView = (TextView) findViewById(R.id.messages_empty_textview);

        // プログレスバー
        mProgressBar = (LinearLayout) findViewById(R.id.messages_progressbar);

        // ネットワークエラー
        mNetworkErrorTextView = (TextView) findViewById(R.id.messages_network_error_textview);
        mNetworkErrorTextView.setVisibility(View.GONE);
        checkNetworkToStart();

        // ListView
        mListView = (ListView) findViewById(R.id.messages_listview);
        mChannelItems = new ArrayList<>();
        mAdapter = new MessagesAdapter(MessagesActivity.this, mChannelItems,
                AuthUtil.getUserId(MessagesActivity.this), mIsAgent);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);

        mTvFunnel = (TextView) findViewById(R.id.tv_header_funnel);
        mTvOrgName = (TextView) findViewById(R.id.tv_org_name);
        mTvFunnel.setEnabled(false);
        if (!mIsAgent) {
            mTvFunnel.setVisibility(View.GONE);
        }

        ChatCenter.initChatCenter(this, null, null);

        if (mIsAgent) {
            prepareAgentMenu();
        }

        mTvFunnel.setOnClickListener(this);

        // Pull down to refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.color_orange, R.color.color_green, R.color.color_blue);
        mSwipeRefreshLayout.setOnRefreshListener(this);

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
        checkNetworkToStart();
        WebSocketHelper.setListener(new WebSocketClientListener());
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
        cancelAllRequest();
    }

    private void cancelAllRequest() {
        mOkHttpClient.cancel(REQUEST_TAG);
        mPostUsersAuthRequest = null;
        mPostChannelsMineRequest = null;
        mPostChannelsCloseRequest = null;
        mGetUsersRequest = null;
        mGetChannelsRequest = null;
        mGetFunnelsRequest = null;
        mGetOrgsRequest = null;
        mGetAppsRequest = null;
        mGetChannelsCountRequest = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkStateReceiver);
        WebSocketHelper.disconnect();
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
                    mMenu.openDrawer(mMenuLayout);
                    if (mMenuOrgItems != null) {
                        requestGetOrgForMenuUpdating();
                    }
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
        /**
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
         } else */
        if (view.equals(mTvFunnel)) {
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
                    PopupWindowCompat.showAsDropDown(mChannelFilterWindow, mToolbar, 0, 0, Gravity.CENTER_HORIZONTAL);
                    requestGetChannelCount(mCurrentOrgItem.uid);
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        GetChannelsMineResponseDto.Channel item = (GetChannelsMineResponseDto.Channel) adapterView.getAdapter().getItem(position);

        if (mListView.getChoiceMode() != ListView.CHOICE_MODE_MULTIPLE) { // 通常モード
            ChatParamDto chatParamDto = new ChatParamDto();
            chatParamDto.providerToken = mParamDto.providerToken;
            chatParamDto.providerTokenCreatedAt = mParamDto.providerTokenCreateAt;
            chatParamDto.kissCd = item.orgUid;
            chatParamDto.channelUid = item.uid;
            chatParamDto.channelName = item.orgName;
            if (mIsAgent) {
                chatParamDto.appToken = mCurrentAppId;
            }

            // 「チャット」アクティビティの起動
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(ChatCenterConstants.Extra.CHAT_PARAM, chatParamDto);
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
        if (!isInternetConnecting) {
            return;
        }

        // Get full user info
        requestGetUsers(AuthUtil.getUserId(MessagesActivity.this));

        if (mIsInit) {
			/*
			 * 初回のみプログレスを表示します。
			 */
            mProgressBar.setVisibility(View.VISIBLE);
            mIsInit = false;
        }

        if (mParamDto.providerTokenCreateAt != AuthUtil.getProviderTokenTimestamp(getApplicationContext()) // トークン生成タイムスタンプが変わっている
                || StringUtil.isBlank(AuthUtil.getUserToken(getApplicationContext()))) { // Userトークンが無い
            // auth->mine
            requestPostUsersAuth();
        } else if (mIsAgent) {
            if (mCurrentAppId == null && mCurrentOrgItem == null) {
                requestGetApps();
            } else {
                requestGetChannels();
            }
        } else {
            requestGetChannelsMine();
        }
    }

    /**
     * POST /api/users/auth
     */
    private void requestPostUsersAuth() {
        if (!isInternetConnecting || mPostUsersAuthRequest != null) {
            return;
        }

        String path = "users/auth";

        PostUsersAuthRequestDto postUsersAuthRequestDto = new PostUsersAuthRequestDto();
        postUsersAuthRequestDto.provider = mParamDto.provider;
        postUsersAuthRequestDto.providerToken = mParamDto.providerToken;
        postUsersAuthRequestDto.setProviderTokenCreateAt(mParamDto.providerTokenCreateAt);
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
        if (!isInternetConnecting || mPostChannelsMineRequest != null) {
            return;
        }

        isChannelsLoading = true;

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
        if (!isInternetConnecting || mPostChannelsCloseRequest != null) {
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

//	/**
//	 * ネットワークエラー表示し、再接続を行います。
//	 */
//	private void errorWithReconnect() {
//		// ネットワークエラー表示
//		mNetoworkErrorTextView.setVisibility(View.VISIBLE);
//		// 再接続
//		mHandler.postDelayed(mReconnectTask, 3000);
//	}

    /**
     * 編集モード/通常モードをトルグします。
     */
    /**
     private void toggleMode() {
     editMode(mListView.getChoiceMode() != ListView.CHOICE_MODE_MULTIPLE);
     }
     */

    /**
     * 編集モード/通常モードにシフトします。
     *
     * @param editMode 編集モードの場合は true、通常モードの場合は false
     */
    /**
     * private void editMode(boolean editMode) {
     * if (editMode) {
     * mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
     * mAdapter.setEdit(true);
     * mEditButton.setText("キャンセル");
     * mDeleteButton.setVisibility(View.VISIBLE);
     * getSupportActionBar().setDisplayHomeAsUpEnabled(false);
     * } else {
     * mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
     * mListView.clearChoices();
     * mAdapter.setEdit(false);
     * mEditButton.setText("編集");
     * mDeleteButton.setVisibility(View.GONE);
     * }
     * mAdapter.notifyDataSetChanged();
     * }
     */

    @Override
    public void onFunnelItemSelected(ChannelFilterView.MessageFunnelItem item) {
        mCurrentFunnel = item;
        mProgressBar.setVisibility(View.VISIBLE);
        mChannelFilterWindow.dismiss();
        mParamDto.funnelId = item.funnel.id;
        isChannelsLoading = false;
        isCanLoadMore = false;

        requestGetChannels();
        setUpFilterLabel();
    }

    @Override
    public void onStatusItemSelected(ChannelFilterView.MessageStatusItem item) {
        mCurrentStatus = item;
        mProgressBar.setVisibility(View.VISIBLE);
        mChannelFilterWindow.dismiss();
        mParamDto.channelStatus = item.value;
        isChannelsLoading = false;
        isCanLoadMore = false;

        requestGetChannels();
        setUpFilterLabel();
    }

    @Override
    public void onDialogOutsideTouched() {
        mChannelFilterWindow.dismiss();
    }

    private void setUpFilterLabel() {
        if ((mCurrentFunnel == null || mCurrentFunnel.funnel.id < 0)
                && (mCurrentStatus == null || mCurrentStatus.value == ChannelItem.ChannelStatus.CHANNEL_ALL)) {
            mTvFunnel.setText(R.string.all);
        } else if (mCurrentFunnel != null && mCurrentFunnel.funnel.id >= 0
                && (mCurrentStatus == null || mCurrentStatus.value == ChannelItem.ChannelStatus.CHANNEL_ALL)) {
            mTvFunnel.setText(mCurrentFunnel.funnel.name);
        } else if ((mCurrentFunnel == null || mCurrentFunnel.funnel.id < 0)
                && mCurrentStatus != null && mCurrentStatus.value != ChannelItem.ChannelStatus.CHANNEL_ALL) {
            mTvFunnel.setText(mCurrentStatus.name);
        } else {
            mTvFunnel.setText(mCurrentFunnel.funnel.name + ", " + mCurrentStatus.name);
        }
    }

    @Override
    public void onRefresh() {
        isCanLoadMore = false;
        isChannelsLoading = false;

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
                    // errorWithReconnect();
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
            AuthUtil.saveTokens(MessagesActivity.this, mParamDto.providerTokenCreateAt, responseDto.token, responseDto.id);

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
            isChannelsLoading = false;
            mPostChannelsMineRequest = null;
            mGetChannelsRequest = null;

            mProgressBar.setVisibility(View.GONE);

            if (!isAuthErrorWithAlert(error)) {
                // エラー表示＆再接続
                // errorWithReconnect();
            }
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void onSuccess(GetChannelsMineResponseDto responseDto) {
            isChannelsLoading = false;
            mPostChannelsMineRequest = null;
            mGetChannelsRequest = null;

            if (responseDto == null) {
                return;
            }

            // リスト作成
            List<GetChannelsMineResponseDto.Channel> items = new ArrayList<>();
            if (responseDto.items == null || responseDto.items.size() == 0) {
                isCanLoadMore = false;
            } else {
                if (responseDto.items.size() < ChatCenterConstants.MAX_CHANNEL_ON_LOAD) {
                    isCanLoadMore = false;
                } else {
                    isCanLoadMore = true;
                }
                for (GetChannelsMineResponseDto.Channel item : responseDto.items) {
                    if (mCurrentStatus != null
                            && mCurrentStatus.value != ChannelItem.ChannelStatus.CHANNEL_CLOSE
                            && item.isClosed()) {
                        continue;
                    }
                    items.add(item);
                }
            }

            // ソート
            Collections.sort(items, COMPARATOR);

            // Make the channel list

            if (mChannelItems == null) {
                mChannelItems = new ArrayList<>();
            } else if (!mIsLoadMore) {
                mChannelItems.clear();
            }


//            if (mCurrentStatus != null
//                    && mCurrentStatus.value == ChannelItem.ChannelStatus.CHANNEL_ASSIGNED_TO_ME) {
//                // add channels that assigned to me
//                for (GetChannelsMineResponseDto.Channel channel : items) {
//                    if (channel.assignee.id.intValue() == AuthUtil.getUserId(MessagesActivity.this)) {
//                        mChannelItems.add(channel);
//                    }
//                }
//            } else {
            mChannelItems.addAll(items);
//            }

            if (mCurrentOrgItem == null) {
                mCurrentOrgItem = new OrgItem();
                mCurrentOrgItem.uid = mChannelItems.get(0).orgUid;
            }

            boolean isEdit;
            isEdit = mAdapter.isEdit();

            mAdapter.setEdit(isEdit);
            mListView.clearChoices();

            if (mAdapter.getCount() == 0) {
                // 0件メッセージ
                mEmptyTextView.setVisibility(View.VISIBLE);
            } else {
                mEmptyTextView.setVisibility(View.GONE);
                // mEditButton.setVisibility(mIsAgent ? View.GONE : View.VISIBLE);
            }

            mProgressBar.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);

            //if (mIsAgent) {
            // Connect to WebSocket
            if (!WebSocketHelper.isConnected()) {
                if (!mIsLoadMore) {
                    if (mCurrentAppId == null) {
                        mCurrentAppId = ApiUtil.getAppToken(MessagesActivity.this);
                    }
                    WebSocketHelper.connectWithAppToken(getApplicationContext(), mCurrentAppId, new WebSocketClientListener());
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNetworkErrorTextView.setBackgroundColor(getResources().getColor(R.color.color_green));
                        mNetworkErrorTextView.setText(R.string.internet_connected);
                    }
                });
                mHandler.postDelayed(mHideNWErrorTask, 1000);
            }
            //}

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
                GetChannelsMineResponseDto.Channel item = mAdapter.getItem(i);
                if (closeChannelUids.contains(item.uid)) {
                    mAdapter.remove(item);
                }
            }
            mListView.clearChoices();

            if (mAdapter.getCount() == 0) {
                mEmptyTextView.setVisibility(View.VISIBLE);
                // mEditButton.setVisibility(View.GONE);

                // 通常モードに切り替え
                // editMode(false);
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////
    // ListView
    // //////////////////////////////////////////////////////////////////////////
    private int mLastItem = -1;

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
            if (!isChannelsLoading && isCanLoadMore && lastItem > 0) {
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
        final HashMap<LeftMenuGroupItem, List<LeftMenuChildItem>> menuItems = new HashMap<>();

        // Create inbox group
        LeftMenuGroupItem inboxItem = new LeftMenuGroupItem(R.drawable.icon_inbox, getString(R.string.inbox));
        mMenuOrgItems = new ArrayList<>();
        menuItems.put(inboxItem, mMenuOrgItems);

        // Menu adapter
        mMenuAdapter = new LeftMenuAdapter(this, menuItems);

        mMenuLayout = (RelativeLayout) findViewById(R.id.messages_drawer_menu);
        mMenuListView = (ExpandableListView) findViewById(R.id.menu_list_view);
        if (mMenuListView == null) {
            return;
        }

        LayoutInflater inflater = getLayoutInflater();
        mMenuHeader = inflater.inflate(R.layout.menu_header, mMenuListView, false);
        mMenuFooter = inflater.inflate(R.layout.menu_footer, mMenuListView, false);

        mMenuListView.addFooterView(mMenuFooter);
        mMenuListView.addHeaderView(mMenuHeader);

        mMenuListView.setAdapter(mMenuAdapter);

        // Expand Inbox group
        mMenuListView.expandGroup(0);
        mMenuListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                LeftMenuChildItem menuItem = menuItems.get(menuItems.keySet().toArray()[groupPosition]).get(childPosition);
                changeOrg(menuItem);
                return false;
            }
        });

        mMenu = (DrawerLayout) findViewById(R.id.messages_drawer);

        Button btSwitchApp = (Button) mMenuHeader.findViewById(R.id.btn_switch_app);
        btSwitchApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMenu.closeDrawers();
                showListAppsDialog();
            }
        });

        TextView tvSettings = (TextView) mMenuFooter.findViewById(R.id.tv_setting);
        tvSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMenu.closeDrawers();
                showSettingsDialog();
            }
        });
    }

    private void showListAppsDialog() {
        if (mAppsListDialog != null) {
            mAppsListDialog.show(getSupportFragmentManager(), AppsListDialogFragment.TAG);
        }
    }

    private void showSettingsDialog() {
        if (mSettingDialog == null) {
            mSettingDialog = new SettingsDialogFragment();
            mSettingDialog.mActivity = MessagesActivity.this;
        }

        mSettingDialog.show(getSupportFragmentManager(), SettingsDialogFragment.TAG);
    }

    /**
     * Setup current user information on Agent menu
     *
     * @param userItem
     */
    private void updateAgentMenu(UserItem userItem) {
        if (userItem != null) {
            TextView tvUserDisplayName = (TextView) mMenuHeader.findViewById(R.id.menu_header_user_name);
            TextView tvUserDisplayEmail = (TextView) mMenuHeader.findViewById(R.id.menu_header_user_email);
            ImageView imvUserAva = (ImageView) mMenuHeader.findViewById(R.id.imv_left_menu_user_ava);
            TextView tvUserAva = (TextView) mMenuHeader.findViewById(R.id.tv_left_menu_user_ava);

            tvUserDisplayName.setText(userItem.displayName);
            tvUserDisplayEmail.setText(userItem.email);

            if (StringUtil.isNotBlank(userItem.iconUrl)) {
                tvUserAva.setVisibility(View.GONE);
                imvUserAva.setVisibility(View.VISIBLE);
                ViewUtil.loadImageCircle(imvUserAva, userItem.iconUrl);
            } else {
                tvUserAva.setVisibility(View.VISIBLE);
                imvUserAva.setVisibility(View.GONE);

                tvUserAva.setText(userItem.displayName.toUpperCase().substring(0, 1));

                GradientDrawable gradientDrawable = (GradientDrawable) tvUserAva.getBackground();
                gradientDrawable.setColor(ViewUtil.getIconColor(userItem.displayName));
            }
        }

    }

    private void updateCurrentAppName() {
        if (!isInternetConnecting || mCurrentApp == null)
            return;

        TextView tvCurrentAppName = (TextView) mMenuHeader.findViewById(R.id.tv_current_app);
        ImageView imvAppAva = (ImageView) mMenuHeader.findViewById(R.id.imv_left_menu_app_icon);
        TextView tvAppAva = (TextView) mMenuHeader.findViewById(R.id.tv_left_menu_app_icon);

        tvCurrentAppName.setText(mCurrentApp.name);

        if (mCurrentApp.icons != null && mCurrentApp.icons.size() > 0) {
            tvAppAva.setVisibility(View.GONE);
            imvAppAva.setVisibility(View.VISIBLE);
            ViewUtil.loadRoundedCornersImage(imvAppAva, mCurrentApp.icons.get(0).iconUrl,
                    getResources().getDimensionPixelSize(R.dimen.left_menu_app_icon_radius));
        } else {
            tvAppAva.setVisibility(View.VISIBLE);
            imvAppAva.setVisibility(View.GONE);

            tvAppAva.setText(mCurrentApp.name.toUpperCase().substring(0, 1));

            GradientDrawable gradientDrawable = (GradientDrawable) tvAppAva.getBackground();
            gradientDrawable.setColor(ViewUtil.getIconColor(mCurrentApp.token));
        }
    }

    /**
     * GET /api/orgs
     */
    private void requestGetOrgs(boolean cleanOldOrgs) {
        if (!isInternetConnecting || mGetOrgsRequest != null) {
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
     * Call this method when menu button clicked
     * <p>
     * GET /api/orgs
     */
    private void requestGetOrgForMenuUpdating() {
        if (!isInternetConnecting || mGetOrgsRequest != null) {
            return;
        }

        String path = "orgs";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

        mGetOrgsRequest = new OkHttpApiRequest<>(getApplicationContext(), OkHttpApiRequest.Method.GET, path, null, headers, new GetOrgForMenuUpdateCallback(), new GetOrgsParser());
        mGetOrgsRequest.setApiToken(mCurrentAppId);

        NetworkQueueHelper.enqueue(mGetOrgsRequest, REQUEST_TAG);
    }

    /**
     * GET /api/apps
     */
    private void requestGetApps() {
        if (!isInternetConnecting || mGetAppsRequest != null) {
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
        if (!isInternetConnecting || mGetChannelsRequest != null || mCurrentOrgItem == null) {
            return;
        }

        isChannelsLoading = true;

        String path = "channels";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

        GetChannelsRequestDto request = new GetChannelsRequestDto();
        request.setStatus(mParamDto.channelStatus);
        request.setType(mParamDto.channelType);
        request.setLastUpdatedAt(lastUpdatedDate);
        request.setOrgUid(mCurrentOrgItem.uid);
        request.setFunnelID(mParamDto.funnelId);

        if (mParamDto.channelStatus == ChannelItem.ChannelStatus.CHANNEL_ASSIGNED_TO_ME) {
            request.setAssigneeID(AuthUtil.getUserId(this));
        }

        Map<String, String> params = request.toParams();

        mGetChannelsRequest = new OkHttpApiRequest<>(getApplicationContext(),
                OkHttpApiRequest.Method.GET, path, params, headers,
                new GetChannelsMineCallback(lastUpdatedDate > 0), new GetChannelsMineParser());
        mGetChannelsRequest.setApiToken(mCurrentAppId);

        NetworkQueueHelper.enqueue(mGetChannelsRequest, REQUEST_TAG);
    }

    /**
     * GET /api/channels
     */
    private void requestGetChannels() {
        requestGetChannels(0);
    }


    /**
     * GET /api/channels/count
     */

    private void requestGetChannelCount(String orgUid) {
        if (!isInternetConnecting || mGetChannelsCountRequest != null) {
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
        if (!isInternetConnecting || mGetFunnelsRequest != null) {
            return;
        }

        String path = "funnels/";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

        mGetFunnelsRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.GET, path, null, headers,
                new GetFunnelsCallback(), new GetFunnelsParser());
        mGetFunnelsRequest.setApiToken(mCurrentAppId);

        NetworkQueueHelper.enqueue(mGetFunnelsRequest, REQUEST_TAG);
    }

    /**
     * GET /api/users/:id
     */
    private void requestGetUsers(int userId) {
        if (!isInternetConnecting || mGetUsersRequest != null) {
            return;
        }

        String path = "users/" + userId;

        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", AuthUtil.getUserToken(getApplicationContext()));

        mGetUsersRequest = new OkHttpApiRequest<>(getApplicationContext(), ApiRequest.Method.GET, path, null, headers, new GetUsersCallback(), new GetUsersParser());
        if (mCurrentApp != null) {
            mGetUsersRequest.setApiToken(mCurrentApp.token);
        }
        NetworkQueueHelper.enqueue(mGetUsersRequest, REQUEST_TAG);
    }

    private class GetOrgsCallback implements OkHttpApiRequest.Callback<GetOrgsResponseDto> {
        private boolean mCleanOldOrgs;

        public GetOrgsCallback(boolean cleanOldOrgs) {
            mCleanOldOrgs = cleanOldOrgs;
        }

        @Override
        public void onSuccess(GetOrgsResponseDto responseDto) {
            // Clean up old Orgs
            if (mCleanOldOrgs && mMenuOrgItems != null) {
                mMenuOrgItems.clear();
            }

            // Add the Orgs before apps
            int currentSize = mMenuOrgItems.size();
            for (OrgItem item : responseDto.items) {
                mMenuOrgItems.add(mMenuOrgItems.size() - currentSize, new LeftMenuChildItem(item.name, item.uid, item, null));
            }

            // Update data
            mMenuAdapter.notifyDataSetChanged();
            if (mCurrentOrgItem == null || mCleanOldOrgs) {

                mCurrentOrgItem = responseDto.items.get(0);

                // Set Save ORG if need
                String lastOrgUid = PreferenceUtil.getLastOrgUid(MessagesActivity.this);
                if (StringUtil.isNotBlank(lastOrgUid)) {
                    for (OrgItem item : responseDto.items) {
                        if (StringUtil.isNotBlank(item.uid) && item.uid.equals(lastOrgUid)) {
                            mCurrentOrgItem = item;
                            break;
                        }
                    }
                }

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
                // errorWithReconnect();
            }
        }
    }

    private class GetOrgForMenuUpdateCallback implements OkHttpApiRequest.Callback<GetOrgsResponseDto> {

        @Override
        public void onSuccess(GetOrgsResponseDto responseDto) {

            for (OrgItem item : responseDto.items) {
                int position = getOrgPositionOnMenu(item, mMenuOrgItems);

                if (position >= 0) {
                    if (mMenuOrgItems.get(position).getOrg().unreadMessagesChannels != null) {
                        mMenuOrgItems.get(position).getOrg().unreadMessagesChannels.clear();
                    }
                    if (item.unreadMessagesChannels != null) {
                        mMenuOrgItems.get(position).getOrg().unreadMessagesChannels.addAll(item.unreadMessagesChannels);
                    }
                } else {
                    mMenuOrgItems.add(new LeftMenuChildItem(item.name, item.uid, item, mCurrentApp));
                }
            }

            // Update data
            mMenuAdapter.notifyDataSetChanged();
            mGetOrgsRequest = null;
        }

        @Override
        public void onError(OkHttpApiRequest.Error error) {
            mGetOrgsRequest = null;
        }
    }

    private class GetAppsCallback implements OkHttpApiRequest.Callback<GetAppsResponseDto> {
        @Override
        public void onSuccess(GetAppsResponseDto responseDto) {
            mGetAppsRequest = null;

            if (mMenuAppItems == null) {
                mMenuAppItems = new ArrayList<>();
            } else if (mMenuAppItems.size() > 0) {
                mMenuAppItems.clear();
            }

            for (GetAppsResponseDto.App item : responseDto.items) {
                mMenuAppItems.add(new LeftMenuChildItem(item.name, item.token, null, item));
            }

            // Get last configuration for App and Org
            String lastAppId = PreferenceUtil.getLastAppId(MessagesActivity.this);
            if (StringUtil.isNotBlank(lastAppId)) {
                for (GetAppsResponseDto.App app : responseDto.items) {
                    if (StringUtil.isNotBlank(app.token) && app.token.equals(lastAppId)) {
                        mCurrentAppId = app.token;
                        mCurrentApp = app;
                    }
                }
            } else {
                // Update current application token
                mCurrentAppId = responseDto.items.get(0).token;
                mCurrentApp = responseDto.items.get(0);
            }

            requestGetOrgs(false);
            requestGetFunnels();

            // Now we register this device token
            ChatCenter.registerDeviceToken(MessagesActivity.this, mCurrentAppId, null);

            // Connect with the newly acquired app token
            WebSocketHelper.reconnectWithAppToken(getApplicationContext(), mCurrentAppId, new WebSocketClientListener());

            // Update current App Name
            updateCurrentAppName();
            if (mAppsListDialog == null) {
                mAppsListDialog = new AppsListDialogFragment();
                mAppsListDialog.mItems = mMenuAppItems;
                mAppsListDialog.mActivity = MessagesActivity.this;
                mAppsListDialog.mAdapter = new MenuAppsAdapter(MessagesActivity.this, 0, mMenuAppItems);
            }
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


    /**
     * GET /api/users/:id のコールバック
     */
    private class GetUsersCallback implements OkHttpApiRequest.Callback<GetUsersResponseDto> {
        @Override
        public void onError(OkHttpApiRequest.Error error) {
            mGetUsersRequest = null;
        }

        @Override
        public void onSuccess(GetUsersResponseDto responseDto) {
            mGetUsersRequest = null;

            if (mIsAgent) {
                UserItem userItem = responseDto;
                updateAgentMenu(userItem);
            }
        }
    }

    // ===================================================================================
    // WebSocket support
    // ===================================================================================

    private class WebSocketClientListener extends CCWebSocketClientListener {

        @Override
        public void onWSConnect() {
            Log.d(TAG, "onWSConnect: ");

            if (mChannelItems != null && mChannelItems.size() > 0) {
                WsConnectChannelRequest wsConnectChannelRequest = new WsConnectChannelRequest();
                wsConnectChannelRequest.channelUid = mChannelItems.get(0).uid;

                WebSocketHelper.send(wsConnectChannelRequest.toJson());
            }


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
            Log.d(TAG, "onWSDisconnect: " + code + " " + reason);
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
            Log.d(TAG, "onWSMessage: " + messageType + " " + mCurrentOrgItem + " " + response.orgUid);
            // Do nothing
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentOrgItem == null || (mCurrentOrgItem != null && !response.orgUid.equals(mCurrentOrgItem.uid))) {
                        // Not current OrgUid, skip
                        return;
                    }

                    GetChannelsMineResponseDto.Channel found = null;
                    for (GetChannelsMineResponseDto.Channel item : mChannelItems) {
                        if (!item.uid.equals(response.channelUid)) {
                            continue;
                        }

                        mChannelItems.remove(item);
                        mChannelItems.add(0, item);
                        found = item;
                        break;
                    }

                    if (found == null || (found.latestMessage != null && found.latestMessage.id.equals(response.id))) {
                        return;
                    }

                    if (ResponseType.SUGGESTION.equals(response.type) || ResponseType.RESPONSE.equals(response.type)
                            && response.widget != null && StringUtil.isNotBlank(response.widget.stickerType)
                            && response.widget.stickerType.equals("co-location")) {
                        // Do nothing
                    } else {
                        found.latestMessage = new GetChannelsMineResponseDto.Channel.LatestMessage();
                        found.latestMessage.id = response.id;
                        found.latestMessage.type = response.type;
                        found.latestMessage.widget = response.widget;
                        found.latestMessage.created = response.created;
                        found.latestMessage.user = response.user;
                        found.unreadMessages += 1;
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onWSRecieveAnswer(Integer messageId, Integer answerType) {
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
                    for (GetChannelsMineResponseDto.Channel item : mChannelItems) {
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
        public void onWSRecieveOnline(String channelUid, JSONObject user, String orgUid, Boolean online) {
        }

        @Override
        public void onWSReceiveReceipt(String channelUid, JSONArray messages, JSONObject user) {
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
            ((ChannelFilterView) mChannelFilterWindow.getContentView()).updateData(responseDto);

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

            if (responseDto != null) {
                mFunnelItems.clear();
                if (responseDto.funnels != null) {
                    mFunnelItems.addAll(responseDto.funnels);
                }
            }

        }
    }

    public static class SettingsDialogFragment extends DialogFragment {
        public static final String TAG = "SettingsDialogFragment";
        public MessagesActivity mActivity;


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            View dialogCustomView = inflater.inflate(R.layout.dialog_settings, null);
            Button openDashboard = (Button) dialogCustomView.findViewById(R.id.btn_open_dashboard);
            Button logout = (Button) dialogCustomView.findViewById(R.id.btn_logout);
            Button cancel = (Button) dialogCustomView.findViewById(R.id.btn_cancel);
            Button about = (Button) dialogCustomView.findViewById(R.id.btn_about);
            TextView title = (TextView) dialogCustomView.findViewById(R.id.tv_title);

            title.setText(String.format(title.getText().toString(), mActivity.getLibVersion()));

            openDashboard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.mSettingDialog.dismiss();
                    mActivity.openDashboard();
                }
            });

            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.mSettingDialog.dismiss();
                    mActivity.logout();
                }
            });

            about.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.mSettingDialog.dismiss();
                    mActivity.showAboutChatCenter();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.mSettingDialog.dismiss();
                }
            });

            builder.setView(dialogCustomView);
            Dialog dialog = builder.create();
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            return dialog;
        }
    }

    public static class AppsListDialogFragment extends DialogFragment {
        public static final String TAG = "AppsListDialogFragment";

        public List<LeftMenuChildItem> mItems;
        public MessagesActivity mActivity;
        public MenuAppsAdapter mAdapter;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            View dialogCustomView = inflater.inflate(R.layout.dialog_list_view, null);

            TextView tvTitle = (TextView) dialogCustomView.findViewById(R.id.dialog_title);
            tvTitle.setText(R.string.change_app);
            ListView lvApps = (ListView) dialogCustomView.findViewById(R.id.lv_apps);
            lvApps.setAdapter(mAdapter);
            lvApps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mActivity.mAppsListDialog.dismiss();
                    mActivity.changeApp(mItems.get(position));
                }
            });

            Button cancel = (Button) dialogCustomView.findViewById(R.id.btn_cancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.mAppsListDialog.dismiss();
                }
            });

            builder.setView(dialogCustomView);
            Dialog dialog = builder.create();

            return dialog;
        }

    }

    private void changeApp(LeftMenuChildItem item) {
        cancelAllRequest();

        // reset filter
        mParamDto.channelStatus = ChannelItem.ChannelStatus.CHANNEL_ALL;
        mParamDto.funnelId = -1;
        mCurrentFunnel = null;
        mCurrentStatus = null;
        setUpFilterLabel();

        mCurrentAppId = item.getValue();
        PreferenceUtil.saveLastAppId(MessagesActivity.this, mCurrentAppId);

        requestGetOrgs(true);

        mCurrentApp = item.getApp();

        // Update UI
        mMenu.closeDrawers();
        mProgressBar.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mEmptyTextView.setVisibility(View.GONE);

        // Now, connect to this app
        WebSocketHelper.reconnectWithAppToken(getApplicationContext(), mCurrentAppId, new WebSocketClientListener());
        ChatCenter.registerDeviceToken(this, mCurrentAppId, null);

        updateCurrentAppName();

        // Request to recreate filter windows.
        if (mChannelFilterWindow != null && mChannelFilterWindow.isShowing()) {
            mChannelFilterWindow.dismiss();
        }
        mChannelFilterWindow = null;
        requestGetFunnels();
    }

    private class MenuAppsAdapter extends ArrayAdapter<LeftMenuChildItem> {

        private List<LeftMenuChildItem> mItems;

        public MenuAppsAdapter(Context context, int resource, List<LeftMenuChildItem> objects) {
            super(context, resource, objects);
            mItems = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView textView = new TextView(MessagesActivity.this);
            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                    (int) convertDpToPixel(40, getContext()));

            textView.setLayoutParams(layoutParams);


            LeftMenuChildItem item = getItem(position);

            if (item.getApp().id == mCurrentApp.id) {
                textView.setTypeface(null, Typeface.BOLD);
            } else {
                textView.setTypeface(null, Typeface.NORMAL);
            }

            textView.setText(item.getApp().name);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(getContext().getResources().getColor(R.color.color_chatcenter_text));

            return textView;
        }

        @Override
        public LeftMenuChildItem getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }
    }

    /**
     * Logout from app and back to Login screen
     */
    private void logout() {
        ChatCenter.signOutAgent(MessagesActivity.this);
        if (ChatCenter.getTopActivity() != null) {
            Intent intent = new Intent(MessagesActivity.this, ChatCenter.getTopActivity().getClass());
            startActivity(intent);
        }
        finish();
    }

    private int getOrgPositionOnMenu(OrgItem org, List<LeftMenuChildItem> menuItems) {

        if (menuItems != null && menuItems.size() > 0) {
            for (int i = 0; i < menuItems.size(); i++) {
                if (menuItems.get(i).getOrg().uid.equals(org.uid)) {
                    return i;
                }
            }
        }

        return -1;
    }

    public String getLibVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "";
    }

    private void changeOrg(LeftMenuChildItem menuItem) {
        if (mCurrentOrgItem.uid.equals(menuItem.getValue())) {
            return;
        }

        // Load new channel
        mCurrentOrgItem = menuItem.getOrg();
        PreferenceUtil.saveLastOrgUid(MessagesActivity.this, mCurrentOrgItem.uid);

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
    }

    private void checkNetworkToStart() {
        if (!isNetworkAvailable()) {
            isInternetConnecting = false;
            if (mHandler != null) {
                mHandler.post(mShowNWErrorTask);
            }
            mProgressBar.setVisibility(View.GONE);
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

    public void showAboutChatCenter() {
        Intent intent = new Intent(MessagesActivity.this, AboutActivity.class);
        startActivity(intent);
    }

    public void openDashboard() {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(ChatCenterConstants.Extra.URL, ChatCenterConstants.URL_DASHBOARD);
        startActivity(intent);
    }
}