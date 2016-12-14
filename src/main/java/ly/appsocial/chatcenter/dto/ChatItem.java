package ly.appsocial.chatcenter.dto;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.widgets.BasicWidget;

/**
 * チャット画面の１項目
 */
public class ChatItem {
	private final String TAG ="ChatItem";


	public enum ChatItemStatus {
		READ, SENDING, SENT, SEND_FAILED
	}

	public ChatItem() {
		widget = new BasicWidget();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Local data
	// //////////////////////////////////////////////////////////////////////////
	public ChatItemStatus localStatus;

	public String getStatusString(Context context) {
		if (isRead()) {
			this.localStatus = ChatItemStatus.READ;
		}

		if (this.localStatus == null) {
			return null;
		}

		switch (this.localStatus) {
			case READ:
				return context.getString(R.string.message_status_read);
			case SENDING:
				return context.getString(R.string.message_status_sending);
			case SENT:
				return context.getString(R.string.message_status_sent);
			case SEND_FAILED:
				return context.getString(R.string.message_status_send_failed);
			default:
				return null;
		}
	}

	/**
	 * Check if the message has been read
	 *
	 * @return
	 */
	private boolean isRead() {
		if (usersReadMessage == null) {
			return false;
		}

		for (UserItem user : usersReadMessage) {
			if (user.id.equals(this.user.id)) {
				// Check only other users
				continue;
			}

			if (user.admin != this.user.admin) {
				// Either admin has read guest's message or guest has read admin's
				// Admin read admin's doesn't count
				return true;
			}
		}
		return false;
	}
	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** メッセージID */
	@SerializedName("id")
	public Integer id;
	/** コンテンツ */
	public BasicWidget widget;
	/** created */
	@SerializedName("created")
	public Long created;
	/** チャネルID */
	@SerializedName("channel_id")
	public String channelId;
	/** チャネルUID */
	@SerializedName("channel_uid")
	public String channelUid;
	/** ユーザー */
	@SerializedName("user")
	public UserItem user;
	/** Message type */
	@SerializedName("type")
	public String type;
	/** Message's Org Uid*/
	@SerializedName("org_uid")
	public String orgUid;
	/** List user who read this message */
	@SerializedName("users_read_message")
	public List<UserItem> usersReadMessage;


	/**
	 * コンテンツ
	 */

	public void setupContent(Class widgetClass, JSONObject jsonObject){
		JSONObject content = null;
		try {
			content = jsonObject.getJSONObject("content");
		} catch (JSONException e) {
		}

		if ( content == null ){
			content = jsonObject;
		}

		if ( content != null ){
			try {
				BasicWidget widgetObject = (BasicWidget) new Gson().fromJson(content.toString(), widgetClass);

				if (widgetObject != null) {
					this.widget = widgetObject;
					this.widget.setJsonObject(content);
				}
			} catch (JsonSyntaxException e){
				Log.e(TAG, "Unexpected format:" + content.toString());
			}
		}
	}

	private static ChatItem createTemporaryMessage(BasicWidget widget, int userId) {
		ChatItem item = new ChatItem();
		item.widget =  widget;
		item.localStatus = ChatItemStatus.SENDING;
		item.user = new UserItem();
		item.user.id = userId;
		item.created = System.currentTimeMillis() / 1000;
		return item;
	}

	private static ChatItem createTemporaryMessage(JSONObject content, int userId) {
		BasicWidget widget =  new Gson().fromJson(content.toString(), BasicWidget.class);
		return createTemporaryMessage(widget, userId);
	}

	public static ChatItem createTemporaryMessage(String text, String tmpUid, int userId) {
		JSONObject content = new JSONObject();
		try {
			content.put("text", text);
			content.put("uid", tmpUid);
		} catch (JSONException e) {
		}
		return createTemporaryMessage(content, userId);
	}

	public static ChatItem createTemporarySticker(String contentString, String tmpUid, int userId) {
		BasicWidget widget = new Gson().fromJson(contentString, BasicWidget.class);
		widget.uid = tmpUid;
		return createTemporaryMessage(widget, userId);
	}

	public static ChatItem createTemporaryImageSticker(final String imagePath, String tmpUid, int userId) {
		BasicWidget widget = new BasicWidget();
		widget.uid = tmpUid;

		widget.stickerContent = new BasicWidget.StickerContent();
		widget.stickerContent.thumbnailUrl = imagePath;
		widget.stickerContent.action = new ArrayList<String>() {{
			add(WidgetAction.OPEN_IMAGE + "?url=" + imagePath);
			add(imagePath);
		}};

		return createTemporaryMessage(widget, userId);
	}

	public static String createLocationStickerContent(Place place) {
		if (place == null) {
			return "";
		}

		BasicWidget widget = new BasicWidget();

		widget.stickerContent = new BasicWidget.StickerContent();

		widget.stickerContent.stickerData = new BasicWidget.StickerContent.StickerData();
		widget.stickerContent.stickerData.location = new BasicWidget.StickerContent.StickerData.Location();
		widget.stickerContent.stickerData.location.longitude = place.getLatLng().longitude;
		widget.stickerContent.stickerData.location.latitude = place.getLatLng().latitude;

		// ChatItem Message
		widget.message = new BasicWidget.Message();
		widget.message.text = "アドレス: " + place.getAddress();

		return new Gson().toJson(widget).toString();
	}

	public static String createScheduleWidgetContent(ArrayList<BasicWidget.StickerAction.ActionData> listActionData, Context context) {
		if (listActionData == null || listActionData.size() == 0) {
			return null;
		}

		BasicWidget widget = new BasicWidget();
		widget.message = new BasicWidget.Message();
		widget.message.text = context.getString(R.string.schedule_widget_title);
		widget.stickerAction = new BasicWidget.StickerAction();
		widget.stickerAction.actionType = ChatCenterConstants.ActionType.SELECT;
		widget.stickerAction.actionData = new ArrayList<>();
		widget.stickerAction.actionData.addAll(listActionData);

		BasicWidget.StickerAction.ActionData actionData = new BasicWidget.StickerAction.ActionData();
		actionData.label = context.getString(R.string.schedule_create_other);
		actionData.action = new ArrayList<String>() {{
			add(WidgetAction.OPEN_CALENDAR);
		}};

		widget.stickerAction.actionData.add(actionData);

		return  new Gson().toJson(widget).toString();
	}

}
