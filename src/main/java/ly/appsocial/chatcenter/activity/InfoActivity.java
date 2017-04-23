package ly.appsocial.chatcenter.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ChannelItem;
import ly.appsocial.chatcenter.dto.FunnelItem;
import ly.appsocial.chatcenter.dto.OrgItem;
import ly.appsocial.chatcenter.dto.UserItem;
import ly.appsocial.chatcenter.dto.param.ChatParamDto;
import ly.appsocial.chatcenter.dto.ws.response.GetFunnelResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.GetMeResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostChannelsResponseDto;
import ly.appsocial.chatcenter.fragment.AlertDialogFragment;
import ly.appsocial.chatcenter.fragment.ProgressDialogFragment;
import ly.appsocial.chatcenter.util.CCAuthUtil;
import ly.appsocial.chatcenter.util.CircleTransformation;
import ly.appsocial.chatcenter.util.DialogUtil;
import ly.appsocial.chatcenter.util.NetworkQueueHelper;
import ly.appsocial.chatcenter.util.CCPrefUtils;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.ws.ApiRequest;
import ly.appsocial.chatcenter.ws.OkHttpApiRequest;
import ly.appsocial.chatcenter.ws.parser.GetFunnelsParser;
import ly.appsocial.chatcenter.ws.parser.PostChannelsParser;

public class InfoActivity extends BaseActivity implements AlertDialogFragment.DialogListener,
        ProgressDialogFragment.DialogListener{

    private static final String REQUEST_TAG = "InfoActivity";

    private static final int REQUEST_SETUP_FUNNEL = 1001;
    // private static final int REQUEST_SETUP_ASSIGNEE = 1002;
    private static final int REQUEST_SETUP_FOLLOW = 1003;
    private static final int REQUEST_SETUP_NOTE = 1004;
    public static final String CHANNEL_DELETED = "CHANNEL_DELETED";

    private LinearLayout mAssigneesView;
    private LinearLayout mFollowersView;
    private LinearLayout mAssigneeFollowersView;
    private TextView mNameTextView;
    private TextView mStatusTextView;
    private ImageView mIconImage;
    private TextView mIconTextView;
    private TextView mEmailTextView;
    private TextView mEmailTextViewIcon;
    private TextView mFacebookTextView;
    private TextView mTwitterTextView;
    private TextView mTvFunnel;
    private TextView mTvCloseChannel;
    private LinearLayout mChannelControlView;

    private String mChannelUid;
    private String mOrgUid;

    /**
     * ParamDto
     */
    private ChatParamDto mParamDto;

    /**
     * GET /api/channels/:channel_uid
     */
    private ApiRequest<PostChannelsResponseDto> mGetChannelsRequest;
    /**
     * POST /api/channels/:channel_uid/assign
     */
    private ApiRequest<PostChannelsResponseDto> mPostChannelRequest;

    /**
     * GET /api/funnels
     */
    private ApiRequest<GetFunnelResponseDto> mGetFunnelsRequest;


    private ChannelItem mCurrentChannel;

    private boolean mIsAgent;

    private ArrayList<FunnelItem> mFunnels;

    private List<UserItem> mFollowers = new ArrayList<>();

    private GetMeResponseDto mUserConfig;

    private AlertDialog.Builder mConfirmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);

        mParamDto = getIntent().getParcelableExtra(ChatCenterConstants.Extra.CHAT_PARAM);
        mChannelUid = getIntent().getStringExtra(ChatCenterConstants.Extra.CHANNEL_UID);
        mOrgUid = getIntent().getStringExtra(ChatCenterConstants.Extra.ORG);
        mUserConfig = CCPrefUtils.getUserConfig(this);

        if (StringUtil.isBlank(mChannelUid) || mParamDto == null) {
            finish();
            return;
        }

        mAssigneesView = (LinearLayout) findViewById(R.id.view_assignee);
        mFollowersView = (LinearLayout) findViewById(R.id.view_followers);
        mAssigneeFollowersView = (LinearLayout) findViewById(R.id.assignee_followers_view);
        mChannelControlView = (LinearLayout) findViewById(R.id.channel_control);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.info_header);

        mNameTextView = (TextView) findViewById(R.id.info_assignee_name);
        mStatusTextView = (TextView) findViewById(R.id.info_assignee_status);
        mIconImage = (ImageView) findViewById(R.id.info_assignee_icon_image);
        mIconTextView = (TextView) findViewById(R.id.info_assignee_icon_textview);
        mEmailTextView = (TextView) findViewById(R.id.info_assignee_email);
        mEmailTextViewIcon = (TextView) findViewById(R.id.info_assignee_email_icon);
        mFacebookTextView = (TextView) findViewById(R.id.info_assignee_facebook);
        mTwitterTextView = (TextView) findViewById(R.id.info_assignee_twitter);
        mTvFunnel = (TextView) findViewById(R.id.tv_channel_funnel);
        mTvCloseChannel = (TextView) findViewById(R.id.bt_close_channel);

        mIsAgent = getIntent().getBooleanExtra(ChatCenterConstants.Extra.IS_AGENT, false);

        if (mIsAgent) {
            mAssigneeFollowersView.setVisibility(View.VISIBLE);
            mChannelControlView.setVisibility(View.VISIBLE);
            findViewById(R.id.btn_about).setVisibility(View.GONE);

            if (mUserConfig != null && mUserConfig.privilege != null) {
                if (!mUserConfig.privilege.channel.contains("destroy")){
                    findViewById(R.id.ll_delete).setVisibility(View.GONE);
                }

                if (!mUserConfig.privilege.channel.contains("assign")) {
                    findViewById(R.id.ll_assign).setVisibility(View.GONE);
                }

                if (!mUserConfig.privilege.channel.contains("close")) {
                    findViewById(R.id.ll_close).setVisibility(View.GONE);
                }
            }
        } else {
            mAssigneeFollowersView.setVisibility(View.GONE);
            mChannelControlView.setVisibility(View.GONE);
        }
        mFunnels = new ArrayList<>();

    }

    public static void startActivityForResult(AppCompatActivity context, String orgUid,
                                              String channelUid, ChatParamDto chatParamDto, int requestCode) {

        Intent intent = new Intent(context, InfoActivity.class);
        intent.putExtra(ChatCenterConstants.Extra.CHANNEL_UID, channelUid);
        intent.putExtra(ChatCenterConstants.Extra.CHAT_PARAM, chatParamDto);
        intent.putExtra(ChatCenterConstants.Extra.IS_AGENT, CCAuthUtil.isCurrentUserAdmin(context));
        intent.putExtra(ChatCenterConstants.Extra.ORG, orgUid);
        context.startActivityForResult(intent, requestCode);

    }

    @Override
    protected void onResume() {
        super.onResume();

        mCurrentChannel = mTbChannel.getChannel(mOrgUid, mChannelUid);
        updateView();

        if (mFunnels == null || mFunnels.size() == 0) {
            requestGetFunnels();
        }
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


    private void updateView() {
        updateHeaderView();
        if (mIsAgent) {
            updateAssigneesView();
            updateFollowersView();
        }

        if (mCurrentChannel != null && mCurrentChannel.isClosed()) {
            mTvCloseChannel.setText(R.string.open_conversation);
        } else {
            mTvCloseChannel.setText(R.string.close_conversation);
        }
    }

    private void updateHeaderView() {
        if (mCurrentChannel == null) {
            return;
        }

        UserItem headerUserItem = null;
        if (mIsAgent) {
            // InBox app
            if (mCurrentChannel.getGuests() != null && mCurrentChannel.getGuests().size() > 0) {
                headerUserItem = mCurrentChannel.getGuests().get(0);
            }
        } else {
            // Agent App
            headerUserItem = mCurrentChannel.getAssignee();
        }

        String displayName = "";
        // For new version of server
        if (mCurrentChannel != null && mCurrentChannel.displayName != null) {
            if (mIsAgent) {
                displayName = mCurrentChannel.displayName.admin.trim();
            } else {
                displayName = mCurrentChannel.displayName.guest.trim();
            }
        } else {
            // For old version of server
            if (headerUserItem != null) {
                displayName = headerUserItem.displayName.trim();
            } else {
                displayName = mCurrentChannel.orgName;
            }
        }

        if (StringUtil.isNotBlank(displayName)) {
            mNameTextView.setText(displayName);
            if (StringUtil.isNotBlank(displayName)) {
                mIconTextView.setText((displayName.charAt(0) + "").toUpperCase());
            }
        }

        if (headerUserItem == null) {
            return;
        }

        if (!StringUtil.isBlank(headerUserItem.iconUrl)) {
            Picasso.with(mIconImage.getContext()).load(headerUserItem.iconUrl).resize(70, 70).centerCrop().transform(new CircleTransformation()).into(mIconImage, new Callback() {
                @Override
                public void onSuccess() {
                    mIconImage.setBackgroundColor(getResources().getColor(R.color.color_chatcenter_background));
                    mIconImage.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError() {

                }
            });
        }

        mNameTextView.setText(headerUserItem.displayName);
        setUpEmailView(headerUserItem);
        mStatusTextView.setText(headerUserItem.online ? getString(R.string.status_online) :
                String.format(getString(R.string.online_at), getLastOnlineTime(headerUserItem.offlineAt)));
    }

    private void setUpEmailView(final UserItem user) {
        if (StringUtil.isNotBlank(user.email)) {
            mEmailTextView.setText(user.email);
            mEmailTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    composeEmail(new String[]{user.email}, "", null);
                }
            });
            mEmailTextView.setVisibility(View.VISIBLE);
        } else {
            mEmailTextView.setVisibility(View.GONE);
        }

        if (StringUtil.isNotBlank(user.facebookID)) {
            mFacebookTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (StringUtil.isNotBlank(user.facebookURL)) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(user.facebookURL));
                        startActivity(browserIntent);
                    }
                }
            });
            mFacebookTextView.setVisibility(View.VISIBLE);
        } else {
            mFacebookTextView.setVisibility(View.GONE);
        }

        if (StringUtil.isNotBlank(user.twitterID)) {
            mTwitterTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (StringUtil.isNotBlank(user.twitterURL)) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(user.twitterURL));
                        startActivity(browserIntent);
                    }
                }
            });

            mTwitterTextView.setVisibility(View.VISIBLE);
        } else {
            mTwitterTextView.setVisibility(View.GONE);
        }

        if (mTwitterTextView.getVisibility() == View.GONE
                && mFacebookTextView.getVisibility() == View.GONE
                && mEmailTextView.getVisibility() == View.VISIBLE) {
            mEmailTextViewIcon.setVisibility(View.VISIBLE);
            mEmailTextViewIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    composeEmail(new String[]{user.email}, "", null);
                }
            });
        } else {
            mEmailTextViewIcon.setVisibility(View.GONE);
        }
    }

    private void composeEmail(String[] addresses, String subject, Uri attachment) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("*/*");
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_STREAM, attachment);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void updateAssigneesView() {
        UserItem assigneeUser = mCurrentChannel.getAssignee();

        mAssigneesView.removeAllViews();

        if (assigneeUser == null) {
            mAssigneesView.addView(createSimpleTextView(getString(R.string.no_assignee)));
        } else {
            mAssigneesView.addView(createAvaLayout(assigneeUser.displayName, assigneeUser.iconUrl));
            mAssigneesView.addView(createSimpleTextView(assigneeUser.displayName));
        }
    }

    private void updateFollowersView() {
        mFollowersView.removeAllViews();
        mFollowers.clear();

        int numberOfFollowers = 0;
        for (UserItem user : mCurrentChannel.users) {
            if (user.admin) {
                if (mCurrentChannel.assignee != null && user.id.equals(mCurrentChannel.assignee.id)) {
                    continue;
                } else {
                    mFollowersView.addView(createAvaLayout(user.displayName, user.iconUrl));
                    numberOfFollowers += 1;
                    mFollowers.add(user);
                }
            }
        }

        if (numberOfFollowers == 0) {
            mFollowersView.addView(createSimpleTextView(getString(R.string.no_followers)));
        }
    }

    private RelativeLayout createAvaLayout(String userName, String iconUrl) {
        int avaSizeInPixel = getResources().getDimensionPixelSize(R.dimen.assignee_ava_size);

        RelativeLayout rlAva = new RelativeLayout(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(avaSizeInPixel, avaSizeInPixel);
        params.setMargins(0, 0, (int) convertDpToPixel(10, this), 0);

        rlAva.setLayoutParams(params);

        TextView circleTextIcon = createCircleTextView(userName);

        rlAva.addView(circleTextIcon);
        if (!StringUtil.isBlank(iconUrl)) {
            rlAva.addView(createImageView(iconUrl, circleTextIcon));
        }

        return rlAva;
    }

    private ImageView createImageView(String iconUrl, final TextView circleTextIcon) {
        final ImageView imv = new ImageView(this);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        imv.setLayoutParams(params);

        Picasso.with(imv.getContext()).load(iconUrl).resize(70, 70).centerCrop().transform(new CircleTransformation()).into(imv, new Callback() {
            @Override
            public void onSuccess() {
                imv.setBackgroundColor(Color.TRANSPARENT);
                circleTextIcon.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError() {

            }
        });


        return imv;
    }

    private TextView createCircleTextView(String name) {
        TextView circleTextView = new TextView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        circleTextView.setLayoutParams(params);
        circleTextView.setBackgroundResource(R.drawable.shape_chat_client_icon_textview);
        circleTextView.setGravity(Gravity.CENTER);
        circleTextView.setTextColor(Color.WHITE);
        circleTextView.setIncludeFontPadding(false);

        circleTextView.setText(name.toUpperCase().charAt(0) + "");

        return circleTextView;
    }

    private TextView createSimpleTextView(String text) {
        TextView simpleTextView = new TextView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        simpleTextView.setLayoutParams(params);
        simpleTextView.setGravity(Gravity.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            simpleTextView.setTextColor(getResources().getColor(R.color.color_chatcenter_text, null));
        } else {
            simpleTextView.setTextColor(getResources().getColor(R.color.color_chatcenter_text));
        }

        simpleTextView.setText(text);
        return simpleTextView;
    }

    public void onAboutButtonClicked(View view) {
        Intent intent = new Intent(InfoActivity.this, AboutActivity.class);
        startActivity(intent);
    }

    public void onAssigneeItemClicked(View view) {
        AssigneeFollowersUsersActivity.startSettingAssignee(this, mOrgUid, mChannelUid);
    }

    public void onFollowersItemClicked(View view) {
        AssigneeFollowersUsersActivity.startSettingFollower(this, mOrgUid, mChannelUid, REQUEST_SETUP_FOLLOW);
    }

    public void onFunnelItemClicked(View view) {
        Intent intent = new Intent(this, FunnelActivity.class);
        intent.putExtra(ChatCenterConstants.Extra.FUNNEL_LIST, mFunnels);
        startActivityForResult(intent, REQUEST_SETUP_FUNNEL);
    }

    public void onNoteButtonClicked(View view) {
        Intent intent = new Intent(this, NoteActivity.class);
        if (mCurrentChannel.note != null) {
            intent.putExtra(NoteActivity.CURRENT_NOTE_CONTENT, mCurrentChannel.note.content);
        }
        startActivityForResult(intent, REQUEST_SETUP_NOTE);
    }

    public void onCloseButtonClicked(View view) {
        if (mCurrentChannel == null) {
            return;
        }
        String message = mCurrentChannel.isClosed() ?
                getString(R.string.confirm_open_conversation) : getString(R.string.confirm_close_conversation);

        showConfirmDialog(message,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (mCurrentChannel.isClosed()) {
                            requestOpenChannel(mChannelUid);
                        } else {
                            requestCloseChannel(mChannelUid);
                        }
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    public void onDeleteButtonClicked(View view) {
        if (mCurrentChannel == null) {
            return;
        }

        String message = getString(R.string.confirm_delete_conversation);

        showConfirmDialog(message,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        requestDelete(mChannelUid);
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            /*if (requestCode == REQUEST_SETUP_ASSIGNEE) {
                ArrayList<UserItem> users = (ArrayList<UserItem>) data.getSerializableExtra("result");
                setUpAssignee(users);
            } else */
            if (requestCode == REQUEST_SETUP_FOLLOW) {
                ArrayList<UserItem> users = (ArrayList<UserItem>) data.getSerializableExtra("result");
                setUpFollowers(users);
            } else if (REQUEST_SETUP_FUNNEL == requestCode) {
                FunnelItem funnel = (FunnelItem) data.getSerializableExtra("result");
                setUpFunnel(funnel);
            } else if (REQUEST_SETUP_NOTE == requestCode) {
                String content = data.getStringExtra("result").trim();

                if (mCurrentChannel.note == null
                        || (mCurrentChannel.note != null && !content.equals(mCurrentChannel.note.content))) {
                    setupNote(content);
                }
            }
        }
    }

    private void setupNote(String content) {
        requestUpdateNote(content);
    }

    private int mFollowResponse = 0;
    private int mFollowRequest = 0;
    private void setUpFollowers(List<UserItem> users) {
        List<UserItem> newFollowers = getNewFollowers(mFollowers, users);
        List<UserItem> unfollowers = getUnfollowers(mFollowers, users);

        mFollowRequest = newFollowers.size() + unfollowers.size();

        if (mFollowRequest > 0) {
            DialogUtil.showProgressDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);

            // Un-follow if need
            for (UserItem user : unfollowers) {
                requestPostChannelUnFollow(mChannelUid, user);
            }

            // Follow if need
            for (UserItem user : newFollowers) {
                requestPostChannelFollow(mChannelUid, user);
            }
        }
    }

    private boolean isUserInList(List<UserItem> users, UserItem user) {
        for (UserItem item: users) {
            if (item.id.equals(user.id)) {
                return true;
            }
        }
        return false;
    }

    private List<UserItem> getNewFollowers(List<UserItem> oldFollowers, List<UserItem> currentFollowers) {
        List<UserItem> newFollowers = new ArrayList<>();
        for(UserItem userItem: currentFollowers) {
            if (!isUserInList(oldFollowers, userItem)) {
                newFollowers.add(userItem);
            }
        }

        return newFollowers;
    }

    private List<UserItem> getUnfollowers(List<UserItem> oldFollowers, List<UserItem> currentFollowers) {
        List<UserItem> unfollowers = new ArrayList<>();
        for (UserItem userItem: oldFollowers) {
            if (!isUserInList(currentFollowers, userItem)) {
                unfollowers.add(userItem);
            }
        }
        return unfollowers;
    }

    private void setUpFunnel(FunnelItem funnel) {
        mTvFunnel.setText(funnel.name);
        requestPostFunnel(funnel.id + "");
    }

    private String getLastOnlineTime(long offlineAt) {
        String time = "";
        Date offlineDate = new Date();
        offlineDate.setTime(offlineAt * 1000);

        Date currentDate = new Date();

        long different = currentDate.getTime() - offlineDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        long monthsInMilli = daysInMilli * 30;
        long yearsInMilli = daysInMilli * 365;

        long elapsedYears = different / yearsInMilli;
        different = different % yearsInMilli;

        long elapsedMonths = different / monthsInMilli;
        different = different % monthsInMilli;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        if (elapsedYears > 0) {
            if (elapsedYears > 1) {
                time = elapsedYears + " " + getString(R.string.years);
            } else {
                time = elapsedYears + " " + getString(R.string.year);
            }
        } else if (elapsedMonths > 0) {
            if (elapsedMonths > 1) {
                time = elapsedMonths + " " + getString(R.string.months);
            } else {
                time = elapsedMonths + " " + getString(R.string.month);
            }
        } else if (elapsedDays > 0) {
            if (elapsedDays > 1) {
                time = elapsedDays + " " + getString(R.string.days);
            } else {
                time = elapsedDays + " " + getString(R.string.day);
            }
        } else if (elapsedHours > 0) {
            if (elapsedHours > 1) {
                time = elapsedHours + " " + getString(R.string.hours);
            } else {
                time = elapsedHours + " " + getString(R.string.hour);
            }
        } else if (elapsedMinutes > 0) {
            if (elapsedMinutes > 1) {
                time = elapsedMinutes + " " + getString(R.string.minutes);
            } else {
                time = elapsedMinutes + " " + getString(R.string.minute);
            }
        } else if (elapsedSeconds > 0) {
            if (elapsedSeconds > 1) {
                time = elapsedSeconds + " " + getString(R.string.seconds);
            } else {
                time = elapsedSeconds + " " + getString(R.string.second);
            }
        }

        return time;
    }

    /**
     * POST /api/channels/:channel_uid/follow
     */
    private void requestPostChannelFollow(String channelUid, UserItem user) {

        String path = "channels/" + channelUid + "/follow";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

        Map<String, String> params = new HashMap<>();
        params.put("user_id", user.id + "");

        ApiRequest<PostChannelsResponseDto> postChannelFollowRequest = new OkHttpApiRequest<>(this,
                ApiRequest.Method.POST, path, params, headers, new PostFollowCallback(), new PostChannelsParser());

        if (mParamDto.appToken != null) {
            postChannelFollowRequest.setApiToken(mParamDto.appToken);
        }
        NetworkQueueHelper.enqueue(postChannelFollowRequest, REQUEST_TAG);
    }

    /**
     * POST /api/channels/:channel_uid/unfollow
     */
    private void requestPostChannelUnFollow(String channelUid, UserItem user) {

        String path = "channels/" + channelUid + "/unfollow";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

        Map<String, String> params = new HashMap<>();
        params.put("user_id", user.id + "");

        ApiRequest<PostChannelsResponseDto> postChannelFollowRequest = new OkHttpApiRequest<>(this,
                ApiRequest.Method.POST, path, params, headers, new PostUnFollowCallback(), new PostChannelsParser());

        if (mParamDto.appToken != null) {
            postChannelFollowRequest.setApiToken(mParamDto.appToken);
        }
        NetworkQueueHelper.enqueue(postChannelFollowRequest, REQUEST_TAG);
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
        headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

        mGetChannelsRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.GET, path, null, headers,
                new GetChannelCallback(), new PostChannelsParser());

        if (mParamDto.appToken != null) {
            mGetChannelsRequest.setApiToken(mParamDto.appToken);
        }
        NetworkQueueHelper.enqueue(mGetChannelsRequest, REQUEST_TAG);
        DialogUtil.showProgressDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
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
        headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

        mGetFunnelsRequest = new OkHttpApiRequest<>(this, ApiRequest.Method.GET, path, null, headers,
                new GetFunnelsCallback(), new GetFunnelsParser());

        if (mParamDto.appToken != null) {
            mGetFunnelsRequest.setApiToken(mParamDto.appToken);
        }

        NetworkQueueHelper.enqueue(mGetFunnelsRequest, REQUEST_TAG);
    }

    /** api/channels/close*/
    private void requestCloseChannel(String channelUid) {
        if (mPostChannelRequest != null) {
            return;
        }
        String path = "channels/close";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

        Map<String, String> params = new HashMap<>();
        params.put("channel_uids[]", channelUid);

        mPostChannelRequest = new OkHttpApiRequest<>(this,
                ApiRequest.Method.POST, path, params, headers, new PostCloseChannelCallback(), new PostChannelsParser());

        if (mParamDto.appToken != null) {
            mPostChannelRequest.setApiToken(mParamDto.appToken);
        }
        NetworkQueueHelper.enqueue(mPostChannelRequest, REQUEST_TAG);
        DialogUtil.showProgressDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
    }

    /** api/channels/open*/
    private void requestOpenChannel(String channelUid) {
        if (mPostChannelRequest != null) {
            return;
        }
        String path = "channels/open";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

        Map<String, String> params = new HashMap<>();
        params.put("channel_uids[]", channelUid);

        mPostChannelRequest = new OkHttpApiRequest<>(this,
                ApiRequest.Method.POST, path, params, headers, new PostCloseChannelCallback(), new PostChannelsParser());

        if (mParamDto.appToken != null) {
            mPostChannelRequest.setApiToken(mParamDto.appToken);
        }
        NetworkQueueHelper.enqueue(mPostChannelRequest, REQUEST_TAG);
        DialogUtil.showProgressDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
    }

    /**
     * POST /api/channels/funnels
     */
    private void requestPostFunnel(String funnel_id) {
        String path = "channels/funnels";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

        Map<String, String> params = new HashMap<>();
        params.put("channel_uid", mCurrentChannel.uid);
        params.put("funnel_id", funnel_id);

        ApiRequest<PostChannelsResponseDto> postFunnelRequest = new OkHttpApiRequest<>(this,
                ApiRequest.Method.POST, path, params, headers, new PostFunnelCallback(), new PostChannelsParser());

        if (mParamDto.appToken != null) {
            postFunnelRequest.setApiToken(mParamDto.appToken);
        }

        NetworkQueueHelper.enqueue(postFunnelRequest, REQUEST_TAG);
    }

    /** PATCH /api/channels/:channel_uid*/
    private void requestUpdateNote(String content) {
        if (mPostChannelRequest != null) {
            return;
        }
        String path = "channels/" + mChannelUid;

        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

        mPostChannelRequest = new OkHttpApiRequest<>(this,
                ApiRequest.Method.PATCH, path, headers, headers, new PostFunnelCallback(), new PostChannelsParser());

        if (mParamDto.appToken != null) {
            mPostChannelRequest.setApiToken(mParamDto.appToken);
        }

        Gson gson = new Gson();
        ChannelItem.Note note = new ChannelItem.Note();
        note.content = content;
        JsonObject params = new JsonObject();
        params.add("note", gson.toJsonTree(note));
        String jsonBody = params.toString();

        mPostChannelRequest.setJsonBody(jsonBody);
        NetworkQueueHelper.enqueue(mPostChannelRequest, REQUEST_TAG);
        // DialogUtil.showProgressDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
    }

    /** DELETE /api/channels/:channel_uid*/
    private void requestDelete(String channelUid) {
        if (mPostChannelRequest != null) {
            return;
        }
        String path = "channels/" + channelUid;

        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", CCAuthUtil.getUserToken(getApplicationContext()));

        mPostChannelRequest = new OkHttpApiRequest<>(this,
                ApiRequest.Method.DELETE, path, headers, headers, new DeleteChannelCallback(), new PostChannelsParser());

        if (mParamDto.appToken != null) {
            mPostChannelRequest.setApiToken(mParamDto.appToken);
        }

        NetworkQueueHelper.enqueue(mPostChannelRequest, REQUEST_TAG);
        DialogUtil.showProgressDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
    }

    /**
     * ダイアログをキャンセルした際のコールバック。
     *
     * @param tag このフラグメントのタグ
     */
    @Override
    public void onDialogCancel(String tag) {

    }

    /**
     * ダイアログの肯定ボタンを押下した際のコールバック。
     *
     * @param tag このフラグメントのタグ
     */
    @Override
    public void onPositiveButtonClick(String tag) {

    }

    /**
     * GET /api/channels のコールバック
     */
    private class GetChannelCallback implements OkHttpApiRequest.Callback<PostChannelsResponseDto> {
        @Override
        public void onError(OkHttpApiRequest.Error error) {
            mGetChannelsRequest = null;
            DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
        }

        @Override
        public void onSuccess(PostChannelsResponseDto responseDto) {
            DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);

            mGetChannelsRequest = null;

            if (responseDto == null) {
                return;
            }


            mTbChannel.updateOrInsert(responseDto);
            mCurrentChannel = mTbChannel.getChannel(mOrgUid, mChannelUid);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateView();
                }
            });

            requestGetFunnels();
        }
    }

    /**
     * POST /api/channels/:channel_uid/follow のコールバック
     */
    private class PostFollowCallback implements OkHttpApiRequest.Callback<PostChannelsResponseDto> {
        @Override
        public void onError(OkHttpApiRequest.Error error) {
            updateFollowView();
        }

        @Override
        public void onSuccess(PostChannelsResponseDto responseDto) {
            mCurrentChannel = responseDto;
            updateFollowView();
        }
    }

    /**
     * POST /api/channels/:channel_uid/unfollow のコールバック
     */
    private class PostUnFollowCallback implements OkHttpApiRequest.Callback<PostChannelsResponseDto> {
        @Override
        public void onError(OkHttpApiRequest.Error error) {
            updateFollowView();
        }

        @Override
        public void onSuccess(PostChannelsResponseDto responseDto) {
            mTbChannel.updateOrInsert(responseDto);
            mCurrentChannel = mTbChannel.getChannel(mOrgUid, mChannelUid);
            updateFollowView();
        }
    }

    private void updateFollowView() {
        mFollowResponse += 1;
        if (mFollowResponse == mFollowRequest) {
            mFollowResponse = 0;
            DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);

            requestGetChannel(mChannelUid);
        }
    }

    /**
     * POST /api/channels/funnels のコールバック
     */
    private class PostCloseChannelCallback implements OkHttpApiRequest.Callback<PostChannelsResponseDto> {
        @Override
        public void onError(OkHttpApiRequest.Error error) {
            mPostChannelRequest = null;
            DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
        }

        @Override
        public void onSuccess(PostChannelsResponseDto responseDto) {
            mPostChannelRequest = null;
            DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
            if (responseDto == null) {
                return;
            }

            mTbChannel.updateOrInsert(responseDto);
            mCurrentChannel = mTbChannel.getChannel(mOrgUid, mChannelUid);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentChannel != null && mCurrentChannel.isClosed()) {
                        Toast.makeText(InfoActivity.this, getString(R.string.conversation_closed), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(InfoActivity.this, getString(R.string.conversation_opened), Toast.LENGTH_SHORT).show();
                    }
                    updateView();
                }
            });
        }
    }

    /**
     * POST /api/channels/funnels のコールバック
     */
    private class PostFunnelCallback implements OkHttpApiRequest.Callback<PostChannelsResponseDto> {
        @Override
        public void onError(OkHttpApiRequest.Error error) {
            mPostChannelRequest = null;
        }

        @Override
        public void onSuccess(PostChannelsResponseDto responseDto) {
            mPostChannelRequest = null;
            mTbChannel.updateOrInsert(responseDto);
            mCurrentChannel = mTbChannel.getChannel(mOrgUid, mChannelUid);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateView();

                    for (FunnelItem funnel : mFunnels) {
                        if (funnel.id == mCurrentChannel.funnel_id) {
                            mTvFunnel.setText(funnel.name);
                            funnel.isSelected = true;
                        } else {
                            funnel.isSelected = false;
                        }
                    }
                }
            });
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
                mFunnels.clear();
                mFunnels.addAll(responseDto.funnels);

                for (FunnelItem funnel : mFunnels) {
                    if (funnel.id == mCurrentChannel.funnel_id) {
                        mTvFunnel.setText(funnel.name);
                        funnel.isSelected = true;
                        break;
                    }
                }
            }

        }
    }

    /**
     * DELETE /api/channels/:channel_uid のコールバック
     */
    private class DeleteChannelCallback implements OkHttpApiRequest.Callback<PostChannelsResponseDto> {
        @Override
        public void onError(OkHttpApiRequest.Error error) {
            mPostChannelRequest = null;
            DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);
            Toast.makeText(InfoActivity.this, getString(R.string.dialog_chat_delete_error_body), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(PostChannelsResponseDto responseDto) {
            mPostChannelRequest = null;
            DialogUtil.closeDialog(getSupportFragmentManager(), DialogUtil.Tag.PROGRESS);

            mTbChannel.deleteChannel(responseDto);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(InfoActivity.this, getString(R.string.conversation_deleted), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra(CHANNEL_DELETED, true);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }
    }

    private void showConfirmDialog (String message, DialogInterface.OnClickListener positiveListener,
                                    DialogInterface.OnClickListener negativeListener) {
        if (mConfirmDialog == null) {
            mConfirmDialog = new AlertDialog.Builder(this);
        }
        mConfirmDialog.setMessage(message);
        mConfirmDialog.setPositiveButton(getString(R.string.ok), positiveListener);
        mConfirmDialog.setNegativeButton(getString(R.string.cancel), negativeListener);
        mConfirmDialog.create().show();
    }
}