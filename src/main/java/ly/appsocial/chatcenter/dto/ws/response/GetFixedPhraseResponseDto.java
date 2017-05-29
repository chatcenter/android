package ly.appsocial.chatcenter.dto.ws.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
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

	public List<ChatItem> getAllFixedPhrases() {
		List<ChatItem> fixedPhrases = new ArrayList<>();

		if (orgFixedPhrases != null) {
			for (ChatItem chatItem : orgFixedPhrases) {
				fixedPhrases.add(chatItem);
			}
		}

		if (userFixedPhrases != null) {
			for (ChatItem chatItem : userFixedPhrases) {
				fixedPhrases.add(chatItem);
			}
		}

		if (appFixedPhrases != null) {
			for (ChatItem chatItem : appFixedPhrases) {
				fixedPhrases.add(chatItem);
			}
		}

		return fixedPhrases;
	}
}
