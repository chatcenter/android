/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.Locale;

import ly.appsocial.chatcenter.BuildConfig;
import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;

/**
 * Created by karasawa on 2016/12/01.
 */

public class ApiUtil {
	public static String getAppToken(Context context){
		String appToken = null;
		try {
			ApplicationInfo info = context.getPackageManager().getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
			appToken = info.metaData.getString(context.getString(R.string.apptoken_param));
		}   catch (PackageManager.NameNotFoundException e) {
		}
		return appToken;
	}

	public static String getApiUrl(Context context){
		return context.getString(R.string.api_chatcenter);
	}

	public static String getWsUrl(Context context){
		return context.getString(R.string.url_chatcenter_websocket);
	}

	public static String getPassRecoveryUrl(Context context) {
		String passwordUrl = context.getString(R.string.password_recovery_chatcenter);

		if ( passwordUrl != null ){
			Locale locale = Locale.getDefault();
			if ( locale.equals(Locale.JAPAN) ){
				passwordUrl += "?locale=ja";
			} else {
				passwordUrl += "?locale=en";
			}
		}
		return passwordUrl;
	}
}
