package ly.appsocial.chatcenter.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

/**
 * プログレスダイアログフラグメント。
 */
public class ProgressDialogFragment extends DialogFragment {

	// //////////////////////////////////////////////////////////////////////////
	// staticフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** メッセージのバンドルキー */
	private final static String KEY_MSG = "msg";

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** ダイアログリスナー */
	private DialogListener listener;

	/**
	 * ダイアログリスナー
	 */
	public interface DialogListener {
		/**
		 * ダイアログをキャンセルした際のコールバック。
		 *
		 * @param tag このフラグメントのタグ
		 */
		void onDialogCancel(String tag);
	}

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);

		final Fragment fragment = getTargetFragment();
		if (fragment instanceof DialogListener) {
			listener = (DialogListener) fragment;
		} else if (activity instanceof DialogListener) {
			listener = (DialogListener) activity;
		} else {
			throw new ClassCastException(activity.toString() + " must implement DialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {

		final Bundle args = getArguments();

		final ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setMessage(args.getString(KEY_MSG));
		dialog.setIndeterminate(true);
		dialog.setCanceledOnTouchOutside(false);
		// dialog.setCancelable(false);
		/*
		 * Cancelable は Fragment 側で設定しないと有効にならない。
		 */

		return dialog;
	}

	@Override
	public void onCancel(final DialogInterface dialog) {
		listener.onDialogCancel(getTag());
	}

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * ダイアログインスタンスを生成します。
	 *
	 * @param msg メッセージ
	 * @return ダイアログのインスタンス
	 */
	public static ProgressDialogFragment newInstance(final String msg) {
		final Bundle args = new Bundle();
		if (msg == null) {
			args.putString(KEY_MSG, "読み込み中...");
		} else {
			args.putString(KEY_MSG, msg);
		}

		final ProgressDialogFragment dialog = new ProgressDialogFragment();
		dialog.setArguments(args);
		return dialog;
	}
}
