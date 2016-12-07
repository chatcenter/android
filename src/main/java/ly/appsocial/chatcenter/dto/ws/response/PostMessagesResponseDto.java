package ly.appsocial.chatcenter.dto.ws.response;

import com.google.gson.annotations.SerializedName;

/**
 * [POST /api/channels/:channel_uid/messages] response.
 */
public class PostMessagesResponseDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** コンテンツ */
	@SerializedName("content")
	public Content content;
	/** 作成日 */
	@SerializedName("created")
	public long created;
	/** ユーザー */
	@SerializedName("user")
	public User user;

	/**
	 * コンテンツ
	 */
	public static class Content {
		/** テキスト */
		@SerializedName("text")
		public String text;
	}

	/**
	 * ユーザー
	 */
	public static class User {
		/** ユーザー名 */
		@SerializedName("display_name")
		public String displayName;
	}
}
