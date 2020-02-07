package com.tpsadvertising.orientnews.injectors;

import com.tpsadvertising.orientnews.api.PushService;
import com.tpsadvertising.orientnews.ui.MainActivity;
import com.tpsadvertising.orientnews.OrientApplication;
import com.tpsadvertising.orientnews.ui.adapters.FeedAdapter;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

/**
 * Created by merdan on 7/12/18.
 */
@Singleton
@Component(modules = {OrientAppModule.class,ViewModelModule.class})
public interface OrientAppComponent extends AndroidInjector<OrientApplication>{

//    void inject (OrientApplication app);
//    void inject(PushService pushService);
    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<OrientApplication> {
    }
}
