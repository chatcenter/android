package ly.appsocial.chatcenter.dto;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.util.StringUtil;

public class ChannelItem implements Serializable {

    public enum ChannelType {
        CHANNEL_ALL, CHANNEL_ARCHIVE, CHANNEL_UNARCHIVE
    }

    public enum ChannelStatus {
        CHANNEL_UNASSIGNED, CHANNEL_ASSIGNED_TO_ME, CHANNEL_CLOSE, CHANNEL_ALL
    }

    public static final String CHANNEL_UNASSIGNED = "unassigned";
    public static final String CHANNEL_ASSIGNED = "assigned";
    public static final String CHANNEL_CLOSED = "closed";
    public static final String CHANNEL_ALL = "all";

    /**
     * チャネルUID
     */
    @SerializedName("uid")
    public String uid;
    /** 作成日 */
    @SerializedName("created")
    public long created;
    /** KISSコード */
    @SerializedName("org_uid")
    public String orgUid;
    /** 店舗名 */
    @SerializedName("org_name")
    public String orgName;
    /** ステータス */
    @SerializedName("status")
    public String status;
    /** Users */
    @SerializedName("users")
    public List<UserItem> users;
    /** 未読メッセージ数 */
    @SerializedName("unread_messages")
    public int unreadMessages;
    @SerializedName("display_name")
    public DisplayName displayName;
    /** 最新メッセージ */
    @SerializedName("latest_message")
    public LatestMessage latestMessage;
    /** Channel's last updated at */
    @SerializedName("last_updated_at")
    public double lastUpdatedAt;
    /** Assignee */
    @SerializedName("assignee")
    public UserItem assignee;
    /** IconUrl*/
    @SerializedName("icon_url")
    public String iconUrl;
    /** Funnel id*/
    @SerializedName("funnel_id")
    public int funnel_id;
    /** Note */
    @SerializedName("note")
    public Note note;

    public int localId;

    /**
     * 最新メッセージ
     */
    public static class LatestMessage extends ChatItem {
    }

    public UserItem getAssignee() {
        if(assignee != null) {
            for (UserItem user : users) {
                if (user.id.equals(assignee.id)) {
                    return user;
                }
            }
        }

        return null;
    }

    /** Get list of guest user in channel*/
    public List<UserItem> getGuests() {
        List<UserItem> guests = new ArrayList<>();
        for (UserItem user : users) {
            if (!user.admin) {
                guests.add(user);
            }
        }
        return guests;
    }

    /** Get first Guest user in list of guests*/
    public UserItem getFirstGuest() {
        List<UserItem> guest = getGuests();
        if (guest != null && guest.size() > 0) {
            return guest.get(0);
        }

        return null;
    }

    public boolean isAdmin(Integer userId) {
        for (UserItem userItem: users) {
            if (userItem.id.equals(userId)) {
                return userItem.admin;
            }
        }

        return false;
    }

    public boolean isAssigneeOnline() {
        UserItem user = getAssignee();
        return user != null && user.online;
    }

    public boolean canUseVideoCall(boolean isAgent) {
        if (users == null || users.size() == 0) {
            return false;
        }
        if (isAgent) {
            // If there is one guest can use video chat, return true
            for (UserItem user: users) {
                if (!user.admin && user.isCanUseVideoChat) {
                    return true;
                }
            }
        } else {
            for (UserItem user: users) {
                if (user.admin && user.isCanUseVideoChat) {
                    return true;
                }
            }
        }
        return false;
    }

    public ChannelStatus getChannelStatus() {
        if (status.equals(CHANNEL_UNASSIGNED)) {
            return ChannelStatus.CHANNEL_UNASSIGNED;
        } else if (status.equals(CHANNEL_ASSIGNED)) {
            return ChannelStatus.CHANNEL_ASSIGNED_TO_ME;
        } else if (status.equals(CHANNEL_CLOSED)) {
            return ChannelStatus.CHANNEL_CLOSE;
        } else {
            return ChannelStatus.CHANNEL_ALL;
        }
    }

    public boolean isClosed() {
        return getChannelStatus() == ChannelStatus.CHANNEL_CLOSE;
    }

    public static class Note implements Serializable {
        @SerializedName("id")
        public int id;
        @SerializedName("content")
        public String content;
        @SerializedName("created_at")
        public String created_at;
        @SerializedName("updated_at")
        public String updated_at;

    }

    public static class DisplayName implements Serializable{
        /** The name that will display on guest's UI*/
        @SerializedName("guest")
        public String guest;

        /** The name that will display on agent's UI*/
        @SerializedName("admin")
        public String admin;
    }

    /** Get of channel to display*/
    public String getDisplayName(Context context, boolean isAgent) {
        String channelDisplayName;
        UserItem guest = getUserToDisplay(isAgent);
        if (isAgent) {
            if (displayName != null && StringUtil.isNotBlank(displayName.admin)) {
                channelDisplayName = displayName.admin;
            } else if (guest != null && StringUtil.isNotBlank(guest.displayName)) {
                channelDisplayName = guest.displayName;
            } else {
                channelDisplayName = context.getString(R.string.guest);
            }
        } else {
            if (displayName != null && StringUtil.isNotBlank(displayName.guest)) {
                channelDisplayName = displayName.guest;
            } else {
                channelDisplayName = orgName;
            }
        }

        return channelDisplayName;
    }

    /** Get user information to display on list*/
    public UserItem getUserToDisplay(boolean isAgentApp) {
        if (isAgentApp) {
            return getFirstGuest();
        } else {
            return assignee;
        }
    }
}
