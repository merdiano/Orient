package com.tps.orientnews;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.tps.orientnews.injectors.DaggerOrientAppComponent;
import com.tps.orientnews.ui.SettingsFragment;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import io.fabric.sdk.android.Fabric;

/**
 * Created by merdan on 7/12/18.
 */

public class OrientApplication extends Application implements HasActivityInjector,HasServiceInjector {
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;
    @Inject
    DispatchingAndroidInjector<Service> dispatchingServiceInjector;
    @Inject @Named("defaultPrefs") SharedPreferences shPrefs;
    private static OrientApplication instance;
    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Fabric.with(this, new Crashlytics());
        PreferenceManager.setDefaultValues(this, R.xml.app_preferences, false);

//        SharedPreferences shPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        DaggerOrientAppComponent.builder().create(this).inject(this);


        if(shPrefs.contains(SettingsFragment.KEY_NIGHT_MODE))
        {
            boolean nightModeOn = shPrefs.getBoolean(SettingsFragment.KEY_NIGHT_MODE,false);
            AppCompatDelegate.setDefaultNightMode(nightModeOn?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        }

    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingServiceInjector;
    }

    public static OrientApplication getInstance(){return instance;}
}

