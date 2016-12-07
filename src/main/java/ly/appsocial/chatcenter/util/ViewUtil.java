package ly.appsocial.chatcenter.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

/**
 * View ユーティリティ。
 */
public class ViewUtil {

	// //////////////////////////////////////////////////////////////////////////
	// static フィールド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * アイコンカラーキャッシュ
	 * <p>
	 * チャネルUID ごとにランダムに生成したカラーをキャッシュします。
	 * </p>
	 */
	private static Map<String, Integer> sIconColors = new HashMap<String, Integer>();

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * スペースを空けるためのビューを生成します。
	 *
	 * @param context
	 * @param w 幅(px)
	 * @param h 高さ(px)
	 * @return ビュー
	 */
	public static View getSpaceView(final Context context, final int w, final int h) {

		final LinearLayout layout = new LinearLayout(context);
		final View spaceView = new View(context);
		spaceView.setTag("space");
		layout.addView(spaceView, new LinearLayout.LayoutParams(w, h));

		return layout;
	}

	/**
	 * アイコンテキストの背景色を取得します。
	 * 
	 * @param channelUid チャネルUID
	 * @return RGB カラー
	 */
	public static int getIconColor(String channelUid) {
		Integer color = sIconColors.get(channelUid);
		if (color == null) {
			color = HSBtoRGB((float) Math.random(), 0.6f, 0.71f);
			sIconColors.put(channelUid, color);
		}
		return color;
	}

	/**
	 * ImageView に URI の画像をロードします。
	 *
	 * @param imageView ImageView
	 * @param uri 画像のURL
	 */
	public static void loadImage(final ImageView imageView, final String uri) {
		if (StringUtil.isBlank(uri)) {
			/*
			 * 画像URLにnullを渡すとエラー画像が表示されないし、空文字だと例外が発生するのでそれらの場合には NoImage
			 * を明示的に設定します。
			 */
			Picasso.with(imageView.getContext()).load("blank").into(imageView);
		} else {
			Picasso.with(imageView.getContext()).load(uri).into(imageView);
		}
	}

	/**
	 * ImageView に URI の画像を円形でロードします。
	 *
	 * @param imageView ImageView
	 * @param uri 画像のURL
	 */
	public static void loadImageCircle(final ImageView imageView, final String uri) {
		if (StringUtil.isBlank(uri)) {
			/*
			 * 画像URLにnullを渡すとエラー画像が表示されないし、空文字だと例外が発生するのでそれらの場合には NoImage
			 * を明示的に設定します。
			 */
			Picasso.with(imageView.getContext()).load("blank").transform(new CircleTransformation()).into(imageView);
		} else {
			Picasso.with(imageView.getContext()).load(uri).transform(new CircleTransformation()).into(imageView);
		}
	}

	/**
	 * リストビューでチェックされている項目データを取得します。
	 * <p>
	 * リストビューは選択モードが CHOICE_MODE_SINGLE, CHOICE_MODE_MULTI である必要があります。
	 * </p>
	 * 
	 * @param lv リストビュー
	 * @return 項目データ
	 */
	public static <T> List<T> getCheckedItems(final ListView lv) {

		final List<T> checkedItems = new ArrayList<T>();

		final SparseBooleanArray checkedItemPositions = lv.getCheckedItemPositions();
		for (int i = 0; i < checkedItemPositions.size(); i++) {
			final int position = checkedItemPositions.keyAt(i);
			final boolean checked = checkedItemPositions.get(position);
			if (!checked) {
				continue;
			}
			checkedItems.add((T) lv.getItemAtPosition(position));
		}
		return checkedItems;
	}

	/**
	 * トーストを表示します。
	 * 
	 * @param context コンテキスト
	 * @param msg メッセージ
	 */
	public static void showToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * トーストを表示します。
	 * 
	 * @param context コンテキスト
	 * @param resId 文字列リソースID
	 */
	public static void showToast(Context context, int resId) {
		showToast(context, context.getString(resId));
	}

	/**
	 * HSB を RGB カラーに変換します。
	 * <p>
	 * ※java.awt.Color#HSBtoRGB のコードをまるまる流用しています。
	 * </p>
	 * 
	 * @param hue 色相(0〜0.999...)
	 * @param saturation 彩度(0〜1)
	 * @param brightness 明度(0〜1)
	 * @return RGB カラー
	 */
	public static int HSBtoRGB(float hue, float saturation, float brightness) {
		int r = 0, g = 0, b = 0;
		if (saturation == 0) {
			r = g = b = (int) (brightness * 255.0f + 0.5f);
		} else {
			float h = (hue - (float) Math.floor(hue)) * 6.0f;
			float f = h - (float) Math.floor(h);
			float p = brightness * (1.0f - saturation);
			float q = brightness * (1.0f - saturation * f);
			float t = brightness * (1.0f - (saturation * (1.0f - f)));
			switch ((int) h) {
			case 0:
				r = (int) (brightness * 255.0f + 0.5f);
				g = (int) (t * 255.0f + 0.5f);
				b = (int) (p * 255.0f + 0.5f);
				break;
			case 1:
				r = (int) (q * 255.0f + 0.5f);
				g = (int) (brightness * 255.0f + 0.5f);
				b = (int) (p * 255.0f + 0.5f);
				break;
			case 2:
				r = (int) (p * 255.0f + 0.5f);
				g = (int) (brightness * 255.0f + 0.5f);
				b = (int) (t * 255.0f + 0.5f);
				break;
			case 3:
				r = (int) (p * 255.0f + 0.5f);
				g = (int) (q * 255.0f + 0.5f);
				b = (int) (brightness * 255.0f + 0.5f);
				break;
			case 4:
				r = (int) (t * 255.0f + 0.5f);
				g = (int) (p * 255.0f + 0.5f);
				b = (int) (brightness * 255.0f + 0.5f);
				break;
			case 5:
				r = (int) (brightness * 255.0f + 0.5f);
				g = (int) (p * 255.0f + 0.5f);
				b = (int) (q * 255.0f + 0.5f);
				break;
			}
		}
		return 0xff000000 | (r << 16) | (g << 8) | (b << 0);
	}

	/**
	 * ソフトキーボードの表示・非表示を検知するリスナーを設定します。
	 *
	 * @param activity アクティビティ
	 * @param listener ソフトキーボードの表示・非表示のリスナー
	 */
	public static void observeSoftKeyBoards(Activity activity, final OnSoftKeyBoardVisibleListener listener) {

		final View contentView = activity.findViewById(android.R.id.content);

		contentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			private int mPreviousHeight;

			@Override
			public void onGlobalLayout() {
				int newHeight = contentView.getHeight();
				if (mPreviousHeight != 0) {
					/*
					 * コンテンツビューの高さが前より高くなったかどうかでソフトキーボードの開閉を判断します。
					 * ただこれだとロリポップとかでソフトボタン？とかが出ても反応してしまうのが問題？
					 */

					if (mPreviousHeight > newHeight) {
						listener.onSoftKeyBoardVisible(true);
					} else if (mPreviousHeight < newHeight) {
						listener.onSoftKeyBoardVisible(false);
					} else {
						// No change
					}
				}
				mPreviousHeight = newHeight;
			}
		});
	}

	// //////////////////////////////////////////////////////////////////////////
	// インナークラス
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * ソフトキーボードの表示・非表示のリスナー
	 */
	public interface OnSoftKeyBoardVisibleListener {
		/**
		 * ソフトキーボードの表示・非表示のコールバック
		 *
		 * @param visible 表示の場合は true、非表示の場合は false
		 */
		public void onSoftKeyBoardVisible(boolean visible);
	}
}
