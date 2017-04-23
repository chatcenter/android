package ly.appsocial.chatcenter.dto;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import ly.appsocial.chatcenter.util.NetworkQueueHelper;
import ly.appsocial.chatcenter.ws.ApiRequest;
import ly.appsocial.chatcenter.ws.OkHttpApiRequest;

public class OrgItem implements Parcelable {
    private static final String REQUEST_TAG = OrgItem.class.getCanonicalName();

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

    public int localId;

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

    /**
     * このチームはオンライですか？オフラインですか？確認します。
     */
    public void checkOrgStatus(Context context, final OrgStatusListener listener) {
        String path = String.format("orgs/%s/online", uid);
        OkHttpApiRequest request = new OkHttpApiRequest(context, ApiRequest.Method.GET,
                path,
                null,
                null,
                new ApiRequest.Callback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject responseDto) {
                        if (listener == null) {
                            return;
                        }

                        if(responseDto != null && !responseDto.isNull("online")) {
                            try {
                                boolean isOnline = responseDto.getBoolean("online");
                                if (isOnline) {
                                    listener.onOrgOnline();
                                } else {
                                    listener.onOrgOffline();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            listener.onOrgOffline();
                        }
                    }

                    @Override
                    public void onError(ApiRequest.Error error) {

                    }
                },
                new ApiRequest.Parser<JSONObject>() {
                    @Override
                    public int getErrorCode() {
                        return 0;
                    }

                    @Override
                    public JSONObject parser(String response) {
                        try {
                            return new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                });

        NetworkQueueHelper.enqueue(request, REQUEST_TAG);
    }

    public interface OrgStatusListener {
        void onOrgOnline();
        void onOrgOffline();
    }
}
