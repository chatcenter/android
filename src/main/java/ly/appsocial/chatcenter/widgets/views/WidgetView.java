package ly.appsocial.chatcenter.widgets.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ly.appsocial.chatcenter.BuildConfig;
import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.dto.ResponseType;
import ly.appsocial.chatcenter.ui.RoundImageView;
import ly.appsocial.chatcenter.util.ViewUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.widgets.LiveLocationUser;


public class WidgetView extends FrameLayout {
	private static final String TAG = "StickerView";

	private TextView mTextView;
	private RoundImageView mImageView;
	private LinearLayout mActionSelectContainer;
	private View mActionConfirmContainer;
	private RelativeLayout mActionLinearContainer;
	private Button mActionConfirmPositive;
	private Button mActionConfirmNegative;

	private RecyclerView mLiveThumbnailView;
	private TextView mLiveLabel;
	private RecyclerView.LayoutManager mLayoutManager;
	private RecyclerView.Adapter mAdapter;

	private int mSelectActionBackgroundId;
	private int mSelectActionLastBackgroundId;
	private int mBackgroundMain;

	private ColorStateList mActionTextColorDrawable;

	private StickerActionListener mStickerActionListener;

	private ChatItem mChatItem;
	private String mUserToken;

	private ArrayList<CheckBox> mCheckBoxList = new ArrayList<>();
	private ArrayList<RadioButton> mRadioButtonList = new ArrayList<>();

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public WidgetView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(attrs);
	}

	public WidgetView(Context context) {
		super(context);
	}

	public WidgetView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public WidgetView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		// Inflate the children
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.view_sticker, this, true);
		findChildViews(v);

		// Now provide the view with values
		setDefaultValue(attrs);
	}

	private void setDefaultValue(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WidgetView, 0, 0);

		// Show image
		Drawable drawable = a.getDrawable(R.styleable.WidgetView_image);
		setDrawable(drawable);

		// Show text
		String text = (String) a.getText(R.styleable.WidgetView_text);
		setText(text);

		// Show text color
		int color = a.getColor(R.styleable.WidgetView_textColor, Color.BLACK);
//		setTextColor(color);

		setConfirmActionBackground(
				a.getResourceId(R.styleable.WidgetView_backgroundConfirmNegative, -1),
				a.getResourceId(R.styleable.WidgetView_backgroundConfirmPositive, -1)
		);

		mSelectActionBackgroundId = a.getResourceId(R.styleable.WidgetView_backgroundSelect, -1);
		mSelectActionLastBackgroundId = a.getResourceId(R.styleable.WidgetView_backgroundSelectLast, -1);

		mActionTextColorDrawable = a.getColorStateList(R.styleable.WidgetView_actionTextColor);
//		setActionTextColor(mActionTextColorDrawable);

		// Setting background for sticker
		mBackgroundMain = a.getResourceId(R.styleable.WidgetView_backgroundMain, 1);
		this.setBackgroundResource(mBackgroundMain);

		mLiveThumbnailView.setVisibility(View.GONE);
		mLiveLabel.setVisibility(View.GONE);

		a.recycle();
	}

	public void setupCustomerView(ChatItem item, StickerActionListener listener){
		setChatItem(item);

		if (ResponseType.STICKER.equals(item.type) || ResponseType.CALL.equals(item.type)) {
			setStickerActionListener(listener);
			return;
		}

		reset();
		setMessageFromCustomer(item.widget.text);
	}

	public void setupClientView(String userToken, ChatItem item, StickerActionListener listener){
		mUserToken = userToken;
		setChatItem(item);

		if (ResponseType.STICKER.equals(item.type) || ResponseType.CALL.equals(item.type)) {
			setStickerActionListener(listener);
			return;
		}

		reset();
		if ( item.widget != null && item.widget.text != null ){
			setMessageFromClient(item.widget.text);
		}
	}

	public void loadImageToImageView(String url){
		Picasso.with(getContext()).load(addToken(mUserToken, url)).into(getImageView());
		getImageView().addRounded(RoundImageView.RoundedOptions.BOTH);
		getImageView().setVisibility(View.VISIBLE);
	}

	/**
	 * URL の末尾にユーザートークンのパラメータを付与します。
	 *
	 * @param url URL
	 * @return トークン付きの URL
	 */
	private String addToken(String userToken, String url) {
		return url + "?authentication=" + userToken;
	}

	public void setConfirmActionBackground(int positiveResId, int negativeResId) {
		mActionConfirmNegative.setBackgroundResource(negativeResId);
		mActionConfirmPositive.setBackgroundResource(positiveResId);
	}

	public void setActionTextColor(ColorStateList color) {
		if (color == null) {
			return;
		}
		mActionConfirmPositive.setTextColor(color);
		mActionConfirmNegative.setTextColor(color);
	}

	private void findChildViews(View v) {
		mTextView = (TextView) v.findViewById(R.id.sticker_textview);
		mImageView = (RoundImageView) v.findViewById(R.id.sticker_image);
		mActionSelectContainer = (LinearLayout)v.findViewById(R.id.sticker_action_select_container);
		mActionLinearContainer = (RelativeLayout)v.findViewById(R.id.sticker_action_linear_container);
		mActionConfirmContainer = v.findViewById(R.id.sticker_action_confirm_contaier);
		mActionConfirmPositive = (Button) v.findViewById(R.id.sticker_action_confirm_positive);
		mActionConfirmNegative = (Button) v.findViewById(R.id.sticker_action_confirm_negative);

		mLiveThumbnailView = (RecyclerView) v.findViewById(R.id.live_thumbnail);
		mLayoutManager = new LinearLayoutManager(getContext(), LinearLayout.HORIZONTAL, false);
		mLiveThumbnailView.setLayoutManager(mLayoutManager);
		mAdapter = new LiveThumbnailAdapter();
		mLiveThumbnailView.setAdapter(mAdapter);

		mLiveLabel = (TextView) v.findViewById(R.id.bt_live);

		mImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( mChatItem.widget != null ){
					mChatItem.widget.onTappedImage(WidgetView.this.getContext());
				}
			}
		});
	}

	public void setDrawable(Drawable drawable) {
		if (drawable == null) {
			mImageView.setVisibility(GONE);
			return;
		}

		mImageView.setVisibility(View.VISIBLE);
		mImageView.setImageDrawable(drawable);
	}

	public void setImageUrl(String url) {
		if (StringUtil.isBlank(url)) {
			Picasso.with(getContext()).cancelRequest(mImageView);
			mImageView.setVisibility(GONE);
			return;
		}

		mImageView.setVisibility(View.VISIBLE);
		if (url.startsWith("http")) {
			Picasso.with(getContext()).load(url).into(mImageView);
		} else {
			Picasso.with(getContext()).load(new File(url)).into(mImageView);
		}
	}

	public void setText(String text) {
		if (text == null) {
			mTextView.setVisibility(GONE);
			mImageView.addRounded(RoundImageView.RoundedOptions.TOP);
			return;
		}

		mImageView.removeRounded(RoundImageView.RoundedOptions.TOP);
		mTextView.setVisibility(View.VISIBLE);
		mTextView.setText(text);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			mTextView.setTextColor(getResources().getColor(R.color.color_chatcenter_widget_text, null));
		} else {
			mTextView.setTextColor(getResources().getColor(R.color.color_chatcenter_widget_text));
		}
	}

	public void setWidgetIcon(String actionType) {
		int drawable = 0;
		if (actionType.equals(BasicWidget.WIDGET_TYPE_CONFIRM)) {
			drawable = R.drawable.icon_widget_question;
		} else if (actionType.equals(BasicWidget.WIDGET_TYPE_SELECT)) {
			drawable = R.drawable.icon_widget_schedule;
		} else if (actionType.equals(BasicWidget.WIDGET_TYPE_LOCATION)) {
			drawable = R.drawable.icon_widget_location;
		} else if (actionType.equals(BasicWidget.WIDGET_TYPE_COLOCATION)) {
			drawable = R.drawable.icon_widget_colocation;
		}

		ImageView widgetIcon = (ImageView) findViewById(R.id.widget_icon);
		widgetIcon.setImageResource(drawable);
		widgetIcon.setVisibility(VISIBLE);
	}

	public void setMessageFromClient(String message) {

		setText(message);

		Drawable backImage = DrawableCompat.wrap(getResources().getDrawable(R.drawable.bg_chat_client_bubble));
		DrawableCompat.setTint(backImage, getResources().getColor(R.color.color_chatcenter_operator_bubble));
		mTextView.setBackgroundDrawable(backImage);

		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTextView.getLayoutParams();
		params.setMargins(0,0,0,0);
		mTextView.setLayoutParams(params);
		mTextView.setTextColor(getResources().getColor(R.color.color_chatcenter_operator_bubble_text));
	}

	public void setMessageFromCustomer(String message) {
		setText(message);

		Drawable backImage = DrawableCompat.wrap(getResources().getDrawable(R.drawable.bg_chat_customer_bubble));
		DrawableCompat.setTint(backImage, getResources().getColor(R.color.color_chatcenter_customer_bubble));
		mTextView.setBackgroundDrawable(backImage);

		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTextView.getLayoutParams();
		params.setMargins(0,0,0,0);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mTextView.setLayoutParams(params);
		mTextView.setTextColor(getResources().getColor(R.color.color_chatcenter_customer_bubble_text));
		mTextView.setLinkTextColor(getResources().getColor(R.color.color_chatcenter_customer_bubble_text));
	}

	public void setTextColor(int color) {
		mTextView.setTextColor(color);
	}

	public StickerActionListener getStickerActionListener() {
		return mStickerActionListener;
	}

	public void setStickerActionListener(StickerActionListener stickerActionListener) {
		mStickerActionListener = stickerActionListener;
	}

	public ChatItem getChatItem() {
		return mChatItem;
	}

	public void reset() {
		mImageView.setVisibility(View.GONE);
		mActionSelectContainer.setVisibility(View.GONE);
		mActionLinearContainer.setVisibility(View.GONE);
		mActionConfirmContainer.setVisibility(View.GONE);
		Picasso.with(getContext()).cancelRequest(getImageView());
		this.setBackgroundResource(0);
	}

	public void setChatItem(ChatItem chatItem) {
		mChatItem = chatItem;
		if ( mChatItem.widget != null ){
			mChatItem.widget.setupWidgetView(this, this.getContext());

			if ( mChatItem.widget.stickerType != null &&
					ChatCenterConstants.StickerName.STICKER_TYPE_CO_LOCATION.equals(mChatItem.widget.stickerType)) {
				mChatItem.initLiveLocationUser(this.getContext());
				setupLiveLocationView();
			}
		}
	}

	private void setupLiveLocationView(){
		if ( mChatItem.getActiveLiveLocationUsersCount() == 0 ){
			mLiveThumbnailView.setVisibility(View.GONE);
			mLiveLabel.setVisibility(View.GONE);
		} else {
			mLiveThumbnailView.setVisibility(View.VISIBLE);
			mLiveLabel.setVisibility(View.VISIBLE);
		}
	}

	public void showSelectActions(BasicWidget.StickerAction stickerAction) {
		List<BasicWidget.StickerAction.ActionData> actions;
		if (stickerAction == null) {
			return;
		}

		actions = stickerAction.actionData;
		mActionSelectContainer.removeAllViews();

		if (actions == null) {
			return;
		}

		// Remove ProposeOtherSlots Action
//		for (BasicWidget.StickerAction.ActionData action: actions) {
//			if (action.isProposeOtherSlotAction()) {
//				actions.remove(action);
//			}
//		}

		// Show list of actions
		for (int i = 0; i < actions.size(); i++) {
			BasicWidget.StickerAction.ActionData action = actions.get(i);

			// Create the view and add UI setting
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			Button btn = (Button) inflater.inflate(R.layout.layout_btn_select_action, null);
			if (btn != null) {
				btn.setText(action.label);
				if (i < actions.size() - 1) {
					btn.setBackgroundResource(mSelectActionBackgroundId);
				} else {
					btn.setBackgroundResource(mSelectActionLastBackgroundId);
				}

			//	btn.setTextColor(mActionTextColorDrawable);

				// Handler
				btn.setOnClickListener(new ActionOnClickListener(action));

				// Add Top divider for first item
				if (i == 0) {
					mActionSelectContainer.addView(getStickerActionsDivider());
				}

				btn.setSelected(mChatItem.widget.isSelectedAction(action));

				LinearLayout.LayoutParams lp = new
						LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						getResources().getDimensionPixelSize(R.dimen.sticker_select_button_height));

				// Add view to container
				mActionSelectContainer.addView(btn, lp);

				// Add Bottom divider
				if (i < actions.size() -1) {
					mActionSelectContainer.addView(getStickerActionsDivider());
				}
			}
		}
		showSelectActions(true);


		Log.d(TAG, "showSelectActions: " + actions.size());
		Log.d(TAG, "showSelectActions: " + mActionSelectContainer.getVisibility());
	}

	public void showCheckboxActions(BasicWidget.StickerAction stickerAction) {
		List<BasicWidget.StickerAction.ActionData> actions;
		if (stickerAction == null) {
			return;
		}

		actions = stickerAction.actionData;
		mActionSelectContainer.removeAllViews();

		if (actions == null) {
			return;
		}

		mCheckBoxList.clear();

		// Show list of actions
		for (int i = 0; i < actions.size(); i++) {
			BasicWidget.StickerAction.ActionData action = actions.get(i);

			// Create the view and add UI setting
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View item = inflater.inflate(R.layout.layout_btn_checkbox_action, null);

			if (item != null) {
				CheckBox check = (CheckBox)item.findViewById(R.id.checkbox);

				final float scale = this.getResources().getDisplayMetrics().density;
				check.setPadding(check.getPaddingLeft() + (int)(10.0f * scale + 0.5f),
						check.getPaddingTop(),
						check.getPaddingRight(),
						check.getPaddingBottom());

				check.setText(action.label);
				item.setBackgroundResource(mSelectActionBackgroundId);

				// Add Top divider for first item
				if (i == 0) {
					mActionSelectContainer.addView(getStickerActionsDivider());
				}

				check.setChecked(mChatItem.widget.isSelectedAction(action));

				mCheckBoxList.add(check);

				LinearLayout.LayoutParams lp = new
						LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
						getResources().getDimensionPixelSize(R.dimen.sticker_select_button_height));

				lp.setMargins(
						getResources().getDimensionPixelSize(R.dimen.margin10dp), 0,
						getResources().getDimensionPixelSize(R.dimen.margin10dp), 0
				);

				// Add view to container
				mActionSelectContainer.addView(item, lp);

				// Add Bottom divider
				if (i < actions.size() -1) {
					mActionSelectContainer.addView(getStickerActionsDivider());
				}
			}
		}

		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Button btn = (Button) inflater.inflate(R.layout.layout_btn_select_action, null);
		if (btn != null) {
			btn.setText(R.string.ok);
			btn.setBackgroundResource(mSelectActionLastBackgroundId);

			// Handler
			btn.setOnClickListener(new CheckboxOnClickListener());

			LinearLayout.LayoutParams lp = new
					LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					getResources().getDimensionPixelSize(R.dimen.sticker_select_button_height));

			// Add view to container
			mActionSelectContainer.addView(getStickerActionsDivider());
			mActionSelectContainer.addView(btn, lp);
		}


		showSelectActions(true);

		Log.d(TAG, "showSelectActions: " + actions.size());
		Log.d(TAG, "showSelectActions: " + mActionSelectContainer.getVisibility());
	}

	public void showLinearActions(BasicWidget.StickerAction stickerAction) {
		List<BasicWidget.StickerAction.ActionData> actions;
		if (stickerAction == null) {
			return;
		}

		LinearLayout linearLayout = (LinearLayout)mActionLinearContainer.findViewById(R.id.linear_layout);
		linearLayout.removeAllViews();

		actions = stickerAction.actionData;

		if (actions == null) {
			return;
		}

		TextView minLabel = (TextView)mActionLinearContainer.findViewById(R.id.linear_label_min);
		TextView maxLabel = (TextView)mActionLinearContainer.findViewById(R.id.linear_label_max);

		if ( stickerAction.viewInfo.minLabel != null ){
			minLabel.setText(stickerAction.viewInfo.minLabel);
		}
		if ( stickerAction.viewInfo.maxLabel != null ){
			maxLabel.setText(stickerAction.viewInfo.maxLabel);
		}

		mRadioButtonList.clear();

		for (int i = 0; i < actions.size(); i++) {
			BasicWidget.StickerAction.ActionData actionData = actions.get(i);
			if ( actionData.value != null && actionData.value.answer != null ) {
				int value = Integer.valueOf(actionData.value.answer);

				// Create the view and add UI setting
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				LinearLayout item = (LinearLayout)inflater.inflate(R.layout.layout_linear_item, null);
				RadioButton radio = (RadioButton)item.findViewById(R.id.radiobutton);
				TextView label = (TextView)item.findViewById(R.id.linear_label);

				label.setText(String.valueOf(value));

				radio.setChecked(mChatItem.widget.isSelectedAction(actionData));
				radio.setOnClickListener(new RadioButtonOnClickListener(actionData));

				mRadioButtonList.add(radio);

				LinearLayout.LayoutParams lp = new
						LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				lp.weight = 1;

				// Add view to container
				linearLayout.addView(item, lp);
			}
		}
		showLinearActions(true);

		Log.d(TAG, "showLinearActions: " + actions.size());
		Log.d(TAG, "showLinearActions: " + mActionLinearContainer.getVisibility());
	}

	public void showConfirmActions(BasicWidget.StickerAction stickerAction) {
		if (stickerAction == null || stickerAction.actionData == null || stickerAction.actionData.size() == 0) {
			return;
		}

		List<BasicWidget.StickerAction.ActionData> actions = stickerAction.actionData;

		showConfirmActions(true);
		String positive = null;
		String negative = null;

		for (BasicWidget.StickerAction.ActionData action : actions) {
			if ( action.value != null /*&& action.value.answer != null */){
				if ("true".equals(action.value.answer)) {
					mActionConfirmPositive.setOnClickListener(new ActionOnClickListener(action));
					positive = action.label;

					mActionConfirmPositive.setSelected(mChatItem.widget.isSelectedAction(action));
				} else if (action.value.answer == null || "false".equals(action.value.answer)) {
					mActionConfirmNegative.setOnClickListener(new ActionOnClickListener(action));
					negative = action.label;

					mActionConfirmNegative.setSelected(mChatItem.widget.isSelectedAction(action));
				}
			}
		}

		setConfirmText(positive, negative);

	}

	public TextView getTextView() {
		return mTextView;
	}

	public RoundImageView getImageView() {
		return mImageView;
	}

	public void setSelectActionBackgroundResId(int selectActionResId, int selectActionLastResId) {
		mSelectActionBackgroundId = selectActionResId;
		mSelectActionLastBackgroundId = selectActionLastResId;
	}

	public void showConfirmActions(boolean enable) {
		this.mActionConfirmContainer.setVisibility(enable ? View.VISIBLE : View.GONE);
	}

	public void setConfirmText(String positive, String negative) {
		mActionConfirmPositive.setVisibility(StringUtil.isBlank(positive) ? View.GONE : View.VISIBLE);
		mActionConfirmPositive.setText(positive);
		mActionConfirmNegative.setVisibility(StringUtil.isBlank(negative) ? View.GONE : View.VISIBLE);
		mActionConfirmNegative.setText(negative);
	}

	public void showSelectActions(boolean enable) {
		this.mActionSelectContainer.setVisibility(enable ? View.VISIBLE : View.GONE);
	}

	public void showLinearActions(boolean enable) {
		this.mActionLinearContainer.setVisibility(enable ? View.VISIBLE : View.GONE);
	}

	private View getStickerActionsDivider() {
		View divider = new View(this.getContext());

		LinearLayout.LayoutParams params = new
				LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				getResources().getDimensionPixelSize(R.dimen.widget_line_width));
		divider.setLayoutParams(params);

		divider.setBackgroundColor(getContext().getResources().getColor(R.color.color_chatcenter_widget_border));
		return divider;
	}

	public interface StickerActionListener {
		void onActionClick(BasicWidget.StickerAction.ActionData action, String messageId);
		void onCheckBoxOK(List<String> labels, List<String> answers, String messageId);

		void onSuggestionBubbleClicked(ChatItem chatItem);
	}

	// //////////////////////////////////////////////////////////////////////////
	// インナークラス
	// //////////////////////////////////////////////////////////////////////////
	private class ActionOnClickListener implements OnClickListener {
		protected BasicWidget.StickerAction.ActionData mAction;

		public ActionOnClickListener(BasicWidget.StickerAction.ActionData action) {
			mAction = action;
		}

		@Override
		public void onClick(View view) {
			if (BuildConfig.DEBUG) {
				Log.d("ActionOnClickListener", "Action clicked: " + mAction.label);
			}

			if (mChatItem.widget.isSelectedAction(mAction)) {
				return;
			}

			if (mStickerActionListener != null) {
				mStickerActionListener.onActionClick(mAction, String.valueOf(mChatItem.id));
			}
		}
	}

	private class CheckboxOnClickListener implements OnClickListener {
		public CheckboxOnClickListener() {
		}

		@Override
		public void onClick(View view) {
			List<String> labels = new ArrayList<>();
			List<String> answers = new ArrayList<>();

			for(int i = 0; i < mCheckBoxList.size(); i++) {
				CheckBox check = mCheckBoxList.get(i);
				if ( check.isChecked() ){
					BasicWidget.StickerAction.ActionData actionData = mChatItem.widget.stickerAction.actionData.get(i);
					if ( actionData != null ){
						labels.add(actionData.label);
						answers.add(actionData.value.answer);
					}
				}
			}

			if (mStickerActionListener != null) {
				mStickerActionListener.onCheckBoxOK(labels, answers, String.valueOf(mChatItem.id));
			}
		}
	}


	private class RadioButtonOnClickListener implements OnClickListener {
		protected BasicWidget.StickerAction.ActionData mAction;

		public RadioButtonOnClickListener(BasicWidget.StickerAction.ActionData action) {
			mAction = action;
		}

		@Override
		public void onClick(View view) {
			if (BuildConfig.DEBUG) {
				Log.d("RadioButton", "Action clicked: " + mAction.label);
			}

			for ( RadioButton r : mRadioButtonList ){
				r.setChecked(false);
			}
			RadioButton myRadio = (RadioButton)view;
			myRadio.setChecked(true);

			if (mChatItem.widget.isSelectedAction(mAction)) {
				return;
			}

			if (mStickerActionListener != null) {
				mStickerActionListener.onActionClick(mAction, String.valueOf(mChatItem.id));
			}
		}
	}

	public class LiveThumbnailAdapter extends RecyclerView.Adapter<LiveThumbnailAdapter.ItemViewHolder> {

		public class ItemViewHolder extends RecyclerView.ViewHolder {
			public ImageView mImageView;
			public TextView mTextView;

			public ItemViewHolder(View v){
				super(v);
				mImageView = (ImageView)v.findViewById(R.id.icon_imageview);
				mTextView = (TextView)v.findViewById(R.id.icon_textview);
			}
		}

		public LiveThumbnailAdapter(){
		}

		@Override public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
			View v = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.live_location_thumb, parent, false);
			return new ItemViewHolder(v);
		}
		@Override public void onBindViewHolder(final ItemViewHolder holder, int position) {
			LiveLocationUser user = mChatItem.mLiveLocationUsers.get(position);
			String url = user.mIconUrl;

			int iconColor = ViewUtil.getIconColor(mChatItem.channelUid);
			if( StringUtil.isNotBlank(url) ) {
				setIconImage(holder, url);
			} else {
				setIconText(holder, user.mDisplayName, iconColor);
			}

			if ( !user.isActive() ){
				holder.itemView.setVisibility(View.GONE);
			} else {
				holder.itemView.setVisibility(View.VISIBLE);
			}
		}
		@Override public int getItemCount() {
			return mChatItem.mLiveLocationUsers.size();
		}

		/**
		 * アイコンテキストを設定します。
		 *
		 * @param userName ユーザー名
		 * @param color アイコンのRGBカラー
		 */
		private void setIconText(ItemViewHolder holder, String userName, int color) {
			holder.mTextView.setVisibility(View.VISIBLE);
			holder.mImageView.setVisibility(View.GONE);

			if (StringUtil.isNotBlank(userName)) {
				holder.mTextView.setText(userName.toUpperCase().substring(0, 1));
			}

			GradientDrawable gradientDrawable = (GradientDrawable) holder.mTextView.getBackground();
			gradientDrawable.setColor(color);
		}

		/**
		 * アイコン画像を設定します。
		 *
		 * @param iconUrl アイコン画像URL
		 */
		private void setIconImage(ItemViewHolder holder, String iconUrl) {
			holder.mTextView.setVisibility(View.GONE);
			holder.mImageView.setVisibility(View.VISIBLE);

			ViewUtil.loadImageCircle(holder.mImageView, iconUrl);
		}

	}

}