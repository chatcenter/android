package ly.appsocial.chatcenter.ws.parser;


import com.google.gson.Gson;

import ly.appsocial.chatcenter.dto.ws.response.PostUsersResponseDto;
import ly.appsocial.chatcenter.ws.ApiRequest;

/**
 * [POST /api/users] parser.
 */
public class PostUsersParser implements ApiRequest.Parser<PostUsersResponseDto> {

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
	public PostUsersResponseDto parser(String response) {

		try {
			return new Gson().fromJson(response, PostUsersResponseDto.class);
		} catch (Exception e) {
			mErrorCode = -1;
		}
		return null;
	}
}
