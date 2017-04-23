package ly.appsocial.chatcenter.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.UserItem;

/**
 * 認証ユーティリティ。
 */
public class CCAuthUtil {

	/**
	 * 保存されているトークン生成タイムスタンプ(ms)を取得します。
	 *
	 * @param context
	 * @return タイムスタンプ。ない場合は0
	 */
	public static long getProviderTokenTimestamp(Context context) {
		return CCPrefUtils.getPreferences(context).getLong(ChatCenterConstants.Preference.TOKEN_TIMESTAMP, 0);
	}

	/**
	 * 保存されているUserトークンを取得します。
	 * 
	 * @param context
	 * @return Userトークン。無い場合は空文字。
	 */
	public static String getUserToken(Context context) {
		UserItem currentUser = getCurrentUser(context);
		if (currentUser == null) {
			return "";
		} else {
			return  currentUser.token;
		}
	}

	/**
	 * 保存されているUser IDを取得します。
	 *
	 * @param context
	 * @return User ID。無い場合は空文字。
	 */
	public static int getUserId(Context context) {
		UserItem currentUser = getCurrentUser(context);
		if (currentUser == null) {
			return 0;
		} else {
			return  currentUser.id;
		}
	}

	/**
	 * Check if the current user is admin
	 *
	 * @param context
	 * @return true if current user is admin, false otherwise
	 */
	public static boolean isCurrentUserAdmin(Context context) {
		UserItem currentUser = getCurrentUser(context);
		if (currentUser == null) {
			return false;
		} else {
			return  currentUser.admin;
		}
	}

	public static void saveTokens(Context context, long tokenTimestamp, UserItem user) {
		final SharedPreferences.Editor editor = CCPrefUtils.getPreferences(context).edit();
		String userJSONString = "";
		if (user != null) {
			userJSONString = new Gson().toJson(user);
		}
		editor.putLong(ChatCenterConstants.Preference.TOKEN_TIMESTAMP, tokenTimestamp);
		editor.putString(ChatCenterConstants.Preference.USER, userJSONString);
		editor.commit();
	}

	/**
	 * Current logged in user
	 * @param context
	 * @return
	 */
	public static UserItem getCurrentUser(Context context) {
		String userJsonString =CCPrefUtils.getPreferences(context).getString(ChatCenterConstants.Preference.USER, "");
		if (StringUtil.isNotBlank(userJsonString) && StringUtil.isJSONValid(userJsonString)) {
			return new Gson().fromJson(userJsonString, UserItem.class);
		}

		return null;
	}

	public static void saveDeviceToken(Context context, String token) {
		SharedPreferences.Editor editor = CCPrefUtils.getPreferences(context).edit();
		editor.putString(ChatCenterConstants.Preference.DEVICE_TOKEN, token).commit();
	}

	public static String getDeviceToken(Context context) {
		SharedPreferences pref = CCPrefUtils.getPreferences(context);
		return pref.getString(ChatCenterConstants.Preference.DEVICE_TOKEN, null);
	}

}
