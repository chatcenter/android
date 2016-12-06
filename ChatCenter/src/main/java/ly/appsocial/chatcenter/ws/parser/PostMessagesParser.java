package ly.appsocial.chatcenter.ws.parser;


import com.google.gson.Gson;

import ly.appsocial.chatcenter.dto.ws.response.PostMessagesResponseDto;
import ly.appsocial.chatcenter.ws.ApiRequest;

/**
 * [POST /api/channels/:channel_uid/messages] parser.
 */
public class PostMessagesParser implements ApiRequest.Parser<PostMessagesResponseDto> {

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
	public PostMessagesResponseDto parser(String response) {

		try {
			return new Gson().fromJson(response, PostMessagesResponseDto.class);
		} catch (Exception e) {
			mErrorCode = -1;
		}
		return null;
	}
}
