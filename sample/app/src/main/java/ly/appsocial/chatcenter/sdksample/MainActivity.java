/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.sdksample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import ly.appsocial.chatcenter.ChatCenter;
import ly.appsocial.chatcenter.ws.ApiRequest;

public class MainActivity extends Activity {

	final String TEAM_ID = "[YOUR_TEAM_ID_HERE]";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ChatCenter.initChatCenter(this);

		View button = findViewById(R.id.chat_button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openChat();
			}
		});

		View history = findViewById(R.id.history);
		history.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openHistory();
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

	private void openChat(){
		if (ChatCenter.hasChatUser(this)) {
			Map<String, String> info = new HashMap<>();
			ChatCenter.showChat(this, TEAM_ID, null, null, 0, 0, info);
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
