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
	}

	public void stopTimer(){
		mIsActive = false;

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ChatCenterConstants.BroadcastAction.UPDATE_CHAT);
		mContext.sendBroadcast(broadcastIntent);
	}
}
