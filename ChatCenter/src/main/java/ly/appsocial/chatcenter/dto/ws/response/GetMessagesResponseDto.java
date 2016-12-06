package ly.appsocial.chatcenter.dto.ws.response;

import java.util.ArrayList;

import ly.appsocial.chatcenter.dto.ChatItem;

/**
 * [GET /api/channels/:channel_uid/messages] response.
 */
public class GetMessagesResponseDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** 項目 */
	public ArrayList<ChatItem> items = new ArrayList<>();

	/**
	 * 項目
	 */
	public static class Item extends ChatItem {
	}
}
