package ly.appsocial.chatcenter.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * プリファレンスのユーティリティ
 */
public class PreferenceUtil {

	public static final String PREF_FILE_KEY = "chatcenter";
	public static final String PREF_LAST_ORG_UID = "last_selected_org_uid";
	public static final String PREF_LAST_APP_ID = "last_selected_app_id";

	public static SharedPreferences getPreferences(Context context) {
		return context.getSharedPreferences(PREF_FILE_KEY, Context.MODE_PRIVATE);
	}

	public static void saveLastOrgUid(Context context, String orgUid) {
		getPreferences(context).edit()
				.putString(PREF_LAST_ORG_UID, orgUid)
				.apply();
	}

	public static String getLastOrgUid(Context context) {
		return getPreferences(context).getString(PREF_LAST_ORG_UID, null);
	}

	public static void saveLastAppId(Context context, String appIid) {
		getPreferences(context).edit()
				.putString(PREF_LAST_APP_ID, appIid)
				.apply();
	}

	public static String getLastAppId(Context context) {
		return getPreferences(context).getString(PREF_LAST_APP_ID, null);
	}
}
