package ly.appsocial.chatcenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.fragment.AlertDialogFragment;
import ly.appsocial.chatcenter.util.DialogUtil;

public class NoteActivity extends  BaseActivity implements View.OnClickListener, AlertDialogFragment.DialogListener{

    public static final String CURRENT_NOTE_CONTENT = "current_note_content";
    private EditText mEdtNoteContent;
    private Button mBtSaveNote;
    private boolean isCanShowAlertTooLongTextMsg = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.note);

        mEdtNoteContent = (EditText) findViewById(R.id.edt_note);
        mBtSaveNote = (Button) findViewById(R.id.bt_save);
        mBtSaveNote.requestFocus();

        mBtSaveNote.setOnClickListener(this);
        mEdtNoteContent.setText(getIntent().getStringExtra(CURRENT_NOTE_CONTENT));
        mEdtNoteContent.setCursorVisible(false);
        mEdtNoteContent.setOnClickListener(this);
        mEdtNoteContent.addTextChangedListener(new NoteTextWatcher());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_save) {
            String content = mEdtNoteContent.getText().toString();
            Intent intent = new Intent();
            intent.putExtra("result", content);
            setResult(RESULT_OK, intent);
            finish();
        } else if (v.getId() == R.id.edt_note) {
            mEdtNoteContent.setCursorVisible(true);
            mEdtNoteContent.setSelection(mEdtNoteContent.getText().length());
        }
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

    /**
     * ノート入力のテキストボックスのウォッチャー
     */
    private class NoteTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // empty
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            Editable editable = mEdtNoteContent.getText();

            // span の削除
            CharacterStyle[] spans = editable.getSpans(0, editable.length(), CharacterStyle.class);
            for (CharacterStyle span : spans) {
                editable.removeSpan(span);
            }

            // 送信ボタンの有効設定
            String text = mEdtNoteContent.getText().toString();
            mBtSaveNote.setEnabled(!text.matches("^[\\s　]*$") && text.length() <= ChatCenterConstants.MAX_NOTE_LENGTH);


            if (text.length() <= ChatCenterConstants.MAX_NOTE_LENGTH) {
                isCanShowAlertTooLongTextMsg = true;
            }

            if (text.length() > ChatCenterConstants.MAX_NOTE_LENGTH && isCanShowAlertTooLongTextMsg) {

                DialogUtil.showAlertDialog(getSupportFragmentManager(),
                        DialogUtil.Tag.ALERT,
                        null,
                        String.format(getString(R.string.alert_message_text_too_long), ChatCenterConstants.MAX_NOTE_LENGTH));

                isCanShowAlertTooLongTextMsg = false;

                String note = text.substring(0, ChatCenterConstants.MAX_NOTE_LENGTH);
                mEdtNoteContent.setText(note);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // empty
        }
    }
}
