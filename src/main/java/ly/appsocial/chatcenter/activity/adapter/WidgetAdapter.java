package ly.appsocial.chatcenter.activity.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.ChatActivity;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.dto.ws.response.GetAppsResponseDto;
import ly.appsocial.chatcenter.util.CCAuthUtil;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.util.ViewUtil;
import ly.appsocial.chatcenter.widgets.views.WidgetView;

/**
 * {@link ChatActivity} adapter.
 */
public class WidgetAdapter extends ArrayAdapter<ChatItem> {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** インフレーター */
	private LayoutInflater mInflater;


	/**
	 * コンストラクタ
	 *
	 * @param context コンテキスト
	 * @param items 項目リスト
	 */
	public WidgetAdapter(Context context, List<ChatItem> items) {
		super(context, 0, items);
		mInflater = LayoutInflater.from(context);
	}

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_list_widget, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ChatItem item = getItem(position);

		holder.nameTextView.setVisibility(View.VISIBLE);
		holder.nameTextView.setText(getDateStr(item.created * 1000) + "  "
				+ getTimeString(item.created * 1000) + "  " + item.user.displayName);

		holder.widgetView.setupCustomerView(item, null, false);

		return convertView;
	}

	/**
	 * 日付文字列を取得します。
	 *
	 * @param time タイムスタンプ(ms)
	 * @return 日付文字列
	 */
	private String getDateStr(long time) {
		String dateTimeString;

		SimpleDateFormat messageDateFormat = new SimpleDateFormat(getContext().getString(R.string.schedule_date_format));
		dateTimeString = messageDateFormat.format(new Date(time));

		return dateTimeString;
	}

	private String getTimeString(long time) {
		String dateTimeString;

		SimpleDateFormat messageDateFormat = new SimpleDateFormat(getContext().getString(R.string.datetime_format_time), Locale.JAPAN);
		dateTimeString = messageDateFormat.format(new Date(time));

		return dateTimeString;
	}

	// //////////////////////////////////////////////////////////////////////////
	// インナークラス
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * カスタマー ViewHolder
	 */
	public static class ViewHolder {

		/** 担当者名 */
		public TextView nameTextView;

		/** Display a widget message*/
		public WidgetView widgetView;

		public ViewHolder (View view) {
			nameTextView = (TextView) view.findViewById(R.id.tv_sender);
			widgetView = (WidgetView) view.findViewById(R.id.widget);
		}
	}

}