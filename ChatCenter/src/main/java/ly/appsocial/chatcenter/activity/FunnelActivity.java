package ly.appsocial.chatcenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.adapter.FunnelAdapter;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.FunnelItem;

public class FunnelActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private ArrayList<FunnelItem> mFunnels;

    private ListView mListView;
    private FunnelAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funnel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.funnel);

        mFunnels = (ArrayList<FunnelItem>) getIntent().getSerializableExtra(ChatCenterConstants.Extra.FUNNEL_LIST);

        mListView = (ListView) findViewById(R.id.lv_funnels);
        mAdapter = new FunnelAdapter(this, R.layout.item_list_funnel, mFunnels);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        for(int i = 0; i < mFunnels.size(); i++) {
            if (position == i) {
                mFunnels.get(i).isSelected = !mFunnels.get(i).isSelected;
            } else {
                mFunnels.get(i).isSelected = false;
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    public void onSaveButtonClicked(View view) {
        FunnelItem selectedItem = null;
        for (FunnelItem item : mFunnels) {
            if (item.isSelected) {
                selectedItem = item;
                break;
            }
        }

        if (selectedItem != null) {
            Intent intent = new Intent();
            intent.putExtra("result", selectedItem);
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }
}
