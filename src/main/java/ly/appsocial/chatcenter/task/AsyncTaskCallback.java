package ly.appsocial.chatcenter.task;

/**
 * タスクコールバックのインタフェース
 */
public interface AsyncTaskCallback<Result> {

	/**
	 * キャンセル時のハンドラ。
	 */
	void onCancel();

	/**
	 * エラー時のハンドラ。
	 *
	 * @param errorCode エラーコード
	 */
	void onError(int errorCode);

	/**
	 * 成功時のハンドラ。
	 *
	 * @param result 結果
	 */
	void onSuccess(Result result);
}