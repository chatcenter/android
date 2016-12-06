package ly.appsocial.chatcenter.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * プリファレンスのユーティリティ
 */
public class PreferenceUtil {

	/**
	 * ChatCenter 用のプリファレンスを取得します。
	 * 
	 * @param context コンテキスト
	 * @return プリファレンス
	 */
	public static SharedPreferences getPreferences(Context context) {
		return context.getSharedPreferences("chatcenter", Context.MODE_PRIVATE);
	}
}
