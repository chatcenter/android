package ly.appsocial.chatcenter.widgets.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import ly.appsocial.chatcenter.dto.ws.request.PostReplyInputWidgetDto;
import ly.appsocial.chatcenter.ui.RoundImageView;
import ly.appsocial.chatcenter.util.CCAuthUtil;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.util.ViewUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;
import ly.appsocial.chatcenter.widgets.LiveLocationUser;


public class WidgetView extends FrameLayout {
	private static final String TAG = "StickerView";

	private TextView mTvMessage;
	private RelativeLayout mRlWidgetView;

	private TextView mWidgetTitle;
	private RoundImageView mWidgetImageView;
	private LinearLayout mActionSelectContainer;
	private View mActionConfirmContainer;
	private RelativeLayout mActionLinearContainer;
	private Button mActionConfirmPositive;
	private Button mActionConfirmNegative;
	private LinearLayout mActionInputContainer;
	private EditText mEdtInputWidget;

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

	private AlertDialog mSelectBoxDialog;
	private AlertDialog mInputMessageDialog;

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
		mUserToken = CCAuthUtil.getUserToken(getContext());

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

	public void setupCustomerView(ChatItem item, StickerActionListener listener, boolean canMail){
		setChatItem(item);

		if (canMail) {
			mTvMessage.setAutoLinkMask(Linkify.WEB_URLS | Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES);
			mWidgetTitle.setAutoLinkMask(Linkify.WEB_URLS | Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES);
		} else {
			mTvMessage.setAutoLinkMask(Linkify.WEB_URLS | Linkify.PHONE_NUMBERS);
			mWidgetTitle.setAutoLinkMask(Linkify.WEB_URLS | Linkify.PHONE_NUMBERS);
		}

		boolean isMessage = StringUtil.isBlank(item.type) || ResponseType.MESSAGE.equals(item.type);
		selectWidgetView(isMessage);

		if (isMessage) {
//			reset();
			if ( item.widget != null && StringUtil.isNotBlank(item.widget.text) ) {
				setMessageFromCustomer(item.widget.text);
			}
		} else {
			setStickerActionListener(listener);
			return;
		}


	}

	public void setupClientView(String userToken, ChatItem item, StickerActionListener listener, boolean canMail){
		mUserToken = userToken;
		setChatItem(item);

		if (canMail) {
			mTvMessage.setAutoLinkMask(Linkify.WEB_URLS | Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES);
			mWidgetTitle.setAutoLinkMask(Linkify.WEB_URLS | Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES);
		} else {
			mTvMessage.setAutoLinkMask(Linkify.WEB_URLS | Linkify.PHONE_NUMBERS);
			mWidgetTitle.setAutoLinkMask(Linkify.WEB_URLS | Linkify.PHONE_NUMBERS);
		}

		boolean isMessage = StringUtil.isBlank(item.type) || ResponseType.MESSAGE.equals(item.type);
		selectWidgetView(isMessage);

		if (isMessage) {
//			reset();
			if ( item.widget != null && StringUtil.isNotBlank(item.widget.text) ){
				setMessageFromClient(item.widget.text);
			}
		} else {
			setStickerActionListener(listener);
			return;
		}
	}

	public void loadImageToImageView(String url){
		Picasso.with(getContext()).load(rebuildUrl(url)).into(getWidgetImageView());
		getWidgetImageView().setVisibility(View.VISIBLE);
	}

	private String rebuildUrl(String url) {
		String newUrl;
		if (url.startsWith(getContext().getString(R.string.api_chatcenter))) {
			Uri uri = Uri.parse(url);
			Uri.Builder newUri = uri.buildUpon().clearQuery();
			boolean addMore = true;
			for (String param : uri.getQueryParameterNames()) {
				String value;
				if (param.equals("authentication")) {
					value = mUserToken;
					addMore = false;
				} else {
					value = uri.getQueryParameter(param);
				}
				newUri.appendQueryParameter(param, value);
			}

			// If there is no authentication param in url
			if (addMore) {
				newUri.appendQueryParameter("authentication", mUserToken);
			}
			newUrl = newUri.build().toString();
		} else {
			newUrl = url;
		}
		return newUrl;
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

		mTvMessage = (TextView) findViewById(R.id.tv_message);

		mRlWidgetView = (RelativeLayout) findViewById(R.id.widget_view);
		mWidgetTitle = (TextView) v.findViewById(R.id.sticker_textview);
		mWidgetImageView = (RoundImageView) v.findViewById(R.id.sticker_image);
		mActionSelectContainer = (LinearLayout)v.findViewById(R.id.sticker_action_select_container);
		mActionLinearContainer = (RelativeLayout)v.findViewById(R.id.sticker_action_linear_container);
		mActionConfirmContainer = v.findViewById(R.id.sticker_action_confirm_contaier);
		mActionConfirmPositive = (Button) v.findViewById(R.id.sticker_action_confirm_positive);
		mActionConfirmNegative = (Button) v.findViewById(R.id.sticker_action_confirm_negative);
		mActionInputContainer = (LinearLayout) v.findViewById(R.id.sticker_action_input_container);
		mEdtInputWidget = (EditText) v.findViewById(R.id.edt_reply_content);

		mLiveThumbnailView = (RecyclerView) v.findViewById(R.id.live_thumbnail);
		mLayoutManager = new LinearLayoutManager(getContext(), LinearLayout.HORIZONTAL, false);
		mLiveThumbnailView.setLayoutManager(mLayoutManager);
		mAdapter = new LiveThumbnailAdapter();
		mLiveThumbnailView.setAdapter(mAdapter);

		mLiveLabel = (TextView) v.findViewById(R.id.bt_live);

		mWidgetImageView.setOnClickListener(new OnClickListener() {
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
			mWidgetImageView.setVisibility(GONE);
			return;
		}

		mWidgetImageView.setVisibility(View.VISIBLE);
		mWidgetImageView.setImageDrawable(drawable);
	}

	public void setImageUrl(String url) {
		if (StringUtil.isBlank(url)) {
			Picasso.with(getContext()).cancelRequest(mWidgetImageView);
			mWidgetImageView.setVisibility(GONE);
			return;
		}

		mWidgetImageView.setVisibility(View.VISIBLE);
		if (url.startsWith("http")) {
			loadImageToImageView(url);
		} else {
			Picasso.with(getContext()).load(new File(url)).into(mWidgetImageView);
		}
	}

	public void setText(String text) {
		if (StringUtil.isBlank(text)) {
			mWidgetTitle.setVisibility(GONE);
			mWidgetImageView.addRounded(RoundImageView.RoundedOptions.TOP);
			return;
		}

		mWidgetImageView.removeRounded(RoundImageView.RoundedOptions.TOP);
		mWidgetTitle.setVisibility(View.VISIBLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			mWidgetTitle.setText(Html.fromHtml(text, 0));
		} else {
			mWidgetTitle.setText(Html.fromHtml(text));
		}
	}

	public void setWidgetIcon(String actionType) {
		int drawable = mChatItem.widget.getWidgetIcon(actionType);

		if (drawable > 0) {
			ImageView widgetIcon = (ImageView) findViewById(R.id.widget_icon);
			widgetIcon.setImageResource(drawable);
			widgetIcon.setVisibility(VISIBLE);
		}
	}

	public void setMessageFromClient(String message) {

		mTvMessage.setText(message);
	}

	public void setMessageFromCustomer(String message) {
		mTvMessage.setText(message);
		mTvMessage.setBackgroundResource(R.drawable.bg_customer_sticker);
		mTvMessage.setTextColor(getResources().getColor(R.color.color_chatcenter_sent_msg_text));
		mTvMessage.setLinkTextColor(getResources().getColor(R.color.color_chatcenter_sent_msg_text));
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

//	public void reset() {
//		mWidgetImageView.setVisibility(View.GONE);
//		mActionSelectContainer.setVisibility(View.GONE);
//		mActionLinearContainer.setVisibility(View.GONE);
//		mActionConfirmContainer.setVisibility(View.GONE);
//		Picasso.with(getContext()).cancelRequest(getWidgetImageView());
//		this.setBackgroundResource(0);
//	}

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

	/**
	 * Show a widget with EditText and some button.
	 * @param stickerAction
	 */
	public void showInputActions(BasicWidget.StickerAction stickerAction) {

		// if StickerAction is null then finish
		if (stickerAction == null) {
			return;
		}

		// If there is no action in StickerAction then finish
		final List<BasicWidget.StickerAction.ActionData> actions = stickerAction.actionData;
		if (actions == null) {
			return;
		}

//		EditText inputView = null;
//		for (int i = 0; i < mActionInputContainer.getChildCount(); i++) {
//			if (mActionInputContainer.getChildAt(i) instanceof EditText) {
//				inputView = (EditText) mActionInputContainer.getChildAt(i);
//				break;
//			}
//		}
//
//		if (inputView != null) {
//			inputView.setOnFocusChangeListener(new OnFocusChangeListener() {
//				@Override
//				public void onFocusChange(View v, boolean hasFocus) {
//					if (hasFocus) {
//						if (mStickerActionListener != null) {
//							mStickerActionListener.onWidgetInputFocused(mChatItem);
//						}
//					}
//				}
//			});
//			inputView.addTextChangedListener(new TextWatcher() {
//				@Override
//				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//				}
//
//				@Override
//				public void onTextChanged(CharSequence s, int start, int before, int count) {
//					mChatItem.tempMessage = String.valueOf(s);
//				}
//
//				@Override
//				public void afterTextChanged(Editable s) {
//
//				}
//			});

//			if (mChatItem.widget != null
//					&& mChatItem.widget.stickerAction != null
//					&& mChatItem.widget.stickerAction.responseActions != null
//					&& mChatItem.widget.stickerAction.responseActions.size() > 0) {
//				BasicWidget.StickerAction.ActionData data = mChatItem.widget.stickerAction.responseActions.get(0).getActions().get(0);
//				inputView.setText(data.input);
//			} else {
//				inputView.setText(mChatItem.tempMessage);
//			}
//		}

		if (mChatItem.widget != null
				&& mChatItem.widget.stickerAction != null
				&& mChatItem.widget.stickerAction.responseActions != null
				&& mChatItem.widget.stickerAction.responseActions.size() > 0) {
			mEdtInputWidget.setBackgroundResource(R.drawable.bg_action_select_selected);
		} else {
			mEdtInputWidget.setBackgroundResource(R.drawable.bg_action_select_normal);
		}

		if (StringUtil.isNotBlank(mChatItem.tempMessage)) {
			mEdtInputWidget.setText(mChatItem.tempMessage);
		} else if (mChatItem.widget != null
				&& mChatItem.widget.stickerAction != null
				&& mChatItem.widget.stickerAction.responseActions != null
				&& mChatItem.widget.stickerAction.responseActions.size() > 0) {
			BasicWidget.StickerAction.ActionData data = mChatItem.widget.stickerAction.responseActions.get(0).getActions().get(0);
			mEdtInputWidget.setText(data.input);

		}

		final BasicWidget.StickerAction.ActionData action = actions.get(0);

		mEdtInputWidget.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAlertToInputReplyMessage(mChatItem.widget.message.text, mEdtInputWidget.getText().toString(), action);
			}
		});

        // for (int i = 0; i < actions.size(); i++) {

        // Create the view and add UI setting
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Button btn = (Button) inflater.inflate(R.layout.layout_btn_select_action, null);
        if (btn != null) {
            btn.setText(action.label);

            btn.setBackgroundResource(mSelectActionLastBackgroundId);

            // Handler
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (mStickerActionListener != null && StringUtil.isNotBlank(mChatItem.tempMessage)) {
//                        PostReplyInputWidgetDto answer = new PostReplyInputWidgetDto();
//                        answer.mActionData = action;
//                        answer.answerLabel = mChatItem.tempMessage;
//                        answer.replyTo = String.valueOf(mChatItem.id);
//                        answer.type = "response";
//                        mStickerActionListener.onWidgetInputFinish(answer);
//                    }
                }
            });

            // Add Top divider for first item
            mActionInputContainer.addView(getStickerActionsDivider());


            // Add view to container
            mActionInputContainer.addView(btn);
        }
        // }

		showInputActions(true);
	}

//	public void setInputWidgetFocus() {
//		EditText inputView = null;
//		for (int i = 0; i < mActionInputContainer.getChildCount(); i++) {
//			if (mActionInputContainer.getChildAt(i) instanceof EditText) {
//				inputView = (EditText) mActionInputContainer.getChildAt(i);
//				break;
//			}
//		}
//
//		if (inputView != null) {
//			inputView.requestFocus();
//			inputView.setSelection(inputView.getText().toString().length());
//		}
//	}

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

				// Handler
				btn.setOnClickListener(new ActionOnClickListener(action));

				// Add Top divider for first item
				if (i == 0) {
					mActionSelectContainer.addView(getStickerActionsDivider());
				}

				btn.setSelected(mChatItem.widget.isSelectedAction(action));

				// Add view to container
				mActionSelectContainer.addView(btn);

				// Add Bottom divider
				if (i < actions.size() -1) {
					mActionSelectContainer.addView(getStickerActionsDivider());
				}
			}
		}
		showSelectActions(true);

	}

	public void showSelectBox(final BasicWidget.StickerAction stickerAction) {
		if (stickerAction == null || stickerAction.actionData == null) {
			return;
		}

		mActionSelectContainer.removeAllViews();

		String lastAnswer = "";

		boolean isReplied = stickerAction.responseActions != null && stickerAction.responseActions.size() > 0;

		// If user replied this widget, show the last answer
		if (isReplied) {
			List<BasicWidget.StickerAction.ActionData> answers = stickerAction.responseActions.get(0).getActions();
			if (answers != null && answers.size() > 0) {
				lastAnswer = answers.get(0).label;
			}
		}

		final String answer = lastAnswer;

		// Create the view and add UI setting
		Button btn = (Button) LayoutInflater.from(getContext()).inflate(R.layout.layout_btn_select_box, null);
		if (btn != null) {
			if (StringUtil.isNotBlank(answer)) {
				btn.setText(answer);
			}
			int padding = getContext().getResources().getDimensionPixelOffset(R.dimen.margin10dp);
			btn.setPadding(padding, 0, padding, 0);

			// Handler
			btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showSelectDialog(stickerAction.actionData, answer, mChatItem.id);
				}
			});

			// Add Top divider for first item
			mActionSelectContainer.addView(getStickerActionsDivider());

			btn.setSelected(isReplied);

			// Add view to container
			mActionSelectContainer.addView(btn);
		}

		showSelectActions(true);
	}


	private int checkedItem = -1; // which item user selected from list

	/**
	 * Show a dialog to user select one
	 * @param actionDatas
	 * @param selected
	 * @param id
	 */
	private void showSelectDialog(final List<BasicWidget.StickerAction.ActionData> actionDatas, String selected, final Integer id) {
		// Create dialog
		if (mSelectBoxDialog == null) {
			// setup the alert builder
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle(R.string.selectbox_please_select);

			// add a radio button list

			String[] items = new String[actionDatas.size()];
			for (int i = 0; i < items.length; i++) {
				items[i] = actionDatas.get(i).label;
				if (items[i].equals(selected)) {
					checkedItem = i;
				}
			}

			builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					checkedItem = which;
				}
			});

			// add OK and Cancel buttons
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (checkedItem >= 0) {
						dialog.dismiss();
						BasicWidget.StickerAction.ActionData answer = actionDatas.get(checkedItem);
						setSelectedActionForPulldown(answer);
						if (mStickerActionListener != null) {
							mStickerActionListener.onActionClick(answer, String.valueOf(id));
						}
					}
				}
			});
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

			// create and show the alert dialog
			mSelectBoxDialog = builder.create();
		}

		// Show dialog
		if (!mSelectBoxDialog.isShowing()) {
			mSelectBoxDialog.show();
		}
	}

	private void setSelectedActionForPulldown(BasicWidget.StickerAction.ActionData action) {
		if (action == null) {
			return;
		}

		Button button = null;
		for (int i = 0; i < mActionSelectContainer.getChildCount(); i++) {
			if (mActionSelectContainer.getChildAt(i) instanceof Button) {
				button = (Button) mActionSelectContainer.getChildAt(i);
				break;
			}
		}

		if (button != null) {
			button.setText(action.label);
			button.setSelected(true);
		}
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

				// Add view to container
				mActionSelectContainer.addView(item);

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

		mRadioButtonList.clear();

		for (int i = 0; i < actions.size(); i++) {
			BasicWidget.StickerAction.ActionData actionData = actions.get(i);
			if ( actionData.value != null && actionData.value.answer != null ) {
				int value = Integer.valueOf(actionData.value.answer);

				// Create the view and add UI setting
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				LinearLayout item = (LinearLayout)inflater.inflate(R.layout.layout_linear_item, null);
				RadioButton radio = (RadioButton)item.findViewById(R.id.radiobutton);
				TextView tvValue = (TextView)item.findViewById(R.id.linear_value);
				TextView tvLabel = (TextView)item.findViewById(R.id.linear_label);

				tvValue.setText(String.valueOf(value));

				if (i == 0) {
					tvLabel.setText(stickerAction.viewInfo.minLabel);
				} else if (i == actions.size() - 1) {
					tvLabel.setText(stickerAction.viewInfo.maxLabel);
				}

				radio.setChecked(mChatItem.widget.isSelectedAction(actionData));
				radio.setOnClickListener(new RadioButtonOnClickListener(actionData));

				mRadioButtonList.add(radio);

				// Add view to container
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
				layoutParams.weight = 1;

				linearLayout.addView(item, layoutParams);
			}
		}
		showLinearActions(true);

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

	public TextView getWidgetTitle() {
		return mWidgetTitle;
	}

	public RoundImageView getWidgetImageView() {
		return mWidgetImageView;
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

	public void showInputActions(boolean enable) {
		this.mActionInputContainer.setVisibility(enable ? VISIBLE : GONE);
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
		void onWidgetInputFinish(PostReplyInputWidgetDto answer);
		void onInputAlertDismiss(Integer messageId);
	}

	private void selectWidgetView(boolean isMessage) {
		mTvMessage.setVisibility(isMessage ? VISIBLE : GONE);
		mRlWidgetView.setVisibility(isMessage ? GONE : VISIBLE);
	}

	private void showAlertToInputReplyMessage(String title, final String content, final BasicWidget.StickerAction.ActionData action) {
		if (mInputMessageDialog != null && mInputMessageDialog.isShowing()) {
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(title);

		final View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_input_widget, null, false);
		final EditText edtInput = (EditText) view.findViewById(R.id.edit_message_reply);
		edtInput.setText(content);

		builder.setView(view);

		edtInput.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					edtInput.setSelection(edtInput.getText().toString().length());
				}
			}
		});

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mChatItem.tempMessage = edtInput.getText().toString();
				mEdtInputWidget.setText(mChatItem.tempMessage);

				if (mStickerActionListener != null && StringUtil.isNotBlank(mChatItem.tempMessage)) {
					PostReplyInputWidgetDto answer = new PostReplyInputWidgetDto();
					answer.mActionData = action;
					answer.answerLabel = mChatItem.tempMessage;
					answer.replyTo = String.valueOf(mChatItem.id);
					answer.type = "response";
					mStickerActionListener.onWidgetInputFinish(answer);
				}

				dialog.dismiss();
				if (mStickerActionListener != null) {
					mStickerActionListener.onInputAlertDismiss(mChatItem.id);
				}
			}
		});

		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				edtInput.setText(content);
				dialog.dismiss();
				if (mStickerActionListener != null) {
					mStickerActionListener.onInputAlertDismiss(mChatItem.id);
				}
			}
		});

		mInputMessageDialog = builder.create();

		mInputMessageDialog.show();

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