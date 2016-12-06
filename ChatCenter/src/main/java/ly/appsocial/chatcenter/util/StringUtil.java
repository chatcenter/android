package ly.appsocial.chatcenter.util;

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
	 * <p>Examples :</p>
	 * <ul>
	 * <li>http://example.com/a/b/c/test.txt -> test.txt</li>
	 * <li>http://example.com/ -> an empty string </li>
	 * <li>http://example.com/test.txt?param=value -> test.txt</li>
	 * <li>http://example.com/test.txt#anchor -> test.txt</li>
	 * </ul>
	 *
	 * @param url The input URL
	 * @return The URL file name
	 */
	public static String getFileNameFromUrl(URL url) {

		String urlString = url.getFile();

		return urlString.substring(urlString.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];
	}
}
