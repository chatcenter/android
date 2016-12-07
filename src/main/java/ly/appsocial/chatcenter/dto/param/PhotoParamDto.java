package ly.appsocial.chatcenter.dto.param;

import android.os.Parcel;
import android.os.Parcelable;

import ly.appsocial.chatcenter.activity.ChatActivity;
import ly.appsocial.chatcenter.activity.PhotoActivity;

/**
 * {@link PhotoActivity} param.
 */
public class PhotoParamDto implements Parcelable {

	// //////////////////////////////////////////////////////////////////////////
	// staticフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** CREATOR */
	public static final Creator<PhotoParamDto> CREATOR = new Creator<PhotoParamDto>() {
		@Override
		public PhotoParamDto createFromParcel(final Parcel in) {
			return new PhotoParamDto(in);
		}

		@Override
		public PhotoParamDto[] newArray(final int size) {
			return new PhotoParamDto[size];
		}
	};

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** ファイル名 */
	public String fileName;
	/** 画像URL */
	public String url;

	/**
	 * コンストラクタ
	 * 
	 * @param url 画像URL
	 */
	public PhotoParamDto(String url) {
		this.url = url;
	}

	/**
	 * コンストラクタ
	 *
	 * @param in
	 */
	public PhotoParamDto(final Parcel in) {
		fileName = in.readString();
		url = in.readString();
	}

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {
		out.writeString(fileName);
		out.writeString(url);
	}
}