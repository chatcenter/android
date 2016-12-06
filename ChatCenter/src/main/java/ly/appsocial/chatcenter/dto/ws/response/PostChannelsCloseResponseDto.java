/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.dto.ws.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * [POST /api/channels/:channel_uid/close] response.
 */
public class PostChannelsCloseResponseDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** 項目 */
	@SerializedName("items")
	public List<Item> items;

	/**
	 * 項目
	 */
	public static class Item {
		/** チャネルUID */
		@SerializedName("uid")
		public String uid;
		/** ステータス */
		@SerializedName("status")
		public String status;
	}
}
