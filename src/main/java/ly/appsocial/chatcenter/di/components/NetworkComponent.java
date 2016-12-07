package ly.appsocial.chatcenter.di.components;

import javax.inject.Singleton;

import dagger.Component;
import ly.appsocial.chatcenter.activity.BaseActivity;
import ly.appsocial.chatcenter.di.NetworkUtilitiesWrapper;
import ly.appsocial.chatcenter.di.modules.ContextModule;
import ly.appsocial.chatcenter.di.modules.NetworkModule;

@Singleton
@Component(modules={ContextModule.class, NetworkModule.class})
public interface NetworkComponent {
    void inject(BaseActivity activity);
    void inject(NetworkUtilitiesWrapper wrapper);
}