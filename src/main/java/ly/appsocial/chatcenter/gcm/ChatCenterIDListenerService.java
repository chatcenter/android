package ly.appsocial.chatcenter.gcm;


import com.google.android.gms.iid.InstanceIDListenerService;

public class ChatCenterIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        // Received token refresh request, refresh the token
        new ChatCenterDeviceTokenRequest(this).execute();
    }
}