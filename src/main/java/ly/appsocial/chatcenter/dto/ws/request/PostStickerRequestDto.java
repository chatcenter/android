package ly.appsocial.chatcenter.dto.ws.request;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * [POST /api/channels/:channel_uid/messages] request.
 */
public class PostStickerRequestDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	public String type;
	public String content;
	public String tmpUid;

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * @return
	 */
	public String toJson() {
		try {

			JSONObject contentObject = new JSONObject(content);
			contentObject.put("uid", tmpUid);

			JSONObject rootObject = new JSONObject();
			rootObject.put("content", contentObject);
			rootObject.put("type", type);

			return rootObject.toString();
		} catch (JSONException e) {
			return "";
		}
	}
}
