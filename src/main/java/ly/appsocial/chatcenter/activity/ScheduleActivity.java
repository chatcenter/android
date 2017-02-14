package ly.appsocial.chatcenter.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.dto.ChatItem;
import ly.appsocial.chatcenter.fragment.AlertDialogFragment;
import ly.appsocial.chatcenter.fragment.ScheduleDateViewFragment;
import ly.appsocial.chatcenter.fragment.ScheduleWeekViewFragment;
import ly.appsocial.chatcenter.fragment.WidgetPreviewDialog;
import ly.appsocial.chatcenter.ui.LockableScrollView;
import ly.appsocial.chatcenter.util.DialogUtil;
import ly.appsocial.chatcenter.widgets.BasicWidget;

public class ScheduleActivity extends BaseActivity implements
		WidgetPreviewDialog.WidgetPreviewListener,
		ScheduleDateViewFragment.ScheduleTimeViewListener,
		AlertDialogFragment.DialogListener{

	private final int HOURS_IN_DAY = 24; // There are 24 hours in day
	private final int TIME_SESSION_PERIOD = 30; // 30 minutes

	public static final String SELECTED_TIME = "selected_time";

	/** Weeks ViewPager */
	private ViewPager mWeekPager;

	/** Date ViewPager */
	private ViewPager mDatePager;

	/** Layout where We show hours of day*/
	private LinearLayout mHourNumberView;

	/** Display what is current date*/
	private TextView mTvSelectedDate;

	/** ScrollView to hold Date's ViewPager*/
	private LockableScrollView mScrollView;

	private WeekPagerAdapter mWeekPagerAdapter;
	private DatePagerAdapter mDatePagerAdapter;

	/** List of weeks were loaded*/
	private List<ScheduleWeek> mWeekList;

	/** List of dates that user selected*/
	private ArrayList<ScheduleDate> mSelectedDates;

	/** List date of all loaded weeks*/
	private ArrayList<ScheduleDate> mLoadedDates;

	/** Current selecting week*/
	public int mCurrentWeekIndex = -1;
	public int mNextWeekOffset = 1;

	private Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle(R.string.schedule_title);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setHomeAsUpIndicator(R.drawable.bt_close);

		mTvSelectedDate = (TextView) findViewById(R.id.schedule_selected_date);
		mWeekPager = (ViewPager) findViewById(R.id.schedule_date_selector);
		mDatePager = (ViewPager) findViewById(R.id.schedule_time_selector);
		mScrollView = (LockableScrollView) findViewById(R.id.scroll_view);

		// Initial WeekList: add this week and next week
		mWeekList = new ArrayList<>();
		mWeekList.add(new ScheduleWeek(0));
		mWeekList.add(new ScheduleWeek(1));

		mSelectedDates = new ArrayList<>();
		mLoadedDates = new ArrayList<>();
		mLoadedDates.addAll(mWeekList.get(0).mDays);
		mLoadedDates.addAll(mWeekList.get(1).mDays);

		// Setup for Weeks' ViewPager
		mWeekPagerAdapter = new WeekPagerAdapter(getSupportFragmentManager(), mWeekList);
		mWeekPager.setAdapter(mWeekPagerAdapter);
		mWeekPager.addOnPageChangeListener(mWeekPageChangeListener);



		// Setup for Dates' ViewPager
		mDatePagerAdapter = new DatePagerAdapter(getSupportFragmentManager(), mLoadedDates);
		mDatePager.setAdapter(mDatePagerAdapter);
		mDatePager.addOnPageChangeListener(mDatePageChangeListener);

		mScrollView.setScrollingEnabled(true);

		setUpHourView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.next_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			cancel();
		} else if (id == R.id.next) {
			previewSchedule();
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Init for view on left of Date ViewPager
	 * Show hour of the day
	 */
	private void setUpHourView() {
		mHourNumberView = (LinearLayout) findViewById(R.id.hour_number);

		for (int i = 0; i < HOURS_IN_DAY; i++) {
			TimeLabelView timeLabelView = new TimeLabelView(this);

			int time = i - 12;
			if (time == 0) {
				time = 12;
			}

			int displayTime;
			if (Math.abs(time) == 12) {
				displayTime = 12;
			} else {
				if (time < 0) {
					displayTime = 12 - Math.abs(time);
				} else {
					displayTime = time;
				}
			}
			if (time < 0) {
				timeLabelView.mTvTime.setText(String.format(getString(R.string.schedule_hour), displayTime, getString(R.string.hour_am)));
			} else {
				timeLabelView.mTvTime.setText(String.format(getString(R.string.schedule_hour), displayTime, getString(R.string.hour_pm)));
			}

			mHourNumberView.addView(timeLabelView);
		}
	}

	/**
	 * When user select a date
	 * @param date
	 */
	public void setSelectedDate(ScheduleDate date) {
		SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.schedule_date_format));
		mTvSelectedDate.setText(sdf.format(date.mDate));

		int dateIndex = mLoadedDates.indexOf(date);
		if (dateIndex != mDatePager.getCurrentItem()) {
			mDatePager.setCurrentItem(dateIndex, false);
		}
	}

	/** Trigger will be called when week was changed*/
	private ViewPager.OnPageChangeListener mWeekPageChangeListener = new ViewPager.OnPageChangeListener() {
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

		}

		@Override
		public void onPageSelected(int position) {
			if (position == 0) {
				// mPreviousOffset -= 1;
				// mWeekList.add(0, new ScheduleWeek(mPreviousOffset));
			}  else if (position == mWeekList.size() -1) {
				// Add new week
				mNextWeekOffset += 1;
				ScheduleWeek newWeek = new ScheduleWeek(mNextWeekOffset);
				mWeekList.add(newWeek);
				mLoadedDates.addAll(newWeek.mDays);
			}

			mCurrentWeekIndex = position;

			mWeekPagerAdapter.notifyDataSetChanged();
			mDatePagerAdapter.notifyDataSetChanged();

			int selectedDateIndex = mDatePager.getCurrentItem();
			((ScheduleWeekViewFragment) mWeekPagerAdapter.mFragments.get(position)).selectDate(selectedDateIndex % 7);
		}

		@Override
		public void onPageScrollStateChanged(int state) {

		}
	};

	private ViewPager.OnPageChangeListener mDatePageChangeListener = new ViewPager.OnPageChangeListener() {
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

		}

		@Override
		public void onPageSelected(int position) {
			int dateIndex = position % 7;
			int weekIndex = position / 7;
			if (weekIndex != mCurrentWeekIndex) {
				mWeekPager.setCurrentItem(weekIndex);
				mCurrentWeekIndex = weekIndex;
			}
			ScheduleWeekViewFragment fragment = (ScheduleWeekViewFragment) mWeekPagerAdapter.mFragments.get(mCurrentWeekIndex);
			fragment.selectDate(dateIndex);
		}

		@Override
		public void onPageScrollStateChanged(int state) {

		}
	};

	/** Preview Schedule Widget before send it*/
	private void previewSchedule() {
		ArrayList<BasicWidget.StickerAction.ActionData> actions = new ArrayList<>();
		for (ScheduleDate date: mSelectedDates) {
			actions.addAll(date.getSelectedActionData());
		}

		Collections.sort(actions, new Comparator<BasicWidget.StickerAction.ActionData>() {
			@Override
			public int compare(BasicWidget.StickerAction.ActionData lhs, BasicWidget.StickerAction.ActionData rhs) {
				return (int) (lhs.value.start - rhs.value.start);
			}
		});

		if (actions != null && actions.size() > 0) {
			String widgetContent = ChatItem.createScheduleWidgetContent(actions, this);
			showDialogWidgetPreview(widgetContent);
		} else {
			String message = getString(R.string.alert_schedule_select_schedule_slot);
			showAlert(message);
		}
	}

	private void showAlert(String message) {
		DialogUtil.showAlertDialog(getSupportFragmentManager(), null, null, message);
	}

	/**
	 * Cancel to create a schedule widget
	 */
	private void cancel() {
		Intent intent = new Intent();
		setResult(RESULT_CANCELED, intent);
		finish();
	}

	@Override
	public void onSendButtonClicked() {
		ArrayList<BasicWidget.StickerAction.ActionData> actions = new ArrayList<>();
		for (ScheduleDate date: mSelectedDates) {
			actions.addAll(date.getSelectedActionData());
		}

		Collections.sort(actions, new Comparator<BasicWidget.StickerAction.ActionData>() {
			@Override
			public int compare(BasicWidget.StickerAction.ActionData lhs, BasicWidget.StickerAction.ActionData rhs) {
				return (int) (lhs.value.start - rhs.value.start);
			}
		});

		if (actions != null && actions.size() > 0) {
			Intent intent = new Intent();
			String content = ChatItem.createScheduleWidgetContent(actions, this);
			intent.putExtra(SELECTED_TIME, content);
			setResult(RESULT_OK, intent);
			finish();
		}
	}

	@Override
	public void enableScrollForRootView(boolean enable) {
		mScrollView.setScrollingEnabled(enable);
	}

	@Override
	public void onTimeSessionSelected(ScheduleDate date) {
		if (!mSelectedDates.contains(date)) {
			mSelectedDates.add(date);
		}
	}

	/**
	 * ダイアログをキャンセルした際のコールバック。
	 *
	 * @param tag このフラグメントのタグ
	 */
	@Override
	public void onDialogCancel(String tag) {

	}

	/**
	 * ダイアログの肯定ボタンを押下した際のコールバック。
	 *
	 * @param tag このフラグメントのタグ
	 */
	@Override
	public void onPositiveButtonClick(String tag) {

	}

	public class DatePagerAdapter extends FragmentStatePagerAdapter {

		private ArrayList<ScheduleDate> mDates;

		public DatePagerAdapter(FragmentManager fm, ArrayList<ScheduleDate> dates) {
			super(fm);
			mDates = dates;
		}


		/**
		 * Return the Fragment associated with a specified position.
		 *
		 * @param position
		 */
		@Override
		public Fragment getItem(int position) {
			Fragment fragment = ScheduleDateViewFragment.newInstance(mDates.get(position), ScheduleActivity.this);
			return fragment;
		}

		/**
		 * Return the number of views available.
		 */
		@Override
		public int getCount() {
			return mDates.size();
		}
	}

	public class WeekPagerAdapter extends FragmentStatePagerAdapter {
		private List<Fragment> mFragments;
		private List<ScheduleWeek> mWeeks;

		public WeekPagerAdapter(FragmentManager fm, List<ScheduleWeek> weeks) {
			super(fm);
			mWeeks = weeks;
			mFragments = new ArrayList<>();
		}


		/**
		 * Return the Fragment associated with a specified position.
		 *
		 * @param position
		 */
		@Override
		public Fragment getItem(int position) {
			if (mFragments == null || mFragments.size() <= position) {
				ScheduleWeekViewFragment fragment = new ScheduleWeekViewFragment();
				fragment.mWeek = mWeeks.get(position);
				fragment.mActivity = ScheduleActivity.this;
				mFragments.add(fragment);
				return fragment;
			}
			return mFragments.get(position);
		}

		/**
		 * Return the number of views available.
		 */
		@Override
		public int getCount() {
			return mWeekList.size();
		}
	}

	public class TimeLabelView extends RelativeLayout {

		private TextView mTvTime;

		public TimeLabelView(Context context) {
			super(context);
			createView(context);
		}

		public TimeLabelView(Context context, AttributeSet attrs) {
			super(context, attrs);
			createView(context);
		}

		private void createView(Context context) {
			inflate(context, R.layout.view_time_label, this);
			mTvTime = (TextView) findViewById(R.id.time_view);
		}
	}

	public class ScheduleWeek {
		public ArrayList<ScheduleDate> mDays = new ArrayList<>();

		ScheduleWeek(int offset) {
			Calendar calendar = Calendar.getInstance();
			calendar.setFirstDayOfWeek(Calendar.MONDAY);
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			calendar.add(Calendar.WEEK_OF_YEAR, offset);

			for (int i = 0; i < 7; i++)
			{
				ScheduleDate date = new ScheduleDate();
				date.mDate = calendar.getTime();
				date.isSelected = false;
				mDays.add(i, date);
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
	}

	public class ScheduleDate {
		public Date mDate;
		boolean isSelected;
		public List<TimeSession> mTimeSessions;
		public List<SelectedSession> mSelectedSessions;

		public List<TimeSession> getTimeSessions () {
			if (mTimeSessions == null) {
				mTimeSessions = new ArrayList<>();
				mSelectedSessions = new ArrayList<>();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(mDate);

				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);

				for (int i = 0; i < 2 * HOURS_IN_DAY; i++) {
					TimeSession timeSession = new TimeSession();

					BasicWidget.StickerAction.ActionData actionData = new BasicWidget.StickerAction.ActionData();
					BasicWidget.StickerAction.ActionData.Value value = new BasicWidget.StickerAction.ActionData.Value();

					value.start = calendar.getTimeInMillis() / 1000;
//					if (i == (2 * HOURS_IN_DAY -1)) {
//						value.end = calendar.getTimeInMillis() / 1000 + (TIME_SESSION_PERIOD - 1) * 60; // 11:59 PM
//					} else {
//						value.end = calendar.getTimeInMillis() / 1000 + TIME_SESSION_PERIOD * 60;
//					}

					value.end = calendar.getTimeInMillis() / 1000 + TIME_SESSION_PERIOD * 60;

					actionData.value = value;
					actionData.label = getActionLabel(value.start, value.end);

					timeSession.mActionData = actionData;

					mTimeSessions.add(timeSession);

					calendar.add(Calendar.MINUTE, TIME_SESSION_PERIOD);
				}
			}

			return mTimeSessions;
		}


		public ArrayList<BasicWidget.StickerAction.ActionData> getSelectedActionData() {
			ArrayList<BasicWidget.StickerAction.ActionData> actions = new ArrayList<>();
			List<SelectedSession> sessions = mSelectedSessions;

			for (SelectedSession session: sessions) {
				TimeSession startSession = mTimeSessions.get(session.startIndex);
				TimeSession endSession = mTimeSessions.get(session.endIndex);
				BasicWidget.StickerAction.ActionData actionData = startSession.mActionData;
				actionData.value.end = endSession.mActionData.value.end;
				actionData.label = getActionLabel(actionData.value.start, actionData.value.end);
				actions.add(actionData);
			}

			return actions;
		}

		public String getDisplayLabelForSelectedSession(SelectedSession session) {
			int endIndex = session.endIndex;
			if ( mTimeSessions.size() <= endIndex ){
				endIndex = mTimeSessions.size() - 1;
				session.endIndex = endIndex;
			}

			TimeSession startSession = mTimeSessions.get(session.startIndex);
			TimeSession endSession = mTimeSessions.get(endIndex);
			return  getDisplayLabel(startSession.mActionData.value.start, endSession.mActionData.value.end);
		}

		public void removeSession(int sessionIndex) {
			mSelectedSessions.remove(sessionIndex);
		}

		public void addSelectedSession(SelectedSession session) {
			mSelectedSessions.add(session);

			Collections.sort(mSelectedSessions, new Comparator<SelectedSession>() {
				@Override
				public int compare(SelectedSession lhs, SelectedSession rhs) {
					return lhs.startIndex - rhs.startIndex;
				}
			});
		}
	}

	private String getDisplayLabel (Long startTime, Long endTime) {
		StringBuilder labelBuilder = new StringBuilder();

		SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format_short));

		labelBuilder.append(sdf.format(new Date(startTime * 1000)));
		labelBuilder.append(" - ");
		labelBuilder.append(sdf.format(new Date(endTime * 1000)));


		return labelBuilder.toString();
	}

	private String getActionLabel (Long startTime, Long endTime) {
		StringBuilder labelBuilder = new StringBuilder();

		SimpleDateFormat fullDateFormat = new SimpleDateFormat(getString(R.string.schedule_date_format));
		String startDate = fullDateFormat.format(new Date(startTime * 1000));
		String endDate = fullDateFormat.format(new Date(endTime * 1000));

		if (startDate.equals(endDate)) {
			SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format_short));
			SimpleDateFormat sdfDate = new SimpleDateFormat(getString(R.string.date_format_date));

			labelBuilder.append(sdfDate.format(new Date(startTime * 1000)));
			labelBuilder.append(" ");
			labelBuilder.append(sdf.format(new Date(startTime * 1000)));
			labelBuilder.append(" - ");
			labelBuilder.append(sdf.format(new Date(endTime * 1000)));
		} else {
			SimpleDateFormat sfd = new SimpleDateFormat(getString(R.string.date_format_date_time));
			labelBuilder.append(sfd.format(new Date(startTime * 1000)));
			labelBuilder.append(" - ");
			labelBuilder.append(sfd.format(new Date(endTime * 1000)));
		}

		labelBuilder.append(String.format(" (%s)", TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)));

		return labelBuilder.toString();
	}

	public class TimeSession {
		public BasicWidget.StickerAction.ActionData mActionData;
	}

	public static class SelectedSession {
		public int endIndex;
		public int startIndex;
	}

	@Override
	public void finish() {
		super.finish();
		this.overridePendingTransition(0, R.anim.activity_close_exit);
	}
}
