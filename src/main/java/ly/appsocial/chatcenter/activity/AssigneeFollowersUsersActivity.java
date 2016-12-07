package ly.appsocial.chatcenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.adapter.AssigneeFollowerUsersAdapter;
import ly.appsocial.chatcenter.dto.ChannelItem;
import ly.appsocial.chatcenter.dto.OrgItem;
import ly.appsocial.chatcenter.dto.UserItem;

public class AssigneeFollowersUsersActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    public static final String LIST_TYPE = "LIST_TYPE";
    public static final String CHANNEL_DATA = "CHANNEL_DATA";
    public static final String ORG_DATA = "ORG_DATA";

    public static final int LIST_TYPE_ASSIGNEE = 1;
    public static final int LIST_TYPE_FOLLOWERS = 2;

    private boolean isAssigneeList;
    private ListView mLVUsers;

    private List<ItemListAssigneeFollower> mItems;
    private ChannelItem mCurrentChannel;
    private OrgItem mCurrentOrg;
    private AssigneeFollowerUsersAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        isAssigneeList = intent.getIntExtra(LIST_TYPE, 0) == LIST_TYPE_ASSIGNEE;
        mCurrentChannel = (ChannelItem) intent.getSerializableExtra(CHANNEL_DATA);
        mCurrentOrg = intent.getParcelableExtra(ORG_DATA);

        mItems = getItems(mCurrentChannel, mCurrentOrg);

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

    private List<ItemListAssigneeFollower> getItems(ChannelItem channelItem, OrgItem orgItem) {
        List<ItemListAssigneeFollower> items = new ArrayList<>();

        if (orgItem != null) {
            if (orgItem.users != null && orgItem.users.size() > 0) {
                for (UserItem user : orgItem.users) {
                    ItemListAssigneeFollower item = new ItemListAssigneeFollower();
                    item.setUser(user);
                    if(!isAssigneeList && mCurrentChannel.assignee != null && user.id == mCurrentChannel.assignee.id) {
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
                        ItemListAssigneeFollower item = new ItemListAssigneeFollower();
                        item.setUser(user);

                        if (isAssigneeList) {
                            if (mCurrentChannel.assignee != null
                                    && user.id == mCurrentChannel.assignee.id) {
                                item.setSelected(true);
                            }
                        } else {
                            if (user.admin) {
                                if (mCurrentChannel.assignee != null && user.id == mCurrentChannel.assignee.id) {
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

    private int getAddedIndex(List<ItemListAssigneeFollower> items, UserItem user) {
        if(items != null && items.size() > 0) {
            for (int i = 0; i < items.size(); i ++) {
                UserItem userItem = items.get(i).getUser();
                if (userItem.id.intValue() == user.id.intValue()) {
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

        Bundle data = new Bundle();
        data.putSerializable("result", selectedUsers);

        Intent intent = new Intent();
        intent.putExtras(data);

        setResult(RESULT_OK, intent);
        finish();
    }

    public List<UserItem> getListSelectedUser() {
        List<UserItem> users = new ArrayList<>();

        for (ItemListAssigneeFollower item :
                mItems) {
            if (item.isSelected()) {
                users.add(item.getUser());
            }
        }

        return users;
    }

    public static class ItemListAssigneeFollower implements Serializable {
        private boolean isSelected;
        private UserItem user;


        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public UserItem getUser() {
            return user;
        }

        public void setUser(UserItem user) {
            this.user = user;
        }
    }
}
