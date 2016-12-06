package ly.appsocial.chatcenter.dto.ws.request;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * [POST /api/users/auth] request.
 */
public class PostUsersAuthRequestDto {


	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////
	/** User's email */
	public String email;
	/** Ppassword */
	public String password;
	/** トークン */
	public String providerToken;
	/** トークン作成日時(yyyy/MM/dd HH:mm:ss) */
	public String providerTokenCreatedAt;
	/** Token expires date */
	public String providerTokenExpires;
	/** Provider */
	public String provider;
	/** Always android */
	private String deviceType = "android";
	/** Token for push notification */
	public String deviceToken;

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * トークン作成日時を設定します。
	 *
	 * @param timestamp タイムスタンプ
	 */
	public void setProviderTokenCreateAt(long timestamp) {
		if (timestamp < 1) {
			providerTokenCreatedAt = null;
		} else {
			providerTokenCreatedAt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(timestamp));
		}
	}
	public void setProviderTokenExpires(long timestamp) {
		if (timestamp < 1) {
			providerTokenExpires = null;
		} else {
			providerTokenExpires = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(timestamp));
		}
	}

	/**
	 * リクストパラメータを生成します。
	 *
	 * @return リクエストパラメータ
	 */
	public Map<String, String> toParams() {
		Map<String, String> params = new HashMap<String, String>();

		// Login by provide
		if (provider != null) {
			params.put("provider", provider);
			params.put("provider_token", providerToken);
			if (providerTokenCreatedAt != null) {
				params.put("provider_created_at", String.valueOf(providerTokenCreatedAt));
			}
			if (providerTokenExpires != null) {
				params.put("provider_expires_at", String.valueOf(providerTokenExpires));
			}
		}

		// Push token
		if (deviceToken != null) {
			params.put("device_type", deviceType);
			params.put("device_token", deviceToken);
		}

		// Login by email and password
		if (email != null) {
			params.put("email", email);
			params.put("password", password);
		}

		return params;
	}
}
