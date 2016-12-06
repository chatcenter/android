package ly.appsocial.chatcenter.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrgItem implements Parcelable {
    public OrgItem() {

    }

    @SerializedName("id")
    public int id;
    @SerializedName("name")
    public String name;
    @SerializedName("uid")
    public String uid;
    @SerializedName("icon_url")
    public String iconUrl;
    @SerializedName("address")
    public String address;
    @SerializedName("phone_number")
    public String phoneNumber;
    @SerializedName("unread_messages_channels")
    public List<String> unreadMessagesChannels;
    @SerializedName("users")
    public List<UserItem> users;

    protected OrgItem(Parcel in) {
        id = in.readInt();
        name = in.readString();
        uid = in.readString();
        iconUrl = in.readString();
        address = in.readString();
        phoneNumber = in.readString();
        unreadMessagesChannels = in.createStringArrayList();
        users = in.createTypedArrayList(UserItem.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(uid);
        dest.writeString(iconUrl);
        dest.writeString(address);
        dest.writeString(phoneNumber);
        dest.writeStringList(unreadMessagesChannels);
        dest.writeTypedList(users);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OrgItem> CREATOR = new Creator<OrgItem>() {
        @Override
        public OrgItem createFromParcel(Parcel in) {
            return new OrgItem(in);
        }

        @Override
        public OrgItem[] newArray(int size) {
            return new OrgItem[size];
        }
    };
}
