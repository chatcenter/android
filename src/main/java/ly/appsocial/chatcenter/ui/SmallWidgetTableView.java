package ly.appsocial.chatcenter.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.util.ViewUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;

/**
 * To create a table to store a set of SmallWidgetView
 */
public class SmallWidgetTableView<T> extends LinearLayout {

    private TextView mTvTitle;
    private LinearLayout mLlContainerTop;
    private LinearLayout mLlContainerBottom;
    private TextView mTvEmptyAlert;

    private int mSuggestionWidth;

    public SmallWidgetTableView(Context context) {
        super(context);
    }

    public SmallWidgetTableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(attrs);
    }


    /**
     * Create view with default attributes
     * @param attrs
     */
    private void init(AttributeSet attrs) {
        mSuggestionWidth = (int) (getScreenWidth() * 0.8);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rootView = inflater.inflate(R.layout.view_table_small_widget, this, true);

        mTvTitle = (TextView) rootView.findViewById(R.id.tv_table_title);
        mLlContainerTop = (LinearLayout) rootView.findViewById(R.id.table_container_top);
        mLlContainerBottom = (LinearLayout) rootView.findViewById(R.id.table_container_bottom);
        mTvEmptyAlert = (TextView) rootView.findViewById(R.id.tv_empty_alert);

        setDefaultValue(attrs);
    }


    /**
     * Set up view attributes
     * @param attrs
     */
    private void setDefaultValue(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SmallWidgetTableView, 0, 0);

        String title = (String) a.getText(R.styleable.SmallWidgetTableView_smallWidgetTableTitle);
        mTvTitle.setText(title);

        String alert = (String) a.getText(R.styleable.SmallWidgetTableView_smallWidgetTableContentEmpty);
        mTvEmptyAlert.setText(alert);

        int color = a.getColor(R.styleable.SmallWidgetTableView_smallWidgetTableBackground, Color.WHITE);
        setBackgroundColor(color);

        a.recycle();
    }

    /**
     * Show alert about empty table or
     *
     * @param isEmpty
     */
    public void setTableEmpty(boolean isEmpty) {
        mTvEmptyAlert.setVisibility(isEmpty ? VISIBLE : GONE);
    }

    /**
     * Bind data with view components
     *
     * @param items
     * @param onItemClickListener
     */
    public void bindData(List<T> items, OnItemClickListener onItemClickListener) {
        setTableEmpty(false);

        mLlContainerTop.removeAllViews();
        mLlContainerBottom.removeAllViews();

        if (items.size() == 1) {
            mLlContainerBottom.setVisibility(View.GONE);
        } else {
            mLlContainerBottom.setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);
            SmallWidgetView view = new SmallWidgetView(getContext());
            if (item instanceof BasicWidget.StickerAction.ActionData) {
                view.bindData((BasicWidget.StickerAction.ActionData) item, onItemClickListener);
            } else if (item instanceof ChatItem) {
                view.bindData((ChatItem) item, onItemClickListener);
            }

            if (i % 2 == 0) {
                mLlContainerTop.addView(view);
            } else {
                mLlContainerBottom.addView(view);
            }
            view.getLayoutParams().width = mSuggestionWidth;
        }


        int paddingHorizontal;
        if (items.size() > 2) {
            paddingHorizontal = (int) ViewUtil.convertDpToPixel(10, getContext());
        } else {
            paddingHorizontal = (getScreenWidth() - mSuggestionWidth) / 2;
        }
        mLlContainerTop.setPadding(paddingHorizontal, 0, paddingHorizontal, 0);
        mLlContainerBottom.setPadding(paddingHorizontal, 0, paddingHorizontal, 0);
    }


    /**
     * To calculate width of physical device
     * @return
     */
    private int getScreenWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }


    /**
     * To set up action for each view on this table
     */
    public interface OnItemClickListener {
        void onSmallWidgetClicked(Object data);
    }

}
