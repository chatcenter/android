package ly.appsocial.chatcenter.dto.ws.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ly.appsocial.chatcenter.dto.ChatItem;

/**
 * [GET /api/users/:id] response.
 */
public class GetFixedPhraseResponseDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** ORGの定型ステッカー */
	@SerializedName("org")
	public List<ChatItem> orgFixedPhrases;
	/** ユーザーが定義した定型ステッカー*/
	@SerializedName("user")
	public List<ChatItem> userFixedPhrases;
	/** 定型ステッカー*/
	@SerializedName("app")
	public List<ChatItem> appFixedPhrases;
}
