package ly.appsocial.chatcenter.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserItem implements Parcelable, Serializable {
    /** ユーザーID */
    @SerializedName("id")
    public Integer id = 0;
    /** 表示名 */
    @SerializedName("display_name")
    public String displayName = "";
    /** アイコンURL */
    @SerializedName("icon_url")
    public String iconUrl = "";
    /** Admin */
    @SerializedName("admin")
    public boolean admin = false;
    /** ユーザートークン */
    @SerializedName("token")
    public String token = "";
    /** User online status */
    @SerializedName("online")
    public boolean online = false;
    @SerializedName("first_name")
    public String firstName = "";
    @SerializedName("family_name")
    public String familyName = "";
    @SerializedName("online_at")
    public Long onlineAt = 0L;
    @SerializedName("offline_at")
    public Long offlineAt = 0L;
    @SerializedName("email")
    public String email = "";
    @SerializedName("facebook_id")
    public String facebookID;
    @SerializedName("facebook_url")
    public String facebookURL;
    @SerializedName("twitter_id")
    public String twitterID;
    @SerializedName("twitter_url")
    public String twitterURL;
    @SerializedName("can_use_video_chat")
    public boolean isCanUseVideoChat;

    public UserItem() {

    }

    protected UserItem(Parcel in) {
        id = in.readInt();
        displayName = in.readString();
        iconUrl = in.readString();
        admin = in.readByte() != 0;
        token = in.readString();
        online = in.readByte() != 0;
        firstName = in.readString();
        familyName = in.readString();
        onlineAt = in.readLong();
        offlineAt = in.readLong();
        email = in.readString();
        facebookID = in.readString();
        facebookURL = in.readString();
        twitterID = in.readString();
        twitterURL = in.readString();
        isCanUseVideoChat = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(displayName);
        dest.writeString(iconUrl);
        dest.writeByte((byte) (admin ? 1 : 0));
        dest.writeString(token);
        dest.writeByte((byte) (online ? 1 : 0));
        dest.writeString(firstName);
        dest.writeString(familyName);
        dest.writeLong(onlineAt != null ? onlineAt : 0L);
        dest.writeLong(offlineAt != null ? offlineAt : 0L);
        dest.writeString(email);
        dest.writeString(facebookID);
        dest.writeString(facebookURL);
        dest.writeString(twitterID);
        dest.writeString(twitterURL);
        dest.writeByte((byte) (isCanUseVideoChat ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserItem> CREATOR = new Creator<UserItem>() {
        @Override
        public UserItem createFromParcel(Parcel in) {
            return new UserItem(in);
        }

        @Override
        public UserItem[] newArray(int size) {
            return new UserItem[size];
        }
    };
}
