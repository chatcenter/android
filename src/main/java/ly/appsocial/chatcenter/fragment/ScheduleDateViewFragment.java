package ly.appsocial.chatcenter.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.activity.ScheduleActivity;
import ly.appsocial.chatcenter.activity.model.GoogleCalendarEvent;
import ly.appsocial.chatcenter.ui.ZoomableView;
import ly.appsocial.chatcenter.util.CCAuthUtil;
import ly.appsocial.chatcenter.util.DateUtils;
import ly.appsocial.chatcenter.util.NetworkQueueHelper;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.ws.ApiRequest;
import ly.appsocial.chatcenter.ws.OkHttpApiRequest;


public class ScheduleDateViewFragment extends Fragment implements ZoomableView.OnScaleButtonTouchListener{
    private static final String ARG_SCHEDULE_DATE = "schedule_date";

    private static final String REQUEST_TAG = "ScheduleDateViewFragment";

    public ScheduleActivity mActivity;

    private ScheduleActivity.ScheduleDate mCurrentSelectedDate;

    private RelativeLayout mContainer;
    private List<ZoomableView> overlayViews;
    private List<LinearLayout> googleViews;
    private ListView mHouView;

    private ScheduleTimeViewListener mListener;

    private TimeViewAdapter mTimeViewAdapter;

    private ScheduleDateViewFragment() {
    }

    public static ScheduleDateViewFragment newInstance(ScheduleActivity.ScheduleDate date,
                                                       ScheduleTimeViewListener listener) {
        ScheduleDateViewFragment fragment = new ScheduleDateViewFragment();

        fragment.mListener = listener;
        fragment.mCurrentSelectedDate = date;

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
        googleViews = new ArrayList<>();

        mHouView = (ListView) view.findViewById(R.id.hour_view);

        setUpTimeSessionView(mCurrentSelectedDate);

        if (DateUtils.isToday(mCurrentSelectedDate.mDate)) {
            drawCurrentTimeView();
        }

        return view;
    }

    private void setUpTimeSessionView(final ScheduleActivity.ScheduleDate date) {
        if (mTimeViewAdapter == null) {
            mTimeViewAdapter = new TimeViewAdapter(getContext(), 0, date.getTimeSessions());
            mHouView.setAdapter(mTimeViewAdapter);
        }

        addOverlayViews();

        getCalendarEvents(mCurrentSelectedDate);

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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isAdded()) {
            mActivity.setAllDayEvent(mCurrentSelectedDate);
            getCalendarEvents(mCurrentSelectedDate);
        }
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

    private void drawGoogleEvents() {
        if (mCurrentSelectedDate == null
                || mCurrentSelectedDate.mGoogleEvents == null
                || mCurrentSelectedDate.mGoogleEvents.size() == 0
                || !isAdded()) {
            return;
        }

        int screenWidth = mHouView.getWidth();

        // Clear old view
        // Remove all view
        for (LinearLayout view : googleViews) {
            mContainer.removeView(view);
        }

        for (int i = 0; i < mCurrentSelectedDate.mGoogleEvents.size(); i++) {
            GoogleCalendarEvent event = mCurrentSelectedDate.mGoogleEvents.get(i);
            int startOfCurrentDate = (int) (mCurrentSelectedDate.mDate.getTime() / 1000);
            int passedTime = (int) (event.getStart() - startOfCurrentDate);
            int topMargin = passedTime * ( 24 * getResources().getDimensionPixelSize(R.dimen.time_label_height)) / (24 * 60 * 60);
            int bottomToTop = (int) ((event.getEnd() - startOfCurrentDate) *
                    ( 24 * getResources().getDimensionPixelSize(R.dimen.time_label_height)) / (24 * 60 * 60));

            LinearLayout eventView = new LinearLayout(getContext());
            mContainer.addView(eventView);
            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) eventView.getLayoutParams();
            lParams.height = bottomToTop - topMargin;
            lParams.width = (int) (screenWidth * (event.getViewWidthRatio()));
            lParams.topMargin = topMargin;
            lParams.leftMargin = screenWidth - lParams.width;
            // lParams.rightMargin = event.marginRight;
            eventView.setBackgroundResource(R.drawable.bg_google_event);

            TextView label = new TextView(getContext());
            eventView.addView(label);
            LinearLayout.LayoutParams lbParams = (LinearLayout.LayoutParams) label.getLayoutParams();
            lbParams.topMargin = 8;
            lbParams.leftMargin = 10;
            label.setText(event.getSummary());
            label.setTextSize(10);
            label.setTextColor(getContext().getResources().getColor(R.color.color_chatcenter_text));

            googleViews.add(eventView);
        }
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

    /**
     * このチームはオンライですか？オフラインですか？確認します。
     */
    public void getCalendarEvents(ScheduleActivity.ScheduleDate scheduleDate) {

        if (scheduleDate.mGoogleEvents != null) {
            scheduleDate.mGoogleEvents.clear();
        }

        // Get google calendar events for this date.
        Date selectedDate = scheduleDate.mDate;
        long from = selectedDate.getTime() / 1000 - 24 * 60 * 60; // in seconds
        long to = selectedDate.getTime() / 1000 + 24 * 60 * 60; // to next day in seconds
        String locale = Locale.getDefault().getLanguage();

        String path = String.format("calendars/?from=%d&to=%d&locale=%s", from, to, locale);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authentication", CCAuthUtil.getUserToken(getContext()));

        OkHttpApiRequest request = new OkHttpApiRequest(getContext(), ApiRequest.Method.GET,
                path,
                headers,
                headers,
                new ApiRequest.Callback<JSONArray>() {
                    @Override
                    public void onSuccess(JSONArray responseDto) {
                        if (responseDto == null) {
                            return;
                        }

                        final List<GoogleCalendarEvent> ggEvents = new ArrayList<>();
                        Gson gson = new Gson();
                        for(int i = 0; i < responseDto.length(); i++) {
                            try {
                                JSONObject jsonObject = responseDto.getJSONObject(i);
                                GoogleCalendarEvent event = gson.fromJson(jsonObject.toString(), GoogleCalendarEvent.class);
                                if (event != null) {
                                    event = event.resetEndpoint(mCurrentSelectedDate.mDate);
                                    if (event.isTodayEvent()) {
                                        ggEvents.add(event);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        Collections.sort(ggEvents, new Comparator<GoogleCalendarEvent>() {
                            @Override
                            public int compare(GoogleCalendarEvent lhs, GoogleCalendarEvent rhs) {
                                return (int) (lhs.getStart() - rhs.getStart());
                            }
                        });
                        mCurrentSelectedDate.addGoogleEvents(ggEvents);
                        mCurrentSelectedDate.resetGoogleEvents(sortGoogleEvents(mCurrentSelectedDate.mGoogleEvents));
                        mContainer.post(new Runnable() {
                            @Override
                            public void run() {
                                drawGoogleEvents();
                                mActivity.setAllDayEvent(mCurrentSelectedDate);
                            }
                        });
                    }

                    @Override
                    public void onError(ApiRequest.Error error) {

                    }
                },
                new ApiRequest.Parser<JSONArray>() {
                    @Override
                    public int getErrorCode() {
                        return 0;
                    }

                    @Override
                    public JSONArray parser(String response) {
                        if(StringUtil.isJSONValid(response)) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonArray = jsonObject.getJSONArray("calendar");
                                return jsonArray;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }
                });

        NetworkQueueHelper.enqueue(request, REQUEST_TAG);
    }

    /**
     * sort list of Google Calendar Events
     * @param events
     * @return list of event
     */
    private List<GoogleCalendarEvent> sortGoogleEvents(List<GoogleCalendarEvent> events) {
        // Final result
        List<GoogleCalendarEvent> result = new ArrayList<>();

        // Temporary ArrayList to store list of overlapping event
        List<GoogleCalendarEvent> overlappingEvents = new ArrayList<>();

        for (int i = 0; i < events.size(); i++) {
            GoogleCalendarEvent event = events.get(i);
            if (overlappingEvents.size() > 0) {
                boolean isOverlapping = false;
                if (isOverlappingOther(event, overlappingEvents)) {
                    isOverlapping = true;
                    int overlappingSize = overlappingEvents.size();
                    int currentMaxOverlappingLevel = overlappingEvents.get(overlappingSize - 1).getOverlappingLevel();
                    for (int k = 0; k <= currentMaxOverlappingLevel; k++) {
                        List<GoogleCalendarEvent> eventsLevelK = getOverlappingEventsByLevel(overlappingEvents, k);
                        if (!isOverlappingOther(event, eventsLevelK)) {
                            // If event is not overlapping any event at level k, then overlapping event will be set k
                            event.setOverlappingLevel(k);
                            break;
                        } else {
                            event.setOverlappingLevel(k + 1);
                            for (GoogleCalendarEvent e : eventsLevelK) {
                                if (e.getStart() == event.getStart()) {
                                    int eventDuration = (int) (event.getEnd() - event.getStart());
                                    int overlappingEventDuration = (int) (e.getEnd() - e.getStart());
                                    if (eventDuration > overlappingEventDuration) {
                                        event.setOverlappingLevel(e.getOverlappingLevel());
                                        e.setOverlappingLevel(e.getOverlappingLevel() + 1);
                                    }
                                }
                            }
                        }
                    }
                }

                if (isOverlapping) {
                    // add event to overlapping event
                    overlappingEvents.add(event);
                    // Sort by overlapping level
                    Collections.sort(overlappingEvents, new Comparator<GoogleCalendarEvent>() {
                        @Override
                        public int compare(GoogleCalendarEvent o1, GoogleCalendarEvent o2) {
                            return o1.getOverlappingLevel() - o2.getOverlappingLevel();
                        }
                    });
                } else {
                    // If this event isn't overlapping add all overlapping events to result list
                    // and start new overlapping list
                    result.addAll(resetWidthRatioOfGoogleOverlappingEventView(overlappingEvents));
                    overlappingEvents.clear();
                    overlappingEvents.add(event);
                }
            } else {
                // add to overlapping events
                overlappingEvents.add(events.get(i));
            }
        }

        result.addAll(resetWidthRatioOfGoogleOverlappingEventView(overlappingEvents));

        return result;
    }

    /**
     * Check if there are overlapping events
     * @param firstEvent first start event
     * @param secondEvent later event
     * @return
     */
    private boolean isOverlappingEvents(GoogleCalendarEvent firstEvent, GoogleCalendarEvent secondEvent) {
        return secondEvent.getStart() < firstEvent.getEnd();
    }

    /**
     * Check is this event overlapping any event in list of events?
     * @param event event for checking
     * @param events predefined events
     * @return TRUE if even overlapped one or more event on list
     */
    private boolean isOverlappingOther(GoogleCalendarEvent event, List<GoogleCalendarEvent> events) {
        if (events == null || events.size() == 0) {
            return false;
        } else {
            for (GoogleCalendarEvent e: events) {
                if (isOverlappingEvents(e, event)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Calculate width for view of google event when draw
     * @param events
     * @return
     */
    private List<GoogleCalendarEvent> resetWidthRatioOfGoogleOverlappingEventView(List<GoogleCalendarEvent> events) {
        if (events == null || events.size() <= 1) {
            return events;
        }
        int maxOverlappingLevel = events.get(events.size() -1).getOverlappingLevel();

        if (maxOverlappingLevel > 0) {
            for (GoogleCalendarEvent event: events) {
                event.setViewWidthRatio(1.0f - (event.getOverlappingLevel() * 1.0f/(maxOverlappingLevel + 1)));
                // event.marginRight = (maxOverlappingLevel - event.mOverlappingLevel) * 20;
            }
        }

        return events;
    }

    /**
     * Get all events with same level from list
     * @param events
     * @param level
     * @return list of google calendar events
     */
    private List<GoogleCalendarEvent> getOverlappingEventsByLevel(List<GoogleCalendarEvent> events, int level) {
        List<GoogleCalendarEvent> result = new ArrayList<>();
        for(GoogleCalendarEvent event : events) {
            if (event.getOverlappingLevel() == level) {
                result.add(event);
            }
        }
        return result;
    }

    public interface ScheduleTimeViewListener {
        void enableScrollForRootView(boolean enable);
        void onTimeSessionSelected(ScheduleActivity.ScheduleDate date);
    }
}
