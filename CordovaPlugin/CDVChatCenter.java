package ly.appsocial.chatcenter.cordova;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import ly.appsocial.chatcenter.ChatCenter;
import ly.appsocial.chatcenter.util.StringUtil;
import ly.appsocial.chatcenter.ws.ApiRequest;

public class CDVChatCenter extends CordovaPlugin {
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("presentChatView".equals(action)) {
           presentChatView(args, callbackContext);
            return true;
        } else if ("presentHistoryView".equals(action)) {
            presentHistoryView();
            return true;
        } else if ("signOut".equals(action)) {
            signOut();
            return true;
        }
        return false;
    }

    /**
     * Show chat view
     * @param args
     * @param callbackContext
     * @throws JSONException
     */
    private void presentChatView(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String orgUid = args.getString(0);
        String firstName = args.getString(1);
        String lastName = args.getString(2);
        String email = args.getString(3);

        presentChatViewWithEmail(orgUid, firstName, lastName, email, callbackContext);
    }

    /**
     * Create new user and open chat activity
     * @param orgUid
     * @param firstName
     * @param lastName
     * @param email
     */
    private void presentChatViewWithEmail(final String orgUid, final String firstName, final String lastName,
                                          final String email, final CallbackContext callbackContext){

        final Context context = this.cordova.getActivity();
        ChatCenter.signInWithNewUser(context, orgUid, firstName, lastName, email, new ChatCenter.SignInCallback() {
            @Override
            public void onSuccess() {
                Map<String, String> info = new HashMap<String, String>();
                ChatCenter.showChat(context, orgUid, firstName,
                        lastName, email, info);

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
    private void presentHistoryView() {
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
    private void signOut() {
        final Context context = this.cordova.getActivity();
        if (ChatCenter.hasChatUser(context)) {
            ChatCenter.signOut(context, new ChatCenter.SignOutCallback() {
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
