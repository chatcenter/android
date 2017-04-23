package ly.appsocial.chatcenter.activity.model;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.widgets.BasicWidget;
import ly.appsocial.chatcenter.widgets.VideoCallWidget;

public class CCNotification {

    public String orgUid;
    public String category;
    public String alert;
    public String appName;
    public String channelUid;
    public Body body;

    public class Body extends ChatItem {

    }

    /**
     * Create a new notification object from push data.
     * @param chatCenterData
     * @return
     */
    public static CCNotification newInstance(Map<String, String> chatCenterData) {
        if (chatCenterData == null) {
            return null;
        } else {
            CCNotification notification = new CCNotification();

            if (chatCenterData.containsKey("org_uid")) {
                notification.orgUid = chatCenterData.get("org_uid");
            }

            if (chatCenterData.containsKey("category")) {
                notification.category = chatCenterData.get("category");
            }

            if (chatCenterData.containsKey("alert")) {
                notification.alert = chatCenterData.get("alert");
            }

            if (chatCenterData.containsKey("channel_uid")) {
                notification.channelUid = chatCenterData.get("channel_uid");
            }

            if (chatCenterData.containsKey("app_name")) {
                notification.appName = chatCenterData.get("app_name");
            }

            if (chatCenterData.containsKey("message")) {
                String message = chatCenterData.get("message");
                notification.body = new Gson().fromJson(message, Body.class);
                try {
                    if (notification.body != null) {
                        JSONObject obj = new JSONObject(message);
                        if (notification.body.type.equals(ResponseType.INFORMATION)) {
                            // Skip message type information for now
                        } else if (notification.body.type.equals(ResponseType.CALL)) {
                            notification.body.setupContent(VideoCallWidget.class, obj);
                        } else if (notification.body.type.equals(ResponseType.SUGGESTION)) {
                            notification.body.setupContent(BasicWidget.class, obj);
                        } else {
                            notification.body.setupContent(BasicWidget.class, obj);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return notification;
        }
    }

}
