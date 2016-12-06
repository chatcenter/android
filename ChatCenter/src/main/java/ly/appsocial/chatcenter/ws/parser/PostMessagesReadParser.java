package ly.appsocial.chatcenter.ws.parser;


import com.google.gson.Gson;

import ly.appsocial.chatcenter.dto.ws.response.PostMessagesReadResponseDto;
import ly.appsocial.chatcenter.ws.ApiRequest;

/**
 * [POST /api/channels/:channel_uid/messages] read parser.
 */
public class PostMessagesReadParser implements ApiRequest.Parser<PostMessagesReadResponseDto> {

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
	public PostMessagesReadResponseDto parser(String response) {

		try {
			return new Gson().fromJson(response, PostMessagesReadResponseDto.class);
		} catch (Exception e) {
			mErrorCode = -1;
		}
		return null;
	}
}
