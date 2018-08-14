package com.tps.orientnews.injectors;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.tps.orientnews.OrientApplication;
import com.tps.orientnews.api.PushService;

import com.tps.orientnews.ui.MainActivity;
import com.tps.orientnews.ui.PostActivity;
import com.tps.orientnews.ui.SearchActivity;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import dagger.android.ContributesAndroidInjector;

import dagger.android.support.AndroidSupportInjectionModule;

import static com.tps.orientnews.OrientPrefs.ORIENT_PREF;

/**
 * Created by merdan on 7/12/18.
 */
@Module(includes = {AndroidSupportInjectionModule.class})
public abstract class OrientAppModule {

    @Binds
    abstract Application application(OrientApplication app);

    /**
     * Provides the injector for the {@link MainActivity}, which has access to the dependencies
     * provided by this application instance (singleton scoped objects).
     */
    @PerActivity
    @ContributesAndroidInjector(modules = {MainActivityModule.class})
    abstract MainActivity mainActivityInjector();

    @PerActivity
    @ContributesAndroidInjector(modules = PostActivityModule.class)
    abstract PostActivity postActivityInjector();

    @Provides
    static SharedPreferences providePreferences(Application context){
        return  context.getSharedPreferences(ORIENT_PREF, Context
                .MODE_PRIVATE);
    }

    @ContributesAndroidInjector
    abstract PushService contributeMyService();

    @PerActivity
    @ContributesAndroidInjector(modules = SearchActivityModule.class)
    abstract SearchActivity searchActivityInjector();
}

