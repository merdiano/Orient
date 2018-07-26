package com.tps.orientnews.injectors;

import com.tps.orientnews.api.PushService;
import com.tps.orientnews.ui.MainActivity;
import com.tps.orientnews.OrientApplication;
import com.tps.orientnews.ui.adapters.FeedAdapter;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

/**
 * Created by merdan on 7/12/18.
 */
@Singleton
@Component(modules = {OrientAppModule.class,DatabaseModule.class,NetworkModule.class})
public interface OrientAppComponent extends AndroidInjector<OrientApplication>{

//    void inject (OrientApplication app);
//    void inject(PushService pushService);
    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<OrientApplication> {
    }
}
