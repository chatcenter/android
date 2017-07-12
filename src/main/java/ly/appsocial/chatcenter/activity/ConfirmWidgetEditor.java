package ly.appsocial.chatcenter.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.gson.Gson;

import java.util.ArrayList;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.fragment.AlertDialogFragment;
import ly.appsocial.chatcenter.fragment.WidgetPreviewDialog;
import ly.appsocial.chatcenter.util.DialogUtil;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;

public class ConfirmWidgetEditor extends BaseActivity implements WidgetPreviewDialog.WidgetPreviewListener,
        AlertDialogFragment.DialogListener {

    public static final String CONFIRM_CONTENT = "confirm_content";
    private EditText mEdtWidgetTitle;
    private RadioGroup mRdgOptions;
    private EditText mEdtCustomText;


    private String mWidgetTitle;
    private String mWidgetAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_widget_editor);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.confirm_activity_title);
        setHomeAsUpIndicator(R.drawable.bt_close);

        mEdtWidgetTitle = (EditText) findViewById(R.id.edt_confirm_title);
        mRdgOptions = (RadioGroup) findViewById(R.id.rdg_actions);
        mEdtCustomText = (EditText) findViewById(R.id.edt_custom_text);

        setupUI(findViewById(R.id.root_view));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.next_menu, menu);
        return true;
    }

    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(ConfirmWidgetEditor.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        } else if (id == R.id.next) {

            mWidgetTitle = mEdtWidgetTitle.getText().toString().trim();
            if (mRdgOptions.getCheckedRadioButtonId() == R.id.rd_custom) {
                mWidgetAction = mEdtCustomText.getText().toString().trim();
            } else if (mRdgOptions.getCheckedRadioButtonId() == R.id.rd_got_it
                    || mRdgOptions.getCheckedRadioButtonId() == R.id.rd_like
                    || mRdgOptions.getCheckedRadioButtonId() == R.id.rd_yes) {
                RadioButton selectedButton = (RadioButton) findViewById(mRdgOptions.getCheckedRadioButtonId());
                mWidgetAction = selectedButton.getText().toString().trim();
            }

            if (StringUtil.isBlank(mWidgetTitle) || StringUtil.isBlank(mWidgetAction)) {
                DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ALERT, null, getString(R.string.widget_create_error));
            } else {
                String content = makeContentString();
                if (content != null) {
                    showDialogWidgetPreview(content,this);
                } else {
                    DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.widget_create_error));
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private String makeContentString() {
        BasicWidget widget = new BasicWidget();
        widget.message = new BasicWidget.Message();
        widget.message.text = mWidgetTitle;

        widget.stickerAction = new BasicWidget.StickerAction();
        widget.stickerAction.actionData = new ArrayList<>();

        widget.stickerAction.actionType = ChatCenterConstants.ActionType.CONFIRM;
        widget.stickerAction.viewInfo = new BasicWidget.StickerAction.ViewInfo();
        BasicWidget.StickerAction.ActionData actionData = new BasicWidget.StickerAction.ActionData();

        actionData.label = mWidgetAction;
        actionData.type = getActionType();

        actionData.value = new BasicWidget.StickerAction.ActionData.Value();
        actionData.value.answer = String.valueOf(true);

        widget.stickerAction.actionData.add(actionData);

        widget.stickerType = ChatCenterConstants.StickerName.STICKER_TYPE_CONFIRM;

        return new Gson().toJson(widget).toString();
    }


    /**
     * ダイアログをキャンセルした際のコールバック。
     *
     * @param tag このフラグメントのタグ
     */
    @Override
    public void onDialogCancel(String tag) {

    }

    /**
     * ダイアログの肯定ボタンを押下した際のコールバック。
     *
     * @param tag このフラグメントのタグ
     */
    @Override
    public void onPositiveButtonClick(String tag) {

    }

    @Override
    public void onSendButtonClicked() {
        Intent intent = new Intent();
        String content = makeContentString();
        if (StringUtil.isNotBlank(content)) {
            intent.putExtra(CONFIRM_CONTENT, content);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            // TODO Error message
        }
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, R.anim.activity_close_exit);
    }

    public String getActionType() {
        if (mRdgOptions.getCheckedRadioButtonId() == R.id.rd_custom) {
            return "custom";
        } else if (mRdgOptions.getCheckedRadioButtonId() == R.id.rd_got_it) {
            return "aware";
        } else if(mRdgOptions.getCheckedRadioButtonId() == R.id.rd_like) {
            return "thumb_up";
        } else if(mRdgOptions.getCheckedRadioButtonId() == R.id.rd_yes) {
            return "positive";
        }

        return "";
    }
}
