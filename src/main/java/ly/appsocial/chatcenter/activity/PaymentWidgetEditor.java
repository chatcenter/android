package ly.appsocial.chatcenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.gson.Gson;

import java.util.ArrayList;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.fragment.AlertDialogFragment;
import ly.appsocial.chatcenter.fragment.WidgetPreviewDialog;
import ly.appsocial.chatcenter.util.DialogUtil;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;

public class PaymentWidgetEditor extends BaseActivity implements WidgetPreviewDialog.WidgetPreviewListener,
        AlertDialogFragment.DialogListener {

    public static final String PAYMENT_CONTENT = "payment_content";

    private EditText mEdtPaymentTitle, mEdtAmount;
    private AppCompatSpinner mCurrencySpinner;

    private String mPaymentWidgetTitle;
    private float mPaymentWidgetAmount;
    private String mPaymentWidgetCurrency;

    public enum Currency {
        USD ("usd"),
        JPY ("jpy");

        private final String name;

        Currency(String s) {
            name = s;
        }

        public boolean equalsName(String otherName) {
            return name.equals(otherName);
        }

        public String toString() {
            return this.name;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_widget_editor);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.payment_activity_title);
        setHomeAsUpIndicator(R.drawable.bt_close);

        mEdtPaymentTitle = (EditText) findViewById(R.id.edt_payment_title);
        mEdtAmount = (EditText) findViewById(R.id.edt_payment_amount);
        mCurrencySpinner = (AppCompatSpinner) findViewById(R.id.spinner_currency);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.next_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        } else if (id == R.id.next) {
            mPaymentWidgetTitle = mEdtPaymentTitle.getText().toString();
            String amount = mEdtAmount.getText().toString();
            mPaymentWidgetCurrency = mCurrencySpinner.getSelectedItem().toString().toLowerCase();

            if (StringUtil.isNotBlank(amount)) {
                mPaymentWidgetAmount = Float.parseFloat(amount);
            }
            if (StringUtil.isBlank(mPaymentWidgetTitle) || StringUtil.isBlank(amount)) {
                DialogUtil.showAlertDialog(getSupportFragmentManager(),
                        DialogUtil.Tag.ALERT,
                        null,
                        getString(R.string.widget_create_error));

            } else if(getPaymentWidgetAmount(mPaymentWidgetCurrency) < getMinAmount(mPaymentWidgetCurrency)) {
                DialogUtil.showAlertDialog(getSupportFragmentManager(),
                        DialogUtil.Tag.ALERT,
                        null,
                        getString(R.string.payment_activity_amount_not_enough));
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
        widget.message.text = mEdtPaymentTitle.getText().toString();

        widget.stickerAction = new BasicWidget.StickerAction();
        widget.stickerAction.actionData = new ArrayList<>();

        widget.stickerAction.actionType = ChatCenterConstants.ActionType.SELECT;
        widget.stickerAction.viewInfo = new BasicWidget.StickerAction.ViewInfo();
        BasicWidget.StickerAction.ActionData actionData = new BasicWidget.StickerAction.ActionData();

        actionData.currency = mPaymentWidgetCurrency;
        actionData.amount = getPaymentWidgetAmount(mPaymentWidgetCurrency);
        actionData.label = getPaymentWidgetLabel(mPaymentWidgetAmount, mPaymentWidgetCurrency);

        widget.stickerAction.actionData.add(actionData);

        widget.stickerType = ChatCenterConstants.StickerName.STICKER_TYPE_PAYMENT;

        return new Gson().toJson(widget).toString();
    }

    /**
     * Get amount of money user want to pay
     * @param currency
     * @return
     */
    private float getPaymentWidgetAmount(String currency) {
        float inputAmount = Float.parseFloat(mEdtAmount.getText().toString());
        if (Currency.USD.equalsName(currency)) {
            inputAmount = 100 * inputAmount;
        }

        return inputAmount;
    }

    /**
     * Min amount to pay
     * @param currency
     * @return
     */
    private float getMinAmount(String currency) {
        if (Currency.USD.equalsName(currency)) {
            return 50;
        } else if (Currency.JPY.equalsName(currency)) {
            return 100;
        }

        return 50;
    }

    /**
     * Create a action label to show on PaymnetWidget
     * @param amount
     * @param currency
     * @return
     */
    private String getPaymentWidgetLabel(float amount, String currency) {
        String label;
        String currencySymbol = "";
        if (Currency.JPY.equalsName(currency)) {
           currencySymbol = "¥";
        } else if (Currency.USD.equalsName(currency)) {
            currencySymbol = "$";
        }

        label = String.format(getString(R.string.payment_widget_content), currencySymbol + amount);
        return label;
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, R.anim.activity_close_exit);
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
            intent.putExtra(PAYMENT_CONTENT, content);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            // TODO Error message
        }
    }
}
