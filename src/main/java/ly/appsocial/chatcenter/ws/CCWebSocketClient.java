package ly.appsocial.chatcenter.ws;

import android.content.Context;
import android.os.Handler;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketCall;
import com.squareup.okhttp.ws.WebSocketListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ly.appsocial.chatcenter.di.InjectorHelper;
import ly.appsocial.chatcenter.di.NetworkUtilitiesWrapper;
import ly.appsocial.chatcenter.util.CCLog;
import okio.Buffer;

/**
 * WebSocket
 */
public class CCWebSocketClient {
    private static final String TAG = CCWebSocketClient.class.getSimpleName();

    private List<Listener> mListeners = new ArrayList<>();
    private WebSocket mWebSocket;

    private NetworkUtilitiesWrapper mWrapper;
    private Request mWebSocketRequest;
    private WebSocketCall mWebSocketCall;

    private String mEndPoint;

    private WebSocketListener mWSListener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            // CCLog.d(TAG, "onOpen: " + webSocket);
            mWebSocket = webSocket;
            mIsDisconnected = false;
            if (mListeners != null) {
                for (Listener listener : mListeners) {
                    listener.onConnect();
                }
            }

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (mWebSocket != null) {
                                    mWebSocket.sendPing(null);
                                }
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
            // CCLog.d(TAG, "onFailure: ");

            if (mListeners != null) {
                for (Listener listener : mListeners) {
                    listener.onError(e);
                }
            }
        }

        @Override
        public void onMessage(ResponseBody message) throws IOException {
            // CCLog.d(TAG, "onMessage: ");
            if (mListeners != null) {
                String messageString = message.string();
                for (Listener listener : mListeners) {
                    listener.onMessage(messageString);
                }
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
            // CCLog.d(TAG, "onClose: " + code + " " + reason);
            mWebSocket = null;
            mIsDisconnected = true;
            if (mListeners != null) {
                for (Listener listener : mListeners) {
                    listener.onDisconnect(code, reason);
                }
            }
        }
    };

    private boolean mIsDisconnected = true;
    private Handler mHandler;

    public CCWebSocketClient(Context context, String endpoint, Listener listener) {
        this.setEndPoint(endpoint);
        this.addListener(listener);

        mWrapper = new NetworkUtilitiesWrapper();
        InjectorHelper.getInstance().injectNetworkModule(context, mWrapper);

        mHandler = new Handler();
    }

    public void send(String message) {
        if (mWebSocket == null) {
            return;
        }
        CCLog.d(TAG, "Sending message: " + message);
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
        CCLog.d(TAG, "isConnected: " + mWebSocket + " " + mIsDisconnected);
        return mWebSocket != null && !mIsDisconnected;
    }

    public void connect() {
        mWebSocketRequest = new Request.Builder().url(mEndPoint).build();
        mWebSocketCall = WebSocketCall.create(mWrapper.getWSOkHttpClient(), mWebSocketRequest);

        mWebSocketCall.enqueue(mWSListener);
    }

    public void removeListener(Listener listener) {
        if (mListeners != null) {
            mListeners.remove(listener);
        }
    }

    public void addListener(Listener listener) {
        if (mListeners != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
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