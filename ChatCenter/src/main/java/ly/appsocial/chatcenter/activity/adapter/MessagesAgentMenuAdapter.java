/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.activity.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.MessagesActivity;

public class MessagesAgentMenuAdapter extends ArrayAdapter<MessagesActivity.MessagesAgentMenuItem> {
    private String mSelectedOrg;
    private String mSelectedApp;
    private Context mContext;
    public MessagesAgentMenuAdapter(Context context, int resource, int textViewResourceId, List<MessagesActivity.MessagesAgentMenuItem> objects) {
        super(context, resource, textViewResourceId, objects);
        mContext = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        MessagesActivity.MessagesAgentMenuItem item = getItem(position);

        // Check if the current item is selected
        boolean selected = false;
        if (item.getType() == MessagesActivity.MessagesAgentMenuItem.ITEM_TYPE_APPS) {
            selected = item.getValue().equals(mSelectedApp);
        } else if (item.getType() == MessagesActivity.MessagesAgentMenuItem.ITEM_TYPE_ORGS) {
            selected = item.getValue().equals(mSelectedOrg);
        }

        // Update the view content
        TextView tv = ((TextView) v.findViewById(android.R.id.text1));
        String text = selected ? "<b>" + item.getDisplayText() + "</br>" : item.getDisplayText();
        tv.setText(Html.fromHtml(text));
        tv.setTextColor(mContext.getResources().getColor(R.color.color_chatcenter_text));
        tv.setVisibility(item.getType() == MessagesActivity.MessagesAgentMenuItem.ITEM_TYPE_SKIP ? View.GONE : View.VISIBLE);
        return v;
    }

    public String getSelectedOrg() {
        return mSelectedOrg;
    }

    public void setSelectedOrg(String selectedOrg) {
        mSelectedOrg = selectedOrg;
    }

    public String getSelectedApp() {
        return mSelectedApp;
    }

    public void setSelectedApp(String selectedApp) {
        mSelectedApp = selectedApp;
    }
}
