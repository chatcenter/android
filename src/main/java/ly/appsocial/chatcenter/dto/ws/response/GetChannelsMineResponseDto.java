/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.dto.ws.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import ly.appsocial.chatcenter.dto.ChannelItem;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.dto.UserItem;

/**
 * [GET /api/channels/mine] response.
 */
public class GetChannelsMineResponseDto {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** 項目 */
	public List<Channel> items = new ArrayList<>();

	/**
	 * 項目
	 */
	public static class Channel {

		/** チャネルUID */
		@SerializedName("uid")
		public String uid;
		/** 作成日 */
		@SerializedName("created")
		public long created;
		/** KISSコード */
		@SerializedName("org_uid")
		public String orgUid;
		/** 店舗名 */
		@SerializedName("org_name")
		public String orgName;
		/** ステータス */
		@SerializedName("status")
		public String status;
		/** ユーザーリスト */
		@SerializedName("users")
		public List<UserItem> users;
		/** 未読メッセージ数 */
		@SerializedName("unread_messages")
		public int unreadMessages;

		/** Channel's display name*/
		@SerializedName("display_name")
		public DisplayName displayName;

		/** 最新メッセージ */
		@SerializedName("latest_message")
		public LatestMessage latestMessage;

		/** Channel's last updated at */
		@SerializedName("last_updated_at")
		public double lastUpdatedAt;

		/**
		 * Assignee
		 */
		@SerializedName("assignee")
		public UserItem assignee;

		/**
		 * IconUrl
		 */
		@SerializedName("icon_url")
		public String iconUrl;

		/**
		 * Funnel id
		 */
		@SerializedName("funnel_id")
		public int funnel_id;

		/**
		 * 最新メッセージ
		 */
		public static class LatestMessage extends ChatItem {
		}

		/**
		 * チャットがクローズされているかどうかを判定します。
		 * 
		 * @return クローズされている場合は true、そうでない場合は false
		 */
		public boolean isClosed() {
			return "closed".equals(status);
		}

		public ChannelItem.ChannelStatus getChannelStatus() {
			if (status.equals(ChannelItem.CHANNEL_UNASSIGNED)) {
				return ChannelItem.ChannelStatus.CHANNEL_UNASSIGNED;
			} else if (status.equals(ChannelItem.CHANNEL_ASSIGNED)) {
				return ChannelItem.ChannelStatus.CHANNEL_ASSIGNED_TO_ME;
			} else if (status.equals(ChannelItem.CHANNEL_CLOSED)) {
				return ChannelItem.ChannelStatus.CHANNEL_CLOSE;
			} else {
				return ChannelItem.ChannelStatus.CHANNEL_ALL;
			}
		}

		public static class DisplayName {
			/** The name that will display on guest's UI*/
			@SerializedName("guest")
			public String guest;

			/** The name that will display on agent's UI*/
			@SerializedName("admin")
			public String admin;
		}
	}
}
