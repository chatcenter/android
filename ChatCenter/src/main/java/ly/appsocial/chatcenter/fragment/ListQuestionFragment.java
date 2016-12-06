package ly.appsocial.chatcenter.fragment;


import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.adapter.ListQuestionAdapter;

/**
 * Created by karasawa on 2016/11/04.
 */

public abstract class ListQuestionFragment extends Fragment {
	public ListQuestionAdapter mAdapter;
	private int mLastItemId = 1;
	private ArrayList<ListQuestionAdapter.QuestionTitle> mTitles;

	protected void setupList(View layout, int res_id){
		ListView lv = (ListView)layout.findViewById(R.id.listview);

		Activity activity = getActivity();
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
	}

	private void addNewOption(){
		mTitles.add(new ListQuestionAdapter.QuestionTitle(String.format(getContext().getString(R.string.option), ++mLastItemId)));
		mAdapter.notifyDataSetChanged();
	}
}
