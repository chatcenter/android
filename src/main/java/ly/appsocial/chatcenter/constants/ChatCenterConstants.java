package ly.appsocial.chatcenter.constants;

/**
 * ChatCenter 全体の定数定義クラス
 */
public class ChatCenterConstants {

	/**
	 * プリファレンスキー
	 */
	public static class Preference {
		/** Providerトークン生成タイムスタンプ(ms) */
		public static final String TOKEN_TIMESTAMP = "chatcenter_token_timestamp";
		/** Userトークン */
		public static final String USER_TOKEN = "chatcenter_user_token";
		/** User ID */
		public static final String USER_ID = "chatcenter_user_id";
		public static final String USER_ADMIN = "chatcenter_user_admin";

		public static final String DEVICE_TOKEN = "chatcenter-device-token";
		/**
		 * 表示済みチャットリスト
		 * <p>
		 * 問い合わせ確認画面のスキップ判定に使用します。値は「KISSコード」
		 * </p>
		 */
		public static final String BROWSED_CHATS = "chatcenter_browsed_chats";
	}

	public static class Extra {
		public static final String CHANNEL_UID = "channel_uid";
		public static final String URL = "url";
		public static final String IS_AGENT = "is_agent";
		public static final String ORG = "org";
		public static final String APP = "app";
		public static final String ACTIVITY_TITLE = "activity_title";
		public static final String FUNNEL_LIST = "funnel_list";
	}

	public static class StickerName {
		public static final String STICKER_TYPE_DATE_TIME_AVAILABILITY = "datetime";
		public static final String STICKER_TYPE_TYPE_FILE = "file_upload";
		public static final String STICKER_TYPE_LOCATION = "location";
		public static final String STICKER_TYPE_QUESTION = "yes_no";
		public static final String STICKER_TYPE_FIXED_PHRASE = "fixed_phrase";
		public static final String STICKER_TYPE_VIDEO_CHAT = "video_chat";
		public static final String STICKER_TYPE_VOICE_CHAT = "voice_chat";
		public static final String STICKER_TYPE_CAMERA = "camera_upload";
		public static final String STICKER_TYPE_TYPE_PHONE_CALL = "phone_call";
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
	}

	public static class CallType {
		public static final String VOICE = "voice_call";
		public static final String VIDEO = "video_call";
	}

	public static final String PUSH_CATEGORY = "chat message";
}
