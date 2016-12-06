/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.dto.ws.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ly.appsocial.chatcenter.dto.OrgItem;

/**
 * [GET /api/users/:id] response.
 */
public class GetOrgsResponseDto {
	@SerializedName("items")
	public List<OrgItem> items;
}
