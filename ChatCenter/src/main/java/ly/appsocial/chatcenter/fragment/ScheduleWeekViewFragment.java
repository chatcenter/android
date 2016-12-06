/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import ly.appsocial.chatcenter.activity.ScheduleActivity;
import ly.appsocial.chatcenter.ui.ScheduleDateView;
import ly.appsocial.chatcenter.util.DateUtils;

/**
 * Created by karasawa on 2016/11/29.
 */

public class ScheduleWeekViewFragment extends Fragment {
	private List<ScheduleDateView> mViews;
	public ScheduleActivity.ScheduleWeek mWeek;
	public ScheduleActivity mActivity;

	public ScheduleWeekViewFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		LinearLayout rootView = new LinearLayout(getContext());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				(int) mActivity.convertDpToPixel(70, getContext()));
		rootView.setLayoutParams(params);
		rootView.setOrientation(LinearLayout.HORIZONTAL);

		mViews = new ArrayList<>();

		for(int i = 0; i < mWeek.mDays.size() ; i++) {
			ScheduleActivity.ScheduleDate date = mWeek.mDays.get(i);
			final ScheduleDateView view = new ScheduleDateView(mActivity, date);

			if (DateUtils.isToday(date.mDate) && mActivity.mCurrentWeekIndex < 0) {
				view.setSelected(true);
				mActivity.setSelectedDate(date);
			}

			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (ScheduleDateView view : mViews) {
						view.setSelected(v.equals(view));
					}
					mActivity.setSelectedDate(((ScheduleDateView)v).mScheduleDate);
				}
			});
			mViews.add(view);
		}

		for (ScheduleDateView view: mViews) {
			rootView.addView(view);
		}

		return rootView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
	}

	public void selectDate(int position) {
		for(int i = 0; i < mViews.size(); i++) {
			ScheduleDateView view = mViews.get(i);
			view.setSelected(i == position);
		}

		mActivity.setSelectedDate(mWeek.mDays.get(position));
	}
}

