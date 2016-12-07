package ly.appsocial.chatcenter.dto.ws.request;

import java.util.HashMap;
import java.util.Map;

/**
 * [POST /api/devices] request.
 */
public class PostDevicesRequestDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////
	/** Device Token */
	public String deviceToken;
	/** Device type (Android) */
	private String deviceType = "android";

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////


	/**
	 * リクストパラメータを生成します。
	 *
	 * @return リクエストパラメータ
	 */
	public Map<String, String> toParams() {
		Map<String, String> params = new HashMap<String, String>();

		// For push notification
		if (deviceToken != null) {
			params.put("device_type", deviceType);
			params.put("device_token", deviceToken);
		}
		return params;
	}

}
