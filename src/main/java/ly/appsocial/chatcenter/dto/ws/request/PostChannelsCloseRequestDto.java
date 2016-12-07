/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.dto.ws.request;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * [POST /api/channels/close] request.
 */
public class PostChannelsCloseRequestDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** チャネルUIDリスト */
	public List<String> channelUids = new ArrayList<String>();

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * @return
	 */
	public String toJson() {
		try {
			JSONObject rootObject = new JSONObject();
			JSONArray channelUidsArray = new JSONArray();
			for (String channelUid : channelUids) {
				channelUidsArray.put(channelUid);
			}
			rootObject.put("channel_uids", channelUidsArray);

			return rootObject.toString();
		} catch (JSONException e) {
			return "";
		}
	}
}
