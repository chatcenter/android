package ly.appsocial.chatcenter.dto;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;
import ly.appsocial.chatcenter.widgets.LiveLocationUser;

/**
 * チャット画面の１項目
 */
public class ChatItem {
	private final String TAG ="ChatItem";


	public enum ChatItemStatus {
		READ, SENDING, SENT, SEND_FAILED, DEFAULT, DRAFT
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
			case SEND_FAILED:
				return context.getString(R.string.message_status_send_failed);
			case SENT:
			case DEFAULT:
			default:
				return null;
		}
	}

	/**
	 * Check if the message has been read
	 *
	 * @return
	 */
	public boolean isRead() {
		if (usersReadMessage == null) {
			return false;
		}

		for (UserItem user : usersReadMessage) {
			if (user == null || (this.user != null && user.id.equals(this.user.id)) || user.admin) {
				// Check only other users
				continue;
			}

			return true;
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

	public String rawContent = "";
	public String stickerType = "";

	public int localId;

	/** To store temp message that user entered into InputWidget*/
	public String tempMessage;


	public transient ArrayList<LiveLocationUser> mLiveLocationUsers = new ArrayList<>();
	private transient boolean mIsInitLocation = false;

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

	private static ChatItem createTemporaryMessage(BasicWidget widget, UserItem sender) {
		ChatItem item = new ChatItem();
		item.widget =  widget;
		item.localStatus = ChatItemStatus.SENDING;
		if (sender != null) {
			item.user = sender;
		} else {
			item.user = new UserItem();
		}
		item.created = System.currentTimeMillis() / 1000;
		return item;
	}

	private static ChatItem createTemporaryMessage(JSONObject content, UserItem sender) {
		BasicWidget widget =  new Gson().fromJson(content.toString(), BasicWidget.class);
		return createTemporaryMessage(widget, sender);
	}

	public static ChatItem createTemporaryMessage(String text, String tmpUid, UserItem sender) {
		JSONObject content = new JSONObject();
		try {
			content.put("text", text);
			content.put("uid", tmpUid);
		} catch (JSONException e) {
		}
		return createTemporaryMessage(content, sender);
	}

	public static ChatItem createTemporarySticker(String contentString, String tmpUid, UserItem sender) {
		BasicWidget widget = new Gson().fromJson(contentString, BasicWidget.class);
		widget.uid = tmpUid;
		return createTemporaryMessage(widget, sender);
	}

	public static ChatItem createTemporaryImageSticker(final String imagePath, String tmpUid, UserItem sender) {
		BasicWidget widget = new BasicWidget();
		widget.uid = tmpUid;

		widget.stickerContent = new BasicWidget.StickerContent();
		widget.stickerContent.thumbnailUrl = imagePath;
		widget.stickerContent.action = new ArrayList<String>() {{
			add(WidgetAction.OPEN_IMAGE + "?url=" + imagePath);
			add(imagePath);
		}};

		widget.stickerType = ChatCenterConstants.StickerName.STICKER_TYPE_TYPE_FILE;

		return createTemporaryMessage(widget, sender);
	}

	public static String createLocationStickerContent(Place place, Context context) {
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
		String placeName = place.getName().toString();
		String address = place.getAddress().toString();
		widget.message.text = StringUtil.isNotBlank(placeName)
				&& StringUtil.isNotBlank(address) ? placeName : context.getString(R.string.venue_widget_title);
		widget.stickerType = ChatCenterConstants.StickerName.STICKER_TYPE_LOCATION;

		return new Gson().toJson(widget).toString();
	}

	public static String createLiveLocationStickerContent(Location location, Context context) {
		BasicWidget widget = new BasicWidget();

		widget.stickerContent = new BasicWidget.StickerContent();

		widget.stickerContent.stickerData = new BasicWidget.StickerContent.StickerData();
		widget.stickerContent.stickerData.location = new BasicWidget.StickerContent.StickerData.Location();
		widget.stickerContent.stickerData.location.longitude = location.getLongitude();
		widget.stickerContent.stickerData.location.latitude = location.getLatitude();
		widget.stickerType = ChatCenterConstants.StickerName.STICKER_TYPE_CO_LOCATION;

		String mapUrl = "http://maps.google.com/maps/api/staticmap?center=" + location.getLatitude() + "," + location.getLongitude() + "&zoom=12&size=400x400&sensor=false";
		widget.stickerContent.thumbnailUrl = mapUrl;

		// ChatItem Message
		widget.message = new BasicWidget.Message();
		widget.message.text = context.getString(R.string.location_title);

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

		widget.stickerType = ChatCenterConstants.StickerName.STICKER_TYPE_SCHEDULE;

		widget.stickerAction.actionData.add(actionData);

		return  new Gson().toJson(widget).toString();
	}

	public boolean updateWithResponse(Context context, ChatItem newItem){
		if (newItem.widget != null && newItem.widget.stickerType != null ){
			switch(newItem.widget.stickerType){
				case ChatCenterConstants.StickerName.STICKER_TYPE_CO_LOCATION:
					return updateLiveLocationUser(context, newItem);
			}
		}
		return true;
	}


	public boolean initLiveLocationUser(Context context){
		if ( this.widget == null || this.widget.stickerContent == null ||
				this.widget.stickerContent.stickerData == null ||
				this.widget.stickerContent.stickerData.users == null ||
				this.widget.stickerContent.stickerData.users.isEmpty() )
			return false;

		if ( mIsInitLocation ){
			return true;
		}
		mIsInitLocation = true;

		for ( LiveLocationUser user : mLiveLocationUsers ){
			user.stopTimer();
		}
		mLiveLocationUsers.clear();

		for ( BasicWidget.StickerContent.StickerData.User user : this.widget.stickerContent.stickerData.users ){
			LiveLocationUser liveUser = new LiveLocationUser(context, user.id, user.displayName, user.iconUrl);
			liveUser.updateTimer();
			mLiveLocationUsers.add(liveUser);
		}
		return true;
	}

	private boolean updateLiveLocationUser(Context context, ChatItem newItem){
		if ( newItem.user == null || newItem.user.id == null )
			return false;

		boolean bFound = false;
		for ( LiveLocationUser user : mLiveLocationUsers ){
			if ( user.mId.equals(newItem.user.id) ){
				bFound = true;
				if ( newItem.widget.stickerContent != null && newItem.widget.stickerContent.stickerData != null
						&& newItem.widget.stickerContent.stickerData.type != null
						&& "stop".equals(newItem.widget.stickerContent.stickerData.type) ){
					user.stopTimer();
				} else {
					user.updateTimer();
				}
			}
		}

		if ( !bFound ){
			LiveLocationUser user = new LiveLocationUser(context, newItem.user);
			mLiveLocationUsers.add(user);
		}
		return true;
	}

	public int getActiveLiveLocationUsersCount(){
		int count = 0;
		for ( LiveLocationUser user : mLiveLocationUsers ){
			if ( user.isActive() )
				count++;
		}
		return count;
	}

	public String getSimpleFormatedCreatedDate() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
		return simpleDateFormat.format(new Date(created * 1000));
	}

	/** If sticker with message only, show as a normal message*/
	public ChatItem rebuildChatItem() {
		if (!ResponseType.MESSAGE.equals(type)
				&& widget != null && widget.message != null
				&& StringUtil.isNotBlank(widget.message.text)
				&& widget.stickerAction == null
				&& widget.stickerContent == null) {

			type = ResponseType.MESSAGE;
			widget.text = widget.message.text;
		}

		return this;
	}

}
