package ly.appsocial.chatcenter.dto.ws.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ly.appsocial.chatcenter.dto.UserItem;

/**
 * [POST /api/users] response.
 */
public class PostUsersResponseDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** チャネルUID */
	@SerializedName("uid")
	public String uid;
	/** チャネル名 */
	@SerializedName("org_name")
	public String orgName;
	/** ユーザーリスト */
	@SerializedName("users")
	public List<User> users;

	/**
	 * ユーザー
	 */
	public static class User extends UserItem {
	}
}
