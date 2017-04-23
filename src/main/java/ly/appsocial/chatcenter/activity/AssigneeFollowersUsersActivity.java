package ly.appsocial.chatcenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.adapter.AssigneeFollowerUsersAdapter;
import ly.appsocial.chatcenter.activity.model.AssigneeFollowerListItem;
import ly.appsocial.chatcenter.dto.ChannelItem;
import ly.appsocial.chatcenter.dto.OrgItem;
import ly.appsocial.chatcenter.dto.UserItem;
import ly.appsocial.chatcenter.dto.ws.response.PostChannelsResponseDto;
import ly.appsocial.chatcenter.fragment.ProgressDialogFragment;
import ly.appsocial.chatcenter.util.CCAuthUtil;
import ly.appsocial.chatcenter.util.DialogUtil;
import ly.appsocial.chatcenter.util.NetworkQueueHelper;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.ws.ApiRequest;
import ly.appsocial.chatcenter.ws.OkHttpApiRequest;
import ly.appsocial.chatcenter.ws.parser.PostChannelsParser;

public class AssigneeFollowersUsersActivity extends BaseActivity implements AdapterView.OnItemClickListener,
        ProgressDialogFragment.DialogListener {

    private static final String REQUEST_TAG = "AssigneeFollower";

    public static final String LIST_TYPE = "LIST_TYPE";
    public static final String CHANNEL_DATA = "CHANNEL_DATA";
    public static final String ORG_DATA = "ORG_DATA";
    public static final String APP_TOKEN = "APP_TOKEN";

    public static final int LIST_TYPE_ASSIGNEE = 1;
    public static final int LIST_TYPE_FOLLOWERS = 2;

    private boolean isAssigneeList;
    private ListView mLVUsers;

    private List<AssigneeFollowerListItem> mItems;
    private ChannelItem mCurrentChannel;
    private OrgItem mCurrentOrg;
    private AssigneeFollowerUsersAdapter mAdapter;
    private String mChannelUid;
    private String mOrgUid;
    private String mAppToken;

    private OkHttpApiRequest<PostChannelsResponseDto> mPostChannelRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        isAssigneeList = intent.getIntExtra(LIST_TYPE, 0) == LIST_TYPE_ASSIGNEE;
        mChannelUid = intent.getStringExtra(CHANNEL_DATA);
        mOrgUid = intent.getStringExtra(ORG_DATA);
        mAppToken = intent.getStringExtra(APP_TOKEN);

        mItems = new ArrayList<>();

        setContentView(R.layout.list_assignee_followers);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLVUsers = (ListView) findViewById(R.id.lv_users);

        mAdapter = new AssigneeFollowerUsersAdapter(this, R.layout.item_list_assignee_followers, mItems);

        mLVUsers.setAdapter(mAdapter);
        mLVUsers.setOnItemClickListener(this);

        if (isAssigneeList) {
            getSupportActionBar().setTitle(R.string.assignee);
        } else {
            getSupportActionBar().setTitle(R.string.followers);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        mCurrentOrg = mTbOrg.getOrg(mOrgUid);
        mCurrentChannel = mTbChannel.getChannel(mOrgUid, mChannelUid);

        mItems.clear();
        mItems.addAll(getItems(mCurrentChannel, mCurrentOrg));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mItems.get(i).setSelected(!mItems.get(i).isSelected());

        // If is assignee list: Just one user will be selected
        if (isAssigneeList && mItems.get(i).isSelected()) {
            for (int j = 0; j < mItems.size(); j++) {
                if (j != i) {
                    mItems.get(j).setSelected(false);
                }
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    /**
     * Open activity for setting Follower
     * @param context
     * @param orgUid
     * @param channelUid
     * @param requestCode
     */
    public static void startSettingFollower(AppCompatActivity context, String orgUid,
                                             String channelUid, int requestCode) {

        Intent intent = new Intent(context, AssigneeFollowersUsersActivity.class);

        Bundle data = new Bundle();
        data.putInt(AssigneeFollowersUsersActivity.LIST_TYPE, LIST_TYPE_FOLLOWERS);
        data.putString(AssigneeFollowersUsersActivity.CHANNEL_DATA, channelUid);
        data.putString(AssigneeFollowersUsersActivity.ORG_DATA, orgUid);

        intent.putExtras(data);
        context.startActivityForResult(intent, requestCode);

    }

    /**
     * Open activity for setting Assignee
     * @param context
     * @param orgUid
     * @param channelUid
     */
    public static void startSettingAssignee(AppCompatActivity context, String orgUid,
                                            String channelUid) {

        Intent intent = new Intent(context, AssigneeFollowersUsersActivity.class);

        Bundle data = new Bundle();
        data.putInt(AssigneeFollowersUsersActivity.LIST_TYPE, LIST_TYPE_ASSIGNEE);
        data.putString(AssigneeFollowersUsersActivity.CHANNEL_DATA, channelUid);
        data.putString(AssigneeFollowersUsersActivity.ORG_DATA, orgUid);

        intent.putExtras(data);
        context.startActivity(intent);

    }


    private List<AssigneeFollowerListItem> getItems(ChannelItem channelItem, OrgItem orgItem) {
        List<AssigneeFollowerListItem> items = new ArrayList<>();

        if (orgItem != null) {
            if (orgItem.users != null && orgItem.users.size() > 0) {
                for (UserItem user : orgItem.users) {
                    AssigneeFollowerListItem item = new AssigneeFollowerListItem();
                    item.setUser(user);
                    if(!isAssigneeList && mCurrentChannel.assignee != null && user.id.equals(mCurrentChannel.assignee.id)) {
                        // If is Followers list, do not add assignee
                    } else {
                        items.add(item);
                    }
                }
            }
        }

        if (channelItem != null) {
            if (channelItem.users != null && channelItem.users.size() > 0) {
                for (UserItem user : channelItem.users) {
                    if (user.admin) {
                        AssigneeFollowerListItem item = new AssigneeFollowerListItem();
                        item.setUser(user);

                        if (isAssigneeList) {
                            if (mCurrentChannel.assignee != null
                                    && user.id.equals(mCurrentChannel.assignee.id)) {
                                item.setSelected(true);
                            }
                        } else {
                            if (user.admin) {
                                if (mCurrentChannel.assignee != null && user.id.equals(mCurrentChannel.assignee.id)) {
                                    // Do not show assignee on list of followers
                                    continue;
                                }
                                item.setSelected(true);
                            }
                        }

                        // If this user was added into list, change item status. Else add new user into list
                        int addedIndex = getAddedIndex(items, user);
                        if (addedIndex >= 0) {
                            items.get(addedIndex).setSelected(item.isSelected());
                        } else {
                            items.add(item);
                        }
                    }
                }
            }
        }

        return items;
    }

    private int getAddedIndex(List<AssigneeFollowerListItem> items, UserItem user) {
        if(items != null && items.size() > 0) {
            for (int i = 0; i < items.size(); i ++) {
                UserItem userItem = items.get(i).getUser();
                if (userItem.id.equals(user.id)) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onButtonSaveClicked(View view) {
        ArrayList<UserItem> selectedUsers = new ArrayList<>(getListSelectedUser());
        if (isAssigneeList) {
            setUpAssignee(selectedUsers);
        } else {
            Bundle data = new Bundle();
            data.putSerializable("result", selectedUsers);

            Intent intent = new Intent();
            intent.putExtras(data);

            setResult(RESULT_OK, intent);
            finish();
        }
    }

    public List<UserItem> getListSelectedUser() {
        List<UserItem> users = new ArrayList<>();

        for (AssigneeFollowerListItem item :
                mItems) {
            if (item.isSelected()) {
                users.add(item.getUser());
            }
        }

        return users;
    }

    /**
     * ダイアログをキャンセルした際のコールバック。
     *
     * @param tag このフラグメントのタグ
     */
    @Override
    public void onDialogCancel(String tag) {

    }

    private void setUpAssignee(List<UserItem> users) {
        if (users != null && users.size() > 0) {
            DialogUtil.showProgressDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
            requestPostChannelAssignIfNeed(mChannelUid, users.get(0));
        } else if (users != null && users.size() == 0 && mCurrentChannel.assignee != null) {
            DialogUtil.showProgressDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
            requestPostChannelUnassign(mChannelUid, mCurrentChannel.assignee);
        }
    }


    /**
     * POST /api/channels/:channel_uid/assign
     */
    private void requestPostChannelAssignIfNeed(String channelUid, UserItem user) {

        if (mCurrentChannel.assignee != null && user.id.equals(mCurrentChannel.assignee.id)) {
            return;
        }

        String path = "channels/" + channelUid + "/assign";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

        Map<String, String> params = new HashMap<>();
        params.put("user_id", user.id + "");

        mPostChannelRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, params, headers, new PostAssigneeCallback(), new PostChannelsParser());
        if (StringUtil.isNotBlank(mAppToken)) {
            mPostChannelRequest.setApiToken(mAppToken);
        }
        NetworkQueueHelper.enqueue(mPostChannelRequest, REQUEST_TAG);
    }

    /**
     * POST /api/channels/:channel_uid/unassign
     */
    private void requestPostChannelUnassign(String channelUid, UserItem user) {

        String path = "channels/" + channelUid + "/unassign";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

        Map<String, String> params = new HashMap<>();
        params.put("user_id", user.id + "");

        mPostChannelRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.POST, path, params, headers, new PostUnAssigneeCallback(), new PostChannelsParser());
        if (StringUtil.isNotBlank(mAppToken)) {
            mPostChannelRequest.setApiToken(mAppToken);
        }
        NetworkQueueHelper.enqueue(mPostChannelRequest, REQUEST_TAG);
    }

    /**
     * POST /api/channels/:channel_uid/assign のコールバック
     */
    private class PostAssigneeCallback implements OkHttpApiRequest.Callback<PostChannelsResponseDto> {
        @Override
        public void onError(OkHttpApiRequest.Error error) {
            DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
            finish();
        }

        @Override
        public void onSuccess(PostChannelsResponseDto responseDto) {
//            mPostChannelRequest = null;
            DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
            mTbChannel.updateOrInsert(responseDto);
            finish();
        }
    }

    /**
     * POST /api/channels/:channel_uid/unassign のコールバック
     */
    private class PostUnAssigneeCallback implements OkHttpApiRequest.Callback<PostChannelsResponseDto> {
        @Override
        public void onError(OkHttpApiRequest.Error error) {
            DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
            finish();
        }

        @Override
        public void onSuccess(PostChannelsResponseDto responseDto) {
            DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
            mTbChannel.updateOrInsert(responseDto);
            finish();
        }
    }
}
