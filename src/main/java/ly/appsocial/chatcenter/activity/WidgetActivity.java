package ly.appsocial.chatcenter.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.adapter.WidgetAdapter;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.dto.ws.request.GetWidgetRequestDto;
import ly.appsocial.chatcenter.dto.ws.response.GetMessagesResponseDto;
import ly.appsocial.chatcenter.util.CCAuthUtil;
import ly.appsocial.chatcenter.util.DialogUtil;
import ly.appsocial.chatcenter.util.NetworkQueueHelper;
import ly.appsocial.chatcenter.ws.ApiRequest;
import ly.appsocial.chatcenter.ws.OkHttpApiRequest;
import ly.appsocial.chatcenter.ws.parser.GetMessagesParser;

/**
 * The activity to show all sent widgets in a channel
 */
public class WidgetActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        AbsListView.OnScrollListener{

    public static String CHANNEL_UID = "channel_uid";
    public static String STICKER_TYPE = "sticker_type";
    public static String ACTIVITY_TITLE = "activity_title";

    private static final String REQUEST_TAG = "WidgetActivity";
    private OkHttpApiRequest<GetMessagesResponseDto> mGetMessagesRequest;
    private String mChannelUid;
    private String mStickerType;

    private ListView mLvWidgets;
    private TextView mTvNoMessage;
    private LinearLayout mLlProgressBar;
    private SwipeRefreshLayout mSwipeLayout;

    private WidgetAdapter mAdapter;
    private List<ChatItem> mWidgets;

    private boolean isLoadingMore;
    private boolean isCanLoadMore;
    private boolean isRefreshing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget);

        mChannelUid = getIntent().getStringExtra(CHANNEL_UID);
        mStickerType = getIntent().getStringExtra(STICKER_TYPE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra(ACTIVITY_TITLE));

        mLvWidgets = (ListView) findViewById(R.id.lv_widgets);
        mTvNoMessage = (TextView) findViewById(R.id.tv_no_message);
        mLlProgressBar = (LinearLayout) findViewById(R.id.progress);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);

        mSwipeLayout.setOnRefreshListener(this);
        mLvWidgets.setOnScrollListener(this);

        mWidgets = new ArrayList<>();
        mAdapter = new WidgetAdapter(this, mWidgets);
        mLvWidgets.setAdapter(mAdapter);

        requestGetMessages(0, mStickerType);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * GET /api/channels/:channel_uid/messages
     */
    private void requestGetMessages(int lastId, String stickerType) {

        if (!isNetworkAvailable()) {
            DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ALERT, null,
                    getString(R.string.chat_network_error_message));
            return;
        }

        String path = "channels/" + mChannelUid + "/messages";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

        GetWidgetRequestDto getWidgetRequestDto = new GetWidgetRequestDto();
        getWidgetRequestDto.lastId = lastId;
        getWidgetRequestDto.stickerType = stickerType;

        mGetMessagesRequest = new OkHttpApiRequest<>(getApplicationContext(), ApiRequest.Method.GET,
                path, getWidgetRequestDto.toParams(), headers, new GetMessagesCallback(), new GetMessagesParser());

        NetworkQueueHelper.enqueue(mGetMessagesRequest, REQUEST_TAG);
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        isRefreshing = true;
        requestGetMessages(0, mStickerType);
    }

    /**
     * Callback method to be invoked while the list view or grid view is being scrolled. If the
     * view is being scrolled, this method will be called before the next frame of the scroll is
     * rendered. In particular, it will be called before any calls to
     *
     * @param view        The view whose scroll state is being reported
     * @param scrollState The current scroll state. One of
     *                    {@link #SCROLL_STATE_TOUCH_SCROLL} or {@link #SCROLL_STATE_IDLE}.
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    /**
     * Callback method to be invoked when the list or grid has been scrolled. This will be
     * called after the scroll has completed
     *
     * @param view             The view whose scroll state is being reported
     * @param firstVisibleItem the index of the first visible cell (ignore if
     *                         visibleItemCount == 0)
     * @param visibleItemCount the number of visible cells
     * @param totalItemCount   the number of items in the list adaptor
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // Load more widget if scroll to bottom
        if ((firstVisibleItem + visibleItemCount == totalItemCount - 4) && isCanLoadMore && !isRefreshing && !isLoadingMore) {
            ChatItem lastItem = mWidgets.get(totalItemCount - 1);
            requestGetMessages(lastItem.id.intValue(), mStickerType);
            isLoadingMore = true;
            mSwipeLayout.setEnabled(false);
        }
    }

    /**
     * GET /api/channels/:channel_uid/messages のコールバック
     */
    private class GetMessagesCallback implements OkHttpApiRequest.Callback<GetMessagesResponseDto> {
        @Override
        public void onError(OkHttpApiRequest.Error error) {
            mLlProgressBar.setVisibility(View.GONE);
            mLvWidgets.setVisibility(View.GONE);
            mTvNoMessage.setVisibility(View.VISIBLE);

            DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ALERT, null,
                    getString(R.string.api_request_error));

            finishRequest();
        }

        @Override
        public void onSuccess(GetMessagesResponseDto responseDto) {

            mLlProgressBar.setVisibility(View.GONE);

            if (responseDto == null || responseDto.items == null || responseDto.items.size() == 0) {
                if (mWidgets == null || mWidgets.size() == 0) {
                    mLvWidgets.setVisibility(View.GONE);
                    mTvNoMessage.setVisibility(View.VISIBLE);
                }
                return;
            }

            mLvWidgets.setVisibility(View.VISIBLE);
            mTvNoMessage.setVisibility(View.GONE);

            if (isRefreshing) {
                mWidgets.clear();
            }

            /** If number of returned widgets is less than MAX LOAD -> Have no prev message*/
            if (responseDto.items.size() < ChatCenterConstants.MAX_MESSAGE_ON_LOAD) {
                isCanLoadMore = false;
            } else {
                isCanLoadMore = true;
            }

            mWidgets.addAll(responseDto.items);
            mAdapter.notifyDataSetChanged();

            finishRequest();
        }
    }

    private void finishRequest() {
        /** Finish refreshing*/
        if (isRefreshing) {
            mSwipeLayout.setRefreshing(false);
            isRefreshing = false;
        }

        /** Finish loading more*/
        if (isLoadingMore) {
            isLoadingMore = false;
            mSwipeLayout.setEnabled(true);
        }
    }
}
