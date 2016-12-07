package ly.appsocial.chatcenter.gcm;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ly.appsocial.chatcenter.constants.ChatCenterConstants;

public abstract class ChatCenterGcmListenerService extends GcmListenerService {
    public static final String TAG = ChatCenterGcmListenerService.class.getSimpleName();
    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);

        if(!isAppIsInBackground(this)) {
            return;
        }

        if (ChatCenterConstants.PUSH_CATEGORY.equals(data.getString("category"))) {
            Map<String, String> pushNotification = new HashMap<>();
            for (String key : data.keySet()) {
                pushNotification.put(key, data.getString(key));
            }
            onChatCenterMessageReceived(pushNotification);
        } else {
            onNormalMessageReceived(from, data);
        }
    }
    public abstract void onNormalMessageReceived(String from, Bundle data);
    public abstract void onChatCenterMessageReceived(Map<String, String> data);

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }
}
