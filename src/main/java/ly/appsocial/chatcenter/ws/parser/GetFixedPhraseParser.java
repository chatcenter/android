package ly.appsocial.chatcenter.ws.parser;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.dto.ws.response.GetFixedPhraseResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.GetMessagesResponseDto;
import ly.appsocial.chatcenter.widgets.BasicWidget;
import ly.appsocial.chatcenter.ws.ApiRequest;

/**
 * [GET /api/fixed_phrases] parser.
 */
public class GetFixedPhraseParser implements ApiRequest.Parser<GetFixedPhraseResponseDto> {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** エラーコード */
	private int mErrorCode;

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

	@Override
	public int getErrorCode() {
		return mErrorCode;
	}

	@Override
	public GetFixedPhraseResponseDto parser(String response) {
		GetFixedPhraseResponseDto resp = new GetFixedPhraseResponseDto();
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONArray orgArray = jsonObject.getJSONArray("org");
			JSONArray userArray = jsonObject.getJSONArray("user");
			JSONArray appArray = jsonObject.getJSONArray("app");

			resp.orgFixedPhrases = getChatItemFromJsonArray(orgArray);
			resp.userFixedPhrases = getChatItemFromJsonArray(userArray);
			resp.appFixedPhrases = getChatItemFromJsonArray(appArray);

		} catch (Exception e) {
			mErrorCode = -1;
		}
		return resp;
	}

	private List<ChatItem> getChatItemFromJsonArray(JSONArray itemArray) {
		if (itemArray == null) {
			return null;
		}
		List<ChatItem> items = new ArrayList<>();
		try {
			for (int i = 0; i < itemArray.length(); i++ ){
				JSONObject obj = itemArray.getJSONObject(i);
				if ( obj != null ){
					String contentType = obj.getString("content_type");
					if (contentType.equals(ResponseType.STICKER)){
						ChatItem item = new Gson().fromJson(obj.toString(), ChatItem.class);
						if (item != null) {
							item.setupContent(BasicWidget.class, obj);
							item.type = ResponseType.STICKER;
							items.add(item);
						}
					} else if (contentType.equals(ResponseType.TEXT)){
						ChatItem item = new ChatItem();
						BasicWidget widget = new BasicWidget();
						item.widget = widget;
						widget.message = new BasicWidget.Message();
						widget.message.text = obj.getString("content");
						item.type = ResponseType.MESSAGE;
						items.add(item);
					}
				}
			}
			return items;
		} catch (JSONException e){
			mErrorCode = -1;
			return null;
		}
	}
}
