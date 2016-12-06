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
public class PostStickerResponseRequestDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	public String type;

	public BasicWidget.StickerAction.ActionData answer;

	public List<BasicWidget.StickerAction.ActionData> answers;

	public String answerLabel = "";

	public String replyTo = "";

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * @return
	 */
	public String toJson() {
		if ( answer != null && ( answers == null || answers.isEmpty() )) {
			answers = new ArrayList<>();
			answers.add(answer);
		}

		try {
			JSONObject contentObject = new JSONObject();
			if ( answer != null ){
				JSONObject answerObj = new JSONObject(new Gson().toJson(answer));
				if ( !answerObj.has("value") ){
					answerObj.put("value", new JSONObject());
				}
				contentObject.put("answer", answerObj);
			}
			if ( answers != null ){
				contentObject.put("answers", new JSONArray(new Gson().toJson(answers)));
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
