package ly.appsocial.chatcenter.cordova;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ly.appsocial.chatcenter.ChatCenter;
import ly.appsocial.chatcenter.ws.ApiRequest;

public class CDVChatCenter extends CordovaPlugin {
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("presentChatView".equals(action)) {
            presentChatView(args, callbackContext);
            return true;
        } else if ("presentHistoryView".equals(action)) {
            presentHistoryView(args);
            return true;
        } else if ("signOut".equals(action)) {
            signOut(args);
            return true;
        }
        return false;
    }

    /**
     * Show chat view
     *
     * @param args
     * @param callbackContext
     * @throws JSONException
     */
    private void presentChatView(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String orgUid = args.getString(0);
        String firstName = args.getString(1);
        String lastName = args.getString(2);
        String email = args.getString(3);
        String provider = args.getString(4);
        String providerToken = args.getString(5);
        String providerTokenSecret = args.getString(6);
        String providerRefreshToken = args.getString(7);
        Long providerCreatedAt = (args.get(8) instanceof Long) ? args.getLong(8) : 0;
        Long providerExpiresAt = (args.get(9) instanceof Long) ? args.getLong(9) : 0;
        String deviceToken = args.getString(10);
        JSONObject channelInfo = (args.get(11) instanceof JSONObject) ? args.getJSONObject(11) : null;

        HashMap<String, String> info = new HashMap<String, String>();
        if (channelInfo != null) {
            info = new Gson().fromJson(
                    channelInfo.toString(), new TypeToken<HashMap<String, String>>() {
                    }.getType()
            );
        }

        if (ChatCenter.LoginType.FACEBOOK.equals(provider)) {
            final Context context = this.cordova.getActivity();
            ChatCenter.showChatWithFacebookAccount(context, orgUid, providerToken, providerExpiresAt, deviceToken, info);
        } else if (ChatCenter.LoginType.TWITTER.equals(provider)) {
            final Context context = this.cordova.getActivity();
            ChatCenter.showChatWithTwitterAccount(context, orgUid, providerToken, providerTokenSecret, deviceToken, info);
        } else if (ChatCenter.LoginType.GOOGLE.equals(provider)) {
            final Context context = this.cordova.getActivity();
            ChatCenter.showChatWithGoogleAccount(context, orgUid, providerToken, providerRefreshToken, deviceToken, info);
        } else if (ChatCenter.LoginType.YAHOO.equals(provider)) {
            final Context context = this.cordova.getActivity();
            ChatCenter.showChatWithYahooJPAccount(context, orgUid, providerToken, providerRefreshToken, deviceToken, info);
        } else {
            presentChatViewWithEmail(orgUid, firstName, lastName, email, deviceToken, info, callbackContext);
        }


    }

    /**
     * Create new user and open chat activity
     *
     * @param orgUid
     * @param firstName
     * @param lastName
     * @param email
     */
    private void presentChatViewWithEmail(final String orgUid, final String firstName, final String lastName,
                                          final String email, final String deviceToken,
                                          final Map<String, String> info, final CallbackContext callbackContext) {

        final Context context = this.cordova.getActivity();
        ChatCenter.signInWithNewUser(context, orgUid, firstName, lastName, email, deviceToken, new ChatCenter.SignInCallback() {
            @Override
            public void onSuccess() {
                ChatCenter.showChat(context, orgUid, firstName,
                        lastName, email, deviceToken, info);

                callbackContext.success();
            }

            @Override
            public void onError(ApiRequest.Error error) {
                callbackContext.error("");
            }
        });
    }

    /**
     * Show list of conversation (History)
     */
    private void presentHistoryView(JSONArray args) throws JSONException {
        final Context context = this.cordova.getActivity();
        if (ChatCenter.hasChatUser(context)) {
            ChatCenter.showMessages(context);
        } else {
            Toast.makeText(context, "Please login first!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sign out
     */
    private void signOut(JSONArray args) throws JSONException {
        final Context context = this.cordova.getActivity();
        String deviceToken = args.getString(0);
        if (ChatCenter.hasChatUser(context)) {
            ChatCenter.signOut(context, deviceToken, new ChatCenter.SignOutCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(context, "Logout successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(ApiRequest.Error error) {
                    Toast.makeText(context, "Logout failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, "Please login first!", Toast.LENGTH_SHORT).show();
        }
    }
}
