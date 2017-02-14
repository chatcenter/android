package ly.appsocial.chatcenter.ws;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import ly.appsocial.chatcenter.BuildConfig;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.dto.ws.response.WsChannelJoinMessageDto;
import ly.appsocial.chatcenter.dto.ws.response.WsMessagesResponseDto;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;
import ly.appsocial.chatcenter.widgets.VideoCallWidget;

/**
 * WebSocket リスナー
 */
public abstract class CCWebSocketClientListener implements CCWebSocketClient.Listener {
	public static final String TAG = "WebSocketClientListener";

    /**
     * このコールバックがキャンセルとなったかどうか
     * <p>
     * キャンセルとなった場合は全てのコールバックの処理をスキップします。
     * </p>
     */
    public boolean mCancelled;

    int subscribingChannelNum;
    int subscribedChannelNum;

    public abstract void onWSConnect();
    public abstract void onWSDisconnect(int code, String reason);
    public abstract void onWSError(Exception exception);
    public abstract void onWSMessage(WsMessagesResponseDto response, String messageType);
    public abstract void onWSChannelJoin(WsChannelJoinMessageDto response);
	public abstract void onWSRecieveAnswer(Integer messageId, Integer answerType);
	public abstract void onWSRecieveOnline(String channelUid, JSONObject user, String orgUid, Boolean online);
	public abstract void onWSReceiveReceipt(String channelUid, JSONArray messages, JSONObject user);

	public void didReceiveInviteCallCallback(Number uid){}

    public CCWebSocketClientListener() {
    }

    @Override
    public void onConnect() {
        if (isCancelled()) {
            return;
        }

		subscribedChannelNum = 0;

        this.onWSConnect();
    }

    @Override
    public void onMessage(String message) {
        if (isCancelled()) {
            return;
        }

        try {

			if (!StringUtil.isJSONValid(message)) {
				return;
			}

            JSONArray rootArray = new JSONArray(message);
            if (rootArray == null || rootArray.length() < 2) {
                return;
            }

            String type = (String) rootArray.get(0);
            String rawData = rootArray.length() >= 2 ? rootArray.get(1).toString() : null;
			JSONObject jsonObject = rootArray.getJSONObject(1);

            if ( "success:subscribe".equals(type) ){
				subscribedChannelNum++;
				if (subscribedChannelNum == subscribingChannelNum) {
					subscribedChannelNum = 0;
					subscribingChannelNum = 0;
					Log.d(TAG, "success:all channel subscribed");
				}
				Log.d(TAG, "success:subscribe");
            } else if ("failure:subscribe".equals(type)){
				Log.d(TAG, "failure:subscribe");
			} else if ( "message".equals(type) // message:message
					|| "message:information".equals(type)
					|| "message:sticker".equals(type)
					|| "message:response".equals(type)
					|| "message:property".equals(type)
					|| "message:message".equals(type)
					|| "message:suggestion".equals(type)
					) {

	            if (rawData == null)
		            return;

	            String messageType = "";
	            if ("message:message".equals(type)) {
		            messageType = ResponseType.MESSAGE;
	            } else if ("message:information".equals(type)) {
		            messageType = ResponseType.INFORMATION;
	            } else if ("message:sticker".equals(type)) {
		            messageType = ResponseType.STICKER;
	            } else if ("message:response".equals(type)) {
		            messageType = ResponseType.RESPONSE;
	            } else if ("message:property".equals(type)) {
		            messageType = ResponseType.PROPERTY;
	            } else if ("message:suggestion".equals(type)) {
		            messageType = ResponseType.SUGGESTION;
	            }

	            WsMessagesResponseDto argument = new Gson().fromJson(rawData, WsMessagesResponseDto.class);
	            argument.setupContent(BasicWidget.class, jsonObject);

	            ///null check
	            if (argument.widget == null
			            || argument.channelId == null
			            || argument.channelUid == null
			            || argument.created == null
			            || argument.id == null
			            || (argument.user == null && !messageType.equals(ResponseType.INFORMATION)
			            && !messageType.equals(ResponseType.PROPERTY))
			            || ((argument.user == null || argument.user.displayName == null) && !messageType.equals(ResponseType.INFORMATION)
			            && !messageType.equals(ResponseType.PROPERTY))
			            || ((argument.user == null || argument.user.id == null) && !messageType.equals(ResponseType.INFORMATION)
			            && !messageType.equals(ResponseType.PROPERTY))
			            || argument.orgUid == null
			            ) {
		            return;
	            }

	            this.onWSMessage(argument, messageType);
            } else if ("message:call".equals(type)){
	            WsMessagesResponseDto argument = new Gson().fromJson(rawData, WsMessagesResponseDto.class);
	            argument.setupContent(VideoCallWidget.class, jsonObject);
	            this.onWSMessage(argument, ResponseType.CALL);
            } else if ("call:call".equals(type)){
	            WsMessagesResponseDto argument = new Gson().fromJson(rawData, WsMessagesResponseDto.class);
	            argument.setupContent(VideoCallWidget.class, jsonObject);
	            this.onWSMessage(argument, ResponseType.CALLINVITE);
            } else if ("channel:join".equals(type)) {
                WsChannelJoinMessageDto wsChannelJoinMessageDto = new Gson().fromJson(rawData, WsChannelJoinMessageDto.class);
                this.onWSChannelJoin(wsChannelJoinMessageDto);
			} else if ("channel:online".equals(type)) {
				String channelUid;
				String org_uid;
				JSONObject user;
				try {
					channelUid = jsonObject.getString("uid");
					org_uid = jsonObject.getString("org_uid");
					user = jsonObject.getJSONObject("user");
				} catch(JSONException e){
					return;
				}
				if ( channelUid == null || org_uid == null || user == null )
					return;

				this.onWSRecieveOnline(channelUid, user, org_uid, true);
			} else if ("channel:offline".equals(type)) {
				String channelUid;
				String org_uid;
				JSONObject user;
				try {
					channelUid = jsonObject.getString("uid");
					org_uid = jsonObject.getString("org_uid");
					user = jsonObject.getJSONObject("user");
				} catch(JSONException e){
					return;
				}
				if ( channelUid == null || org_uid == null || user == null )
					return;

				this.onWSRecieveOnline(channelUid, user, org_uid, false);
			} else if ("message:receipt".equals(type)) {
				String channelUid;
				JSONObject user;
				JSONObject content;
				Integer userId = null;
				JSONArray messages = null;
				try {
					channelUid = jsonObject.getString("channel_uid");
					user = jsonObject.getJSONObject("user");
					if ( user != null ){
						userId = user.getInt("id");
					}
					content = jsonObject.getJSONObject("content");
					if ( content != null ){
						messages = content.getJSONArray("messages");
					}
				} catch(JSONException e){
					return;
				}
				if ( channelUid == null || content == null || user == null || userId == null || messages == null)
					return;
				this.onWSReceiveReceipt(channelUid, messages, user);
			} else if ("message:answer".equals(type)) {
				Integer messageId;
				Integer answerType;
				try {
					messageId = jsonObject.getInt("message_id");
					answerType = jsonObject.getInt("answer_type");
				} catch(JSONException e){
					return;
				}
				if ( messageId == null || answerType == null )
					return;
				this.onWSRecieveAnswer(messageId, answerType);
			} else if ("message:link".equals(type)) {
				///TODO: This may not be used but I remain just incase
			} else if ("channel:assigned".equals(type)) {
				// TODO
			} else if ("channel:unassigned".equals(type)) {
				// TODO
			} else if ("channel:followed".equals(type)) {
				// TODO
			} else if ("channel:unfollowed".equals(type)) {
				// TODO
			} else if ("channel:deleted".equals(type)) {
				// TODO
			} else if (type != null && type.contains("message:") ) {
				// TODO
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e("###", e.getMessage(), e);
            }
        }
    }

    @Override
    public void onMessage(byte[] data) {
        // empty
    }

    @Override
    public void onDisconnect(int code, String reason) {
        // empty
        this.onWSDisconnect(code, reason);
    }

    @Override
    public void onError(Exception exception) {
        if (isCancelled()) {
            return;
        }
        this.onWSError(exception);
    }

    public boolean isCancelled() {
        return mCancelled;
    }
    public void cancel() {
        this.mCancelled = true;
    }
}