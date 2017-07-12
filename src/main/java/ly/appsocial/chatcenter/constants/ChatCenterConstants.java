package ly.appsocial.chatcenter.constants;

import ly.appsocial.chatcenter.BuildConfig;

/**
 * ChatCenter 全体の定数定義クラス
 */
public class ChatCenterConstants {

	public static final String CHATCENTER_SDK_VERSION = "1.0.8";

	public static final int MAX_MESSAGE_ON_LOAD = 20;
	public static final int MAX_CHANNEL_ON_LOAD = 20;
    public static final long SEND_TYPING_TIME_OUT = 500;
	public static final long TIME_DISPLAYING_TYPING = 2000; // Display "typing..." in 2 seconds
	public static final int MAX_NOTE_LENGTH = 500; // Max length of Note is 500 characters
	public static final String LICENSE_URL = "https://chatcenter.github.io/android/Copyright/%s/Copyright";

	public static String getDashboardUrl() {
		return "https://app.chatcenter.io";
	}

	/**
	 * プリファレンスキー
	 */
	public static class Preference {
		/** Providerトークン生成タイムスタンプ(ms) */
		public static final String TOKEN_TIMESTAMP = "chatcenter_token_timestamp";
		public static final String USER = "chatcenter_user";
		public static final String DEVICE_TOKEN = "chatcenter-device-token";
		/**
		 * 表示済みチャットリスト
		 * <p>
		 * 問い合わせ確認画面のスキップ判定に使用します。値は「KISSコード」
		 * </p>
		 */
		public static final String BROWSED_CHATS = "chatcenter_browsed_chats";

		public static final String LAST_LOCATION = "chatcenter_last_location";

		/** To save last stage of app for next launching*/
		public static final String LAST_ORG_UID = "chatcenter_last_selected_org_uid";
		public static final String LAST_APP_ID = "chatcenter_last_selected_app_id";

		public static final String LAST_CHANNEL_STATUS = "chatcenter_last_channel_status";
		public static final String LAST_CHANNEL_FUNNEL = "chatcenter_last_channel_funnel";
		public static final String LAST_CHANNEL_FILTER_STRING = "chatcenter_last_channel_filter_string";

		public static final String USER_CONFIG = "chatcenter_user_config";
	}

	public static class Extra {
		public static final String CHANNEL_UID = "channel_uid";
		public static final String URL = "url";
		public static final String IS_AGENT = "is_agent";
		public static final String ORG = "org";
		public static final String APP = "app";
		public static final String ACTIVITY_TITLE = "activity_title";
		public static final String FUNNEL_LIST = "funnel_list";
		public static final String CHAT_PARAM = "chat_param";
		public static final String WEBVIEW_HEADER = "open_webview_with_auth_token";
	}

	public static class StickerName {
		public static final String STICKER_TYPE_DATE_TIME_AVAILABILITY = "datetime";
		public static final String STICKER_TYPE_TYPE_FILE = "file_upload";
		public static final String STICKER_TYPE_LOCATION = "location";
		public static final String STICKER_TYPE_CO_LOCATION = "co-location";
		public static final String STICKER_TYPE_QUESTION = "yes_no";
		public static final String STICKER_TYPE_FIXED_PHRASE = "fixed_phrase";
		public static final String STICKER_TYPE_VIDEO_CHAT = "video_chat";
		public static final String STICKER_TYPE_VOICE_CHAT = "voice_chat";
		public static final String STICKER_TYPE_CAMERA = "camera_upload";
		public static final String STICKER_TYPE_TYPE_PHONE_CALL = "phone_call";
		public static final String STICKER_TYPE_SCHEDULE = "schedule";
		public static final String STICKER_TYPE_SELECT = "select";
		public static final String STICKER_TYPE_LANDING_PAGE = "landing_page";
		public static final String STICKER_TYPE_PAYMENT = "payment";
		public static final String STICKER_TYPE_CONFIRM = "confirm";
    }

	public static class ActionType {
		public static final String CONFIRM = "confirm";
		public static final String OPEN = "open";
		public static final String TEXT = "text";
		public static final String SELECT = "select";
	}

	public static class ViewType {
		public static final String YESNO = "yesno";
		public static final String DEFAULT = "default";
		public static final String CHECKBOX = "checkbox";
		public static final String LINEAR = "linear";
		public static final String SELECTBOX = "selectbox";
	}

	public static class CallType {
		public static final String VOICE = "voice_call";
		public static final String VIDEO = "video_call";
	}

	public static class LocationService {
		public static final String START = "location_start";
		public static final String STOP = "location_stop";
		public static final String STOP_ALL = "location_stop_all";
		public static final String UPDATE = "location_update";
	}

	public static class BroadcastAction {
		public static final String UPDATE_STATUS = "ly.appsocial.chatcenter.update_status";
		public static final String UPDATE_CHAT = "ly.appsocial.chatcenter.update_chat";
		public static final String RELOAD_CHAT = "ly.appsocial.chatcenter.reload_chat";
	}

	public static final String PUSH_CATEGORY = "chat message";

	public static class QuestionWidget {
		public static final int TITLE_MAX_LENGTH = 100;
		public static final int QUESTION_MAX_LENGTH = 100;
		public static final int MAX_CHOICE = 10;
	}
}
