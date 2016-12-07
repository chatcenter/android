package ly.appsocial.chatcenter.di;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import ly.appsocial.chatcenter.activity.BaseActivity;
import ly.appsocial.chatcenter.di.components.DaggerNetworkComponent;
import ly.appsocial.chatcenter.di.components.NetworkComponent;
import ly.appsocial.chatcenter.di.modules.ContextModule;
import ly.appsocial.chatcenter.di.modules.NetworkModule;

public class InjectorHelper {

    private final int OKHTTP_CONNECTION_TIMEOUT = 120; // 2 minutes
    private final int OKHTTP_READ_TIMEOUT = 120; // 2 minutes
    private final int OKHTTP_WRITE_TIMEOUT = 120; // 2 minutes

    private OkHttpClient mClient;
    private OkHttpClient mWSClient;

    // =========================================================
    // Singleton
    // =========================================================
    private static InjectorHelper sInstance;
    private InjectorHelper() {

    }

    public static InjectorHelper getInstance() {
        if (sInstance == null) {
            sInstance = new InjectorHelper();
        }
        return sInstance;
    }

    // =========================================================
    // Network injector helper methods
    // =========================================================
    private NetworkComponent mNetworkComponent;

    /**
     * Override the network component
     *
     * @param component
     */
    public void setDefaultNetworkComponent(NetworkComponent component) {
        mNetworkComponent = component;
    }

    public void injectNetworkModule(BaseActivity activity) {
        if (mNetworkComponent == null) {
            initHTTPClient();
            mNetworkComponent = DaggerNetworkComponent.builder()
                    .contextModule(new ContextModule(activity))
                    .networkModule(new NetworkModule(mClient, mWSClient))
                    .build();
        }
        mNetworkComponent.inject(activity);
    }

    public void injectNetworkModule(Context context, NetworkUtilitiesWrapper wrapper) {
        if (mNetworkComponent == null) {
            initHTTPClient();
            mNetworkComponent = DaggerNetworkComponent.builder()
                    .contextModule(new ContextModule(context))
                    .networkModule(new NetworkModule(mClient, mWSClient))
                    .build();
        }
        mNetworkComponent.inject(wrapper);
    }


    private void initHTTPClient() {
        mClient = new OkHttpClient();
        mWSClient = new OkHttpClient();

        if (mClient != null) {
            mClient.setConnectTimeout(OKHTTP_CONNECTION_TIMEOUT, TimeUnit.SECONDS);
            mClient.setReadTimeout(OKHTTP_READ_TIMEOUT, TimeUnit.SECONDS);
            mClient.setWriteTimeout(OKHTTP_WRITE_TIMEOUT, TimeUnit.SECONDS);
        }

        if (mWSClient != null) {
            mWSClient.setConnectTimeout(OKHTTP_CONNECTION_TIMEOUT, TimeUnit.SECONDS);
            mWSClient.setReadTimeout(OKHTTP_READ_TIMEOUT, TimeUnit.SECONDS);
            mWSClient.setWriteTimeout(OKHTTP_WRITE_TIMEOUT, TimeUnit.SECONDS);
        }
    }
}