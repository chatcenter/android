package ly.appsocial.chatcenter.ws.parser;

import com.google.gson.Gson;

import ly.appsocial.chatcenter.dto.ws.response.PostUsersAuthResponseDto;
import ly.appsocial.chatcenter.ws.ApiRequest;

/**
 * [POST /api/channels] parser.
 */
public class PostUsersAuthParser implements ApiRequest.Parser<PostUsersAuthResponseDto> {

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
	public PostUsersAuthResponseDto parser(String response) {

		try {
			return new Gson().fromJson(response, PostUsersAuthResponseDto.class);
		} catch (Exception e) {
			mErrorCode = -1;
		}
		return null;
	}
}
