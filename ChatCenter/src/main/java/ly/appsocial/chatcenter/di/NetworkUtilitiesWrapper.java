package ly.appsocial.chatcenter.di;

import com.squareup.okhttp.OkHttpClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Support class for dependency injector (some generic classes cannot be injected using dagger)
 */
public class NetworkUtilitiesWrapper {
    @Inject @Named("client")
    OkHttpClient mOkHttpClient;
    @Inject @Named("ws-client")
    OkHttpClient mWSOkHttpClient;

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }
    public OkHttpClient getWSOkHttpClient() {
        return mWSOkHttpClient;
    }

}