/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.util.ViewUtil;

/**
 * 履歴画面の1行のカスタムビュー
 */
public class MessageItemView extends LinearLayout {

	// //////////////////////////////////////////////////////////////////////////
	// インスタンスフィールド
	// //////////////////////////////////////////////////////////////////////////

	/** コンテキスト */
	private Context mContext;

	// View
	/** チェッボックス */
	// private CheckBox mCheckBox;
	/** アイコンテキスト */
	private TextView mIconTextView;
	/** アイコン画像 */
	private ImageView mIconImageView;
	/** 名前 */
	private TextView mNameTextView;
	/** メッセージ */
	private TextView mMessageTextView;
	/** 日付 */
	private TextView mDateTextView;
	/** Channel Status*/
	private TextView mTvChannelStatus;
	/** Unread message count*/
	private TextView mTvUnreadMessage;

	// etc
	/** 未読表示にするかどうか */
	// private int mIsUnread;

	/**
	 * コンストラクタ
	 *
	 * @param context
	 */
	public MessageItemView(final Context context) {
		this(context, null);
	}

	/**
	 * コンストラクタ
	 *
	 * @param context
	 * @param attrs
	 */
	public MessageItemView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		inflate(context, R.layout.ui_messageitemview, this);
		setOrientation(VERTICAL);

		// mCheckBox = (CheckBox) findViewById(R.id.ui_messageitemview_checkbox);
		mIconTextView = (TextView) findViewById(R.id.ui_messageitemview_icon_textview);
		mIconImageView = (ImageView) findViewById(R.id.ui_messageitemview_icon_imageview);
		mNameTextView = (TextView) findViewById(R.id.ui_messageitemview_name_textview);
		mMessageTextView = (TextView) findViewById(R.id.ui_messageitemview_message_textview);
		mDateTextView = (TextView) findViewById(R.id.ui_messageitemview_date_textview);
		mTvChannelStatus = (TextView) findViewById(R.id.tv_channel_status);
		mTvUnreadMessage = (TextView) findViewById(R.id.tv_unread_msg);
	}

	// //////////////////////////////////////////////////////////////////////////
	// イベントメソッド
	// //////////////////////////////////////////////////////////////////////////

	/*@Override
	public boolean isChecked() {
		return mCheckBox.isChecked();
	}

	@Override
	public void setChecked(final boolean checked) {
		mCheckBox.setChecked(checked);
	}

	@Override
	public void toggle() {
	}*/

	// //////////////////////////////////////////////////////////////////////////
	// パブリックメソッド
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * 未読表示にするかどうかを設定します。
	 */
//	public void setUnread(boolean isUnread) {
//		mIsUnread = isUnread;
//	}

	/**
	 * チェックボックの表示/非表示を設定します。
	 *
	 * @param visibility
	 */
	/*public void setCheckBoxVisibility(boolean visibility) {
		mCheckBox.setVisibility(visibility ? View.VISIBLE : View.GONE);
	}*/

	/**
	 * アイコンテキストを設定します。
	 * 
	 * @param userName ユーザー名
	 * @param color アイコンのRGBカラー
	 */
	public void setIconText(String userName, int color) {
		mIconTextView.setVisibility(View.VISIBLE);
		mIconImageView.setVisibility(View.GONE);

		if (StringUtil.isNotBlank(userName)) {
			mIconTextView.setText(userName.toUpperCase().substring(0, 1));
		}

		GradientDrawable gradientDrawable = (GradientDrawable) mIconTextView.getBackground();
		gradientDrawable.setColor(color);
	}

	/**
	 * アイコン画像を設定します。
	 * 
	 * @param iconUrl アイコン画像URL
	 */
	public void setIconImage(String iconUrl) {
		mIconTextView.setVisibility(View.GONE);
		mIconImageView.setVisibility(View.VISIBLE);

		ViewUtil.loadImageCircle(mIconImageView, iconUrl);
	}

	/**
	 * 名前を設定します。
	 * 
	 * @param name
	 */
	public void setName(String name) {
		mNameTextView.setText(name);
//		mNameTextView.setTextAppearance(mContext, mIsUnread ? R.style.common_bold_textview : R.style.common_normal_textview);
	}

	/**
	 * メッセージを設定します。
	 * 
	 * @param message
	 */
	public void setMessage(String message) {
		mMessageTextView.setText(message);
//		mMessageTextView.setTextAppearance(mContext, mIsUnread ? R.style.common_bold_textview : R.style.common_normal_textview);
	}

	/**
	 * 日付を設定します。
	 * 
	 * @param date
	 */
	public void setDate(String date) {
		mDateTextView.setText(date);
//		mDateTextView.setTextAppearance(mContext, mIsUnread ? R.style.common_bold_textview : R.style.common_normal_textview);
	}

	public void setTvUnreadMessage(int unreadMessage) {
		if (unreadMessage > 0) {
			mTvUnreadMessage.setText(unreadMessage + "");
			mTvUnreadMessage.setVisibility(VISIBLE);
		} else {
			mTvUnreadMessage.setVisibility(GONE);
		}
	}

	public void setTvChannelStatusShow(boolean show) {
		mTvChannelStatus.setVisibility(show ? VISIBLE : GONE);
	}
}
