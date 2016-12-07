package ly.appsocial.chatcenter.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.fragment.AlertDialogFragment;
import ly.appsocial.chatcenter.fragment.ListQuestionFragment;
import ly.appsocial.chatcenter.fragment.WidgetPreviewDialog;
import ly.appsocial.chatcenter.util.DialogUtil;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;

public class QuestionActivity extends BaseActivity implements WidgetPreviewDialog.WidgetPreviewListener, AlertDialogFragment.DialogListener {

	public static final String QUESTION_CONTENT = "question_content";

	final static int ANSWERTYPE_YESNO		= 0;
	final static int ANSWERTYPE_MULTIPLE	= 1;
	final static int ANSWERTYPE_CHECKBOX	= 2;
	final static int ANSWERTYPE_LINEARSCALE	= 3;

	private int mAnswerType = -1;

	private EditText mEditText;

	private YesNoQuestionFragment yesNoContentFragment;
	private MultipleQuestionFragment multipleQuestionFragment;
	private CheckboxQuestionFragment checkboxQuestionFragment;
	private LinearScaleQuestionFragment linearScaleQuestionFragment;

	private InputMethodManager inputMethodManager;
	private View mainLayout;

	private ArrayList<AnswerTypeSpinnerItem> mQuestionTypeSpinnerItems = new ArrayList<AnswerTypeSpinnerItem>() {{
		add(new AnswerTypeSpinnerItem(R.drawable.icon_yesno, R.string.answer_type_yes_no, ANSWERTYPE_YESNO));
		add(new AnswerTypeSpinnerItem(R.drawable.icon_multiplechoice, R.string.answer_type_multi_choice, ANSWERTYPE_MULTIPLE));
		add(new AnswerTypeSpinnerItem(R.drawable.icon_checkbox, R.string.answer_type_checkbox, ANSWERTYPE_CHECKBOX));
		add(new AnswerTypeSpinnerItem(R.drawable.icon_linearscale, R.string.answer_type_leaner_scale, ANSWERTYPE_LINEARSCALE));
	}};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_question);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.sticker_label_confirm);

		Button nextButton = (Button)toolbar.findViewById(R.id.next_button);
		nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String content = makeContentString();
				if ( content != null ){
					showDialogWidgetPreview(content);
				} else {
					DialogUtil.showAlertDialog(getSupportFragmentManager(), DialogUtil.Tag.ERROR, null, getString(R.string.widget_create_error));
				}
			}
		});

		mEditText = (EditText)findViewById(R.id.edit_question);

		Spinner spinner = (Spinner)findViewById(R.id.spinner);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				setAnswerType(mQuestionTypeSpinnerItems.get(position).mAnswerType);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		spinner.setAdapter(new AnswerTypeAdapter(this, mQuestionTypeSpinnerItems));
//		setAnswerType(ANSWERTYPE_YESNO);

		inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		mainLayout = findViewById(R.id.mainLayout);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		mainLayout.requestFocus();
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
			case android.R.id.home: {
				Intent intent = new Intent();
				setResult(RESULT_CANCELED, intent);
				finish();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void setAnswerType(int answerType){
		if ( answerType != mAnswerType ){
			mAnswerType = answerType;

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			switch (answerType){
				case ANSWERTYPE_YESNO:{
					if ( yesNoContentFragment == null ){
						yesNoContentFragment = new YesNoQuestionFragment();
					}
					ft.replace(R.id.container, yesNoContentFragment);
					break;
				}
				case ANSWERTYPE_MULTIPLE:{
					if ( multipleQuestionFragment == null ){
						multipleQuestionFragment = new MultipleQuestionFragment();
					}
					ft.replace(R.id.container, multipleQuestionFragment);
					break;
				}
				case ANSWERTYPE_CHECKBOX: {
					if ( checkboxQuestionFragment == null ){
						checkboxQuestionFragment = new CheckboxQuestionFragment();
					}
					ft.replace(R.id.container, checkboxQuestionFragment);
					break;
				}
				case ANSWERTYPE_LINEARSCALE: {
					if ( linearScaleQuestionFragment == null ){
						linearScaleQuestionFragment = new LinearScaleQuestionFragment();
					}
					ft.replace(R.id.container, linearScaleQuestionFragment);
					break;
				}
			}
			ft.commit();
		}
	}

	private String makeContentString(){
		BasicWidget widget = new BasicWidget();
		widget.message = new BasicWidget.Message();
		widget.message.text = mEditText.getText().toString();

		widget.stickerAction = new BasicWidget.StickerAction();
		widget.stickerAction.actionData = new ArrayList<>();

		switch (mAnswerType){
			case ANSWERTYPE_YESNO:{
				widget.stickerAction.actionType = ChatCenterConstants.ActionType.SELECT;
				widget.stickerAction.viewInfo = new BasicWidget.StickerAction.ViewInfo();
				widget.stickerAction.viewInfo.type = ChatCenterConstants.ViewType.YESNO;

				int n = yesNoContentFragment.mRadioGroup.getCheckedRadioButtonId();
				boolean isYesNo = (n == R.id.radioButton2 );

				for ( int i = 0; i < 2; i++ ){
					BasicWidget.StickerAction.ActionData actionData = new BasicWidget.StickerAction.ActionData();
					actionData.value = new BasicWidget.StickerAction.ActionData.Value();

					if ( i == 0 ){
						actionData.value.answer = "true";
						if ( isYesNo ){
							actionData.label = getString(R.string.yes);
						} else {
							actionData.label = "\uD83D\uDC4D";
						}
					} else {
						actionData.value.answer = "false";
						if ( isYesNo ){
							actionData.label = getString(R.string.no);
						} else {
							actionData.label = "\uD83D\uDC4E";
						}
					}
					widget.stickerAction.actionData.add(actionData);
				}
				break;
			}
			case ANSWERTYPE_MULTIPLE:{
				if (multipleQuestionFragment.mAdapter.getOptionCount() == 0) {
					// There is no option entered
					return null;
				}

				widget.stickerAction.actionType = ChatCenterConstants.ActionType.SELECT;
				widget.stickerAction.viewInfo = new BasicWidget.StickerAction.ViewInfo();
				widget.stickerAction.viewInfo.type = ChatCenterConstants.ViewType.DEFAULT;

				int nCount = multipleQuestionFragment.mAdapter.getCount();
				for (int i = 0; i < nCount; i++ ){
					String str = multipleQuestionFragment.mAdapter.getItem(i).getContent();
					if (StringUtil.isNotBlank(str)) {
						BasicWidget.StickerAction.ActionData actionData = new BasicWidget.StickerAction.ActionData();
						actionData.label = str;
						actionData.value = new BasicWidget.StickerAction.ActionData.Value();
						actionData.value.answer = String.valueOf(i + 1);
						widget.stickerAction.actionData.add(actionData);
					}
				}
				break;
			}
			case ANSWERTYPE_CHECKBOX: {
				if (checkboxQuestionFragment.mAdapter.getOptionCount() == 0) {
					// There is no option entered
					return null;
				}

				widget.stickerAction.actionType = ChatCenterConstants.ActionType.SELECT;
				widget.stickerAction.viewInfo = new BasicWidget.StickerAction.ViewInfo();
				widget.stickerAction.viewInfo.type = ChatCenterConstants.ViewType.CHECKBOX;

				int nCount = checkboxQuestionFragment.mAdapter.getCount();
				for (int i = 0; i < nCount; i++ ){
					String str = checkboxQuestionFragment.mAdapter.getItem(i).getContent();
					if (StringUtil.isNotBlank(str)) {

						BasicWidget.StickerAction.ActionData actionData = new BasicWidget.StickerAction.ActionData();
						actionData.label = str;
						actionData.value = new BasicWidget.StickerAction.ActionData.Value();
						actionData.value.answer = String.valueOf(i + 1);
						widget.stickerAction.actionData.add(actionData);
					}
				}
				break;
			}
			case ANSWERTYPE_LINEARSCALE: {
				widget.stickerAction.actionType = ChatCenterConstants.ActionType.SELECT;
				widget.stickerAction.viewInfo = new BasicWidget.StickerAction.ViewInfo();
				widget.stickerAction.viewInfo.type = ChatCenterConstants.ViewType.LINEAR;

				widget.stickerAction.viewInfo.minLabel = linearScaleQuestionFragment.mScaleEdit1.getText().toString();
				widget.stickerAction.viewInfo.maxLabel = linearScaleQuestionFragment.mScaleEdit2.getText().toString();

				int minValue = linearScaleQuestionFragment.mMinScale;
				int maxValue = linearScaleQuestionFragment.mMaxScale;

				for ( int i = minValue; i <= maxValue; i++ ){
					BasicWidget.StickerAction.ActionData actionData = new BasicWidget.StickerAction.ActionData();
					actionData.label = String.valueOf(i+1);
					actionData.value = new BasicWidget.StickerAction.ActionData.Value();
					actionData.value.answer = String.valueOf(i+1);
					widget.stickerAction.actionData.add(actionData);
				}
				break;
			}
		}

		return new Gson().toJson(widget).toString();
	}

	@Override
	public void onSendButtonClicked() {
		Intent intent = new Intent();
		String content = makeContentString();
		if (StringUtil.isNotBlank(content)){
			intent.putExtra(QUESTION_CONTENT, content);
			setResult(RESULT_OK, intent);
			finish();
		} else {
			// TODO Error message
		}
	}


	static public class YesNoQuestionFragment extends Fragment {
		public RadioGroup mRadioGroup;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View layout = inflater.inflate(R.layout.question_yesno, container, false);
			mRadioGroup = (RadioGroup)layout.findViewById(R.id.radio_group);
			mRadioGroup.check(R.id.radioButton1);

			TextView thumbUp = (TextView)layout.findViewById(R.id.answer_thumbup);
			thumbUp.setText("\uD83D\uDC4D");
			TextView thumbDown = (TextView)layout.findViewById(R.id.answer_thumbdown);
			thumbDown.setText("\uD83D\uDC4E");
			return layout;
		}
	}

	static public class MultipleQuestionFragment extends ListQuestionFragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View layout = inflater.inflate(R.layout.question_multiple, container, false);
			setupList(layout, R.layout.question_cell_multiple);
			return layout;
		}
	}

	static public class CheckboxQuestionFragment extends ListQuestionFragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View layout = inflater.inflate(R.layout.question_checkbox, container, false);
			setupList(layout, R.layout.question_cell_checkbox);
			return layout;
		}
	}

	static public class LinearScaleQuestionFragment extends Fragment {
		private Spinner mSpinner1;
		private Spinner mSpinner2;

		private TextView mScaleLabel1;
		private TextView mScaleLabel2;
		public EditText mScaleEdit1;
		public EditText mScaleEdit2;

		public int mMinScale = 0;
		public int mMaxScale = 4;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View layout = inflater.inflate(R.layout.question_linearscale, container, false);

			mScaleLabel1 = (TextView)layout.findViewById(R.id.label1);
			mScaleLabel2 = (TextView)layout.findViewById(R.id.label2);
			mScaleEdit1 = (EditText)layout.findViewById(R.id.edit_label1);
			mScaleEdit2 = (EditText)layout.findViewById(R.id.edit_label2);

			mSpinner1 = (Spinner)layout.findViewById(R.id.scale_from);
			mSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if ( position < mMaxScale ){
						mMinScale = position;
						mScaleLabel1.setText(String.valueOf(position+1));
					} else {
						mSpinner1.setSelection(mMinScale);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});
			mSpinner1.setSelection(mMinScale);
			mScaleLabel1.setText(String.valueOf(mMinScale+1));

			mSpinner2 = (Spinner)layout.findViewById(R.id.scale_to);
			mSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if ( position > mMinScale ){
						mMaxScale = position;
						mScaleLabel2.setText(String.valueOf(position+1));
					} else {
						mSpinner2.setSelection(mMaxScale);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});
			mSpinner2.setSelection(mMaxScale);
			mScaleLabel2.setText(String.valueOf(mMaxScale+1));

			return layout;
		}

	}

	public static class AnswerTypeSpinnerItem {
		int mIconDrawable;
		int mTitleId;
		int mAnswerType;

		public AnswerTypeSpinnerItem(int iconDrawable, int titleId, int answerType) {
			mIconDrawable = iconDrawable;
			mTitleId = titleId;
			mAnswerType = answerType;
		}
	}


	public class AnswerTypeAdapter extends BaseAdapter {

		private ArrayList<AnswerTypeSpinnerItem> items;
		private Context mContext;

		public AnswerTypeAdapter(Context context, ArrayList<AnswerTypeSpinnerItem> objects) {

			items = objects;
			mContext = context;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public AnswerTypeSpinnerItem getItem(int position) {
			return items.get(position);
		}

		/**
		 * Get the row id associated with the specified position in the list.
		 *
		 * @param position The position of the item within the adapter's data set whose row id we want.
		 * @return The id of the item at the specified position.
		 */
		@Override
		public long getItemId(int position) {
			return 0;
		}

		@NonNull
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_answer_type_spiner, parent, false);

			ImageView imageView = (ImageView) convertView.findViewById(R.id.question_type_icon);
			TextView textView = (TextView) convertView.findViewById(R.id.question_type_title);

			AnswerTypeSpinnerItem item = getItem(position);

			imageView.setImageResource(item.mIconDrawable);
			textView.setText(item.mTitleId);

			return convertView;
		}
	}


	@Override
	public void onDialogCancel(String tag){
	}

	/**
	 * ダイアログの肯定ボタンを押下した際のコールバック。
	 *
	 * @param tag このフラグメントのタグ
	 */
	@Override
	public void onPositiveButtonClick(String tag){
	}


}
