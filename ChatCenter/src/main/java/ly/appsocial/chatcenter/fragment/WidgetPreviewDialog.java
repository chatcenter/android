package ly.appsocial.chatcenter.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.ScheduleActivity;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.widgets.views.WidgetView;

public class WidgetPreviewDialog extends DialogFragment implements View.OnClickListener {

    private static final String KEY_STICKERS = "Stickers";

    /**
     * 閉じるボタン
     */
    private ImageButton mBtCloseStickerMenu;
    private Button mBtPreview;

    private WidgetView mWidgetView;
    private WidgetPreviewListener mListener;

    public static WidgetPreviewDialog newInstance(String widgetContent) {
        WidgetPreviewDialog dialog = new WidgetPreviewDialog();
        dialog.setStyle(STYLE_NORMAL, R.style.Dialog_StickerMenu);

        final Bundle args = new Bundle();
        args.putString(KEY_STICKERS, widgetContent);

        dialog.setArguments(args);


        return dialog;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.bt_cancel) {
            closeStickerMenu();
        } else if (viewId == R.id.bt_send) {
            // closeStickerMenu();
            if (mListener != null) {
                mListener.onSendButtonClicked();
            } else {
                closeStickerMenu();
            }
        }
    }

    private void closeStickerMenu() {
        getDialog().dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_preview_widget, container, false);

        mBtCloseStickerMenu = (ImageButton) v.findViewById(R.id.bt_cancel);
        mBtCloseStickerMenu.setOnClickListener(this);

        mBtPreview = (Button) v.findViewById(R.id.bt_send);
        mBtPreview.setOnClickListener(this);

        mWidgetView = (WidgetView) v.findViewById(R.id.schedule_widget);
        String widgetContent = getArguments().getString(KEY_STICKERS);
        ChatItem chatItem = ChatItem.createTemporarySticker(widgetContent, "", 0);
        chatItem.type = ResponseType.STICKER;

        mWidgetView.setupCustomerView(chatItem, null);

        return v;
    }

    @Override
    public void onResume() {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof WidgetPreviewListener) {
            mListener = (WidgetPreviewListener) activity;
        }
    }

    public interface WidgetPreviewListener {
        void onSendButtonClicked();
    }
}
