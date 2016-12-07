package ly.appsocial.chatcenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.fragment.PreviewImageFragment;
import ly.appsocial.chatcenter.fragment.PreviewWidgetFragment;

public class PreviewActivity extends BaseActivity {

	private PreviewImageFragment previewImageFragment = null;
	private PreviewWidgetFragment previewWidgetFragment = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.preview_title);

		Button sendButton = (Button)toolbar.findViewById(R.id.send_button);
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				setResult(RESULT_OK, intent);
				finish();
			}
		});

		if ( getIntent().hasExtra("type") ) {
			String type = getIntent().getStringExtra("type");
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			if ( type.equals("image") ){
				if ( getIntent().hasExtra("image_path") ){
					String imagePath = getIntent().getStringExtra("image_path");
					previewImageFragment = new PreviewImageFragment();
					previewImageFragment.mImagePath = imagePath;
					ft.replace(R.id.container, previewImageFragment);
				} else {
					finish();
				}
			} else {
				if ( getIntent().hasExtra("content") ){
					String content = getIntent().getStringExtra("content");
					previewWidgetFragment = new PreviewWidgetFragment();
					previewWidgetFragment.mContent = content;
					ft.replace(R.id.container, previewWidgetFragment);
				} else {
					finish();
				}
			}
			ft.commit();
		} else {
			finish();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
			case android.R.id.home: {
				Intent intent = new Intent();
				setResult(RESULT_CANCELED, intent);
				finish();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}
}
