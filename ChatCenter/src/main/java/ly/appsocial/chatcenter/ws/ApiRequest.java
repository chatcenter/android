package ly.appsocial.chatcenter.ws;

import com.squareup.okhttp.Response;

/**
 * Chat Center リクエスト
 */
public interface ApiRequest<T> {

	/**
	 * Enqueue the request
	 *
	 * @param tag
     */
	void enqueue(String tag);

	/**
	 * Modify the API token
	 *
	 * @param token
     */
	void setApiToken(String token);

	/**
	 * Set JSON body
	 * @param body
     */
	void setJsonBody(String body);

	public static class Method {
		public static final int POST 	= 0;
		public static final int GET 	= 1;
		public static final int PUT 	= 2;
		public static final int DELETE 	= 3;
		public static final int PATCH 	= 4;
	}

	// //////////////////////////////////////////////////////////////////////////
	// インナークラス
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * コールバックのインタフェース
	 *
	 * @param <T> レスポンスDTOの型
	 */
	public interface Callback<T> {
		/**
		 * レスポンスが成功の場合のコールバック
		 *
		 * @param responseDto レスポンスDTO
		 */
		public void onSuccess(T responseDto);

		/**
		 * レスポンスが失敗の場合のコールバック
		 *
		 * @param error エラー
		 */
		public void onError(Error error);
	}

	/**
	 * パーサーのインタフェース
	 *
	 * @param <T> レスポンスDTOの型
	 */
	public interface Parser<T> {
		/**
		 * パース結果がエラーかどうかの判定時に呼ばれます。
		 *
		 * @return エラーコード
		 */
		public int getErrorCode();

		/**
		 * パース実行時に呼ばれます。
		 *
		 * @param response HTTPレスポンス文字列
		 * @return レスポンスDTO
		 */
		public T parser(String response);
	}

	/**
	 * Unified error class
	 */
	public static class Error {
		private Response networkResponse;
		private Exception error;

		public Error(Response networkResponse) {
			this.networkResponse = networkResponse;
		}
		public Error(Exception error) {
			this.error = error;
		}

		public Response getNetworkResponse() {
			return networkResponse;
		}

		public Exception getError() {
			return error;
		}

	}
}
