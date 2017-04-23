package ly.appsocial.chatcenter.activity.model;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ly.appsocial.chatcenter.util.StringUtil;

public class GoogleCalendarEvent {

    private final int ONE_DAY_IN_SECONDS = 24 * 60 * 60;

    @SerializedName("summary")
    private String mSummary; // Title of event

    @SerializedName("start")
    private long mStart; // Start Timestamp

    @SerializedName("end")
    private long mEnd; // End Timestamp

    @SerializedName("isAllDay")
    private boolean isAllDay;

    @SerializedName("startAllDay")
    private String startAllDay;

    /**
     * If this event overlap on other, we have to set level of overlapping
     * It will help us to create a good-looking UI
     */
    private int mOverlappingLevel = 0;

    /**
     * Use to calculate width of EventView when draw
     */
    private float viewWidthRatio = 1.0f;

    private boolean isTodayEvent = false;

    /**
     * To get title of event
     *
     * @return
     */
    public String getSummary() {

        // If is "All day event" just return title.
        if (isAllDay()) {
            return mSummary;
        }

        Date startDate = new Date(getStart() * 1000);
        Date endDate = new Date(getEnd() * 1000);

        SimpleDateFormat simpleFormat = new SimpleDateFormat("HH:mm");

        String startTime = simpleFormat.format(startDate);

        // If event end in 00:00 of next day, change time to 24:00
        String endTime = simpleFormat.format(endDate);
        if (endTime.equals("00:00")) {
            endTime = "24:00";
        }

        // Return title with start/end time
        return startTime + " - " + endTime + " " + mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public long getStart() {
        return mStart;
    }

    public void setStart(int start) {
        mStart = start;
    }

    public long getEnd() {
        return mEnd;
    }

    public void setEnd(int end) {
        mEnd = end;
    }

    /**
     * If an event start in one day and end in another day,
     *
     * @param date
     * @return
     */
    public GoogleCalendarEvent resetEndpoint(Date date) {
        Calendar calStart = Calendar.getInstance();
        long timeStamp = mStart * 1000;
        calStart.setTimeInMillis(timeStamp);

        Calendar calDayAfterStartDay = Calendar.getInstance();
        calDayAfterStartDay.set(Calendar.YEAR, calStart.get(Calendar.YEAR));
        calDayAfterStartDay.set(Calendar.MONTH, calStart.get(Calendar.MONTH));
        calDayAfterStartDay.set(Calendar.DAY_OF_MONTH, calStart.get(Calendar.DAY_OF_MONTH));
        calDayAfterStartDay.add(Calendar.DATE, 1);

        long dayAfterStartDayInSeconds = calDayAfterStartDay.getTimeInMillis() / 1000;

        Calendar calEnd = Calendar.getInstance();
        timeStamp = mEnd * 1000;
        calEnd.setTimeInMillis(timeStamp);

        Calendar currentCal = Calendar.getInstance();
        currentCal.setTime(date);

        // The day that event start
        int startDay = calStart.get(Calendar.DAY_OF_WEEK);
        // The day that event end
        int endDay = startDay;
        if (mEnd > dayAfterStartDayInSeconds) {
            endDay = startDay + (int)((mEnd - mStart) /ONE_DAY_IN_SECONDS) + 1;
        }

        int currentDay = currentCal.get(Calendar.DAY_OF_WEEK);

        if (isAllDay()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date startDate = dateFormat.parse(startAllDay);
                Date endDate = new Date(startDate.getTime() + (mEnd - mStart) * 1000);
                isTodayEvent = startDate.getTime() <= date.getTime() && date.getTime() < endDate.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {

            if (startDay == endDay && startDay == currentDay) {
                isTodayEvent = true;
            } else if (startDay < endDay) {

                // if start today, then end at 24:00 today
                if (startDay == currentDay) {
                    calStart.set(Calendar.YEAR, currentCal.get(Calendar.YEAR));
                    calStart.set(Calendar.MONTH, currentCal.get(Calendar.MONTH));
                    calStart.set(Calendar.DAY_OF_MONTH, currentCal.get(Calendar.DAY_OF_MONTH));

                    mStart = calStart.getTime().getTime() / 1000;
                    mEnd = currentCal.getTimeInMillis() / 1000 + ONE_DAY_IN_SECONDS;

                    isTodayEvent = true;
                } else if (endDay == currentDay) {

                    // if end in today, then start at 00:00 today
                    mStart = currentCal.getTimeInMillis() / 1000;

                    calEnd.set(Calendar.YEAR, currentCal.get(Calendar.YEAR));
                    calEnd.set(Calendar.MONTH, currentCal.get(Calendar.MONTH));
                    calEnd.set(Calendar.DAY_OF_MONTH, currentCal.get(Calendar.DAY_OF_MONTH));

                    mEnd = calEnd.getTime().getTime() / 1000;

                    isTodayEvent = true;
                } else if (startDay < currentDay && endDay > currentDay) {
                    isTodayEvent = true;
                    isAllDay = true;
                }

            }

        }
        return this;
    }

    public boolean isAllDay() {
        return isAllDay;
    }

    public void setAllDay(boolean allDay) {
        isAllDay = allDay;
    }

    public int getOverlappingLevel() {
        return mOverlappingLevel;
    }

    public void setOverlappingLevel(int overlappingLevel) {
        mOverlappingLevel = overlappingLevel;
    }

    public float getViewWidthRatio() {
        return viewWidthRatio;
    }

    public void setViewWidthRatio(float viewWidthRatio) {
        this.viewWidthRatio = viewWidthRatio;
    }

    public boolean isTodayEvent() {
        return isTodayEvent;
    }

    public void setTodayEvent(boolean todayEvent) {
        isTodayEvent = todayEvent;
    }

    public void setStartAllDay(String startAllDay) {
        this.startAllDay = startAllDay;
    }
}
