package ly.appsocial.chatcenter.gcm;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.util.AuthUtil;

public class ChatCenterDeviceTokenRequest extends AsyncTask<Object, String, String> {
    public static final String TAG = ChatCenterDeviceTokenRequest.class.getSimpleName();

    /** Listener */
    private DeviceTokenRequestCallback mListener;
    /** Context */
    private Context mContext;

    public ChatCenterDeviceTokenRequest(Context context, DeviceTokenRequestCallback listener) {
        mListener = listener;
        mContext = context;
    }
    public ChatCenterDeviceTokenRequest(Context context) {
        this(context, null);
    }

    public void setCallback(DeviceTokenRequestCallback listener) {
        mListener = listener;
    }

    @Override
    protected String doInBackground(Object... params) {
        if (mContext.getString(R.string.product_gcm_notification_sender).isEmpty()) {
            Log.e(TAG, "GCM Push notification SenderID not found");
            return null;
        }
        final int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Google Play Service is not available on this device");
            return null;
        }

        // Actual getting the token here
        InstanceID instanceID = InstanceID.getInstance(mContext);
        String deviceToken;
        try {
            deviceToken = instanceID.getToken(mContext.getString(R.string.product_gcm_notification_sender),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.d("###", "deviceToken: " + deviceToken);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        // Save the token
        AuthUtil.saveDeviceToken(mContext, deviceToken);
        return deviceToken;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (mListener != null) {
            mListener.onTokenReceived(s);
        }
    }

    /**
     * Callback for when the request finished
     */
    public interface DeviceTokenRequestCallback {
        /**
         * This will always be called, whether there's an error or not.
         * @param token The token received from Google, null if error.
         */
        void onTokenReceived(String token);
    }
}
