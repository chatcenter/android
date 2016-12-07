package ly.appsocial.chatcenter.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import ly.appsocial.chatcenter.R;

/**
 *
 */
public class RoundImageView extends ImageView {

	public class RoundedOptions {
		public static final int TOP 		= 0x01;
		public static final int BOTTOM 		= 0x02;
		public static final int BOTH 		= 0x03;
		public static final int NONE 		= 0x00;
	}

	/** マスクペイント */
	private Paint mMaskedPaint;
	/** コピーペイント */
	private Paint mCopyPaint;
	/** マスク Drawable */
	private Drawable mMaskDrawable;
	/** ビューの矩形 */
	private Rect mBounds;
	/** ビューの矩形(float) */
	private RectF mBoundsF;

	private int mRoundedOptions = RoundedOptions.NONE;

	/**
	 * コンストラクタ
	 *
	 * @param context コンテキスト
	 */
	public RoundImageView(Context context) {
		this(context, null);
	}

	/**
	 * コンストラクタ
	 *
	 * @param context コンテキスト
	 * @param attrs 属性
	 */
	public RoundImageView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mMaskedPaint = new Paint();
		mMaskedPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

		mCopyPaint = new Paint();
		mMaskDrawable = getResources().getDrawable(R.drawable.mask_rounded_none);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mBounds = new Rect(0, 0, w, h);
		mBoundsF = new RectF(mBounds);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		// カラーレイヤー・透過レイヤーを保存
		int saveCount = canvas.saveLayer(mBoundsF, mCopyPaint, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG);

		// マスク描画
		mMaskDrawable.setBounds(mBounds);
		mMaskDrawable.draw(canvas);

		// 保存
		canvas.saveLayer(mBoundsF, mMaskedPaint, 0);

		super.onDraw(canvas);

		// 保存状態を戻す
		canvas.restoreToCount(saveCount);
	}

	public int getRoundedOptions() {
		return mRoundedOptions;
	}

	public void setRoundedOptions(int roundedOptions) {
		mRoundedOptions = roundedOptions;

		// Retrieve the mask
		switch (roundedOptions) {
			case RoundedOptions.TOP:
				mMaskDrawable = getResources().getDrawable(R.drawable.mask_rounded_top);
				break;
			case RoundedOptions.BOTTOM:
				mMaskDrawable = getResources().getDrawable(R.drawable.mask_rounded_bottom);
				break;
			case RoundedOptions.BOTH:
				mMaskDrawable = getResources().getDrawable(R.drawable.mask_rounded_both);
				break;
			case RoundedOptions.NONE:
			default:
				mMaskDrawable = getResources().getDrawable(R.drawable.mask_rounded_none);
				break;
		}

		// Update the view
		postInvalidate();
	}

	public void removeRounded(int options) {
		this.setRoundedOptions(mRoundedOptions & ~options);
	}

	public void addRounded(int options) {
		this.setRoundedOptions(mRoundedOptions | options);
	}

}
