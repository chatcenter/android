package ly.appsocial.chatcenter.widgets;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.ChatActivity;
import ly.appsocial.chatcenter.activity.PhotoActivity;
import ly.appsocial.chatcenter.activity.WebViewActivity;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.UserItem;
import ly.appsocial.chatcenter.dto.WidgetAction;
import ly.appsocial.chatcenter.dto.param.PhotoParamDto;
import ly.appsocial.chatcenter.dto.ws.response.GetAppsResponseDto;
import ly.appsocial.chatcenter.ui.RoundImageView;
import ly.appsocial.chatcenter.util.AuthUtil;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.widgets.views.WidgetView;


/**
 * Created by karasawa on 2016/10/27.
 */

public class BasicWidget extends Widget {
	private static final String TAG = BasicWidget.class.getSimpleName();

	public static final String WIDGET_TYPE_LOCATION = "location";
	public static final String WIDGET_TYPE_CONFIRM = "confirm";
	public static final String WIDGET_TYPE_SELECT = "select";

	/** テキスト */
	@SerializedName("text")
	public String text;

	/** Temporary Uid */
	public String uid;

	/** Sticker message */
	@SerializedName("message")
	public Message message;

	@SerializedName("sticker-content")
	public StickerContent stickerContent;

	@SerializedName("sticker-action")
	public StickerAction stickerAction;

	public void setupWithUsers(List<UserItem> users){}

	public void onSendMessageReplySuccess(StickerAction.ActionData answer){
		if(this.stickerAction.responseActions == null) {
			this.stickerAction.responseActions = new ArrayList<>();
		} else {
			this.stickerAction.responseActions.clear();
		}

		StickerAction.ResponseAction rs = new StickerAction.ResponseAction(answer);
		this.stickerAction.responseActions.add(rs);
	}

	public void onSendCheckboxReplySuccess(List<StickerAction.ActionData> answers){
		if(this.stickerAction.responseActions == null) {
			this.stickerAction.responseActions = new ArrayList<>();
		} else {
			this.stickerAction.responseActions.clear();
		}

		for(StickerAction.ActionData answer : answers ){
			StickerAction.ResponseAction rs = new StickerAction.ResponseAction(answer);
			this.stickerAction.responseActions.add(rs);
		}
	}

	public void onTappedImage(Context context){
		if ( stickerContent != null && stickerContent.action != null && stickerContent.action.size() > 0 ){
			String action = stickerContent.action.get(0);
			if ( action.contains(WidgetAction.OPEN_IMAGE) ) {
				if (stickerContent.action.size() > 1) {
					openImage(stickerContent.action.get(1), context);
				}
			} else if (action.contains(WidgetAction.OPEN_LOCATION)) {
				if (stickerContent.action.size() > 1) {
					openLocation(stickerContent.action.get(1), context);
				}
			} else if (action.contains(WidgetAction.OPEN_CALENDAR)) {
				if (stickerContent.action.size() > 1) {
					openCalender(stickerContent.action.get(1), context);
				}
			} else {
				openUrl(stickerContent.action.get(0), context);
			}
		}
	}

	protected String addAuthToUrl(Context context, String url){
		String appToken = getAppToken(context);
		String token = getUserToken(context);
		String newUrl = url + "?authentication=" + token + "&app_token=" + appToken;
		return newUrl;
	}

	protected String getUserToken(Context context){
		return AuthUtil.getUserToken(context.getApplicationContext());
	}

	protected String getAppToken(Context context){
		if ( context.getClass().equals(ChatActivity.class) ){
			ChatActivity chatActivity = (ChatActivity)context;
			GetAppsResponseDto.App currentApp = chatActivity.getCurrentApp();
			if ( currentApp != null ){
				return currentApp.token;
			}
		}
		return null;
	}

	public void setupWidgetView(WidgetView widgetView, Context context){
		if (StringUtil.isNotBlank(this.text)) {
			widgetView.setText(this.text);
		} else if (this.message != null && StringUtil.isNotBlank(this.message.text)) {
			widgetView.setText(this.message.text);
		}

		if (this.stickerContent == null) {
			widgetView.getImageView().setVisibility(View.GONE);
		} else {
			widgetView.setImageUrl(this.stickerContent.thumbnailUrl);
			if (this.stickerContent.stickerData != null && this.stickerContent.stickerData.location != null) {
				widgetView.setWidgetIcon(WIDGET_TYPE_LOCATION);
			}
		}

		// Hide action first, it will be shown later
		widgetView.showConfirmActions(false);
		widgetView.showSelectActions(false);

		if (this.stickerAction == null ||
				this.stickerAction.actionData == null ||
				this.stickerAction.actionData.size() == 0) {
			// No action found
			widgetView.getImageView().addRounded(RoundImageView.RoundedOptions.BOTTOM);
			widgetView.postInvalidate();
			return;
		}

		widgetView.setWidgetIcon(this.stickerAction.actionType);

		Log.d(TAG, "setChatItem: actionType: " + this.stickerAction.actionType);
		if (WIDGET_TYPE_CONFIRM.equals(this.stickerAction.actionType)) {
			if (this.stickerAction.actionData.size() == 2) {
				widgetView.showConfirmActions(this.stickerAction);
			} else {
				widgetView.showSelectActions(this.stickerAction);
			}
		} else if (WIDGET_TYPE_SELECT.equals(this.stickerAction.actionType)) {
			Log.d(TAG, "setChatItem: before showSelectActions");

			if ( this.stickerAction.viewInfo != null && this.stickerAction.viewInfo.type != null ){
				switch (this.stickerAction.viewInfo.type){
					case ChatCenterConstants.ViewType.DEFAULT:
						widgetView.showSelectActions(this.stickerAction);
						break;
					case ChatCenterConstants.ViewType.CHECKBOX:
						widgetView.showCheckboxActions(this.stickerAction);
						break;
					case ChatCenterConstants.ViewType.LINEAR:
						widgetView.showLinearActions(this.stickerAction);
						break;
					case ChatCenterConstants.ViewType.YESNO:
						widgetView.showConfirmActions(this.stickerAction);
						break;
					default:
						widgetView.showSelectActions(this.stickerAction);
						break;
				}
			} else {
				widgetView.showSelectActions(this.stickerAction);
			}
		}

		// Now it has action data
		widgetView.getImageView().removeRounded(RoundImageView.RoundedOptions.BOTTOM);
		widgetView.postInvalidate();
	}

	public boolean isSelectedAction(StickerAction.ActionData action) {
		List<StickerAction.ResponseAction> responseActions = this.stickerAction.responseActions;
		if (responseActions != null && responseActions.size() > 0)
			for (StickerAction.ResponseAction responseAction : responseActions) {
				List<StickerAction.ActionData> actionDataList = responseAction.getActions();
				if ( actionDataList != null ){
					for ( StickerAction.ActionData actionData : actionDataList ){
						if (actionData != null && actionData.label != null &&
								actionData.label.equals(action.label)) {
							return true;
						}
					}
				}
			}
		return  false;
	}


	private void openCalender(String data, Context context){
	}

	private void openLocation(String mapUrl, Context context) {
		Intent intent = new Intent(context, WebViewActivity.class);
		intent.putExtra(ChatCenterConstants.Extra.URL, mapUrl);
		intent.putExtra(ChatCenterConstants.Extra.ACTIVITY_TITLE, context.getString(R.string.google_map));
		context.startActivity(intent);
	}

	protected void openImage(String imageUrl, Context context) {
		try {
			URL url = new URL(imageUrl);
			PhotoParamDto photoParam = new PhotoParamDto(imageUrl);
			photoParam.fileName = StringUtil.getFileNameFromUrl(url);
			Intent intent = new Intent(context, PhotoActivity.class);
			intent.putExtra(PhotoActivity.PHOTO_DATA, photoParam);
			context.startActivity(intent);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	protected void openUrl(String url, Context context) {
		Intent intent = new Intent(context, WebViewActivity.class);
		intent.putExtra(ChatCenterConstants.Extra.URL, url);
		context.startActivity(intent);
	}


	/**
	 *
	 */
	public static class StickerContent {
		/** The thumbnail url */
		@SerializedName("thumbnail-url")
		public String thumbnailUrl;

		/** The sticker data */
		@SerializedName("sticker-data")
		public StickerData stickerData;

		/** The thumbnail's action */
		@SerializedName("action")
		public List<String> action;

		public static class StickerData {
			/** Location data */
			@SerializedName("location")
			public Location location;

			public static class Location {
				/** Lattitude */
				@SerializedName("lat")
				public double latitude;

				/** Longitude */
				@SerializedName("lng")
				public double longitude;
			}
		}
	}

	/**
	 *
	 */
	public static class StickerAction {
		/** The action type */
		@SerializedName("action-type")
		public String actionType;

		/** The action data */
		@SerializedName("action-data")
		public List<ActionData> actionData;

		@SerializedName("action-response-data")
		public List<ResponseAction> responseActions;

		@SerializedName("view-info")
		public ViewInfo viewInfo;

		/**
		 *
		 */
		public static class ActionData {
			/** Action's label */
			@SerializedName("label")
			public String label;

			/** Action list */
			@SerializedName("action")
			public List<String> action;

			@SerializedName("value")
			public Value value;

			public static class Value {
				/** The start value of a datetime action */
				@SerializedName("start")
				public long start;

				/** The end value of a datetime action */
				@SerializedName("end")
				public long end;

				/** The answer value */
				@SerializedName("answer")
				public String answer;
			}

			public boolean isProposeOtherSlotAction () {
				if (action != null && action.size() > 0 && action.contains(WidgetAction.OPEN_CALENDAR)) {
					return true;
				}
				return false;
			}
		}

		public static class ResponseAction {
			@SerializedName("action")
			public Object actionObj;

			public List<ActionData> actionDataList;

			public List<ActionData> getActions(){
				if ( actionDataList == null && actionObj != null ){
					actionDataList = new ArrayList<>();
					if (actionObj.getClass().equals(ArrayList.class)) {
						for( Object obj : ((ArrayList)actionObj) ){
							JsonObject jsonObject = new Gson().toJsonTree(obj).getAsJsonObject();
							ActionData actionData = new Gson().fromJson(jsonObject.toString(), ActionData.class);
							actionDataList.add(actionData);
						}
					} else {
						JsonObject jsonObject = new Gson().toJsonTree(actionObj).getAsJsonObject();
						ActionData actionData = new Gson().fromJson(jsonObject.toString(), ActionData.class);
						actionDataList.add(actionData);
					}
				}
				return actionDataList;
			}

			public ResponseAction(ActionData act) {
				actionObj = new ArrayList<>();
				((List<ActionData>)actionObj).add(act);
			}
		}

		public static class ViewInfo {
			@SerializedName("type")
			public String type;

			@SerializedName("min-label")
			public String minLabel;

			@SerializedName("max-label")
			public String maxLabel;
		}
	}

	public static class Message {
		/** Sticker message */
		@SerializedName("text")
		public String text;
	}


}
