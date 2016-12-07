/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.ScheduleActivity;

/**
 * Created by karasawa on 2016/11/29.
 */

public class ScheduleDateView extends LinearLayout {
	private TextView mTvDay;
	private TextView mTvDate;
	public ScheduleActivity.ScheduleDate mScheduleDate;
	private Context mContext;

	private int[] mDays = new int[] {
			R.string.schedule_sunday,
			R.string.schedule_monday,
			R.string.schedule_tuesday,
			R.string.schedule_wednesday,
			R.string.schedule_thursday,
			R.string.schedule_friday,
			R.string.schedule_saturday
	};

	public ScheduleDateView(Context context, ScheduleActivity.ScheduleDate scheduleDate) {
		super(context);

		mContext = context;

		inflate(context, R.layout.view_schedule_date, this);
		setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

		mScheduleDate = scheduleDate;

		mTvDay = (TextView) findViewById(R.id.schedule_day);
		mTvDate = (TextView) findViewById(R.id.schedule_date);

		Calendar cal = Calendar.getInstance();
		cal.setTime(mScheduleDate.mDate);

		mTvDate.setText(cal.get(Calendar.DAY_OF_MONTH) + "");
		mTvDay.setText(mDays[cal.get(Calendar.DAY_OF_WEEK) - 1]);
	}

	public void setSelected(boolean isSelected) {
		super.setSelected(isSelected);
		if (isSelected) {
			mTvDate.setBackgroundResource(R.drawable.oval_shape_bg_light_blue);
			mTvDate.setTextColor(Color.WHITE);
		} else {
			mTvDate.setBackgroundColor(Color.TRANSPARENT);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
				mTvDate.setTextColor(getResources().getColor(R.color.color_chatcenter_text_dark_gray));
			} else {
				mTvDate.setTextColor(mContext.getColor(R.color.color_chatcenter_text_dark_gray));
			}
		}
	}
}
