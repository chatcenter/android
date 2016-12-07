package ly.appsocial.chatcenter.dto.ws.request;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ly.appsocial.chatcenter.R;

/**
 * [POST /api/users] request.
 */
public class PostUsersRequestDto {

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
	/** Providerトークン */
	public String providerToken;
	/** Providerトークン作成日時(yyyy/MM/dd HH:mm:ss) */
	public String providerTokenTimeStamp;
	/** Providerトークン作成日時(yyyy/MM/dd HH:mm:ss) */
	public String providerTokenExpires;
	/** KISコード */
	public String kissCd;
	/** Device Token */
	public String deviceToken;
	/** Device type (Android) */
	private String deviceType = "android";
	/** Channel information */
	public Map<String, String> channelInformations;

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Providerトークン作成日時を設定します。
	 *
	 * @param timestamp タイムスタンプ
	 */
	public void setProviderTokenCreateAt(Context context, long timestamp) {
		if (timestamp < 1) {
			providerTokenTimeStamp = null;
		} else {
			providerTokenTimeStamp = new SimpleDateFormat(context.getString(R.string.datetime_format_full))
					.format(new Date(timestamp));
		}
	}

	/**
	 * Providerトークン作成日時を設定します。
	 *
	 * @param timestamp タイムスタンプ
	 */
	public void setProviderExpires(Context context, long timestamp) {
		if (timestamp < 1) {
			providerTokenExpires = null;
		} else {
			providerTokenExpires = new SimpleDateFormat(context.getString(R.string.datetime_format_full))
					.format(new Date(timestamp));
		}
	}

	/**
	 * リクストパラメータを生成します。
	 *
	 * @return リクエストパラメータ
	 */
	public Map<String, String> toParams() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("org_uid", kissCd);
		if ( channelInformations != null ) {
			for (String key : channelInformations.keySet()) {
				params.put("channel_informations[" + key + "]", channelInformations.get(key));
			}
		}

		// Login by provider
		if (provider != null) {
			params.put("provider", provider);
			params.put("provider_token", providerToken);
			if (providerTokenTimeStamp != null) {
				params.put("provider_created_at", String.valueOf(providerTokenTimeStamp));
			}
			if (providerTokenExpires != null) {
				params.put("provider_expires_at", String.valueOf(providerTokenExpires));
			}
		}

		// For push notification
		if (deviceToken != null) {
			params.put("device_type", deviceType);
			params.put("device_token", deviceToken);
		}

		// Login by name and email
		if (firstName != null) {
			params.put("first_name", firstName);
		}
		if (familyName != null) {
			params.put("family_name", familyName);
		}
		if (email != null) {
			params.put("email", email);
		}

		// TODO Viable parameters for POST /users
		//    mobile_number	Mobile phone number	false
		//    email_opt_in	Email opt in(true/false)	false
		//    store_id	Store ID (Salesforce)	false
		return params;
	}

}
