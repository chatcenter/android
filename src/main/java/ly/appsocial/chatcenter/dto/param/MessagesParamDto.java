package ly.appsocial.chatcenter.dto.param;

import android.os.Parcel;
import android.os.Parcelable;

import ly.appsocial.chatcenter.dto.ChannelItem;

public class MessagesParamDto implements Parcelable {

	// //////////////////////////////////////////////////////////////////////////
	// staticフィールド
	// //////////////////////////////////////////////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** Email */
	public String email;
	/** Password */
	public String password;
	/** Provider */
	public String provider;
	/** Providerトークン */
	public String providerToken;
	/** Providerトークン作成タイムスタンプ(ms) */
	public long providerTokenCreateAt;
	/**  */
	public long providerTokenExpires;
	/** */
	public ChannelItem.ChannelType channelType;
	/** */
	public ChannelItem.ChannelStatus channelStatus;
	/** */
	public String deviceToken;

	public boolean isAgent = false;

	public int funnelId;

	/**
	 * コンストラクタ
	 */
	public MessagesParamDto() {
	}

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

	protected MessagesParamDto(Parcel in) {
		provider = in.readString();
		providerToken = in.readString();
		providerTokenCreateAt = in.readLong();
		providerTokenExpires = in.readLong();
		deviceToken = in.readString();

		int ordinal = in.readInt();
		channelType = ordinal < 0 ? null : ChannelItem.ChannelType.values()[ordinal];

		ordinal = in.readInt();
		channelStatus = ordinal < 0 ? null: ChannelItem.ChannelStatus.values()[ordinal];

		email = in.readString();
		password = in.readString();

		isAgent = in.readByte() == 0;

		funnelId = in.readInt();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(provider);
		dest.writeString(providerToken);
		dest.writeLong(providerTokenCreateAt);
		dest.writeLong(providerTokenExpires);
		dest.writeString(deviceToken);
		dest.writeInt(channelType == null ? -1 : channelType.ordinal());
		dest.writeInt(channelStatus == null ? -1 : channelStatus.ordinal());
		dest.writeString(email);
		dest.writeString(password);
		dest.writeByte((byte) (isAgent ? 0 : 1));
		dest.writeInt(funnelId);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<MessagesParamDto> CREATOR = new Creator<MessagesParamDto>() {
		@Override
		public MessagesParamDto createFromParcel(Parcel in) {
			return new MessagesParamDto(in);
		}

		@Override
		public MessagesParamDto[] newArray(int size) {
			return new MessagesParamDto[size];
		}
	};
}