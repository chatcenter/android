package ly.appsocial.chatcenter.activity.adapter;

import android.app.Activity;
import android.database.DataSetObserver;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.util.ArrayList;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.util.StringUtil;

/**
 * Created by karasawa on 2016/11/04.
 */

public class ListQuestionAdapter extends ArrayAdapter<ListQuestionAdapter.QuestionTitle> {

    private Activity mOwner;
    private int cellResource;
    private ArrayList<QuestionTitle> mTitles;

    public ListQuestionAdapter(Activity activity, int res_id, ArrayList<QuestionTitle> objects) {
        super(activity, res_id, objects);
        this.mOwner = activity;
        this.cellResource = res_id;
        this.mTitles = objects;
    }


    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        LayoutInflater inflater = this.mOwner.getLayoutInflater();
        view = inflater.inflate(cellResource, parent, false);
        EditText editText = (EditText) view.findViewById(R.id.edit_text);

        View removeButton = view.findViewById(R.id.remove_btn);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(position);
            }
        });

        if (position == 0 && getCount() == 1) {
            removeButton.setVisibility(View.INVISIBLE);
        } else {
            removeButton.setVisibility(View.VISIBLE);
        }

        final QuestionTitle title = getItem(position);
        if (StringUtil.isBlank(title.getContent())) {
            editText.setHint(title.getHintString());
        } else {
            editText.setText(title.getContent());
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                title.setContent(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }

    @Override
    public int getCount() {
        return mTitles.size();
    }

    @Override
    public QuestionTitle getItem(int position) {
        return mTitles.get(position);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }

    private void removeItem(int position) {
        mTitles.remove(position);
        for (int i = 0; i < mTitles.size(); i++) {
            mTitles.get(i).setHintString(String.format(getContext().getString(R.string.option), i + 1));
        }
        notifyDataSetChanged();
    }

    public int getOptionCount() {
        int option = 0;
        for (QuestionTitle title : mTitles) {
            if (StringUtil.isNotBlank(title.getContent())) {
                option += 1;
            }
        }

        return option;
    }

    public static class QuestionTitle {
        private String mContent;
        private String mHintString;

        public QuestionTitle(String hintString) {
            mHintString = hintString;
        }

        public String getContent() {
            return mContent;
        }

        public void setContent(String content) {
            mContent = content;
        }

        public String getHintString() {
            return mHintString;
        }

        public void setHintString(String hintString) {
            mHintString = hintString;
        }
    }

}
