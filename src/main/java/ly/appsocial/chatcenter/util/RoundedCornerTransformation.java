package ly.appsocial.chatcenter.util;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

public class RoundedCornerTransformation implements Transformation {
    private float mRadiusRatio;

    public RoundedCornerTransformation(float radiusRatio) {
        mRadiusRatio = radiusRatio;
    }

    @Override
    public Bitmap transform(Bitmap bitmap) {

        int smallestWidth = Math.min(bitmap.getWidth(), bitmap.getHeight());

        int x = (bitmap.getWidth() - smallestWidth) / 2;
        int y = (bitmap.getHeight() - smallestWidth) / 2;

        Bitmap squareBitmap = Bitmap.createBitmap(bitmap, x, y, smallestWidth, smallestWidth);
        if (squareBitmap != bitmap) {
            bitmap.recycle();
        }

        Bitmap destBitmap = Bitmap.createBitmap(smallestWidth, smallestWidth, bitmap.getConfig());

        final Rect rect = new Rect(0, 0, smallestWidth, smallestWidth);
        final RectF rectF = new RectF(rect);
        final float roundPx = mRadiusRatio * smallestWidth;

        Canvas canvas = new Canvas(destBitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squareBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        squareBitmap.recycle();

        return destBitmap;
    }

    @Override
    public String key() {
        return "RoundedCorner";
    }
}
