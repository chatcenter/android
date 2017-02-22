package ly.appsocial.chatcenter.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ChannelItem;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.fragment.AlertDialogFragment;
import ly.appsocial.chatcenter.fragment.ListQuestionFragment;
import ly.appsocial.chatcenter.fragment.WidgetPreviewDialog;
import ly.appsocial.chatcenter.util.DialogUtil;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;

public class QuestionActivity extends BaseActivity implements WidgetPreviewDialog.WidgetPreviewListener,
        AlertDialogFragment.DialogListener {

    public static final String QUESTION_CONTENT = "question_content";

    final static int ANSWERTYPE_YESNO = 0;
    final static int ANSWERTYPE_MULTIPLE = 1;
    final static int ANSWERTYPE_CHECKBOX = 2;
    final static int ANSWERTYPE_LINEARSCALE = 3;

    private int mAnswerType = -1;

    private EditText mEdtTitle;
    private TextView mTvRemainingCharacters;

    private YesNoQuestionFragment yesNoContentFragment;
    private MultipleQuestionFragment multipleQuestionFragment;
    private CheckboxQuestionFragment checkboxQuestionFragment;
    private LinearScaleQuestionFragment linearScaleQuestionFragment;

    private InputMethodManager inputMethodManager;
    private View mainLayout;
    private static ScrollView mScrollView;

    private ArrayList<AnswerTypeSpinnerItem> mQuestionTypeSpinnerItems = new ArrayList<AnswerTypeSpinnerItem>() {{
        add(new AnswerTypeSpinnerItem(R.drawable.icon_yesno, R.string.answer_type_yes_no, ANSWERTYPE_YESNO));
        add(new AnswerTypeSpinnerItem(R.drawable.icon_multiplechoice, R.string.answer_type_multi_choice, ANSWERTYPE_MULTIPLE));
        add(new AnswerTypeSpinnerItem(R.drawable.icon_checkbox, R.string.answer_type_checkbox, ANSWERTYPE_CHECKBOX));
        add(new AnswerTypeSpinnerItem(R.drawable.icon_linearscale, R.string.answer_type_leaner_scale, ANSWERTYPE_LINEARSCALE));
    }};

    private TextWatcher mTitleTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > ChatCenterConstants.QuestionWidget.TITLE_MAX_LENGTH) {
                /*
                String message = String.format(getString(R.string.alert_question_widget_title_too_long),
                        ChatCenterConstants.QuestionWidget.TITLE_MAX_LENGTH);
                DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ALERT, null, message);
                */

                s = s.subSequence(0, ChatCenterConstants.QuestionWidget.TITLE_MAX_LENGTH);
                mEdtTitle.setText(s);
                mEdtTitle.setSelection(s.length());
            }
            mTvRemainingCharacters.setText("" + (ChatCenterConstants.QuestionWidget.TITLE_MAX_LENGTH - s.length()));
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.sticker_label_confirm);
        setHomeAsUpIndicator(R.drawable.bt_close);

        mEdtTitle = (EditText) findViewById(R.id.edit_question);
        mTvRemainingCharacters = (TextView) findViewById(R.id.tv_remaining_characters);

        mEdtTitle.addTextChangedListener(mTitleTextWatcher);
        mTvRemainingCharacters.setText("" + ChatCenterConstants.QuestionWidget.TITLE_MAX_LENGTH);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setAnswerType(mQuestionTypeSpinnerItems.get(position).mAnswerType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner.setAdapter(new AnswerTypeAdapter(this, mQuestionTypeSpinnerItems));

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mainLayout = findViewById(R.id.mainLayout);
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        mainLayout.requestFocus();
        return false;
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

            String title = mEdtTitle.getText().toString();
            if (StringUtil.isBlank(title)) {
                DialogUtil.showAlertDialog(getSupportFragmentManager(),
                        DialogUtil.Tag.ALERT,
                        null,
                        String.format(getString(R.string.alert_question_widget_title_too_long), ChatCenterConstants.QuestionWidget.TITLE_MAX_LENGTH));
            } else {
                String content = makeContentString();
                if (content != null) {
                    showDialogWidgetPreview(content);
                } else {
                    DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.widget_create_error));
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setAnswerType(int answerType) {
        if (answerType != mAnswerType) {
            mAnswerType = answerType;

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            switch (answerType) {
                case ANSWERTYPE_YESNO: {
                    if (yesNoContentFragment == null) {
                        yesNoContentFragment = new YesNoQuestionFragment();
                    }
                    ft.replace(R.id.container, yesNoContentFragment);
                    break;
                }
                case ANSWERTYPE_MULTIPLE: {
                    if (multipleQuestionFragment == null) {
                        multipleQuestionFragment = new MultipleQuestionFragment();
                    }
                    ft.replace(R.id.container, multipleQuestionFragment);
                    break;
                }
                case ANSWERTYPE_CHECKBOX: {
                    if (checkboxQuestionFragment == null) {
                        checkboxQuestionFragment = new CheckboxQuestionFragment();
                    }
                    ft.replace(R.id.container, checkboxQuestionFragment);
                    break;
                }
                case ANSWERTYPE_LINEARSCALE: {
                    if (linearScaleQuestionFragment == null) {
                        linearScaleQuestionFragment = new LinearScaleQuestionFragment();
                    }
                    ft.replace(R.id.container, linearScaleQuestionFragment);
                    break;
                }
            }
            ft.commit();
        }
    }

    private String makeContentString() {
        BasicWidget widget = new BasicWidget();
        widget.message = new BasicWidget.Message();
        widget.message.text = mEdtTitle.getText().toString();

        widget.stickerAction = new BasicWidget.StickerAction();
        widget.stickerAction.actionData = new ArrayList<>();

        switch (mAnswerType) {
            case ANSWERTYPE_YESNO: {
                widget.stickerAction.actionType = ChatCenterConstants.ActionType.SELECT;
                widget.stickerAction.viewInfo = new BasicWidget.StickerAction.ViewInfo();
                widget.stickerAction.viewInfo.type = ChatCenterConstants.ViewType.YESNO;

                int n = yesNoContentFragment.mRadioGroup.getCheckedRadioButtonId();
                boolean isYesNo = (n == R.id.radioButton2);

                for (int i = 0; i < 2; i++) {
                    BasicWidget.StickerAction.ActionData actionData = new BasicWidget.StickerAction.ActionData();
                    actionData.value = new BasicWidget.StickerAction.ActionData.Value();

                    if (i == 0) {
                        actionData.value.answer = "true";
                        if (isYesNo) {
                            actionData.label = getString(R.string.yes);
                        } else {
                            actionData.label = "\uD83D\uDC4D";
                        }
                    } else {
                        actionData.value.answer = "false";
                        if (isYesNo) {
                            actionData.label = getString(R.string.no);
                        } else {
                            actionData.label = "\uD83D\uDC4E";
                        }
                    }
                    widget.stickerAction.actionData.add(actionData);
                }
                break;
            }
            case ANSWERTYPE_MULTIPLE: {
                if (multipleQuestionFragment.mAdapter.getOptionCount() == 0) {
                    // There is no option entered
                    return null;
                }

                widget.stickerAction.actionType = ChatCenterConstants.ActionType.SELECT;
                widget.stickerAction.viewInfo = new BasicWidget.StickerAction.ViewInfo();
                widget.stickerAction.viewInfo.type = ChatCenterConstants.ViewType.DEFAULT;

                int nCount = multipleQuestionFragment.mAdapter.getCount();
                for (int i = 0; i < nCount; i++) {
                    String str = multipleQuestionFragment.mAdapter.getItem(i).getContent();
                    if (StringUtil.isNotBlank(str)) {
                        BasicWidget.StickerAction.ActionData actionData = new BasicWidget.StickerAction.ActionData();
                        actionData.label = str;
                        actionData.value = new BasicWidget.StickerAction.ActionData.Value();
                        actionData.value.answer = String.valueOf(i + 1);
                        widget.stickerAction.actionData.add(actionData);
                    }
                }
                break;
            }
            case ANSWERTYPE_CHECKBOX: {
                if (checkboxQuestionFragment.mAdapter.getOptionCount() == 0) {
                    // There is no option entered
                    return null;
                }

                widget.stickerAction.actionType = ChatCenterConstants.ActionType.SELECT;
                widget.stickerAction.viewInfo = new BasicWidget.StickerAction.ViewInfo();
                widget.stickerAction.viewInfo.type = ChatCenterConstants.ViewType.CHECKBOX;

                int nCount = checkboxQuestionFragment.mAdapter.getCount();
                for (int i = 0; i < nCount; i++) {
                    String str = checkboxQuestionFragment.mAdapter.getItem(i).getContent();
                    if (StringUtil.isNotBlank(str)) {

                        BasicWidget.StickerAction.ActionData actionData = new BasicWidget.StickerAction.ActionData();
                        actionData.label = str;
                        actionData.value = new BasicWidget.StickerAction.ActionData.Value();
                        actionData.value.answer = String.valueOf(i + 1);
                        widget.stickerAction.actionData.add(actionData);
                    }
                }
                break;
            }
            case ANSWERTYPE_LINEARSCALE: {
                widget.stickerAction.actionType = ChatCenterConstants.ActionType.SELECT;
                widget.stickerAction.viewInfo = new BasicWidget.StickerAction.ViewInfo();
                widget.stickerAction.viewInfo.type = ChatCenterConstants.ViewType.LINEAR;

                widget.stickerAction.viewInfo.minLabel = linearScaleQuestionFragment.mScaleEditLeft.getText().toString();
                widget.stickerAction.viewInfo.maxLabel = linearScaleQuestionFragment.mScaleEditRight.getText().toString();

                int minValue = linearScaleQuestionFragment.mMinScale;
                int maxValue = linearScaleQuestionFragment.mMaxScale;

                for (int i = minValue; i <= maxValue; i++) {
                    BasicWidget.StickerAction.ActionData actionData = new BasicWidget.StickerAction.ActionData();
                    actionData.label = String.valueOf(i + 1);
                    actionData.value = new BasicWidget.StickerAction.ActionData.Value();
                    actionData.value.answer = String.valueOf(i + 1);
                    widget.stickerAction.actionData.add(actionData);
                }
                break;
            }
        }

        return new Gson().toJson(widget).toString();
    }

    @Override
    public void onSendButtonClicked() {
        Intent intent = new Intent();
        String content = makeContentString();
        if (StringUtil.isNotBlank(content)) {
            intent.putExtra(QUESTION_CONTENT, content);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            // TODO Error message
        }
    }


    static public class YesNoQuestionFragment extends Fragment {
        public RadioGroup mRadioGroup;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View layout = inflater.inflate(R.layout.question_yesno, container, false);
            mRadioGroup = (RadioGroup) layout.findViewById(R.id.radio_group);
            mRadioGroup.check(R.id.radioButton1);

            TextView thumbUp = (TextView) layout.findViewById(R.id.answer_thumbup);
            thumbUp.setText("\uD83D\uDC4D");
            TextView thumbDown = (TextView) layout.findViewById(R.id.answer_thumbdown);
            thumbDown.setText("\uD83D\uDC4E");
            return layout;
        }
    }

    static public class MultipleQuestionFragment extends ListQuestionFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View layout = inflater.inflate(R.layout.question_multiple, container, false);
            setupList(layout, R.layout.question_cell_multiple, mScrollView);
            mScrollView.fullScroll(View.FOCUS_DOWN);
            return layout;
        }
    }

    static public class CheckboxQuestionFragment extends ListQuestionFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View layout = inflater.inflate(R.layout.question_checkbox, container, false);
            setupList(layout, R.layout.question_cell_checkbox, mScrollView);
            mScrollView.fullScroll(View.FOCUS_DOWN);
            return layout;
        }
    }

    static public class LinearScaleQuestionFragment extends Fragment {
        private Spinner mSpinner1;
        private Spinner mSpinner2;

        private TextView mScaleLabelLeft;
        private TextView mScaleLabelRight;
        public EditText mScaleEditLeft;
        public EditText mScaleEditRight;

        public int mMinScale = 0;
        public int mMaxScale = 4;

        private TextWatcher mScaleEdit1Watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > ChatCenterConstants.QuestionWidget.TITLE_MAX_LENGTH) {
                /*
                String message = String.format(getString(R.string.alert_question_widget_title_too_long),
                        ChatCenterConstants.QuestionWidget.TITLE_MAX_LENGTH);
                DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ALERT, null, message);
                */

                    s = s.subSequence(0, ChatCenterConstants.QuestionWidget.TITLE_MAX_LENGTH);
                    mScaleEditLeft.setText(s);
                    mScaleEditLeft.setSelection(s.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };


        private TextWatcher mScaleEdit2Watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > ChatCenterConstants.QuestionWidget.TITLE_MAX_LENGTH) {
                /*
                String message = String.format(getString(R.string.alert_question_widget_title_too_long),
                        ChatCenterConstants.QuestionWidget.TITLE_MAX_LENGTH);
                DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ALERT, null, message);
                */

                    s = s.subSequence(0, ChatCenterConstants.QuestionWidget.TITLE_MAX_LENGTH);
                    mScaleEditRight.setText(s);
                    mScaleEditRight.setSelection(s.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View layout = inflater.inflate(R.layout.question_linearscale, container, false);

            mScaleLabelLeft = (TextView) layout.findViewById(R.id.label1);
            mScaleLabelRight = (TextView) layout.findViewById(R.id.label2);
            mScaleEditLeft = (EditText) layout.findViewById(R.id.edit_label1);
            mScaleEditRight = (EditText) layout.findViewById(R.id.edit_label2);

            mScaleEditLeft.addTextChangedListener(mScaleEdit1Watcher);
            mScaleEditRight.addTextChangedListener(mScaleEdit2Watcher);

            mSpinner1 = (Spinner) layout.findViewById(R.id.scale_from);
            mSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position < mMaxScale) {
                        mMinScale = position;
                        mScaleLabelLeft.setText(String.valueOf(position + 1));
                    } else {
                        mSpinner1.setSelection(mMinScale);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            mSpinner1.setSelection(mMinScale);
            mScaleLabelLeft.setText(String.valueOf(mMinScale + 1));

            mSpinner2 = (Spinner) layout.findViewById(R.id.scale_to);
            mSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position > mMinScale) {
                        mMaxScale = position;
                        mScaleLabelRight.setText(String.valueOf(position + 1));
                    } else {
                        mSpinner2.setSelection(mMaxScale);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            mSpinner2.setSelection(mMaxScale);
            mScaleLabelRight.setText(String.valueOf(mMaxScale + 1));

            return layout;
        }

    }

    public static class AnswerTypeSpinnerItem {
        int mIconDrawable;
        int mTitleId;
        int mAnswerType;

        public AnswerTypeSpinnerItem(int iconDrawable, int titleId, int answerType) {
            mIconDrawable = iconDrawable;
            mTitleId = titleId;
            mAnswerType = answerType;
        }
    }


    public class AnswerTypeAdapter extends BaseAdapter {

        private ArrayList<AnswerTypeSpinnerItem> items;
        private Context mContext;

        public AnswerTypeAdapter(Context context, ArrayList<AnswerTypeSpinnerItem> objects) {

            items = objects;
            mContext = context;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public AnswerTypeSpinnerItem getItem(int position) {
            return items.get(position);
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return 0;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_answer_type_spiner, parent, false);

            ImageView imageView = (ImageView) convertView.findViewById(R.id.question_type_icon);
            TextView textView = (TextView) convertView.findViewById(R.id.question_type_title);

            AnswerTypeSpinnerItem item = getItem(position);

            imageView.setImageResource(item.mIconDrawable);
            textView.setText(item.mTitleId);

            return convertView;
        }
    }


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
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, R.anim.activity_close_exit);
    }

    private void hideSoftKeyboard() {
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        boolean handleReturn = super.dispatchTouchEvent(ev);

        View view = getCurrentFocus();

        int x = (int) ev.getX();
        int y = (int) ev.getY();

        if(view instanceof EditText){
            View innerView = getCurrentFocus();

            if (ev.getAction() == MotionEvent.ACTION_UP &&
                    !getLocationOnScreen(innerView).contains(x, y)) {

                InputMethodManager input = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                input.hideSoftInputFromWindow(getWindow().getCurrentFocus()
                        .getWindowToken(), 0);
            }
        }

        return handleReturn;
    }

    protected Rect getLocationOnScreen(View view) {
        Rect mRect = new Rect();
        int[] location = new int[2];

        view.getLocationOnScreen(location);

        mRect.left = location[0];
        mRect.top = location[1];
        mRect.right = location[0] + view.getWidth();
        mRect.bottom = location[1] + view.getHeight();

        return mRect;
    }
}
