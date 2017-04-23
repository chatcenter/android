package ly.appsocial.chatcenter.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ChannelItem;
import ly.appsocial.chatcenter.dto.ws.response.GetMeResponseDto;

/**
 * プリファレンスのユーティリティ
 */
public class CCPrefUtils {

	public static final String PREF_FILE_KEY = "chatcenter";

	public static SharedPreferences getPreferences(Context context) {
		return context.getSharedPreferences(PREF_FILE_KEY, Context.MODE_PRIVATE);
	}

	/** アプリを閉じる前に使っているORGを保存する*/
	public static void saveLastOrgUid(Context context, String orgUid) {
		getPreferences(context).edit()
				.putString(ChatCenterConstants.Preference.LAST_ORG_UID, orgUid)
				.apply();
	}

	public static String getLastOrgUid(Context context) {
		return getPreferences(context).getString(ChatCenterConstants.Preference.LAST_ORG_UID, null);
	}

	public static void saveLastAppId(Context context, String appIid) {
		getPreferences(context).edit()
				.putString(ChatCenterConstants.Preference.LAST_APP_ID, appIid)
				.apply();
	}

	public static String getLastAppId(Context context) {
		return getPreferences(context).getString(ChatCenterConstants.Preference.LAST_APP_ID, null);
	}

	public static void saveLastFunnelId(Context context, int funnelId) {
		getPreferences(context).edit()
				.putInt(ChatCenterConstants.Preference.LAST_CHANNEL_FUNNEL, funnelId)
				.apply();
	}

	public static int getLastFunnelId(Context context) {
		return getPreferences(context).getInt(ChatCenterConstants.Preference.LAST_CHANNEL_FUNNEL, -1);
	}

	public static void saveLastChannelStatus(Context context, ChannelItem.ChannelStatus status) {
		getPreferences(context).edit()
				.putInt(ChatCenterConstants.Preference.LAST_CHANNEL_STATUS, status.ordinal())
				.apply();
	}

	public static ChannelItem.ChannelStatus getLastChannelStatus(Context context) {
		int ordinal = getPreferences(context).getInt(ChatCenterConstants.Preference.LAST_CHANNEL_STATUS,
				ChannelItem.ChannelStatus.CHANNEL_ALL.ordinal());

		return ChannelItem.ChannelStatus.values()[ordinal];
	}

	public static void saveLastChannelFilterString(Context context, String filterString) {
		getPreferences(context).edit()
				.putString(ChatCenterConstants.Preference.LAST_CHANNEL_FILTER_STRING, filterString)
				.apply();
	}

	public static String getLastChannelFilterString(Context context) {
		return getPreferences(context).getString(ChatCenterConstants.Preference.LAST_CHANNEL_FILTER_STRING, context.getString(R.string.all));
	}

	public static void saveUserConfig(Context context, GetMeResponseDto responseDto) {
		String config = "";
		if (responseDto != null) {
			Gson gson = new Gson();
			config = gson.toJson(responseDto);
		}
		getPreferences(context).edit()
				.putString(ChatCenterConstants.Preference.USER_CONFIG, config)
				.apply();
	}

	public static GetMeResponseDto getUserConfig(Context context) {
		String configString = getPreferences(context).getString(ChatCenterConstants.Preference.USER_CONFIG, "");
		if (StringUtil.isNotBlank(configString) && StringUtil.isJSONValid(configString)) {
			try {
				return new Gson().fromJson(configString, GetMeResponseDto.class);
			} catch (Exception e) {
				return null;
			}
		}
		return  null;
	}

	/**
	 * Clear all data from preference
	 * @param context
	 */
	public static void clear(Context context) {
		saveLastAppId(context, null);
		saveLastChannelFilterString(context, context.getString(R.string.all));
		saveUserConfig(context, null);
		saveLastFunnelId(context, -1);
		saveLastChannelStatus(context, ChannelItem.ChannelStatus.CHANNEL_ALL);
		saveLastOrgUid(context, null);
	}
}
