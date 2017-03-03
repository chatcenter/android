package ly.appsocial.chatcenter.util;

import android.util.Log;

import ly.appsocial.chatcenter.BuildConfig;

public class CCLog {
    /**
     * AndroidのLog.dのラップメソッド。AndroidManifest.xmlのdebbugable設定でログ出力を切り分ける。
     * debuggableが TRUE の場合ログを出力し、FALSE の場合はログを出力しない。
     *
     * @param tag
     *            タグ文字列
     * @param msg
     *            ログメッセージ
     */
    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
    }

    /**
     * AndroidのLog.dのラップメソッド。AndroidManifest.xmlのdebbugable設定でログ出力を切り分ける。
     * debuggableが TRUE の場合ログを出力し、FALSE の場合はログを出力しない。
     *
     * @param tag
     *            タグ文字列
     * @param msg
     *            ログメッセージ
     */
    public static void d(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg, tr);
        }
    }

    /**
     * AndroidのLog.eのラップメソッド。AndroidManifest.xmlのdebbugable設定でログ出力を切り分ける
     * 。 debuggableが TRUE の場合ログを出力し、FALSE の場合はログを出力しない。
     *
     * @param tag
     *            タグ文字列
     * @param msg
     *            ログメッセージ
     */
    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg);
        }
    }

    /**
     * AndroidのLog.eのラップメソッド。AndroidManifest.xmlのdebbugable設定でログ出力を切り分ける
     * 。 debuggableが TRUE の場合ログを出力し、FALSE の場合はログを出力しない。
     *
     * @param tag
     *            タグ文字列
     * @param msg
     *            ログメッセージ
     */
    public static void e(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg, tr);
        }
    }
}
