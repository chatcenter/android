package ly.appsocial.chatcenter.activity.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.AssigneeFollowersUsersActivity;
import ly.appsocial.chatcenter.activity.ChatActivity;
import ly.appsocial.chatcenter.dto.FunnelItem;
import ly.appsocial.chatcenter.util.ViewUtil;

/**
 * {@link ChatActivity} adapter.
 */
public class FunnelAdapter extends ArrayAdapter<FunnelItem> {

	List<FunnelItem> mItems;

	public FunnelAdapter(Context context, int resource, List<FunnelItem> objects) {
		super(context, resource, objects);

		mItems = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.item_list_funnel, parent, false);

		TextView tvName = (TextView) view.findViewById(R.id.tv_funnel_name);
		ImageView cbSelected = (ImageView) view.findViewById(R.id.cb_selected);

		FunnelItem item = getItem(position);
		tvName.setText(item.name);
		cbSelected.setSelected(item.isSelected);

		return view;
	}

	@Override
	public FunnelItem getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public int getCount() {
		return mItems.size();
	}
}