package ly.appsocial.chatcenter.dto.ws.request;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * [POST /api/channels/:channel_uid/messages] read request.
 */
public class PostMessageReadRequestDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** メッセージID */
	public List<Integer> messageIds = new ArrayList<Integer>();

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * @return
	 */
	public String toJson() {
		try {
			JSONObject contentObject = new JSONObject();
			JSONArray messagesArray = new JSONArray();
			for (int messageId : messageIds) {
				messagesArray.put(messageId);
			}
			contentObject.put("messages", messagesArray);

			JSONObject rootObject = new JSONObject();
			rootObject.put("type", "receipt");
			rootObject.put("content", contentObject);

			return rootObject.toString();
		} catch (JSONException e) {
			return "";
		}
	}
}
