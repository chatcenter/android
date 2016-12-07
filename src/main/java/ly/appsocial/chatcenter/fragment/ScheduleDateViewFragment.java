package ly.appsocial.chatcenter.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.ScheduleActivity;
import ly.appsocial.chatcenter.ui.ZoomableView;
import ly.appsocial.chatcenter.util.DateUtils;


public class ScheduleDateViewFragment extends Fragment implements ZoomableView.OnScaleButtonTouchListener{
    private static final String ARG_SCHEDULE_DATE = "schedule_date";

    private ScheduleActivity.ScheduleDate mCurrentSelectedDate;

    private RelativeLayout mContainer;
    private List<ZoomableView> overlayViews;
    private ListView mHouView;

    private ScheduleTimeViewListener mListener;

    private ScheduleDateViewFragment() {
    }

    public static ScheduleDateViewFragment newInstance(ScheduleActivity.ScheduleDate date, ScheduleTimeViewListener listener) {
        ScheduleDateViewFragment fragment = new ScheduleDateViewFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SCHEDULE_DATE, date);
        fragment.mListener = listener;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurrentSelectedDate = (ScheduleActivity.ScheduleDate) getArguments().getSerializable(ARG_SCHEDULE_DATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_time_view, container, false);

        mContainer = (RelativeLayout) view.findViewById(R.id.container);

        overlayViews = new ArrayList<>();

        mHouView = (ListView) view.findViewById(R.id.hour_view);

        setUpTimeSessionView(mCurrentSelectedDate);

        if (DateUtils.isToday(mCurrentSelectedDate.mDate)) {
            drawCurrentTimeView();
        }

        return view;
    }

    private void setUpTimeSessionView(final ScheduleActivity.ScheduleDate date) {
        final TimeViewAdapter adapter = new TimeViewAdapter(getContext(), 0, date.getTimeSessions());
        mHouView.setAdapter(adapter);

        addOverlayViews();

        mHouView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListener != null) {
                    mListener.onTimeSessionSelected(mCurrentSelectedDate);
                }

                ScheduleActivity.SelectedSession session = new ScheduleActivity.SelectedSession();
                session.startIndex = position;
                session.endIndex = position;

                mCurrentSelectedDate.addSelectedSession(session);

                addOverlayViews();
            }
        });
    }

    @Override
    public void onTouchBtScaleDown(ZoomableView view) {

        if (mListener != null) {
            mListener.enableScrollForRootView(false);
        }

        int index = overlayViews.indexOf(view);
        if (index == 0) {
            view.setMinY(0);
        } else {
            view.setMinY(overlayViews.get(index - 1).getBottomY());
        }

        if (index == overlayViews.size() - 1) {
            view.setMaxY(getResources().getDimensionPixelSize(R.dimen.list_view_height));
        } else {
            view.setMaxY(overlayViews.get(index + 1).getTopY());
        }
    }

    @Override
    public void onTouchBtScaleUp(ZoomableView view) {

        if (mListener != null) {
            mListener.enableScrollForRootView(true);
        }

        int index = overlayViews.indexOf(view);
        ScheduleActivity.SelectedSession selectedSession = mCurrentSelectedDate.mSelectedSessions.get(index);

        if (view.isScaleToBottom()) {
            selectedSession.endIndex = selectedSession.startIndex + view.getNumberOfSlots() - 1;
        }

        if (view.isScaleToTop()) {
            selectedSession.startIndex = selectedSession.endIndex - view.getNumberOfSlots() + 1;
        }

        addOverlayViews();
    }

    @Override
    public void onDeleteZoomView(ZoomableView view) {
        int viewIndex = overlayViews.indexOf(view);
        mCurrentSelectedDate.removeSession(viewIndex);

        addOverlayViews();
    }

    public static class TimeViewAdapter extends ArrayAdapter<ScheduleActivity.TimeSession> {
        List<ScheduleActivity.TimeSession> mTimeSessions;

        public TimeViewAdapter(Context context, int resource, List<ScheduleActivity.TimeSession> objects) {
            super(context, resource, objects);
            mTimeSessions = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_time_session, parent, false);
            return convertView;
        }

        @Override
        public ScheduleActivity.TimeSession getItem(int position) {
            return mTimeSessions.get(position);
        }

        @Override
        public int getCount() {
            return mTimeSessions.size();
        }
    }


    private void drawCurrentTimeView() {
        long currentTimeInSeconds = Calendar.getInstance().getTimeInMillis() / 1000;
        long startOfCurrentDate = mCurrentSelectedDate.getTimeSessions().get(0).mActionData.value.start;

        int passedTime = (int) (currentTimeInSeconds - startOfCurrentDate);
        int topMargin = passedTime * ( 24 * getResources().getDimensionPixelSize(R.dimen.time_label_height)) / (24 * 60 * 60);

        View line = new View(getContext());
        mContainer.addView(line);
        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) line.getLayoutParams();
        lParams.height = 1;
        lParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        lParams.topMargin = topMargin;
        line.setBackgroundColor(Color.RED);

        View rectangle = new View(getContext());
        mContainer.addView(rectangle);
        RelativeLayout.LayoutParams rParams = (RelativeLayout.LayoutParams) rectangle.getLayoutParams();
        rParams.height = 6;
        rParams.width = 6;
        rParams.topMargin = topMargin - 3;
        rectangle.setBackgroundColor(Color.RED);
    }


    private void addOverlayViews() {

        // Remove all view
        for (ZoomableView view : overlayViews) {
            mContainer.removeView(view);
        }

        overlayViews.clear();

        List<ScheduleActivity.SelectedSession> selectedSessions = mCurrentSelectedDate.mSelectedSessions;

        for (int i = 0; i < selectedSessions.size(); i++) {
            drawViewForSession(selectedSessions.get(i), i);
        }
        mContainer.invalidate();
    }

    private void drawViewForSession(ScheduleActivity.SelectedSession session, int sessionIndex) {
        int marginTop = session.startIndex * getResources().getDimensionPixelSize(R.dimen.time_block_height)
                - getResources().getDimensionPixelSize(R.dimen.resizable_view_margin);

        ZoomableView zoomableView = new ZoomableView(getContext());
        mContainer.addView(zoomableView);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) zoomableView.getLayoutParams();
        layoutParams.topMargin = marginTop;

        layoutParams.height = (-session.startIndex + session.endIndex + 1) * getResources().getDimensionPixelSize(R.dimen.time_block_height)
                + 2 * getResources().getDimensionPixelSize(R.dimen.resizable_view_margin);

        zoomableView.setLayoutParams(layoutParams);
        zoomableView.setListener(this);

        zoomableView.requestLayout();

        overlayViews.add(zoomableView);

        zoomableView.setSelectedSessionLabel(mCurrentSelectedDate.getDisplayLabelForSelectedSession(session));

//        zoomableView.setZoomToTopEnable(isEnableZoomToTop(sessionIndex));
//        zoomableView.setZoomToBottomEnable(isEnableZoomToBottom(sessionIndex));
    }

//    private boolean isEnableZoomToTop(int sessionIndex) {
//        ScheduleActivity.SelectedSession currentSession = mCurrentSelectedDate.mSelectedSessions.get(sessionIndex);
//
//        if (currentSession.startIndex == 0) {
//            return false;
//        }
//
//        if (sessionIndex == 0) {
//            return true;
//        }
//
//        ScheduleActivity.SelectedSession preSession = mCurrentSelectedDate.mSelectedSessions.get(sessionIndex - 1);
//        if (preSession.endIndex == currentSession.startIndex - 1) {
//            return false;
//        }
//        return true;
//    }
//
//    private boolean isEnableZoomToBottom(int sessionIndex) {
//        ScheduleActivity.SelectedSession currentSession = mCurrentSelectedDate.mSelectedSessions.get(sessionIndex);
//        if (currentSession.endIndex == mCurrentSelectedDate.mTimeSessions.size() - 1) {
//            return false;
//        }
//
//        if (sessionIndex == mCurrentSelectedDate.mSelectedSessions.size() - 1) {
//            return true;
//        }
//
//        ScheduleActivity.SelectedSession nextSession = mCurrentSelectedDate.mSelectedSessions.get(sessionIndex + 1);
//        if (nextSession.startIndex == currentSession.endIndex + 1) {
//            return false;
//        }
//        return true;
//    }

    public interface ScheduleTimeViewListener {
        void enableScrollForRootView(boolean enable);
        void onTimeSessionSelected(ScheduleActivity.ScheduleDate date);
    }
}
