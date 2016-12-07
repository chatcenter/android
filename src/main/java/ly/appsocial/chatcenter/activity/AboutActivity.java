package ly.appsocial.chatcenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;

public class AboutActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView mAboutListView;
    private List<AboutItem> mItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.about_header);


        mAboutListView = (ListView) findViewById(R.id.about_listview);
        setupAboutListView();
    }

    private void setupAboutListView() {
        mItems = new ArrayList<>();
        mItems.add(new AboutItem(AboutItem.TYPE_URL, getString(R.string.about_header), "https://www.chatcenter.io/ja/"));
        mItems.add(new AboutItem(AboutItem.TYPE_URL, getString(R.string.terms_of_service), "https://www.chatcenter.io/termsofservice/"));
        mItems.add(new AboutItem(AboutItem.TYPE_URL, getString(R.string.privacy_policy), "https://www.chatcenter.io/privacypolicy/"));
        mItems.add(new AboutItem(AboutItem.TYPE_URL, getString(R.string.service_level_agreement), "https://www.chatcenter.io/sla/"));
        mItems.add(new AboutItem(AboutItem.TYPE_OPEN_LISTVIEW_ACTIVITY, getString(R.string.copyright), null));

        String[] itemsStr = new String[mItems.size()];
        for (int i = 0; i < mItems.size(); i++) {
            itemsStr[i] = mItems.get(i).getText();
        }

        mAboutListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemsStr));
        mAboutListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AboutItem item = mItems.get(position);
        if (item.getType() == AboutItem.TYPE_URL) {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(ChatCenterConstants.Extra.URL, item.getUrl());
            intent.putExtra(ChatCenterConstants.Extra.ACTIVITY_TITLE, item.getText());
            startActivity(intent);
        } else if (item.getType() == AboutItem.TYPE_OPEN_LISTVIEW_ACTIVITY){
            Intent intent = new Intent(this, CopyrightActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public static class AboutItem {
        public static final int TYPE_URL = 0;
        public static final int TYPE_OPEN_LISTVIEW_ACTIVITY = 1;

        private int mType;
        private String mText;
        private String mUrl;

        public AboutItem(int type, String text, String url) {
            this.mType = type;
            this.mText = text;
            this.mUrl = url;
        }

        public int getType() {
            return mType;
        }

        public void setType(int type) {
            this.mType = type;
        }

        public String getText() {
            return mText;
        }

        public void setText(String text) {
            this.mText = text;
        }

        public String getUrl() {
            return mUrl;
        }

        public void setmUrl(String url) {
            this.mUrl = url;
        }
    }
}
