package ly.appsocial.chatcenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ly.appsocial.chatcenter.activity.ChatActivity;
import ly.appsocial.chatcenter.activity.MessagesActivity;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ChannelItem;
import ly.appsocial.chatcenter.dto.param.ChatParamDto;
import ly.appsocial.chatcenter.dto.param.MessagesParamDto;
import ly.appsocial.chatcenter.dto.ws.response.GetChannelsMineResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostDevicesSignInResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostDevicesSignOutResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostUsersAuthResponseDto;
import ly.appsocial.chatcenter.dto.ws.response.PostUsersResponseDto;
import ly.appsocial.chatcenter.util.ApiUtil;
import ly.appsocial.chatcenter.util.AuthUtil;
import ly.appsocial.chatcenter.util.NetworkQueueHelper;
import ly.appsocial.chatcenter.util.PreferenceUtil;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.ws.ApiRequest;
import ly.appsocial.chatcenter.ws.OkHttpApiRequest;
import ly.appsocial.chatcenter.ws.parser.GetChannelsMineParser;

/**
 * ChatCenter SDK main class
 *
 */
public class ChatCenter {

	// //////////////////////////////////////////////////////////////////////////
	// ChatCenterClient
	// //////////////////////////////////////////////////////////////////////////
	private static ChatCenterClient mClient;
	public static ChatCenterClient client(Context context) {
		if (mClient == null) {
			mClient = new ChatCenterClient(context.getApplicationContext());
		}
		return mClient;
	}

	public static String mAppToken;
	public static String mAppName;
	public static int mAppIconId;

	/*
	 * Initialize SDK with App information
	 *
	 */
	public static void initChatCenter(Context context, String appName, Integer appIconId ) {
		client(context);

		mAppToken = ApiUtil.getAppToken(context);
		if ( appName != null ) {
			mAppName = appName;
		}
		if ( appIconId != null ) {
			mAppIconId = appIconId.intValue();
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Push notifications
	// //////////////////////////////////////////////////////////////////////////
	public static void getDeviceToken(Context context, ChatCenterClient.GetDeviceTokenCallback callback){
		client(context).getDeviceToken(callback);
	}

	public static void registerDeviceToken(Context context, @Nullable final RegisterDeviceCallback callback) {
		client(context).signInPushNotification(null, new ApiRequest.Callback<PostDevicesSignInResponseDto>() {
			@Override
			public void onSuccess(PostDevicesSignInResponseDto responseDto) {
				if ( callback != null ) {
					callback.onSuccess();
				}
			}

			@Override
			public void onError(ApiRequest.Error error) {
				if ( callback != null ) {
					callback.onError(error);
				}
			}
		});
	}
	public static void registerDeviceToken(Context context, String appToken, @Nullable final RegisterDeviceCallback callback) {
		client(context).signInPushNotification(appToken, new ApiRequest.Callback<PostDevicesSignInResponseDto>() {
			@Override
			public void onSuccess(PostDevicesSignInResponseDto responseDto) {
				if ( callback != null ) {
					callback.onSuccess();
				}
			}

			@Override
			public void onError(ApiRequest.Error error) {
				if ( callback != null ) {
					callback.onError(error);
				}
			}
		});
	}

	private static void signOutDeviceToken(final Context context, final SignOutCallback callback) {
		client(context).signOutPushNotification(new ApiRequest.Callback<PostDevicesSignOutResponseDto>() {
			@Override
			public void onSuccess(PostDevicesSignOutResponseDto responseDto) {
				callback.onSuccess();
			}

			@Override
			public void onError(ApiRequest.Error error) {
				callback.onError(error);
			}
		});
	}
	public static void signInDeviceToken(final Context context, String email, String password,
										 String provider, String providerToken, String providerTokenSecret, long providerCreatedAt, long providerExpiresAt,
										 final SignInCallback callback) {
		client(context).getUserToken(email, password, provider, providerToken, providerTokenSecret, providerCreatedAt,
				providerExpiresAt, new ApiRequest.Callback<PostUsersAuthResponseDto>() {
					@Override
					public void onSuccess(PostUsersAuthResponseDto responseDto) {
						callback.onSuccess();
					}

					@Override
					public void onError(ApiRequest.Error error) {
						callback.onError(error);
					}
				});
	}

	public static void signIn(final Context context, String email, String password, final SignInCallback callback ) {
		client(context).getUserToken(email, password, null, null, null,  0, 0, new ApiRequest.Callback<PostUsersAuthResponseDto>() {
			@Override
			public void onSuccess(PostUsersAuthResponseDto responseDto) {
				callback.onSuccess();
			}

			@Override
			public void onError(ApiRequest.Error error) {
				callback.onError(error);
			}
		});
	}

	public static void signInWithNewUser(final Context context, String orgId, String firstName, String familyName, String email,
										 final SignInCallback callback) {
		client(context).getUserToken(orgId, email, firstName, familyName, null, null, 0, 0, new ApiRequest.Callback<PostUsersResponseDto>() {
			@Override
			public void onSuccess(PostUsersResponseDto responseDto) {
				callback.onSuccess();
			}

			@Override
			public void onError(ApiRequest.Error error) {
				callback.onError(error);
			}
		});
	}

	public static void signOut(final Context context, final SignOutCallback callback) {
		client(context).signOutPushNotification(new ApiRequest.Callback<PostDevicesSignOutResponseDto>() {
			@Override
			public void onSuccess(PostDevicesSignOutResponseDto responseDto) {
				AuthUtil.saveTokens(context, 0, null, 0);
				callback.onSuccess();

				if (mListener != null) {
					mListener.onAgentSignOut(context);
				}
			}

			@Override
			public void onError(ApiRequest.Error error) {
				callback.onError(error);
			}
		});
	}

	/**
	 * CallBack for register device
	 */
	public interface RegisterDeviceCallback {
		void onSuccess();
		void onError(ApiRequest.Error error);
	}

	/**
	 * CallBack for signing in
	 */
	public interface SignInCallback {
		void onSuccess();
		void onError(ApiRequest.Error error);
	}

	/**
	 * CallBack for signing out
	 */
	public interface SignOutCallback {
		void onSuccess();
		void onError(ApiRequest.Error error);
	}

	public static void signOutAgent(final Context context) {
		AuthUtil.saveTokens(context, 0, null, 0);
		AuthUtil.saveDeviceToken(context, null);

		if (mListener != null) {
			mListener.onAgentSignOut(context);
		}
	}

	public static boolean hasChatUser(Context context) {
		return client(context).hasChatUser(context);
	}

	// //////////////////////////////////////////////////////////////////////////
	// Activities manipulation
	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////
	public static void showMessages(final Context context, String email, String password) {
		MessagesParamDto messagesParamDto = new MessagesParamDto();
		messagesParamDto.email = email;
		messagesParamDto.password = password;
		messagesParamDto.isAgent = true;

		// 「履歴」アクティビティの起動
		Intent intent = new Intent(context, MessagesActivity.class);
		intent.putExtra(MessagesParamDto.class.getCanonicalName(), messagesParamDto);
		context.startActivity(intent);
		if (context instanceof Activity) {
			((Activity) context).overridePendingTransition(R.anim.activity_open_enter, 0);
		}
	}

	/**
	 * Open Messages List (History) Activity for client (Not agent)
	 * 履歴画面を表示します。
	 *
	 * @param context
	 */
	public static void showMessages(final Context context) {
		MessagesParamDto messagesParamDto = new MessagesParamDto();
		messagesParamDto.channelType = ChannelItem.ChannelType.CHANNEL_ALL;
		messagesParamDto.channelStatus = ChannelItem.ChannelStatus.CHANNEL_ALL;

		// 「履歴」アクティビティの起動
		Intent intent = new Intent(context, MessagesActivity.class);
		intent.putExtra(MessagesParamDto.class.getCanonicalName(), messagesParamDto);
		context.startActivity(intent);
	}

	/**
	 * Open Messages List (History) Activity
	 * 履歴画面を表示します。
	 *
	 * @param context
	 * @param channelType
	 * @param provider
	 * @param providerToken
	 * @param providerTokenCreateAt
	 * @param providerTokenExpires
	 */
	public static void showMessages(final Context context,
									final ChannelItem.ChannelType channelType,
									final ChannelItem.ChannelStatus channelStatus,
									final String provider, final String providerToken,
									final long providerTokenCreateAt,
									final long providerTokenExpires) {
		MessagesParamDto messagesParamDto = new MessagesParamDto();
		messagesParamDto.provider = provider;
		messagesParamDto.providerToken = providerToken;
		messagesParamDto.providerTokenCreateAt = providerTokenCreateAt;
		messagesParamDto.providerTokenExpires = providerTokenExpires;
		messagesParamDto.channelType = channelType;
		messagesParamDto.channelStatus = channelStatus;

		// 「履歴」アクティビティの起動
		Intent intent = new Intent(context, MessagesActivity.class);
		intent.putExtra(MessagesParamDto.class.getCanonicalName(), messagesParamDto);
		context.startActivity(intent);
	}

	/**
	 * Open Messages List (History) Activity
	 * 履歴画面を表示します。
	 *
	 * @param context コンテキスト
	 * @param providerToken Providerトークン
	 * @param providerTokenCreateAt Providerトークン生成タイムスタンプ(ms)
	 */
	@Deprecated
	public static void showMessages(final Context context, final String provider, final String providerToken, final long providerTokenCreateAt) {

		MessagesParamDto messagesParamDto = new MessagesParamDto();
		messagesParamDto.provider = provider;
		messagesParamDto.providerToken = providerToken;
		messagesParamDto.providerTokenCreateAt = providerTokenCreateAt;

		// 「履歴」アクティビティの起動
		Intent intent = new Intent(context, MessagesActivity.class);
		intent.putExtra(MessagesParamDto.class.getCanonicalName(), messagesParamDto);
		context.startActivity(intent);
	}

	/**
	 * Get Unread Messages Count
	 * 未読メッセージ件数を取得します。
	 *
	 * @param context コンテキスト
	 * @param providerToken Providerトークン
	 * @param providerTokenCreateAt providerToken生成タイムスタンプ(ms)
	 * @param listener 未読メッセージ取得のリスナー
	 */
	public static void getUnreadMessages(Context context, String providerToken, long providerTokenCreateAt, ChatCenter.OnGetUnreadMessagesListener listener) {

		String userToken = AuthUtil.getUserToken(context);
		if (StringUtil.isBlank(userToken)) {
			return;
		}

		// channels/mine request
		String path = "channels/mine";

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authentication", userToken);

		ApiRequest<GetChannelsMineResponseDto> postChannelsMineRequest = new OkHttpApiRequest<GetChannelsMineResponseDto>(context, OkHttpApiRequest.Method.GET, path, null, headers, new GetChannelsMineCallback(listener), new GetChannelsMineParser());
		NetworkQueueHelper.enqueue(postChannelsMineRequest, ChatCenter.class.getSimpleName());
	}

	/**
	 * Open Chat Activity
	 * チャット画面を表示します。
	 *
	 * @param context
	 * @param chatParamDto
	 */
	private static void showChat(Context context, ChatParamDto chatParamDto) {
		// 「チャット」アクティビティの起動
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra(ChatCenterConstants.Extra.CHAT_PARAM, chatParamDto);
		context.startActivity(intent);

		// 表示済みチャットに追加
		SharedPreferences preferences = PreferenceUtil.getPreferences(context);
		Set<String> browsedChats = preferences.getStringSet(ChatCenterConstants.Preference.BROWSED_CHATS, new HashSet<String>(0));
		Set<String> newBrowsedChats = new HashSet<>();
		for (String browsedChat: browsedChats) {
			newBrowsedChats.add(browsedChat);
		}
		newBrowsedChats.add(chatParamDto.kissCd);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putStringSet(ChatCenterConstants.Preference.BROWSED_CHATS, newBrowsedChats);
		editor.commit();
	}

	/**
	 * Open Chat Activity
	 * チャット画面を表示します。
	 *
	 * @param context
	 * @param orgUid
	 * @param firstName
	 * @param familyName
	 * @param email
	 * @param channelInformations
	 */
	public static void showChat(final Context context,
								final String orgUid,
								final String firstName,
								final String familyName,
								final String email,
								final Map<String, String> channelInformations) {
		client(context).getDeviceToken(new ChatCenterClient.GetDeviceTokenCallback() {
			@Override
			public void onSuccess(String deviceToken) {
				ChatParamDto chatParamDto = new ChatParamDto();
				chatParamDto.firstName = firstName;
				chatParamDto.familyName = familyName;
				chatParamDto.email = email;
				chatParamDto.deviceToken = deviceToken;
				chatParamDto.kissCd = orgUid;
				chatParamDto.channelInformations = channelInformations;

				showChat(context, chatParamDto);
			}
			@Override
			public void onError() {
			}
		});
	}

	/**
	 * Open Chat Activity
	 * チャット画面を表示します。
	 *
	 * @param context
	 * @param orgUid
	 * @param provider
	 * @param providerToken
	 * @param providerTokenCreatedAt
	 * @param providerTokenExpiresDate
	 * @param channelInformation
	 */
	public static void showChat(final Context context,
								final String orgUid,
								final String provider,
								final String providerToken,
								final String providerTokenSecret,
								final String providerRefreshToken,
								final long providerTokenCreatedAt,
								final long providerTokenExpiresDate,
								final Map<String, String> channelInformation) {
		client(context).getDeviceToken(new ChatCenterClient.GetDeviceTokenCallback() {
			@Override
			public void onSuccess(String deviceToken) {
				ChatParamDto chatParamDto = new ChatParamDto();
				chatParamDto.provider = provider;
				chatParamDto.providerToken = providerToken;
				chatParamDto.providerTokenSecret = providerTokenSecret;
				chatParamDto.providerRefreshToken = providerRefreshToken;
				chatParamDto.providerTokenCreatedAt = providerTokenCreatedAt;
				chatParamDto.providerTokenExpires = providerTokenExpiresDate;
				chatParamDto.deviceToken = deviceToken;
				chatParamDto.kissCd = orgUid;
				chatParamDto.channelInformations = channelInformation;

				showChat(context, chatParamDto);
			}
			@Override
			public void onError() {
			}
		});
	}

	/**
	 * Open Chat activity after Using Twitter Account for Log In
	 * @param context
	 * @param orgUid
	 * @param providerToken
	 * @param providerTokenSecret
	 * @param channelInformation
     */
	public static void showChatWithTwitterAccount(final Context context,
												  final String orgUid,
												  final String providerToken,
												  final String providerTokenSecret,
												  final Map<String, String> channelInformation) {
		showChat(context,
				orgUid,
				LoginType.TWITTER,
				providerToken,
				providerTokenSecret,
				null,
				0,
				0,
				channelInformation);
	}

	/**
	 * Open Chat activity after Using Facebook Account for Log In
	 * @param context
	 * @param orgUid
	 * @param providerToken
	 * @param providerTokenExpiresDate
	 * @param channelInformation
	 */
	public static void showChatWithFacebookAccount(final Context context,
												  final String orgUid,
												  final String providerToken,
												  final long providerTokenExpiresDate,
												  final Map<String, String> channelInformation) {
		showChat(context,
				orgUid,
				LoginType.FACEBOOK,
				providerToken,
				null,
				null,
				0,
				providerTokenExpiresDate,
				channelInformation);
	}

	public static void showChatWithGoogleAccount(final Context context,
												   final String orgUid,
												   final String providerToken,
												   final String providerRefreshToken,
												   final Map<String, String> channelInformation) {
		showChat(context,
				orgUid,
				LoginType.GOOGLE,
				providerToken,
				null,
				providerRefreshToken,
				0,
				0,
				channelInformation);
	}

	public static void showChatWithYahooJPAccount(final Context context,
												 final String orgUid,
												 final String providerToken,
												 final String providerRefreshToken,
												 final Map<String, String> channelInformation) {
		showChat(context,
				orgUid,
				LoginType.YAHOO,
				providerToken,
				null,
				providerRefreshToken,
				0,
				0,
				channelInformation);
	}

	/**
	 *
	 * @param context
	 * @param orgUid
	 * @param channelInformations
	 * @param providerTokenCreatedAt
	 * @return
	 */
	public static Intent getShowChatIntent(final Context context,
										   final String orgUid, final String channelUid,
										   final Map<String, String> channelInformations,
										   String provider,
										   long providerTokenCreatedAt, long providerTokenExpires) {
		ChatParamDto chatParamDto = new ChatParamDto();
		chatParamDto.kissCd = orgUid;
		chatParamDto.channelUid = channelUid;
		chatParamDto.channelInformations = channelInformations;
		chatParamDto.provider = provider;
		chatParamDto.providerTokenExpires = providerTokenExpires;
		chatParamDto.providerTokenCreatedAt = providerTokenCreatedAt;
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra(ChatCenterConstants.Extra.CHAT_PARAM, chatParamDto);
		return intent;
	}
	public static Intent getShowChatIntent(final Context context,
										   final String orgUid, String channelUid,
										   final Map<String, String> channelInformations) {
		return getShowChatIntent(context, orgUid, channelUid, channelInformations, null, 0, 0);
	}
	public static Intent getShowChatIntent(final Context context,
										   final String orgUid,
										   final Map<String, String> channelInformations) {
		return getShowChatIntent(context, orgUid, null, channelInformations, null, 0, 0);
	}

	/**
	 * Open Chat Activity
	 * チャット画面を表示します。
	 *
	 * @param context コンテキスト
	 * @param providerToken Providerトークン
	 * @param providerTokenCreatedAt Providerトークン生成タイムスタンプ(ms)
	 * @param kissCd KISSコード
	 */
	@Deprecated
	public static void showChat(final Context context, final String providerToken, final long providerTokenCreatedAt, final String kissCd) {

		ChatParamDto chatParamDto = new ChatParamDto();
		chatParamDto.provider = null;
		chatParamDto.providerToken = providerToken;
		chatParamDto.providerTokenCreatedAt = providerTokenCreatedAt;
		chatParamDto.kissCd = kissCd;

		// 「チャット」アクティビティの起動
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra(ChatCenterConstants.Extra.CHAT_PARAM, chatParamDto);
		context.startActivity(intent);

		// 表示済みチャットに追加
		SharedPreferences preferences = PreferenceUtil.getPreferences(context);
		Set<String> browsedChats = preferences.getStringSet(ChatCenterConstants.Preference.BROWSED_CHATS, new HashSet<String>(0));
		Set<String> newBrowsedChats = new HashSet<>();
		for (String browsedChat: browsedChats) {
			newBrowsedChats.add(browsedChat);
		}
		newBrowsedChats.add(kissCd);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putStringSet(ChatCenterConstants.Preference.BROWSED_CHATS, newBrowsedChats);
		editor.commit();
	}

	/**
	 * Is this chat is already browsed
	 * 表示済みチャットかを判定します。
	 *
	 * @param context コンテキスト
	 * @param kissCd KISSコード
	 * @return 同意済みの場合は true、そうでない場合は false
	 */
	public static boolean isBrowsedChat(Context context, String kissCd) {
		if (StringUtil.isBlank(kissCd)) {
			return false;
		}
		return PreferenceUtil.getPreferences(context).getStringSet(ChatCenterConstants.Preference.BROWSED_CHATS, new HashSet<String>(0)).contains(kissCd);
	}

	/**
	 * Clear browsed chat
	 * 表示済みチャットをクリアします。
	 *
	 * @param context コンテキスト
	 */
	public static void clearBrowsedChats(Context context) {
		final SharedPreferences.Editor editor = PreferenceUtil.getPreferences(context).edit();
		editor.putStringSet(ChatCenterConstants.Preference.BROWSED_CHATS, new HashSet<String>(0));
		editor.commit();
	}

	// //////////////////////////////////////////////////////////////////////////
	// コールバック
	// //////////////////////////////////////////////////////////////////////////
	/**
	 * GET /api/channels/mine のコールバック
	 */
	private static class GetChannelsMineCallback implements ApiRequest.Callback<GetChannelsMineResponseDto> {

		/** 未読メッセージ取得のリスナー */
		private ChatCenter.OnGetUnreadMessagesListener listener;

		/**
		 * コールバック
		 *
		 * @param listener 未読メッセージ取得のリスナー
		 */
		public GetChannelsMineCallback(ChatCenter.OnGetUnreadMessagesListener listener) {
			this.listener = listener;

		}

		@Override
		public void onError(ApiRequest.Error error) {
			// empty
		}

		@Override
		public void onSuccess(GetChannelsMineResponseDto responseDto) {
			int unreadMessages = 0;
			if (responseDto.items != null) {
				for (GetChannelsMineResponseDto.Channel item : responseDto.items) {
					if (item.isClosed()) {
						continue;
					}
					unreadMessages += item.unreadMessages;
				}
			}
			listener.onGetUnreadMessages(unreadMessages);
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// インナークラス
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * 未読メッセージ取得のリスナー。
	 */
	public interface OnGetUnreadMessagesListener {
		/**
		 * 未読メッセージ取得のコールバック。
		 *
		 * @param unreads 未読メッセージ件数
		 */
		public void onGetUnreadMessages(int unreads);
	}

	// //////////////////////////////////////////////////////////////////////////
	// Listener
	// //////////////////////////////////////////////////////////////////////////
	private static ChatCenterListener mListener;
	public static void setListener(ChatCenterListener listener) {
		mListener = listener;
	}
	public interface ChatCenterListener {
		void onAgentSignOut(Context context);
	}

	// /////////////////////////////////////////////////
	// TOP activity
	// /////////////////////////////////////////////////
	private static AppCompatActivity mTopActivity;

	public static AppCompatActivity getTopActivity() {
		return mTopActivity;
	}

	public static void setTopActivity(AppCompatActivity topActivity) {
		ChatCenter.mTopActivity = topActivity;
	}

	public interface LoginType {
		String EMAIL = "email";
		String FACEBOOK = "facebook";
		String TWITTER = "twitter";
		String GOOGLE = "google";
		String YAHOO = "yahoojp";
	}
}