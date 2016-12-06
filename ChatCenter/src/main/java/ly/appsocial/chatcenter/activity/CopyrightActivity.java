package ly.appsocial.chatcenter.activity;

import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ly.appsocial.chatcenter.R;

public class CopyrightActivity extends BaseActivity {

    private static final String LIB_NAME = "name";
    private static final String COPYRIGHT = "copyright";

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copyright);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.copyright);

        mListView = (ListView) findViewById(R.id.copyright_view);

        try {
            List<Map<String, String>> data = getXMLFromResource();
            SimpleAdapter adapter = new SimpleAdapter(this, data,
                    android.R.layout.simple_list_item_2,
                    new String[] {LIB_NAME, COPYRIGHT},
                    new int[] {android.R.id.text1, android.R.id.text2});

            mListView.setAdapter(adapter);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<Map<String, String>> getXMLFromResource() throws IOException, XmlPullParserException {
        List<Map<String, String>> data = new ArrayList<>();
        HashMap<String, String> item = null;
        // Create ResourceParser for XML file
        XmlResourceParser xpp = getResources().getXml(R.xml.copyright);
        // check state
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (LIB_NAME.equals(xpp.getName())) {
                    if(item == null) {
                        item = new HashMap<>();
                    }
                    xpp.next();
                    item.put(LIB_NAME, xpp.getText().replaceAll("\t", ""));
                } else if (COPYRIGHT.equals(xpp.getName())) {
                    xpp.next();
                    item.put(COPYRIGHT, xpp.getText().replaceAll("\t", ""));
                    data.add(item);
                    item = null;
                }
            }
            eventType = xpp.next();

        }
        // indicate app done reading the resource.
        xpp.close();

        return data;
    }
}
