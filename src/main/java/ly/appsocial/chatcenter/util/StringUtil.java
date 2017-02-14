package ly.appsocial.chatcenter.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

/**
 * String ユーティリティ
 */
public class StringUtil {

	/**
	 * 文字列がブランクかどうかを判定します。
	 * 
	 * @param s 文字列
	 * @return ブランクの場合は true、そうでない場合は false
	 */
	public static boolean isBlank(String s) {
		if (s == null || s.length() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 文字列がブランクで無いかどうかを判定します。
	 * 
	 * @param s 文字列
	 * @return ブランクの場合は true、そうでない場合は false
	 */
	public static boolean isNotBlank(String s) {
		return !isBlank(s);
	}

	/**
	 * This function will take an URL as input and return the file name.
	 *
	 * @param url The input URL
	 * @return The URL file name
	 */
	public static String getFileNameFromUrl(URL url) {

		String urlString = url.getFile();

		return urlString.substring(urlString.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];
	}

	/** Check if String is json object*/
	public static boolean isJSONValid(String testString) {
		try {
			new JSONObject(testString);
		} catch (JSONException ex) {
			// edited, to include @Arthur's comment
			// e.g. in case JSONArray is valid as well...
			try {
				new JSONArray(testString);
			} catch (JSONException ex1) {
				return false;
			}
		}
		return true;
	}
}
