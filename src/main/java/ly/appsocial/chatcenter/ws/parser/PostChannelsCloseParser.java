/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.ws.parser;


import com.google.gson.Gson;

import ly.appsocial.chatcenter.dto.ws.response.PostChannelsCloseResponseDto;
import ly.appsocial.chatcenter.ws.ApiRequest;

/**
 * [POST /api/channels] parser.
 */
public class PostChannelsCloseParser implements ApiRequest.Parser<PostChannelsCloseResponseDto> {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** エラーコード */
	private int mErrorCode;

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

	@Override
	public int getErrorCode() {
		return mErrorCode;
	}

	@Override
	public PostChannelsCloseResponseDto parser(String response) {

		try {
			return new Gson().fromJson("{items:" + response + "}", PostChannelsCloseResponseDto.class);
		} catch (Exception e) {
			mErrorCode = -1;
		}
		return null;
	}
}
