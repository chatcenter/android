package ly.appsocial.chatcenter.util;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

/**
 * Picasso の 円形にする Transformation.
 */
public class CircleTransformation implements Transformation {

	@Override
	public Bitmap transform(Bitmap source) {

		int smallestWidth = Math.min(source.getWidth(), source.getHeight());

		int x = (source.getWidth() - smallestWidth) / 2;
		int y = (source.getHeight() - smallestWidth) / 2;

		Bitmap squareBitmap = Bitmap.createBitmap(source, x, y, smallestWidth, smallestWidth);
		if (squareBitmap != source) {
			source.recycle();
		}

		Bitmap destBitmap = Bitmap.createBitmap(smallestWidth, smallestWidth, source.getConfig());

		Canvas canvas = new Canvas(destBitmap);
		Paint paint = new Paint();
		BitmapShader shader = new BitmapShader(squareBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
		paint.setShader(shader);
		paint.setAntiAlias(true);

		float r = smallestWidth / 2f;
		canvas.drawCircle(r, r, r, paint);

		squareBitmap.recycle();
		return destBitmap;
	}

	@Override
	public String key() {
		return "circle";
	}
}
