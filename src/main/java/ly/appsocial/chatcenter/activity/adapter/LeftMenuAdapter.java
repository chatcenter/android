package ly.appsocial.chatcenter.activity.adapter;


import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.model.LeftMenuChildItem;
import ly.appsocial.chatcenter.activity.model.LeftMenuGroupItem;
import ly.appsocial.chatcenter.util.ViewUtil;

public class LeftMenuAdapter extends BaseExpandableListAdapter{

    private HashMap<LeftMenuGroupItem, List<LeftMenuChildItem>> expandableListDetail;
    private Context mContext;
    private String mSelectedOrg;


    public LeftMenuAdapter(Context context, HashMap<LeftMenuGroupItem, List<LeftMenuChildItem>> listDetail) {
        mContext = context;
        expandableListDetail = listDetail;
    }

    @Override
    public int getGroupCount() {
        return expandableListDetail.keySet().size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return expandableListDetail.get(getGroup(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return expandableListDetail.keySet().toArray()[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return expandableListDetail.get(getGroup(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new GroupViewHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.item_menu_left_group, parent, false);

            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_group_title);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GroupViewHolder) convertView.getTag();
        }
        LeftMenuGroupItem menuItem = (LeftMenuGroupItem) getGroup(groupPosition);

        viewHolder.tvTitle.setText(menuItem.getTitle());
        viewHolder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(menuItem.getIconResource(), 0, R.drawable.icon_left_menu_indicator, 0);
        viewHolder.tvTitle.setSelected(isExpanded);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {


        ChildViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.item_menu_org, parent, false);
            holder = new ChildViewHolder(convertView);

            convertView.setTag(holder);

        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }

        LeftMenuChildItem item = (LeftMenuChildItem) getChild(groupPosition, childPosition);

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
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.color_chatcenter_separator_light));
        } else {
            convertView.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public String getSelectedOrg() {
        return mSelectedOrg;
    }

    public void setSelectedOrg(String selectedOrg) {
        mSelectedOrg = selectedOrg;
    }

    private class ChildViewHolder {
        private TextView tvOrgName;
        private TextView tvOrgUnreadCount;
        private ImageView imvOrgAva;
        private TextView tvOrgAva;

        public ChildViewHolder(View convertView) {
            tvOrgName = (TextView) convertView.findViewById(R.id.tv_org_name);
            tvOrgUnreadCount = (TextView) convertView.findViewById(R.id.tv_org_unread_count);
            tvOrgAva = (TextView) convertView.findViewById(R.id.tv_org_icon);
            imvOrgAva = (ImageView) convertView.findViewById(R.id.imv_org_icon);
        }
    }

    private class GroupViewHolder {
        private TextView tvTitle;
    }
}
