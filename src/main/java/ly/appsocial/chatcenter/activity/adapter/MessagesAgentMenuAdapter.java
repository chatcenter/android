/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.activity.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.MessagesActivity;
import ly.appsocial.chatcenter.util.ViewUtil;

public class MessagesAgentMenuAdapter extends ArrayAdapter<MessagesActivity.MessagesAgentMenuItem> {
    private String mSelectedOrg;

    public MessagesAgentMenuAdapter(Context context, int resource,
                                    List<MessagesActivity.MessagesAgentMenuItem> objects) {
        super(context, resource, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_menu_org, parent, false);

            holder = new ViewHolder(convertView);


            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MessagesActivity.MessagesAgentMenuItem item = getItem(position);

        // ORG name
        holder.tvOrgName.setText(item.getOrg().name);

        // ORG unread count
        if (item.getOrg().unreadMessagesChannels != null && item.getOrg().unreadMessagesChannels.size() > 0) {
            holder.tvOrgUnreadCount.setText("" + item.getOrg().unreadMessagesChannels.size());
        }

        // Setup ava for ORG
        ViewUtil.loadImageCircle(holder.imvOrgAva, item.getOrg().iconUrl);
        holder.tvOrgAva.setText(item.getOrg().name.toUpperCase().substring(0, 1));

        GradientDrawable gradientDrawable = (GradientDrawable) holder.tvOrgAva.getBackground();
        gradientDrawable.setColor(ViewUtil.getIconColor(item.getOrg().uid));

        // Check if the current item is selected
        boolean selected = item.getValue().equals(mSelectedOrg);
        // Update the view content
        if (selected) {
            convertView.setBackgroundColor(getContext().getResources().getColor(R.color.color_chatcenter_separator_light));
        } else {
            convertView.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
        }
        return convertView;
    }

    public String getSelectedOrg() {
        return mSelectedOrg;
    }

    public void setSelectedOrg(String selectedOrg) {
        mSelectedOrg = selectedOrg;
    }

    private class ViewHolder {
        private TextView tvOrgName;
        private TextView tvOrgUnreadCount;
        private ImageView imvOrgAva;
        private TextView tvOrgAva;

        public ViewHolder(View convertView) {
            tvOrgName = (TextView) convertView.findViewById(R.id.tv_org_name);
            tvOrgUnreadCount = (TextView) convertView.findViewById(R.id.tv_org_unread_count);
            tvOrgAva = (TextView) convertView.findViewById(R.id.tv_org_icon);
            imvOrgAva = (ImageView) convertView.findViewById(R.id.imv_org_icon);
        }

    }
}
