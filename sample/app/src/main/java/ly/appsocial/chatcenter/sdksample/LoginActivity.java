package ly.appsocial.chatcenter.sdksample;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

import ly.appsocial.chatcenter.ChatCenter;
import ly.appsocial.chatcenter.ws.ApiRequest;

public class LoginActivity extends AppCompatActivity {

	// Views
	private EditText mEdtFirstName;
	private EditText mEdtLastName;
	private EditText mEdtEmail;

	private String mTeamID;

	private InputMethodManager inputMethodManager;
	private View mainLayout;

	private Dialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle("");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mTeamID = getIntent().getStringExtra("team_id");
		if (mTeamID == null) {
			finish();
			return;
		}

		mEdtFirstName = (EditText) findViewById(R.id.edt_first_name);
		mEdtLastName = (EditText) findViewById(R.id.edt_last_name);
		mEdtEmail = (EditText) findViewById(R.id.edt_email);

		View btnLogin = findViewById(R.id.btn_login);
		btnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String firstName = mEdtFirstName.getText().toString();
				String lastName = mEdtLastName.getText().toString();
				String email = mEdtEmail.getText().toString();

				openChatActivityWithEmail(firstName, lastName, email);
			}
		});

		inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		mainLayout = findViewById(ly.appsocial.chatcenter.R.id.mainLayout);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		mainLayout.requestFocus();
		return false;
	}

	private void closeProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = null;
	}

	private void openChatActivityWithEmail(final String firstName, final String lastName, final String email) {
		mProgressDialog = ProgressDialog.show(this, getString(R.string.app_name), "Loading...", true);
		ChatCenter.signInWithNewUser(LoginActivity.this, mTeamID, firstName, lastName, email, new ChatCenter.SignInCallback() {
			@Override
			public void onSuccess() {
				closeProgressDialog();
				finish();
				Map<String, String> info = new HashMap<>();
				ChatCenter.showChat(LoginActivity.this, mTeamID, firstName,
						lastName, email, info);
			}

			@Override
			public void onError(ApiRequest.Error error) {
				closeProgressDialog();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
