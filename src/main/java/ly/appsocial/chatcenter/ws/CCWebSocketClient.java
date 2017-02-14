package ly.appsocial.chatcenter.ws;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketCall;
import com.squareup.okhttp.ws.WebSocketListener;

import java.io.IOException;

import ly.appsocial.chatcenter.di.InjectorHelper;
import ly.appsocial.chatcenter.di.NetworkUtilitiesWrapper;
import okio.Buffer;

/**
 * WebSocket
 */
public class CCWebSocketClient {
    private static final String TAG = CCWebSocketClient.class.getSimpleName();

    private Listener mListener;
    private WebSocket mWebSocket;

    private NetworkUtilitiesWrapper mWrapper;
    private Request mWebSocketRequest;
    private WebSocketCall mWebSocketCall;

    private String mEndPoint;

    private WebSocketListener mWSListener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.d(TAG, "onOpen: " + webSocket);
            mWebSocket = webSocket;
            mIsDisconnected = false;
            if (mListener != null) {
                mListener.onConnect();
            }

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mWebSocket.sendPing(null);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }).start();
                }
            }, 1000);
        }

        @Override
        public void onFailure(IOException e, Response response) {
            Log.d(TAG, "onFailure: ");

            if (mListener != null) {
                mListener.onError(e);
            }
        }

        @Override
        public void onMessage(ResponseBody message) throws IOException {
            Log.d(TAG, "onMessage: ");
            if (mListener != null) {
                mListener.onMessage(message.string());
            }
        }

        @Override
        public void onPong(Buffer payload) {
            try {
                if (mWebSocket != null) {
                    mWebSocket.sendPing(payload);
                }
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClose(int code, String reason) {
            Log.d(TAG, "onClose: " + code + " " + reason);
            mWebSocket = null;
            mIsDisconnected = true;
            if (mListener != null) {
                mListener.onDisconnect(code, reason);
            }
        }
    };

    private boolean mIsDisconnected = true;
    private Handler mHandler;

    public CCWebSocketClient(Context context, String endpoint, Listener listener) {
        this.setEndPoint(endpoint);
        this.setListener(listener);

        mWrapper = new NetworkUtilitiesWrapper();
        InjectorHelper.getInstance().injectNetworkModule(context, mWrapper);

        mHandler = new Handler();
    }

    public void send(String message) {
        if (mWebSocket == null) {
            return;
        }
        Log.e(TAG, "Sending message: " + message);
        try {
            mWebSocket.sendMessage(RequestBody.create(WebSocket.TEXT, message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (mWebSocket == null) {
            return;
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mWebSocket.close(0, "Just disconnect");
                    mWebSocket = null;
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        t.start();
    }

    public boolean isConnected() {
        Log.d(TAG, "isConnected: " + mWebSocket + " " + mIsDisconnected);
        return mWebSocket != null && !mIsDisconnected;
    }

    public void connect() {
        mWebSocketRequest = new Request.Builder().url(mEndPoint).build();
        mWebSocketCall = WebSocketCall.create(mWrapper.getWSOkHttpClient(), mWebSocketRequest);

        mWebSocketCall.enqueue(mWSListener);
    }

    public Listener getListener() {
        return mListener;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public String getEndPoint() {
        return mEndPoint;
    }

    public void setEndPoint(String endPoint) {
        mEndPoint = endPoint;
    }

    public interface Listener {
        void onConnect();
        void onMessage(String message);
        void onMessage(byte[] data);
        void onDisconnect(int code, String reason);
        void onError(Exception exception);
    }
}