package ly.appsocial.chatcenter.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.ViewUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.util.ViewUtil;

public class WidgetMenuBar extends LinearLayout {

    public void setMenuWidgetListener(WidgetMenuClickListener listener) {
        mListener = listener;
    }

    public enum ButtonType {
        TYPE_TEXT_INPUT,
        TYPE_SUGGESTION,
        TYPE_WIDGET_FILE,
        TYPE_WIDGET_FIXED_PHRASE,
        TYPE_WIDGET_CAMERA,
        TYPE_WIDGET_LOCATION,
        TYPE_WIDGET_VOICE_CALL,
        TYPE_WIDGET_VIDEO_CALL,
        TYPE_WIDGET_SCHEDULE,
        TYPE_WIDGET_QUESTION,
        TYPE_LANDING_PAGE,
        TYPE_CONFIRM,
        TYPE_PAYMENT
    }

    private List<ButtonType> mButtonTypes = new ArrayList<>();
    private WidgetMenuClickListener mListener;
    private List<AppCompatImageButton> mButtons = new ArrayList<>();
    private List<View> mSpaceViews = new ArrayList<>();

    public WidgetMenuBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        addButton(ButtonType.TYPE_TEXT_INPUT);
        addButton(ButtonType.TYPE_SUGGESTION);
        drawMenu();

        setActive(ButtonType.TYPE_SUGGESTION, false);
    }

    public void addButton(ButtonType type) {
        if (!mButtonTypes.contains(type)) {
            if (type == ButtonType.TYPE_WIDGET_FIXED_PHRASE) {
                mButtonTypes.add(2, type);
            } else {
                mButtonTypes.add(type);
            }
        }
        drawMenu();
    }

    /**
     * Draw the WidgetMenu
     */
    public void drawMenu() {
        removeAllViews();
        mButtons.clear();
        mSpaceViews.clear();
        for (int i = 0; i < mButtonTypes.size(); i++) {

            if (i < mButtonTypes.size() && i > 0) {
                View spaceView = createSpaceView();
                addView(spaceView);
                mSpaceViews.add(spaceView);
            }

            AppCompatImageButton button = createButton(mButtonTypes.get(i));
            addView(button);
            mButtons.add(button);
        }
    }

    /**
     * Create the button to show on WidgetMenu
     * @param type
     * @return
     */
    public AppCompatImageButton createButton(final ButtonType type) {
        AppCompatImageButton button = new AppCompatImageButton(getContext());
        button.setImageResource(getRadioButtonDrawable(type));
        button.setBackgroundResource(android.R.color.transparent);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        button.setLayoutParams(layoutParams);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onMenuButtonClicked(type);
                }
            }
        });
        return  button;
    }

    /**
     * Create a white space between buttons
     * @return
     */
    public View createSpaceView() {
        View spaceView = new View(getContext());
        LayoutParams layoutParams = new LayoutParams((int) ViewUtil.convertDpToPixel(20, getContext()), 1);
        spaceView.setLayoutParams(layoutParams);

        return spaceView;
    }

    public void setActive(ButtonType buttonType, boolean active) {
        int index = mButtonTypes.indexOf(buttonType);
        if (index >= 0) {
            mButtons.get(index).setSelected(active);
        }
    }

    public int getRadioButtonDrawable(ButtonType type) {
        int drawable = 0;
        switch (type) {
            case TYPE_TEXT_INPUT:
                drawable = R.drawable.ic_text_input;
                break;
            case TYPE_SUGGESTION:
                drawable = R.drawable.ic_suggestion;
                break;
            case TYPE_WIDGET_FILE:
                drawable = R.drawable.ic_attach_file;
                break;
            case TYPE_WIDGET_CAMERA:
                drawable = R.drawable.ic_photo_camera;
                break;
            case TYPE_WIDGET_FIXED_PHRASE:
                drawable = R.drawable.ic_save;
                break;
            case TYPE_WIDGET_LOCATION:
                drawable = R.drawable.ic_location;
                break;
            case TYPE_WIDGET_SCHEDULE:
                drawable = R.drawable.ic_schedule;
                break;
            case TYPE_WIDGET_VIDEO_CALL:
                drawable = R.drawable.ic_videocam;
                break;
            case TYPE_WIDGET_VOICE_CALL:
                drawable = R.drawable.ic_call;
                break;
            case TYPE_WIDGET_QUESTION:
                drawable = R.drawable.ic_question;
                break;

            case TYPE_LANDING_PAGE:
                drawable = R.drawable.ic_landing_page;
                break;

            case TYPE_PAYMENT:
                drawable = R.drawable.ic_payment;
                break;

            case TYPE_CONFIRM:
                drawable = R.drawable.ic_confirm;
                break;
        }

        return drawable;
    }

    public interface WidgetMenuClickListener {
        void onMenuButtonClicked(ButtonType type);
    }

}
