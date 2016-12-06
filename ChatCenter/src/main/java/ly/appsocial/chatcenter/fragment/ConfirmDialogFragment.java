package ly.appsocial.chatcenter.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

/**
 * 確認ダイアログフラグメント。
 *
 */
public class ConfirmDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

	// //////////////////////////////////////////////////////////////////////////
	// staticフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** タイトルのバンドルキー */
	private final static String KEY_TITLE = "title";

	/** メッセージのバンドルキー */
	private final static String KEY_MSG = "msg";

	/** 肯定ボタンテキストのバンドルキー */
	private final static String KEY_POSITIVE_BUTTON_TEXT = "positiveButtonText";

	/** 否定ボタンテキストのバンドルキー */
	private final static String KEY_NEGATIVE_BUTTON_TEXT = "negativeButtonText";

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

		/**
		 * ダイアログの肯定ボタンを押下した際のコールバック。
		 *
		 * @param tag このフラグメントのタグ
		 */
		void onPositiveButtonClick(String tag);

		/**
		 * ダイアログの否定ボタンを押下した際のコールバック。
		 *
		 * @param tag このフラグメントのタグ
		 */
		void onNegativeButtonClick(String tag);
	}

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

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

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(args.getString(KEY_TITLE));
		builder.setMessage(args.getString(KEY_MSG));
		builder.setPositiveButton(args.getString(KEY_POSITIVE_BUTTON_TEXT), this);
		builder.setNegativeButton(args.getString(KEY_NEGATIVE_BUTTON_TEXT), this);
		final AlertDialog dialog = builder.create();

		// dialog.setIndeterminate(true);
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

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			listener.onPositiveButtonClick(getTag());
		} else if (which == DialogInterface.BUTTON_NEGATIVE) {
			listener.onNegativeButtonClick(getTag());
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * @return
	 */
	public static ConfirmDialogFragment newInstance(final String title, final String msg, final String positiveButtonText, final String negaiveButtonText) {
		final ConfirmDialogFragment dialog = new ConfirmDialogFragment();

		final Bundle args = new Bundle();
		args.putString(KEY_TITLE, title);
		args.putString(KEY_MSG, msg);
		if (positiveButtonText == null) {
			args.putString(KEY_POSITIVE_BUTTON_TEXT, "OK");
		} else {
			args.putString(KEY_POSITIVE_BUTTON_TEXT, positiveButtonText);
		}
		if (negaiveButtonText == null) {
			args.putString(KEY_NEGATIVE_BUTTON_TEXT, "キャンセル");
		} else {
			args.putString(KEY_NEGATIVE_BUTTON_TEXT, negaiveButtonText);
		}
		dialog.setArguments(args);
		return dialog;
	}
}
