package com.tps.orientnews.ui;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tps.orientnews.R;

/**
 * Created by merdan on 8/21/18.
 */

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener{
    public static final String KEY_NIGHT_MODE = "night_mode";
    public static final String KEY_PUSH = "push";
    public static final String KEY_FONT_SIZE = "font_size";
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.app_preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(KEY_NIGHT_MODE)){
            SwitchPreferenceCompat nightModPref = (SwitchPreferenceCompat)findPreference(key);

            AppCompatDelegate.setDefaultNightMode(nightModPref.isChecked()?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            getActivity().recreate();
        }else if(key.equals(KEY_PUSH)){
            SwitchPreferenceCompat pushPref = (SwitchPreferenceCompat)findPreference(key);
            if(pushPref.isChecked()){
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
            else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("news")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (!task.isSuccessful()) {
                            Log.d("Firebase", "UnSubscribe failed");
                        }
                        Log.d("Firebase", "UnSubscribed");
                    }

                });
            }
        }
    }
}
