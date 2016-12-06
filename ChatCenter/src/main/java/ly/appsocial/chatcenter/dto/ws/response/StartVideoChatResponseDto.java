/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.dto.ws.response;

import com.google.gson.annotations.SerializedName;

import ly.appsocial.chatcenter.dto.UserItem;
import ly.appsocial.chatcenter.widgets.BasicWidget;
import ly.appsocial.chatcenter.widgets.VideoCallWidget;

/**
 * [GET /api/channels/:channel_uid/call] response.
 */
public class StartVideoChatResponseDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** コンテンツ */
	@SerializedName("content")
	public VideoCallWidget widget;

	/** メッセージID */
	public Integer id;
	/** created */
	public Long created;
	/** チャネルID */
	@SerializedName("channel_id")
	public String channelId;
	/** チャネルUID */
	@SerializedName("channel_uid")
	public String channelUid;
	/** ユーザー */
	public UserItem user;
	/** Message type */
	public String type;
	/** Message's Org Uid*/
	@SerializedName("org_uid")
	public String orgUid;
}
