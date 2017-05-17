package ly.appsocial.chatcenter.activity.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.ChatActivity;
import ly.appsocial.chatcenter.activity.model.AssigneeFollowerListItem;
import ly.appsocial.chatcenter.util.CircleTransformation;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.util.ViewUtil;

/**
 * {@link ChatActivity} adapter.
 */
public class AssigneeFollowerUsersAdapter extends ArrayAdapter<AssigneeFollowerListItem> {

	List<AssigneeFollowerListItem> mItems;

	public AssigneeFollowerUsersAdapter(Context context, int resource, List<AssigneeFollowerListItem> objects) {
		super(context, resource, objects);

		mItems = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {

			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(R.layout.item_list_assignee_followers, parent, false);

			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final AssigneeFollowerListItem item = getItem(position);

		if (item == null || item.getUser() == null) {
			return null;
		}

		holder.setUpView(item);

		return convertView;
	}

	@Override
	public AssigneeFollowerListItem getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	public class ViewHolder {
		TextView tvName;
		ImageView cbSelected;
		TextView iconTextView;
		ImageView iconImageView;

		public ViewHolder(View view) {
			tvName = (TextView) view.findViewById(R.id.tv_name);
			cbSelected = (ImageView) view.findViewById(R.id.cb_selected);
			iconTextView = (TextView) view.findViewById(R.id.chat_client_listitem_icon_textview);
			iconImageView = (ImageView) view.findViewById(R.id.chat_client_listitem_icon_imageview);
		}

		public void setUpView(AssigneeFollowerListItem item) {
			tvName.setText(item.getUser().displayName);
			cbSelected.setVisibility(item.isSelected() ? View.VISIBLE : View.GONE);

			// アイコン
			String iconText = item.getUser().displayName != null && item.getUser().displayName.length() > 0 ? item.getUser().displayName.substring(0, 1) : "";
			iconTextView.setText(iconText.toUpperCase());
			GradientDrawable gradientDrawable = (GradientDrawable) iconTextView.getBackground();
			gradientDrawable.setColor(ViewUtil.getIconColor(item.getUser().id + ""));

			// アイコン画像
			if(StringUtil.isNotBlank(item.getUser().iconUrl)) {
                iconImageView.setVisibility(View.VISIBLE);
				String newUrl = item.getUser().iconUrl;
				Picasso.with(getContext()).load(newUrl).resize(70, 70).centerCrop().transform(new CircleTransformation()).into(iconImageView,
						new Callback() {
							@Override
							public void onSuccess() {
								iconImageView.setBackgroundColor(Color.WHITE);
							}

							@Override
							public void onError() {
								iconImageView.setVisibility(View.INVISIBLE);
							}
						});
			} else {
                iconImageView.setVisibility(View.INVISIBLE);
            }
		}
	}
}