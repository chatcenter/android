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

import org.w3c.dom.Text;

import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.AssigneeFollowersUsersActivity;
import ly.appsocial.chatcenter.activity.ChatActivity;
import ly.appsocial.chatcenter.util.ViewUtil;

/**
 * {@link ChatActivity} adapter.
 */
public class AssigneeFollowerUsersAdapter extends ArrayAdapter<AssigneeFollowersUsersActivity.ItemListAssigneeFollower> {

	List<AssigneeFollowersUsersActivity.ItemListAssigneeFollower> mItems;

	public AssigneeFollowerUsersAdapter(Context context, int resource, List<AssigneeFollowersUsersActivity.ItemListAssigneeFollower> objects) {
		super(context, resource, objects);

		mItems = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.item_list_assignee_followers, null);

		TextView tvName = (TextView) view.findViewById(R.id.tv_name);
		CheckBox cbSelected = (CheckBox) view.findViewById(R.id.cb_selected);
		TextView iconTextView = (TextView) view.findViewById(R.id.chat_client_listitem_icon_textview);
		ImageView iconImageView = (ImageView) view.findViewById(R.id.chat_client_listitem_icon_imageview);

		AssigneeFollowersUsersActivity.ItemListAssigneeFollower item = getItem(position);
		tvName.setText(item.getUser().displayName);
		cbSelected.setSelected(item.isSelected());

		// アイコン
		if (item.getUser().iconUrl == null || item.getUser().iconUrl.isEmpty()) { // アイコンテキスト
			iconTextView.setVisibility(View.VISIBLE);
			iconImageView.setVisibility(View.GONE);

			String iconText = item.getUser().displayName != null && item.getUser().displayName.length() > 0 ? item.getUser().displayName.substring(0, 1) : "";
			iconTextView.setText(iconText);
			GradientDrawable gradientDrawable = (GradientDrawable) iconTextView.getBackground();
			gradientDrawable.setColor(ViewUtil.getIconColor(item.getUser().id + ""));
		} else { // アイコン画像
			iconTextView.setVisibility(View.GONE);
			iconImageView.setVisibility(View.VISIBLE);

			ViewUtil.loadImageCircle(iconImageView, item.getUser().iconUrl);
		}

		return view;
	}

	@Override
	public AssigneeFollowersUsersActivity.ItemListAssigneeFollower getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public int getCount() {
		return mItems.size();
	}
}