/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.ws.parser;


import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ly.appsocial.chatcenter.dto.ChannelItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.dto.ws.response.GetChannelsMineResponseDto;
import ly.appsocial.chatcenter.widgets.BasicWidget;
import ly.appsocial.chatcenter.widgets.VideoCallWidget;
import ly.appsocial.chatcenter.ws.ApiRequest;

/**
 * [GET /api/channels/mine] parser.
 */
public class GetChannelsMineParser implements ApiRequest.Parser<GetChannelsMineResponseDto> {

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
	public GetChannelsMineResponseDto parser(String response) {
		GetChannelsMineResponseDto resps = new GetChannelsMineResponseDto();
		try {
			JSONArray itemArray = new JSONArray(response);
			for (int i = 0; i < itemArray.length(); i++ ){
				JSONObject obj = itemArray.getJSONObject(i);
				if ( obj != null ){
					ChannelItem item = new Gson().fromJson(obj.toString(), ChannelItem.class);
					if ( item != null && item.latestMessage != null ){
						if (item.latestMessage.type.equals(ResponseType.INFORMATION)) {
							continue;
						} else if ( item.latestMessage.type.equals(ResponseType.CALL) ){
							item.latestMessage.setupContent(VideoCallWidget.class, obj.getJSONObject("latest_message"));
						} else {
							item.latestMessage.setupContent(BasicWidget.class, obj.getJSONObject("latest_message"));
						}
					}
					resps.items.add(item);
				}
			}
			return resps;
		} catch (JSONException e){
			mErrorCode = -1;
			return null;
		}

	}
}
