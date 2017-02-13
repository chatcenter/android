package ly.appsocial.chatcenter.sdksample;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import ly.appsocial.chatcenter.ChatCenter;
import ly.appsocial.chatcenter.ChatCenterClient;
import ly.appsocial.chatcenter.ws.ApiRequest;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    final String TEAM_ID = "developer_success";

    // Views
    private EditText mEdtFirstName;
    private EditText mEdtLastName;
    private EditText mEdtEmail;

    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);
        ChatCenter.initChatCenter(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


        mEdtFirstName = (EditText) findViewById(R.id.edt_first_name);
        mEdtLastName = (EditText) findViewById(R.id.edt_last_name);
        mEdtEmail = (EditText) findViewById(R.id.edt_email);

        View btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);

        View button = findViewById(R.id.open_history);
        button.setOnClickListener(this);


        View clearHistory = findViewById(R.id.clear_history);
        clearHistory.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();


        // Your app can not start if device not support google play services
        ChatCenter.getDeviceToken(this, new ChatCenterClient.GetDeviceTokenCallback() {
            @Override
            public void onSuccess(String deviceToken) {

            }

            @Override
            public void onError() {
                int errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
                GoogleApiAvailability.getInstance().showErrorDialogFragment(MainActivity.this, errorCode, 0);
            }
        });

    }

    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
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
        int id = v.getId();

        if (id == R.id.btn_login) {
            String firstName = mEdtFirstName.getText().toString();
            String lastName = mEdtLastName.getText().toString();
            String email = mEdtEmail.getText().toString();

            openChatActivityWithEmail(firstName, lastName, email);
        } else if (id == R.id.open_history) {
            if (ChatCenter.hasChatUser(this)) {
                openHistory();
            }
        } else if (id == R.id.clear_history) {
            clearHistory();
        }
    }

    private void openHistory() {
        ChatCenter.showMessages(this);
    }

    private void clearHistory() {
        ChatCenter.signOut(this, new ChatCenter.SignOutCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, R.string.history_cleared, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(ApiRequest.Error error) {
            }
        });
    }

    private void openChatActivityWithEmail(final String firstName, final String lastName, final String email) {
        mProgressDialog = ProgressDialog.show(this, getString(R.string.app_name), "Loading...", true);
        ChatCenter.signInWithNewUser(MainActivity.this, TEAM_ID, firstName, lastName, email, new ChatCenter.SignInCallback() {
            @Override
            public void onSuccess() {
                closeProgressDialog();
                Map<String, String> info = new HashMap<>();
                ChatCenter.showChat(MainActivity.this, TEAM_ID, firstName,
                        lastName, email, info);
            }

            @Override
            public void onError(ApiRequest.Error error) {
                closeProgressDialog();
            }
        });
    }
}
