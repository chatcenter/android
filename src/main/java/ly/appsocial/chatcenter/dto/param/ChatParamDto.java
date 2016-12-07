package ly.appsocial.chatcenter.dto.param;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ly.appsocial.chatcenter.activity.ChatActivity;

/**
 * {@link ChatActivity} param.
 */
public class ChatParamDto implements Parcelable {

	// //////////////////////////////////////////////////////////////////////////
	// staticフィールド
	// //////////////////////////////////////////////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** User's firstname */
	public String firstName;
	/** User's familyname */
	public String familyName;
	/** User's email */
	public String email;
	/** Provider */
	public String provider;
	/** Token received from provider */
	public String providerToken;
	/** Token created timestamp */
	public long providerTokenTimestamp;
	/** Token expires timestamp */
	public long providerTokenExpires;
	/** Device token for Push Notification */
	public String deviceToken;
	/** チャネルUID */
	public String channelUid;
	/** org_name */
	public String channelName;
	/** KISSコード */
	public String kissCd;
	/** AppToken */
	public String appToken;
	/** Channel information */
	public Map<String, String> channelInformations;

	/**
	 * コンストラクタ
	 */
	public ChatParamDto() {
	}

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

	protected ChatParamDto(Parcel in) {
		provider = in.readString();
		providerToken = in.readString();
		providerTokenTimestamp = in.readLong();
		providerTokenExpires = in.readLong();
		deviceToken = in.readString();
		channelUid = in.readString();
		channelName = in.readString();
		kissCd = in.readString();
		appToken = in.readString();
		firstName = in.readString();
		familyName = in.readString();
		email = in.readString();

		channelInformations = new HashMap<>();
		List<String> keys = new ArrayList<>();
		List<String> values = new ArrayList<>();
		in.readStringList(keys);
		in.readStringList(values);
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = values.get(i);
			channelInformations.put(key, value);
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(provider);
		dest.writeString(providerToken);
		dest.writeLong(providerTokenTimestamp);
		dest.writeLong(providerTokenExpires);
		dest.writeString(deviceToken);
		dest.writeString(channelUid);
		dest.writeString(channelName);
		dest.writeString(kissCd);
		dest.writeString(appToken);
		dest.writeString(firstName);
		dest.writeString(familyName);
		dest.writeString(email);

		List<String> keys = new ArrayList<>();
		List<String> values = new ArrayList<>();
		if (channelInformations != null) {
			for (String key : channelInformations.keySet()) {
				keys.add(key);
				values.add(channelInformations.get(key));
			}
		}

		dest.writeStringList(keys);
		dest.writeStringList(values);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<ChatParamDto> CREATOR = new Creator<ChatParamDto>() {
		@Override
		public ChatParamDto createFromParcel(Parcel in) {
			return new ChatParamDto(in);
		}

		@Override
		public ChatParamDto[] newArray(int size) {
			return new ChatParamDto[size];
		}
	};
}