package ly.appsocial.chatcenter.util;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import ly.appsocial.chatcenter.fragment.AlertDialogFragment;
import ly.appsocial.chatcenter.fragment.ConfirmDialogFragment;
import ly.appsocial.chatcenter.fragment.ProgressDialogFragment;

/**
 * ダイアログユーティリティ
 */
public class DialogUtil {

	// //////////////////////////////////////////////////////////////////////////
	// static フィールド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * ダイアログタグ
	 */
	public static class Tag {
		/** 401エラー */
		public static final String ERROR_401 = "error_401";
		/** 共通エラー */
		public static final String ERROR = "error";
		/** プログレス */
		public static final String PROGRESS = "progress";
		/** アラート */
		public static final String ALERT = "alert";
		/** 電話確認 */
		public static final String TEL = "tel";
		/** 確認*/
		public static final String CONFIRM = "confirm";
	}

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * プログレスダイアログを表示します。
	 *
	 * @param manager FragmentManager
	 * @param tag タグ
	 */
	public static void showProgressDialog(final FragmentManager manager, final String tag) {
		showDialog(manager, ProgressDialogFragment.newInstance(null), tag);
	}

	public static void showProgressDialog(final FragmentManager manager, String msg, final String tag) {
		showDialog(manager, ProgressDialogFragment.newInstance(msg), tag);
	}

	/**
	 * アラートダイアログを表示します。
	 *
	 * @param manager FragmentManager
	 * @param tag タグ
	 * @param title タイトル
	 * @param msg メッセージ
	 */
	public static void showAlertDialog(final FragmentManager manager, final String tag, final String title, final String msg) {
		showDialog(manager, AlertDialogFragment.newInstance(title, msg, "OK"), tag);
	}

	/**
	 * 確認ダイアログを表示します。
	 *
	 * @param manager FragmentManager
	 * @param tag タグ
	 * @param title タイトル
	 * @param msg メッセージ
	 */
	public static void showConfirmDialog(final FragmentManager manager, final String tag, final String title, final String msg) {
		showDialog(manager, ConfirmDialogFragment.newInstance(title, msg, null, null), tag);
	}

	/**
	 * ダイアログを表示します。
	 *
	 * @param manager FragmentManager
	 * @param dialogFragment ダイアログフラグメント
	 * @param tag タグ
	 */
	private static synchronized void showDialog(final FragmentManager manager, DialogFragment dialogFragment, String tag) {

		// 同じタグのフラグメントがあれば閉じる
		final DialogFragment prev = (DialogFragment) manager.findFragmentByTag(tag);
		if (prev != null) {
			if (prev.getDialog() != null && prev.getDialog().isShowing()) {
				return;
			}
			closeDialog(manager, tag);
		}

		FragmentTransaction transaction = manager.beginTransaction();
		transaction.add(dialogFragment, tag);
		transaction.commitAllowingStateLoss();
		manager.executePendingTransactions();
	}

	/**
	 * ダイアログを閉じます。
	 *
	 * @param manager FragmentManager
	 * @param tag タグ
	 */
	public static synchronized void closeDialog(final FragmentManager manager, final String tag) {
		final Fragment fragment = manager.findFragmentByTag(tag);

		if (fragment == null) {
			return;
		}

		final FragmentTransaction transaction = manager.beginTransaction();
		transaction.remove(fragment);
		transaction.commitAllowingStateLoss();
		manager.executePendingTransactions();
	}
}
