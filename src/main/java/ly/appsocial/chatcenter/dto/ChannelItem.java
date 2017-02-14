package ly.appsocial.chatcenter.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    /**
     * 店舗名
     */
    @SerializedName("org_name")
    public String orgName;
    /**
     * Assignee
     */
    @SerializedName("assignee")
    public UserItem assignee;
    /**
     * Status
     */
    @SerializedName("status")
    public String statusString;
    /**
     * Users
     */
    @SerializedName("users")
    public List<UserItem> users;
    /**
     * IconUrl
     */
    @SerializedName("icon_url")
    public String iconUrl;

    /**
     * Funnel id
     */
    @SerializedName("funnel_id")
    public int funnel_id;

    /**
     * Note
     */
    @SerializedName("note")
    public Note note;

    @SerializedName("display_name")
    public DisplayName displayName;

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

    public List<UserItem> getGuests() {
        List<UserItem> guests = new ArrayList<>();
        for (UserItem user : users) {
            if (!user.admin) {
                guests.add(user);
            }
        }
        return guests;
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

    public ChannelStatus getChannelStatus() {
        if (statusString.equals(CHANNEL_UNASSIGNED)) {
            return ChannelStatus.CHANNEL_UNASSIGNED;
        } else if (statusString.equals(CHANNEL_ASSIGNED)) {
            return ChannelStatus.CHANNEL_ASSIGNED_TO_ME;
        } else if (statusString.equals(CHANNEL_CLOSED)) {
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
}
