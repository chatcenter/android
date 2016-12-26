/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.dto.ws.request;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * [POST /api/channels/:channel_uid/messages] request.
 */
public class StartVideoChatRequestDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	public String action;

	public List<Receiver> receivers;

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * @return
	 */
	public String toJson() {
		try {
			Gson gson = new Gson();
			JSONObject rootObject = new JSONObject();
			rootObject.put("content", new JSONObject(gson.toJson(this)));

			return rootObject.toString();
		} catch (JSONException e) {
			return "";
		}
	}

	public static class Receiver {
		public String user_id;

		public Receiver(String userId) {
			user_id = userId;
		}
	}
}
