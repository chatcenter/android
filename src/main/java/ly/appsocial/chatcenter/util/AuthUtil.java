package ly.appsocial.chatcenter.util;

import android.content.Context;
import android.content.SharedPreferences;

import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.UserItem;

/**
 * 認証ユーティリティ。
 */
public class AuthUtil {

	/**
	 * 保存されているトークン生成タイムスタンプ(ms)を取得します。
	 *
	 * @param context
	 * @return タイムスタンプ。ない場合は0
	 */
	public static long getProviderTokenTimestamp(Context context) {
		return PreferenceUtil.getPreferences(context).getLong(ChatCenterConstants.Preference.TOKEN_TIMESTAMP, 0);
	}

	/**
	 * 保存されているUserトークンを取得します。
	 * 
	 * @param context
	 * @return Userトークン。無い場合は空文字。
	 */
	public static String getUserToken(Context context) {
		return PreferenceUtil.getPreferences(context).getString(ChatCenterConstants.Preference.USER_TOKEN, "");
	}

	/**
	 * 保存されているUser IDを取得します。
	 *
	 * @param context
	 * @return User ID。無い場合は空文字。
	 */
	public static int getUserId(Context context) {
		return PreferenceUtil.getPreferences(context).getInt(ChatCenterConstants.Preference.USER_ID, 0);
	}

	/**
	 * Check if the current user is admin
	 *
	 * @param context
	 * @return true if current user is admin, false otherwise
	 */
	public static boolean isCurrentUserAdmin(Context context) {
		return PreferenceUtil.getPreferences(context).getBoolean(ChatCenterConstants.Preference.USER_ADMIN, false);
	}

	/**
	 *
	 * @param context
	 * @param userToken Userトークン
	 * @param userId User ID
	 */
	@Deprecated
	public static void saveTokens(Context context, long tokenTimestamp, String userToken, int userId) {
		final SharedPreferences.Editor editor = PreferenceUtil.getPreferences(context).edit();
		editor.putLong(ChatCenterConstants.Preference.TOKEN_TIMESTAMP, tokenTimestamp);
		editor.putString(ChatCenterConstants.Preference.USER_TOKEN, userToken);
		editor.putInt(ChatCenterConstants.Preference.USER_ID, userId);
		editor.commit();
	}

	public static void saveTokens(Context context, long tokenTimestamp, UserItem user) {
		final SharedPreferences.Editor editor = PreferenceUtil.getPreferences(context).edit();
		editor.putLong(ChatCenterConstants.Preference.TOKEN_TIMESTAMP, tokenTimestamp);
		editor.putString(ChatCenterConstants.Preference.USER_TOKEN, user.token);
		editor.putInt(ChatCenterConstants.Preference.USER_ID, user.id);
		editor.putBoolean(ChatCenterConstants.Preference.USER_ADMIN, user.admin);
		editor.commit();
	}

	public static void saveDeviceToken(Context context, String token) {
		SharedPreferences.Editor editor = PreferenceUtil.getPreferences(context).edit();
		editor.putString(ChatCenterConstants.Preference.DEVICE_TOKEN, token).commit();
	}

	public static String getDeviceToken(Context context) {
		SharedPreferences pref = PreferenceUtil.getPreferences(context);

		return pref.getString(ChatCenterConstants.Preference.DEVICE_TOKEN, null);
	}

}
