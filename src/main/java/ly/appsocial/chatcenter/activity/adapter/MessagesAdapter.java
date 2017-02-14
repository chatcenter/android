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
public class MessagesAdapter extends ArrayAdapter<GetChannelsMineResponseDto.Channel> {

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
	private boolean mIsAgent;

	/**
	 * コンストラクタ
	 * 
	 * @param context コンテキスト
	 * @param items 項目
	 * @param userId ユーザーID
	 */
	public MessagesAdapter(Context context, List<GetChannelsMineResponseDto.Channel> items, int userId, boolean isAgent) {
		super(context, 0, items);

		mInflater = LayoutInflater.from(context);
		mUserId = userId;
		mIsAgent = isAgent;
	}

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		MessageItemView itemView;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.messages_listitem, parent, false);
			itemView = (MessageItemView) convertView;
			convertView.setTag(itemView);
		} else {
			itemView = (MessageItemView) convertView.getTag();
		}

		GetChannelsMineResponseDto.Channel item = getItem(position);

		// 未読
//		itemView.setUnread(item.unreadMessages > 0);

		// チェックボック
		itemView.setCheckBoxVisibility(mIsEdit);

		UserItem guestUser = new UserItem();
		UserItem asignee = item.assignee;
		for (UserItem user : item.users) {
			if (!user.admin) {
				guestUser = user;
				break;
			}
		}

		// アイコン
		int iconColor = ViewUtil.getIconColor(item.uid);

		UserItem displayUser;
		if (mIsAgent) {
			displayUser = guestUser;
		} else {
			displayUser = asignee;
		}

		String channelDisplayName;
		if (mIsAgent) {
			if (item.displayName != null && StringUtil.isNotBlank(item.displayName.admin)) {
				channelDisplayName = item.displayName.admin;
			} else if (StringUtil.isNotBlank(displayUser.displayName)) {
				channelDisplayName = displayUser.displayName;
			} else {
				channelDisplayName = getContext().getString(R.string.guest);
			}
		} else {
			if (item.displayName != null && StringUtil.isNotBlank(item.displayName.guest)) {
				channelDisplayName = item.displayName.guest;
			} else {
				channelDisplayName = item.orgName;
			}
		}

		if(displayUser != null && StringUtil.isNotBlank(displayUser.iconUrl)) {
			itemView.setIconImage(displayUser.iconUrl);
		} else {
			itemView.setIconText(channelDisplayName, iconColor);
		}

		// タイトル設定
		itemView.setName(channelDisplayName);

		// メッセージ
		StringBuilder latestMessageBuilder = new StringBuilder();
		if (item.latestMessage != null && item.latestMessage.user != null) {
			if (item.latestMessage.user.id != null && item.latestMessage.user.id == mUserId) {
				latestMessageBuilder.append(getContext().getString(R.string.you));
			} else {
				latestMessageBuilder.append(item.latestMessage.user.displayName);
				latestMessageBuilder.append(getContext().getString(R.string.person_name_suffix));
				latestMessageBuilder.append(": ");
			}
		}

		if (item.latestMessage == null) {
			latestMessageBuilder.append(getContext().getString(R.string.no_message));
		} else if (ResponseType.STICKER.equals(item.latestMessage.type)) {
			latestMessageBuilder.append(getContext().getString(R.string.sent_a_widget));
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
		if (mIsAgent) {
			itemView.setTvChannelStatusShow(item.getChannelStatus() == ChannelItem.ChannelStatus.CHANNEL_UNASSIGNED);
		} else {
			itemView.setTvChannelStatusShow(false);
		}

		return convertView;
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
