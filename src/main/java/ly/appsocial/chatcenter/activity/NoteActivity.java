package ly.appsocial.chatcenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ly.appsocial.chatcenter.R;

public class NoteActivity extends  BaseActivity implements View.OnClickListener{

    public static final String CURRENT_NOTE_CONTENT = "current_note_content";
    private EditText mEdtNoteContent;
    private Button mBtSaveNote;

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
}
