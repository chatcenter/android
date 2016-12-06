package ly.appsocial.chatcenter.di.modules;

import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class NetworkModule {

    private OkHttpClient mClient;
    private OkHttpClient mWSClient;

    public NetworkModule(OkHttpClient client) {
        new NetworkModule(client, client);
    }

    public NetworkModule(OkHttpClient client, OkHttpClient wsClient) {
        this.mClient = client;
        this.mWSClient = wsClient;
    }

    @Provides
    @Singleton
    Cache provideOkHttpCache(Context context) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(context.getCacheDir(), cacheSize);
        return cache;
    }

    @Provides @Named("client")
    @Singleton
    OkHttpClient provideOkHttpClient(Cache cache) {
        if (mClient == null) {
            mClient = new OkHttpClient();
        }
        mClient.setCache(cache);
        return mClient;
    }

    @Provides @Named("ws-client")
    @Singleton
    OkHttpClient provideWSOkHttpClient(Cache cache) {
        if (mWSClient == null) {
            mWSClient = new OkHttpClient();
        }
        mWSClient.setCache(cache);
        return mWSClient;
    }

}
