package ly.appsocial.chatcenter.dto.ws.request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * [WebSocket subscribe] request.
 */
public class WsTypingRequest {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** チャンネルUID */
	public String channelUid;

	/** ユーザーID*/
	public int userId;

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * @return
	 */
	public String toJson() {
		try {
			JSONObject dataObject = new JSONObject();
			dataObject.put("channel_uid", channelUid);

			JSONObject userObject = new JSONObject();
			userObject.put("id", userId);

			dataObject.put("user", userObject);

			JSONArray jsonArray = new JSONArray();
			jsonArray.put("message:typing");
			jsonArray.put(dataObject);

			return jsonArray.toString();
		} catch (JSONException e) {
			return "";
		}
	}
}
