package ly.appsocial.chatcenter.activity;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import com.squareup.okhttp.OkHttpClient;

import javax.inject.Inject;
import javax.inject.Named;

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
}
