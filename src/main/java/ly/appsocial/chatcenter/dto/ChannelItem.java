package ly.appsocial.chatcenter.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ChannelItem implements Serializable {

    public enum ChannelType {
        CHANNEL_ALL, CHANNEL_ARCHIVE, CHANNEL_UNARCHIVE
    }

    public enum ChannelStatus {
        CHANNEL_UNASSIGNED, CHANNEL_ASSIGNED, CHANNEL_CLOSE, CHANNEL_ALL
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

    public UserItem getGuest() {
        for (UserItem user : users) {
            if (!user.admin) {
                return user;
            }
        }
        return null;
    }

    public boolean isAssigneeOnline() {
        UserItem user = getAssignee();
        return user != null && user.online;
    }

    public ChannelStatus getChannelStatus() {
        if (statusString.equals(CHANNEL_UNASSIGNED)) {
            return ChannelStatus.CHANNEL_UNASSIGNED;
        } else if (statusString.equals(CHANNEL_ASSIGNED)) {
            return ChannelStatus.CHANNEL_ASSIGNED;
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
}
