package ly.appsocial.chatcenter.ws;

import android.content.Context;

import ly.appsocial.chatcenter.util.ApiUtil;
import ly.appsocial.chatcenter.util.CCAuthUtil;
import ly.appsocial.chatcenter.util.CCLog;


public class WebSocketHelper {
    private static CCWebSocketClient mWebSocketClient;

    public static boolean connectWithAppToken(Context context, String appToken, CCWebSocketClient.Listener listener) {
        if (mWebSocketClient != null && mWebSocketClient.isConnected()) {
            CCLog.d("WebSocketHelper", "connectWithAppToken: ");
            mWebSocketClient.addListener(listener);
            return false;
        }

        String url = ApiUtil.getWsUrl(context) + "/?authentication=" + CCAuthUtil.getUserToken(context) + "&app_token=" + appToken; //;
        CCLog.d("WebSocketHelper", "connectWithAppToken: " + mWebSocketClient + " " + url);

        mWebSocketClient = new CCWebSocketClient(context, url, listener);
        mWebSocketClient.connect();
        return true;
    }

    public static void reconnectWithAppToken(Context context, String appToken, CCWebSocketClient.Listener listener) {
        CCLog.d("WebSocketHelper", "reconnectWithAppToken: " + mWebSocketClient);

        if (mWebSocketClient != null && mWebSocketClient.isConnected()) {
            mWebSocketClient.disconnect();
            mWebSocketClient = null;
        }
        connectWithAppToken(context, appToken, listener);
    }

    public static void disconnect() {
        CCLog.d("WebSocketHelper", "disconnect: " + mWebSocketClient);

        if (mWebSocketClient != null && mWebSocketClient.isConnected()) {
            mWebSocketClient.disconnect();
        }
        mWebSocketClient = null;
    }

    public static void setListener(CCWebSocketClient.Listener listener) {
        CCLog.d("WebSocketHelper", "setListener: " + mWebSocketClient);

        if (mWebSocketClient == null) {
            CCLog.d("WebSocketHelper", "setListener: skip");

            return;
        }
        mWebSocketClient.addListener(listener);
    }

    public static void removeListener(CCWebSocketClient.Listener listener) {
        CCLog.d("WebSocketHelper", "setListener: " + mWebSocketClient);

        if (mWebSocketClient == null) {
            CCLog.d("WebSocketHelper", "setListener: skip");

            return;
        }
        mWebSocketClient.removeListener(listener);
    }

    public static void send(final String message) {
        CCLog.d("WebSocketHelper", "send: " + mWebSocketClient);

        if (mWebSocketClient == null || !mWebSocketClient.isConnected()) {
            return;
        }

        Thread t = new Thread() {
            @Override
            public void run() {
                mWebSocketClient.send(message);
            }
        };
        t.start();
    }

    public static boolean isConnected() {
        return mWebSocketClient != null && mWebSocketClient.isConnected();
    }
}
