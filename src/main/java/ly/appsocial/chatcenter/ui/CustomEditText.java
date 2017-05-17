package ly.appsocial.chatcenter.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by trungnq on 4/21/17.
 */

public class CustomEditText extends EditText{
    private static final String TAG = "CustomEditText";
    OnKeyPreImeListener onKeyPreImeListener;

    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnKeyPreImeListener(OnKeyPreImeListener onKeyPreImeListener) {
        this.onKeyPreImeListener = onKeyPreImeListener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if(onKeyPreImeListener != null)
                onKeyPreImeListener.onBackPressed();
            Log.d(TAG, "HIDING KEYBOARD");
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    public interface OnKeyPreImeListener {
        void onBackPressed();
    }
}
