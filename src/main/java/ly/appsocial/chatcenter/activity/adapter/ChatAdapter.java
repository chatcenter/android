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
public class ChatAdapter extends ArrayAdapter<ChatItem> {

	// //////////////////////////////////////////////////////////////////////////
	// staticフィールド
	// //////////////////////////////////////////////////////////////////////////

	// ViewType
	/** カスタマー */
	public static final int VIEW_TYPE_CUSTOMER = 0;
	/** クライアント */
	public static final int VIEW_TYPE_CLIENT = 1;
	/** Information */
	public static final int VIEW_TYPE_INFORMATION = 2;
	/** View種類数*/
	public static final int NUMBER_OF_VIEW_TYPE = 3;
	private final int mUserId;

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** コンテキスト */
	private Context mContext;
	/** インフレーター */
	private LayoutInflater mInflater;

	/** チャネルUID */
	private String mChannelUid;
	/** メーラー起動可能かどうか */
	private boolean mCanMail = false;
	/** ユーザートークン */
	private String mUserToken;
	/** カスタマーアクションのクリック*/
	private WidgetView.StickerActionListener mStickerActionListener;


	/* このユーザーはAgentですか？Guestですか？*/
	private boolean isAgent;

	private GetAppsResponseDto.App mApp;

	/**
	 * コンストラクタ
	 *
	 * @param context コンテキスト
	 * @param items 項目リスト
	 */
	public ChatAdapter(Context context, List<ChatItem> items, WidgetView.StickerActionListener listener,
					   boolean isAgent, GetAppsResponseDto.App app) {
		super(context, 0, items);

		mContext = context;
		mStickerActionListener = listener;
		mInflater = LayoutInflater.from(context);
		mCanMail = canMail(getContext());
		mUserToken = CCAuthUtil.getUserToken(context);
		this.isAgent = isAgent;
		mUserId = CCAuthUtil.getUserId(context);
		mApp = app;
	}

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int itemViewType = getItemViewType(position);

		if (itemViewType == VIEW_TYPE_CUSTOMER) { // client
			return getCustomerView(position, convertView, parent);
		} else if (itemViewType == VIEW_TYPE_CLIENT) { // customer
			return getClientView(position, convertView, parent);
		} else {
			return null;
		}
	}

	@Override
	public int getItemViewType(int position) {
		ChatItem item = getItem(position);
		if (item.user == null) {
			return VIEW_TYPE_INFORMATION;
		} else {
			if (isAgent) {
				return item.user.admin || item.user.id == mUserId? VIEW_TYPE_CUSTOMER : VIEW_TYPE_CLIENT;
			} else {
				return item.user.admin ? VIEW_TYPE_CLIENT : VIEW_TYPE_CUSTOMER;
			}
		}
	}

	@Override
	public int getViewTypeCount() {
		return NUMBER_OF_VIEW_TYPE;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * チャットの情報を設定します。
	 * <p>
	 * チャネルUIDはアイコンカラーの決定に、ユーザーIDあメッセージがカスタマーのものかの判定にします。
	 * </p>
	 *
	 * @param channelUid チャネルUID
	 */
	public void setChatInfo(String channelUid) {
		mChannelUid = channelUid;
	}

	public void setApp(GetAppsResponseDto.App app) {
		mApp = app;
	}

	@Override
	public void notifyDataSetChanged() {
		mCanMail = canMail(getContext());
		/*
		 * このタイミングでメールアドレスをリンクにするかの情報も更新します。<br>
		 * メーラーをアンインストールまたは無効化された場合の対応のためです。
		 */
		super.notifyDataSetChanged();
	}

	// //////////////////////////////////////////////////////////////////////////
	// プライベートメソッド
	// //////////////////////////////////////////////////////////////////////////

	private String getItemStatusString(ChatItem item) {
		return null;
	}

	/**
	 * カスタマーのビューを生成します。
	 *
	 * @param position 項目位置
	 * @param convertView convertView
	 * @return 生成したビュー
	 */
	private View getCustomerView(int position, View convertView, ViewGroup parent) {

		ChatItem item = getItem(position);
		if (ResponseType.SUGGESTION.equals(item.type)) {
			return getSuggestionBubble(parent, item);
		}

		CustomerViewHolder holder;
		View view = convertView;
//		if (view == null) {
        view = mInflater.inflate(R.layout.chat_customer_listitem, parent, false);
        holder = new CustomerViewHolder(view);
        view.setTag(holder);
//		} else {
//            holder = (CustomerViewHolder) view.getTag();
//        }

		// 日付
		ChatItem prevItem = null;
		if (position > 0) {
			prevItem = getItem(position - 1);
		}
		if (prevItem == null || !item.getSimpleFormatedCreatedDate().equals(prevItem.getSimpleFormatedCreatedDate())) {
			holder.timestampTextView.setVisibility(View.VISIBLE);
			holder.timestampTextView.setText(getDateStr(item.created * 1000));
		} else {
			holder.timestampTextView.setVisibility(View.GONE);
		}

		// 担当者名
		/*UserItem user = position > 0 ? getItem(position - 1).user : null;
		if (position > 0 && user != null && user.id.equals(item.user.id)) {
			holder.nameTextView.setVisibility(View.GONE);
		} else {*/
			holder.nameTextView.setVisibility(View.VISIBLE);
			holder.nameTextView.setText(getTimeString(item.created * 1000) + "  " + item.user.displayName);
		/*}*/

		// Message status
		String statusStr = item.getStatusString(getContext());
		if (StringUtil.isNotBlank(statusStr)) {
			if (item.localStatus == ChatItem.ChatItemStatus.READ && !mApp.readForGuest
						&& !CCAuthUtil.isCurrentUserAdmin(getContext())) {
				statusStr = ""; // We do not let guest user view read status.
			}
			holder.messageStatusTextView.setText(statusStr);
		} else {
			holder.messageStatusTextView.setText("");
		}

		if (item.localStatus != null && item.localStatus == ChatItem.ChatItemStatus.SEND_FAILED) {
			holder.messageStatusTextView.setTextColor(Color.RED);
		} else {
			int textColor;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				textColor = getContext().getColor(R.color.color_chatcenter_text);
			} else {
				textColor = getContext().getResources().getColor(R.color.color_chatcenter_text);
			}
			holder.messageStatusTextView.setTextColor(textColor);
		}

		holder.widgetView.setupCustomerView(item, mStickerActionListener, mCanMail);

		return view;
	}

	/**
	 * クライアントのビューを生成します。
	 *
	 * @param position 項目位置
	 * @param convertView convertView
	 * @return 生成したビュー
	 */
	private View getClientView(int position, View convertView, ViewGroup parent) {

		ChatItem item = getItem(position);
		if (ResponseType.SUGGESTION.equals(item.type)) {
			return getSuggestionBubble(parent, item);
		}

		ClientViewHolder holder;
		View view = convertView;
//		if (view == null) {
        view = mInflater.inflate(R.layout.chat_client_listitem, parent, false);
        holder = new ClientViewHolder(view);
        view.setTag(holder);
//		} else {
//            holder = (ClientViewHolder) view.getTag();
//        }


		// 日付
		ChatItem prevItem = null;
		if (position > 0) {
			prevItem = getItem(position - 1);
		}
		if (prevItem == null || !item.getSimpleFormatedCreatedDate().equals(prevItem.getSimpleFormatedCreatedDate())) {
			holder.timestampTextView.setVisibility(View.VISIBLE);
			holder.timestampTextView.setText(getDateStr(item.created * 1000));
		} else {
			holder.timestampTextView.setVisibility(View.GONE);
		}

		// 担当者名
		/*UserItem user = position > 0 ? getItem(position - 1).user : null;
		if (position > 0 && user != null && user.id.equals(item.user.id)) {
			holder.nameTextView.setVisibility(View.GONE);
		} else {*/
			holder.nameTextView.setVisibility(View.VISIBLE);
			holder.nameTextView.setText(item.user.displayName + "  " + getTimeString(item.created * 1000));
		/*}*/

		// アイコン
		if (item.user.iconUrl == null || item.user.iconUrl.isEmpty()) { // アイコンテキスト
			holder.iconTextView.setVisibility(View.VISIBLE);
			holder.iconImageView.setVisibility(View.GONE);

			String iconText = item.user.displayName != null && item.user.displayName.length() > 0 ? item.user.displayName.substring(0, 1) : "";
			holder.iconTextView.setText(iconText);
			GradientDrawable gradientDrawable = (GradientDrawable) holder.iconTextView.getBackground();
			gradientDrawable.setColor(ViewUtil.getIconColor(mChannelUid));
		} else { // アイコン画像
			holder.iconTextView.setVisibility(View.GONE);
			holder.iconImageView.setVisibility(View.VISIBLE);
			ViewUtil.loadImageCircle(holder.iconImageView, item.user.iconUrl);
		}

		holder.widgetView.setupClientView(mUserToken, item, mStickerActionListener, mCanMail);


		return view;
	}

	private View getSuggestionBubble(ViewGroup parent, final ChatItem chatItem) {
		View view = mInflater.inflate(R.layout.view_suggestion_bubble, parent, false);

		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mStickerActionListener != null) {
					mStickerActionListener.onSuggestionBubbleClicked(chatItem);
				}
			}
		});

		return view;
	}

	/**
	 * 日付文字列を取得します。
	 *
	 * @param time タイムスタンプ(ms)
	 * @return 日付文字列
	 */
	private String getDateStr(long time) {
		/*Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		long todayTime = cal.getTime().getTime();
		long dayTime = 3600 * 24 * 1000;

		String retVal = "";
		if (time > todayTime) { // 今日
			String todayTimeString = getContext().getString(R.string.datetime_format_today);
			String timeFormat = todayTimeString.substring(todayTimeString.indexOf(" "));
			String today = todayTimeString.substring(0, todayTimeString.indexOf(" "));
			return today + new SimpleDateFormat(timeFormat, Locale.JAPAN).format(new Date(time));
		} else if (time > todayTime - dayTime) { // 昨日
			retVal = getContext().getString(R.string.datetime_yesterday) + " ";
			retVal += new SimpleDateFormat(getContext().getString(R.string.datetime_format_time), Locale.JAPAN).format(new Date(time));
		} else if (time > todayTime - dayTime * 2) { // 一昨日
			retVal = getContext().getString(R.string.datetime_2_days_ago) + " ";
			retVal += new SimpleDateFormat(getContext().getString(R.string.datetime_format_time), Locale.JAPAN).format(new Date(time));
		} else {
			return new SimpleDateFormat(getContext().getString(R.string.datetime_format_default), Locale.JAPAN).format(new Date(time));
		}
		return retVal;*/
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

	/**
	 * TextView のリンクからメール起動のインテントが発行可能か判定します。
	 *
	 * @param context コンテキスト
	 * @return インテント発行可能であれば true、そうでなければ false
	 */
	private static boolean canMail(final Context context) {

		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.fromParts("mailto", "xxx@xxx.xxx", null));

		// メーラーアプリがインストールされているか確認
		final List<ResolveInfo> appInfo = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if (appInfo != null && appInfo.size() > 0) {
			return true;
		}
		return false;
	}


	// //////////////////////////////////////////////////////////////////////////
	// インナークラス
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * カスタマー ViewHolder
	 */
	public static class CustomerViewHolder {
		/** タイムスタンプ */
		public TextView timestampTextView;

		/** 担当者名 */
		public TextView nameTextView;

		/** メッセージ */
		public TextView messageTextView;

		/** To show message status */
		public TextView messageStatusTextView;

		/** Display a widget message*/
		public WidgetView widgetView;

		public CustomerViewHolder (View view) {
			timestampTextView = (TextView) view.findViewById(R.id.chat_customer_listitem_timestamp_textview);
			nameTextView = (TextView) view.findViewById(R.id.chat_customer_listitem_name_textview);
			messageTextView = (TextView) view.findViewById(R.id.sticker_textview);
			messageStatusTextView = (TextView) view.findViewById(R.id.chat_customer_listitem_message_status_textview);
			widgetView = (WidgetView) view.findViewById(R.id.chat_customer_listitem_message_sticker);
		}
	}

	/**
	 * クライアント ViewHolder
	 */
	public static class ClientViewHolder {
		/** タイムスタンプ */
		public TextView timestampTextView;
		/** 担当者名 */
		public TextView nameTextView;
		/** アイコンテキスト */
		public TextView iconTextView;
		/** アイコン画像 */
		public ImageView iconImageView;

		public WidgetView widgetView;

		public ClientViewHolder(View view) {
			timestampTextView = (TextView) view.findViewById(R.id.chat_client_listitem_timestamp_textview);
			nameTextView = (TextView) view.findViewById(R.id.chat_client_listitem_name_textview);
			iconTextView = (TextView) view.findViewById(R.id.chat_client_listitem_icon_textview);
			iconImageView = (ImageView) view.findViewById(R.id.chat_client_listitem_icon_imageview);
			widgetView = (WidgetView) view.findViewById(R.id.chat_client_listitem_sticker);
		}
	}

}