/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.dto.ws.request;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * [POST /api/channels/:channel_uid/messages] request.
 */
public class StartVideoChatRequestDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** Temporary Uid */
	public String tmpUid;

	public String action;

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * @return
	 */
	public String toJson() {
		try {
			JSONObject contentObject = new JSONObject();
			contentObject.put("uid", tmpUid);
			contentObject.put("action", action);

			JSONObject rootObject = new JSONObject();
			rootObject.put("content", contentObject);

			return rootObject.toString();
		} catch (JSONException e) {
			return "";
		}
	}
}
