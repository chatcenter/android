/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.MessagesActivity;

import ly.appsocial.chatcenter.dto.ChannelItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.dto.UserItem;
import ly.appsocial.chatcenter.dto.ws.response.GetChannelsMineResponseDto;
import ly.appsocial.chatcenter.ui.MessageItemView;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.util.ViewUtil;

/**
 * {@link MessagesActivity} adapter.
 */
public class MessagesAdapter extends ArrayAdapter<GetChannelsMineResponseDto.Item> {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** コンテキスト */
	private Context mContext;
	/** インフレーター */
	private LayoutInflater mInflater;
	/** ユーザーID */
	private int mUserId;
	/** 編集モードか */
	private boolean mIsEdit;

	/**
	 * コンストラクタ
	 * 
	 * @param context コンテキスト
	 * @param items 項目
	 * @param userId ユーザーID
	 */
	public MessagesAdapter(Context context, List<GetChannelsMineResponseDto.Item> items, int userId) {
		super(context, 0, items);

		mInflater = LayoutInflater.from(context);
		mUserId = userId;
	}

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = convertView;
		if (view == null) {
			view = mInflater.inflate(R.layout.messages_listitem, null);
		}
		MessageItemView itemView = (MessageItemView) view;

		GetChannelsMineResponseDto.Item item = getItem(position);

		// 未読
//		itemView.setUnread(item.unreadMessages > 0);

		// チェックボック
		itemView.setCheckBoxVisibility(mIsEdit);

		UserItem guestUser = new UserItem();
		for (UserItem user : item.users) {
			if (!user.admin) {
				guestUser = user;
				break;
			}
		}

		// アイコン
		int iconColor = ViewUtil.getIconColor(item.uid);
		if (guestUser == null) {
			itemView.setIconText(item.orgName, iconColor);
		} else if(StringUtil.isNotBlank(guestUser.iconUrl)) {
			itemView.setIconImage(guestUser.iconUrl);
		} else {
			itemView.setIconText(guestUser.displayName, iconColor);
		}

		// 名前
		if (StringUtil.isBlank(guestUser.displayName)) {
			itemView.setName(item.orgName);
		} else {
			itemView.setName(guestUser.displayName);
		}

		// メッセージ
		StringBuilder latestMessageBuilder = new StringBuilder();
		if (item.latestMessage != null && item.latestMessage.user != null) {
			if (item.latestMessage.user.id == mUserId) {
				latestMessageBuilder.append(getContext().getString(R.string.you));
			} else {
				latestMessageBuilder.append(item.latestMessage.user.displayName);
				latestMessageBuilder.append(": ");
			}
		}

		if (item.latestMessage == null) {
			latestMessageBuilder.append(getContext().getString(R.string.no_message));
		} else if (ResponseType.STICKER.equals(item.latestMessage.type)) {
			latestMessageBuilder.append(getContext().getString(R.string.sent_a_sticker));
		} else if (ResponseType.CALL.equals(item.latestMessage.type)) {
			latestMessageBuilder.append(getContext().getString(R.string.called));
		} else {
			latestMessageBuilder.append(item.latestMessage.widget == null || StringUtil.isBlank(item.latestMessage.widget.text) ?
					getContext().getString(R.string.no_message) : item.latestMessage.widget.text);
		}
		itemView.setMessage(latestMessageBuilder.toString());

		// 日付
		if (item.latestMessage == null || item.latestMessage.created == null ) {
			itemView.setDate(getDateStr(item.created * 1000));
		} else {
			itemView.setDate(getDateStr(item.latestMessage.created * 1000));
		}

		// Set Unread message
		itemView.setTvUnreadMessage(item.unreadMessages);

		// Set show channel status
		itemView.setTvChannelStatusShow(item.getChannelStatus() == ChannelItem.ChannelStatus.CHANNEL_UNASSIGNED);

		return view;
	}

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * 編集モードかどうかを設定します。
	 * 
	 * @param isEdit 編集モードの場合は true、そうで無い場合は false
	 */
	public void setEdit(boolean isEdit) {
		mIsEdit = isEdit;
	}

	/**
	 * 編集モードかどうかを取得します。
	 * 
	 * @return 編集モードの場合は true、そうで無い場合は false
	 */
	public boolean isEdit() {
		return mIsEdit;
	}

	// //////////////////////////////////////////////////////////////////////////
	// プライベートメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * 日付文字列を取得します。
	 * 
	 * @param time タイムスタンプ(s)
	 * @return 日付文字列
	 */
	private String getDateStr(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		long todayTime = cal.getTime().getTime();

		if (time > todayTime) { // 今日
			return new SimpleDateFormat("HH:mm", Locale.JAPAN).format(new Date(time));
		} else {
			return new SimpleDateFormat("MM/dd", Locale.JAPAN).format(new Date(time));
		}
	}
}
