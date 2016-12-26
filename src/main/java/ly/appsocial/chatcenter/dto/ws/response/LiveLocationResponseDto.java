/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.dto.ws.response;

import com.google.gson.annotations.SerializedName;

import ly.appsocial.chatcenter.widgets.BasicWidget;

/**
 * Created by karasawa on 2016/12/19.
 */

public class LiveLocationResponseDto {
	@SerializedName("id")
	public String id;

	@SerializedName("content")
	public BasicWidget content;

}
