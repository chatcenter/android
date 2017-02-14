package ly.appsocial.chatcenter.fragment;


import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ScrollView;

import java.util.ArrayList;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.adapter.ListQuestionAdapter;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.ui.ExpandedListView;
import ly.appsocial.chatcenter.util.DialogUtil;

/**
 * ListQuestionFragment
 * Created by karasawa on 2016/11/04.
 */

public abstract class ListQuestionFragment extends Fragment {
	public ListQuestionAdapter mAdapter;
	private int mLastItemId = 1;
	private ArrayList<ListQuestionAdapter.QuestionTitle> mTitles;
	private ScrollView mScrollView;

	protected void setupList(View layout, int res_id, ScrollView scrollView){
		ExpandedListView lv = (ExpandedListView) layout.findViewById(R.id.listview);

		AppCompatActivity activity = (AppCompatActivity) getActivity();
		LayoutInflater inflater = activity.getLayoutInflater();
		View footer = inflater.inflate(R.layout.question_cell_footer, null);
		lv.addFooterView(footer);

		footer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addNewOption();
			}
		});

		mTitles = new ArrayList<>();
		mTitles.add(new ListQuestionAdapter.QuestionTitle(String.format(getContext().getString(R.string.option), mLastItemId)));

		mAdapter = new ListQuestionAdapter(activity, res_id, mTitles);

		lv.setAdapter(mAdapter);
		mScrollView = scrollView;
	}

	private void addNewOption(){
		if (mLastItemId < ChatCenterConstants.QuestionWidget.MAX_CHOICE) {
			mTitles.add(new ListQuestionAdapter.QuestionTitle(String.format(getContext().getString(R.string.option), ++mLastItemId)));
			mAdapter.notifyDataSetChanged();
			mScrollView.fullScroll(View.FOCUS_DOWN);
		} else {
			String message = String.format(getContext().getString(R.string.alert_question_widget_choice_too_many),
					ChatCenterConstants.QuestionWidget.MAX_CHOICE);
			DialogUtil.showAlertDialog(getFragmentManager(), DialogUtil.Tag.ALERT, null, message);
		}
	}
}
