package ly.appsocial.chatcenter.widgets;

import android.content.Context;
import android.view.View;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.dto.UserItem;
import ly.appsocial.chatcenter.widgets.views.WidgetView;

/**
 * Created by karasawa on 2016/10/27.
 */

public class VideoCallWidget extends BasicWidget {
	private static final String TAG = BasicWidget.class.getSimpleName();

	public final static String VIDEO_CALL_ACTION_ACCEPT = "accept";
	public final static String VIDEO_CALL_ACTION_HANGUP = "hangup";
	public final static String VIDEO_CALL_ACTION_REJECT = "reject";

	/** VideoCall Message: Caller */
	@SerializedName("caller")
	public VideoCallUser caller;

	/** VideoCall Message: Receivers */
	@SerializedName("receivers")
	public List<VideoCallUser> receivers;

	@SerializedName("action")
	public String action;

	@SerializedName("api_key")
	public String apiKey;

	@SerializedName("session")
	public String sessionId;

	public String messageId;

	@SerializedName("events")
	public List<VideoCallEvent> events;


	@Override
	public void setupWithUsers(List<UserItem> users){
		if(this.caller != null) {
			for (UserItem userItem : users) {
				if (userItem.id == this.caller.userId) {
					this.caller.displayName = userItem.displayName;
					this.caller.iconUrl = userItem.iconUrl;
					break;
				}
			}
		}

		// Set receiver name
		if(this.receivers != null) {
			for (UserItem userItem : users) {
				for (int i = 0; i < this.receivers.size(); i++) {
					if (this.receivers.get(i).userId == userItem.id) {
						this.receivers.get(i).displayName = userItem.displayName;
						this.receivers.get(i).iconUrl = userItem.iconUrl;
					}
				}
			}
		}
	}


	@Override
	public void setupWidgetView(WidgetView widgetView, Context context){
		StringBuilder text;
		if ( this.receivers != null && this.receivers.size() > 0 && !this.receivers.get(0).displayName.isEmpty() ) {
			text = new StringBuilder(String.format(context.getString(R.string.sticker_video_call_title),
					this.caller.displayName, this.receivers.get(0).displayName ));
		} else if ( this.caller.displayName != null && !this.caller.displayName.isEmpty() ){
			text = new StringBuilder(String.format(context.getString(R.string.sticker_video_call_now), this.caller.displayName));
		} else {
			text = new StringBuilder();
		}

		if (this.events != null && this.events.size() > 0) {
			VideoCallEventContent content = this.events.get(0).content;
			if ( content != null ){
				if ( VIDEO_CALL_ACTION_REJECT.equals(content.action) ) {
					text.append(context.getString(R.string.sticker_video_call_missed));
				} else {
					Collections.sort(this.events, new Comparator<VideoCallEvent>() {
						@Override
						public int compare(VideoCallWidget.VideoCallEvent lhs, VideoCallWidget.VideoCallEvent rhs) {
							return (int) (lhs.createdAt - rhs.createdAt);
						}
					});

					int callingTime = (int) (this.events.get(this.events.size() - 1).createdAt
							- this.events.get(0).createdAt);

					int minutes = callingTime / 60;
					int seconds = callingTime % 60;
					text.append(String.format(context.getString(R.string.sticker_video_call_time), minutes, seconds));
				}
			}
		}

		widgetView.setText(text.toString());
		widgetView.getImageView().setVisibility(View.GONE);
		// Hide action first, it will be shown later
		widgetView.showConfirmActions(false);
		widgetView.showSelectActions(false);
	}


	public static class VideoCallUser {
		/** User id*/
		@SerializedName("user_id")
		public int userId;

		@SerializedName("token")
		public String token;

		/** 表示名 */
		@SerializedName("display_name")
		public String displayName = "";

		/** アイコンURL */
		@SerializedName("icon_url")
		public String iconUrl = "";
	}

	public static class VideoCallEvent{
		@SerializedName("content")
		public VideoCallEventContent content;

		@SerializedName("created_at")
		public long createdAt;
	}

	public static class VideoCallEventContent{
		@SerializedName("action")
		public String action;

		@SerializedName("call_id")
		public int callId;

		@SerializedName("reason")
		public VideoCallReason reason;

		@SerializedName("user")
		public VideoCallUser user;
	}


	public static class VideoCallReason{
		@SerializedName("type")
		public String type;

		@SerializedName("message")
		public String message = "";
	}
}
