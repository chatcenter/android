/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.dto.ws.response;

import java.util.ArrayList;
import java.util.List;

import ly.appsocial.chatcenter.dto.ChannelItem;

/**
 * [GET /api/channels/mine] response.
 */
public class GetChannelsMineResponseDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** 項目 */
	public List<ChannelItem> items = new ArrayList<>();
}
