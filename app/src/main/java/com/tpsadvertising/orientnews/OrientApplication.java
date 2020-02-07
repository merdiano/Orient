package com.tpsadvertising.orientnews;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

//import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tpsadvertising.orientnews.injectors.DaggerOrientAppComponent;
import com.tpsadvertising.orientnews.ui.SettingsFragment;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import dagger.android.support.HasSupportFragmentInjector;
//import io.fabric.sdk.android.Fabric;

/**
 * Created by merdan on 7/12/18.
 */

public class OrientApplication extends Application implements HasActivityInjector,
        HasServiceInjector,HasSupportFragmentInjector {
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;
    @Inject
    DispatchingAndroidInjector<Service> dispatchingServiceInjector;
    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingFragmentInjector;
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
//        final Fabric fabric = new Fabric.Builder(this)
//                .kits(new Crashlytics())
//                .debuggable(true)           // Enables Crashlytics debugger todo release dont forget
//                .build();
//        Fabric.with(fabric);
//        Crashlytics.getInstance().crash();
        PreferenceManager.setDefaultValues(this, R.xml.app_preferences, false);
        FirebaseApp.initializeApp(getApplicationContext());
//        SharedPreferences shPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        DaggerOrientAppComponent.builder().create(this).inject(this);


        if(shPrefs.contains(SettingsFragment.KEY_NIGHT_MODE))
        {
            boolean nightModeOn = shPrefs.getBoolean(SettingsFragment.KEY_NIGHT_MODE,false);
            AppCompatDelegate.setDefaultNightMode(nightModeOn?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        }

        if(shPrefs.contains(SettingsFragment.KEY_PUSH)){
            boolean pushMode = shPrefs.getBoolean(SettingsFragment.KEY_PUSH,true);
            if(pushMode)subscribeToSource();
        }
        String lang = "ru";
        if(shPrefs.contains(SettingsFragment.KEY_LANG)){
            lang = shPrefs.getString(SettingsFragment.KEY_LANG,
                    Locale.getDefault().getLanguage());

        }
        Configuration cfg = new Configuration();
        cfg.locale = new Locale(lang);
        this.getResources().updateConfiguration(cfg,null);

    }

    private void subscribeToSource(){
        FirebaseMessaging.getInstance().subscribeToTopic("news")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (!task.isSuccessful()) {
                            Log.d("Firebase", "Subscribe failed");
                        }
                        Log.d("Firebase", "Subscribed");
                    }

                });
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

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingFragmentInjector;
    }
}

