/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

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
import ly.appsocial.chatcenter.dto.ws.response.GetMeResponseDto;
import ly.appsocial.chatcenter.ui.MessageItemView;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.util.ViewUtil;

/**
 * {@link MessagesActivity} adapter.
 */
public class MessagesAdapter extends ArrayAdapter<ChannelItem> {

    // //////////////////////////////////////////////////////////////////////////
    // インスタンスフィールド
    // //////////////////////////////////////////////////////////////////////////

    /**
     * コンテキスト
     */
    private Context mContext;
    /**
     * インフレーター
     */
    private LayoutInflater mInflater;
    /**
     * ユーザーID
     */
    private int mUserId;
    /**
     * 編集モードか
     */
    // private boolean mIsEdit;
    private boolean mIsAgent;

    ChannelListItemListener mListener;
    private GetMeResponseDto.Privilege mPrivilege;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param items   項目
     * @param userId  ユーザーID
     */
    public MessagesAdapter(Context context, List<ChannelItem> items, int userId, boolean isAgent, ChannelListItemListener listener) {
        super(context, 0, items);

        mInflater = LayoutInflater.from(context);
        mUserId = userId;
        mIsAgent = isAgent;
        mListener = listener;
    }

    // //////////////////////////////////////////////////////////////////////////
    // イベントメソッド
    // //////////////////////////////////////////////////////////////////////////

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.messages_listitem, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ChannelItem item = getItem(position);

        holder.mSLListItem.setShowMode(SwipeLayout.ShowMode.PullOut);
        holder.mSLListItem.addDrag(SwipeLayout.DragEdge.Right, holder.mSLListItem.findViewById(R.id.bottom_wrapper));

        holder.mSLListItem.getSurfaceView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.showChatScreen(item);
                }
            }
        });


        holder.btDelete.setVisibility(View.GONE);
        holder.btAssign.setVisibility(View.GONE);
        holder.btClose.setVisibility(View.GONE);

        if (mPrivilege != null && mPrivilege.channel != null) {
            if (mPrivilege.channel.contains("destroy")) {
                holder.btDelete.setVisibility(View.VISIBLE);
                holder.btDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.deleteChannel(item);
                        }
                    }
                });
            }

            if (mPrivilege.channel.contains("assign")) {
                holder.btAssign.setVisibility(View.VISIBLE);
                holder.btAssign.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.assignChannel(item);
                        }
                    }
                });
            }

            if (mPrivilege.channel.contains("close")) {
                holder.btCloseText.setText(item.isClosed() ? getContext().getString(R.string.open)
                        : getContext().getString(R.string.close));

                holder.btCloseIcon.setImageResource(item.isClosed() ? R.drawable.ic_conversation_open
                        : R.drawable.ic_conversation_close);

                holder.btClose.setVisibility(View.VISIBLE);
                holder.btClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.closeChannel(item);
                        }
                    }
                });
            }
        }

        // アイコン
        int iconColor = ViewUtil.getIconColor(item.uid);

        UserItem displayUser = item.getUserToDisplay(mIsAgent);
        String channelDisplayName = item.getDisplayName(getContext(), mIsAgent);

        if (displayUser != null && StringUtil.isNotBlank(displayUser.iconUrl)) {
            holder.mMvMainContent.setIconImage(displayUser.iconUrl);
        } else {
            holder.mMvMainContent.setIconText(channelDisplayName, iconColor);
        }

        // タイトル設定
        holder.mMvMainContent.setName(channelDisplayName);

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
        holder.mMvMainContent.setMessage(latestMessageBuilder.toString());

        // 日付
        if (item.latestMessage == null || item.latestMessage.created == null) {
            holder.mMvMainContent.setDate(getDateStr(item.created * 1000));
        } else {
            holder.mMvMainContent.setDate(getDateStr(item.latestMessage.created * 1000));
        }

        // Set Unread message
        holder.mMvMainContent.setTvUnreadMessage(item.unreadMessages);

        // Set show channel status
        if (mIsAgent) {
            holder.mMvMainContent.setTvChannelStatusShow(item.getChannelStatus() == ChannelItem.ChannelStatus.CHANNEL_UNASSIGNED);
        } else {
            holder.mMvMainContent.setTvChannelStatusShow(false);
        }

        return convertView;
    }

    public void setUserConfig(GetMeResponseDto.Privilege privilege) {
        this.mPrivilege = privilege;
    }

    public class ViewHolder {
        public SwipeLayout mSLListItem;
        public MessageItemView mMvMainContent;
        public LinearLayout btDelete;
        public LinearLayout btAssign;
        public LinearLayout btClose;
        public ImageView btCloseIcon;
        public TextView btCloseText;

        public ViewHolder(View view) {
            mSLListItem = (SwipeLayout) view.findViewById(R.id.swipe);
            mMvMainContent = (MessageItemView) view.findViewById(R.id.mv_content);
            btDelete = (LinearLayout) view.findViewById(R.id.bt_delete);
            btAssign = (LinearLayout) view.findViewById(R.id.bt_assign);
            btClose = (LinearLayout) view.findViewById(R.id.bt_close);
            btCloseIcon = (ImageView) view.findViewById(R.id.bt_close_icon);
            btCloseText = (TextView) view.findViewById(R.id.bt_close_text);
        }
    }

    // //////////////////////////////////////////////////////////////////////////
    // パブリックメソッド
    // //////////////////////////////////////////////////////////////////////////

    /**
     * 編集モードかどうかを設定します。
     *
     * @param isEdit 編集モードの場合は true、そうで無い場合は false
     */
    /*
	public void setEdit(boolean isEdit) {
		mIsEdit = isEdit;
	}
	*/

    /**
     * 編集モードかどうかを取得します。
     *
     * @return 編集モードの場合は true、そうで無い場合は false
     */
	/*
	public boolean isEdit() {
		return mIsEdit;
	}
	*/
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

    public interface ChannelListItemListener {
        /**
         * チャット画面に移転する
         * @param channel
         */
        void showChatScreen(ChannelItem channel);
        void deleteChannel(ChannelItem channel);
        void assignChannel(ChannelItem channel);
        void closeChannel(ChannelItem channel);
    }
}
