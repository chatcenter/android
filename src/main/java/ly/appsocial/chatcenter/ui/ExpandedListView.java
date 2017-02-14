package ly.appsocial.chatcenter.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ListView;

import ly.appsocial.chatcenter.R;

public class ExpandedListView extends ListView {
    private android.view.ViewGroup.LayoutParams params;
    private int old_count = 0;

    public ExpandedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getCount() != old_count) {
            old_count = getCount();
            int itemHeight = 0;
            int footerHeight = 0;
            if (old_count > 0) {
                itemHeight = getChildAt(0).getHeight() + getDividerHeight();
                footerHeight = getContext().getResources().getDimensionPixelSize(R.dimen.activity_header_height)
                        + getDividerHeight();
            }
            params = getLayoutParams();
            params.height = (old_count - 1) * itemHeight + footerHeight;

            setLayoutParams(params);
        }

        super.onDraw(canvas);
    }
}
