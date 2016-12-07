package ly.appsocial.chatcenter.ws.parser;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import ly.appsocial.chatcenter.dto.ws.response.PostChannelsResponseDto;
import ly.appsocial.chatcenter.ws.ApiRequest;

/**
 * [POST /api/channels] parser.
 */
public class PostChannelsParser implements ApiRequest.Parser<PostChannelsResponseDto> {

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
	public PostChannelsResponseDto parser(String response) {
		try {
			Object json = new JSONTokener(response).nextValue();
			if (json instanceof JSONObject) {
				return new Gson().fromJson(response, PostChannelsResponseDto.class);
			} else if (json instanceof JSONArray) {
				JsonParser jsonParser = new JsonParser();
				JsonArray jsonArray = (JsonArray) jsonParser.parse(response);
				return new Gson().fromJson(jsonArray.get(0).toString(), PostChannelsResponseDto.class);
			}
		} catch (Exception e) {
			mErrorCode = -1;
		}
		return null;
	}
}
