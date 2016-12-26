/*
 * Copyright (c) 2016.
 */

package ly.appsocial.chatcenter.widgets;

import android.content.Context;
import android.content.Intent;

import java.util.Timer;
import java.util.TimerTask;

import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.dto.UserItem;

/**
 * Created by karasawa on 2016/12/21.
 */

public class LiveLocationUser {
	public Integer mId;
	public String mDisplayName;
	public String mIconUrl;

	private Context mContext;
	private boolean mIsActive = false;
	private Timer mTimer;
	private int mTimerCount = 0;
	final int INTERVAL_PERIOD = 1000;
	final int LIVE_SECOND = 30;

	public LiveLocationUser(Context context, UserItem user){
		mContext = context;
		mId = user.id;
		mDisplayName = user.displayName;
		mIconUrl = user.iconUrl;
	}

	public LiveLocationUser(Context context, Integer id, String displayName, String iconUrl){
		mContext = context;
		mId = id;
		mDisplayName = displayName;
		mIconUrl = iconUrl;
	}

	public boolean isActive(){
		return mIsActive;
	}

	public void updateTimer(){
		mIsActive = true;

		if (mTimer != null) {
			mTimer.cancel();
		}
		mTimerCount = 0;
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				mTimerCount++;
				if ( mTimerCount >= LIVE_SECOND){
					stopTimer();
				}
			}
		}, 0, INTERVAL_PERIOD);
	}

	public void stopTimer(){
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
			mIsActive = false;
		}
		mTimerCount = 0;

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ChatCenterConstants.BroadcastAction.UPDATE_CHAT);
		mContext.sendBroadcast(broadcastIntent);
	}
}
