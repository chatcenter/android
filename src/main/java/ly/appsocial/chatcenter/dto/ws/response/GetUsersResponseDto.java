package ly.appsocial.chatcenter.dto.ws.response;

import com.google.gson.annotations.SerializedName;

/**
 * [GET /api/users/:id] response.
 */
public class GetUsersResponseDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** モバイル番号 */
	@SerializedName("mobile_number")
	public String mobileNumber;
}
