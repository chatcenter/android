package ly.appsocial.chatcenter.ws;

import android.content.Context;
import android.util.Log;

import ly.appsocial.chatcenter.util.ApiUtil;
import ly.appsocial.chatcenter.util.AuthUtil;


public class WebSocketHelper {
    private static CCWebSocketClient mWebSocketClient;

    public static boolean connectWithAppToken(Context context, String appToken, CCWebSocketClient.Listener listener) {
        if (mWebSocketClient != null && mWebSocketClient.isConnected()) {
            Log.d("WebSocketHelper", "connectWithAppToken: ");
            mWebSocketClient.setListener(listener);
            return false;
        }

        String url = ApiUtil.getWsUrl(context) + "/?authentication=" + AuthUtil.getUserToken(context) + "&app_token=" + appToken; //;
        Log.d("WebSocketHelper", "connectWithAppToken: " + mWebSocketClient + " " + url);

        mWebSocketClient = new CCWebSocketClient(context, url, listener);
        mWebSocketClient.connect();
        return true;
    }

    public static void reconnectWithAppToken(Context context, String appToken, CCWebSocketClient.Listener listener) {
        Log.d("WebSocketHelper", "reconnectWithAppToken: " + mWebSocketClient);

        if (mWebSocketClient != null && mWebSocketClient.isConnected()) {
            mWebSocketClient.disconnect();
            mWebSocketClient = null;
        }
        connectWithAppToken(context, appToken, listener);
    }

    public static void disconnect() {
        Log.d("WebSocketHelper", "disconnect: " + mWebSocketClient);

        if (mWebSocketClient != null && mWebSocketClient.isConnected()) {
            mWebSocketClient.disconnect();
        }
        mWebSocketClient = null;
    }

    public static void setListener(CCWebSocketClient.Listener listener) {
        Log.d("WebSocketHelper", "setListener: " + mWebSocketClient);

        if (mWebSocketClient == null) {
            return;
        }
        mWebSocketClient.setListener(listener);
    }
    public static void send(String message) {
        Log.d("WebSocketHelper", "send: " + mWebSocketClient);

        if (mWebSocketClient == null || !mWebSocketClient.isConnected()) {
            return;
        }

        mWebSocketClient.send(message);
    }

    public static boolean isConnected() {
        return mWebSocketClient != null && mWebSocketClient.isConnected();
    }
}
