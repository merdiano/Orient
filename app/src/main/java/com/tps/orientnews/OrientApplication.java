package com.tps.orientnews;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.support.v7.app.AppCompatDelegate;

import com.tps.orientnews.injectors.DaggerOrientAppComponent;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;

/**
 * Created by merdan on 7/12/18.
 */

public class OrientApplication extends Application implements HasActivityInjector,HasServiceInjector {
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;
    @Inject
    DispatchingAndroidInjector<Service> dispatchingServiceInjector;

    private static OrientApplication instance;
    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        DaggerOrientAppComponent.builder().create(this).inject(this);
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

