package ly.appsocial.chatcenter.activity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import com.squareup.okhttp.OkHttpClient;

import javax.inject.Inject;
import javax.inject.Named;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.di.InjectorHelper;
import ly.appsocial.chatcenter.fragment.WidgetPreviewDialog;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    @Inject @Named("client")
    protected OkHttpClient mOkHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InjectorHelper.getInstance().injectNetworkModule(this);
    }

    public float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public void showDialogWidgetPreview(String widgetContent) {
        DialogFragment newFragment = WidgetPreviewDialog.newInstance(widgetContent);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void setHomeAsUpIndicator(int drawableId) {
        Drawable drawable = setDrawableTint(drawableId, R.color.color_chatcenter_title_text);
        getSupportActionBar().setHomeAsUpIndicator(drawable);
    }

    public Drawable setDrawableTint(int drawableId, int color) {
        Drawable drawable;
        int drawableTintColor;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            drawable = getDrawable(drawableId);
            drawableTintColor = getColor(color);
        } else {
            drawable = getResources().getDrawable(drawableId);
            drawableTintColor = getResources().getColor(color);
        }
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, drawableTintColor);

        return drawable;
    }
}
