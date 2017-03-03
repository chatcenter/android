package ly.appsocial.chatcenter.util;

import android.content.Context;
import android.content.SharedPreferences;

import ly.appsocial.chatcenter.dto.ChannelItem;

/**
 * プリファレンスのユーティリティ
 */
public class PreferenceUtil {

	public static final String PREF_FILE_KEY = "chatcenter";
	public static final String PREF_LAST_ORG_UID = "last_selected_org_uid";
	public static final String PREF_LAST_APP_ID = "last_selected_app_id";

	public static final String PREF_LAST_CHANNEL_STATUS = "last_channel_status";
	public static final String PREF_LAST_CHANNEL_FUNNEL = "last_channel_funnel";
	public static final String PREF_LAST_CHANNEL_FILTER_STRING = "last_channel_filter_string";

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

	public static void saveLastFunnelId(Context context, int funnelId) {
		getPreferences(context).edit()
				.putInt(PREF_LAST_CHANNEL_FUNNEL, funnelId)
				.apply();
	}

	public static int getLastFunnelId(Context context) {
		return getPreferences(context).getInt(PREF_LAST_CHANNEL_FUNNEL, -1);
	}

	public static void saveLastChannelStatus(Context context, ChannelItem.ChannelStatus status) {
		getPreferences(context).edit()
				.putInt(PREF_LAST_CHANNEL_STATUS, status.ordinal())
				.apply();
	}

	public static ChannelItem.ChannelStatus getLastChannelStatus(Context context) {
		int ordinal = getPreferences(context).getInt(PREF_LAST_CHANNEL_STATUS, -1);
		return ordinal < 0 ? ChannelItem.ChannelStatus.CHANNEL_ALL: ChannelItem.ChannelStatus.values()[ordinal];
	}

	public static void saveLastChannelFilterString(Context context, String filterString) {
		getPreferences(context).edit()
				.putString(PREF_LAST_CHANNEL_FILTER_STRING, filterString)
				.apply();
	}

	public static String getLastChannelFilterString(Context context) {
		return getPreferences(context).getString(PREF_LAST_CHANNEL_FILTER_STRING, "All");
	}
}
