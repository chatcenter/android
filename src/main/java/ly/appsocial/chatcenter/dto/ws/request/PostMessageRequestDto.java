package ly.appsocial.chatcenter.dto.ws.request;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * [POST /api/channels/:channel_uid/messages] request.
 */
public class PostMessageRequestDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** テキスト */
	public String text;

	/** Temporary Uid */
	public String tmpUid;

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * @return
	 */
	public String toJson() {
		try {
			JSONObject contentObject = new JSONObject();
			contentObject.put("text", text);
			contentObject.put("uid", tmpUid);

			JSONObject rootObject = new JSONObject();
			rootObject.put("content", contentObject);

			return rootObject.toString();
		} catch (JSONException e) {
			return "";
		}
	}
}
