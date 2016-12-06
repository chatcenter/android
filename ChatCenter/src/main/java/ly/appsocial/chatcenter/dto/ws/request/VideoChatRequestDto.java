/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.dto.ws.request;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import ly.appsocial.chatcenter.dto.UserItem;

/**
 * [POST /api/channels/:channel_uid/messages] request.
 */
public class VideoChatRequestDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	public String callId;

	public UserItem user;

	public Reason reason;

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * @return
	 */
	public String toJson() {
		try {
			JSONObject contentObject = new JSONObject();
			JSONObject userObject = new JSONObject();
			userObject.put("user_id", user.id);

			contentObject.put("user", userObject);
			contentObject.put("call_id", callId);
			if ( reason != null ){
				JSONObject reasonObject = new JSONObject();
				reasonObject.put("type", reason.type);
				reasonObject.put("message", reason.message);
				contentObject.put("reason", reasonObject);
			}

			JSONObject rootObject = new JSONObject();
			rootObject.put("content", contentObject);

			return rootObject.toString();
		} catch (JSONException e) {
			return "";
		}
	}

	public static class Reason {
		public String type;
		public String message;
	}

}
