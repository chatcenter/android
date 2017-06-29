package ly.appsocial.chatcenter.dto.ws.request;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ly.appsocial.chatcenter.widgets.BasicWidget;

/**
 * [POST /api/channels/:channel_uid/messages] request.
 */
public class PostReplyInputWidgetDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////
	
	public BasicWidget.StickerAction.ActionData mActionData;

	public String answerLabel = "";

	public String replyTo = "";

	public String type;

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * @return
	 */
	public String toJson() {

		try {
			JSONObject contentObject = new JSONObject();
			if ( mActionData != null ){
				JSONObject answerObj = new JSONObject(new Gson().toJson(mActionData));
				if ( !answerObj.has("value") ){
					answerObj.put("value", new JSONObject());
				}

				if (!answerObj.has("input")) {
					answerObj.put("input", answerLabel);
				}
				contentObject.put("answer", answerObj);
			}

			contentObject.put("answer_label", answerLabel);
			contentObject.put("reply_to", replyTo);

			JSONObject rootObject = new JSONObject();
			rootObject.put("content", contentObject);
			rootObject.put("type", type);

			return rootObject.toString();
		} catch (JSONException e) {
			return "";
		}
	}
}
