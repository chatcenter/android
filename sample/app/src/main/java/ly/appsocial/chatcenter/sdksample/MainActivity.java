/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.sdksample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;

import ly.appsocial.chatcenter.ChatCenter;
import ly.appsocial.chatcenter.ChatCenterClient;
import ly.appsocial.chatcenter.ws.ApiRequest;

public class MainActivity extends AppCompatActivity {

	final String TEAM_ID = "developer_success";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle("");

		ChatCenter.initChatCenter(this);

		View button = findViewById(R.id.chat_button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startConversation();
			}
		});


		View clearHistory = findViewById(R.id.clear_history);
		clearHistory.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				clearHistory();
			}
		});

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

	private void startConversation(){
		if (ChatCenter.hasChatUser(this)) {
			openHistory();
		} else {
			Intent intent = new Intent(this, LoginActivity.class);
			intent.putExtra("team_id", TEAM_ID);
			startActivity(intent);
		}
	}

	private void openHistory(){
		ChatCenter.showMessages(this);
	}

	private void clearHistory(){
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

}
