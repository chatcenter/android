package ly.appsocial.chatcenter.ws.parser;

import com.google.gson.Gson;

import ly.appsocial.chatcenter.dto.ws.response.GetUsersResponseDto;
import ly.appsocial.chatcenter.ws.ApiRequest;

/**
 * [GET /api/users/:id] parser.
 */
public class GetUsersParser implements ApiRequest.Parser<GetUsersResponseDto> {

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
	public GetUsersResponseDto parser(String response) {
		try {
			return new Gson().fromJson(response, GetUsersResponseDto.class);
		} catch (Exception e) {
			mErrorCode = -1;
		}
		return null;
	}
}
