package com.tpsadvertising.orientnews.ui;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.tpsadvertising.orientnews.R;
import com.tpsadvertising.orientnews.data.PostRepository;
import com.tpsadvertising.orientnews.room.AppDatabase;

import java.util.Locale;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

/**
 * Created by merdan on 8/21/18.
 */

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener{
    public static final String KEY_NIGHT_MODE = "night_mode";
    public static final String KEY_PUSH = "push";
    public static final String KEY_FONT_SIZE = "font_size";
    public static final String KEY_LANG = "language";
    public static final String KEY_VERSION = "version";
    public static final String KEY_PRIVACY = "privacy";
    public static final String PRIVACY_URL = "http://tpsadvertising.com/privacy_policy/orient.html";
//    public static final int RESULT_LANG_CHANGED = 55;
//    public static final int RESULT_NIGHTMODE_CHANGED = 44;

    @Inject
    Executor executor;
    @Inject
    AppDatabase database;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.app_preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        PreferenceScreen versionPref = (PreferenceScreen) findPreference(KEY_VERSION);
        PreferenceScreen privacyPref = (PreferenceScreen) findPreference(KEY_PRIVACY);

        privacyPref.setOnPreferenceClickListener(preference -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(PRIVACY_URL));
            try {
                startActivity(i);
            }catch(Exception ex){
                Log.e("Pref_Fragment","Error in starting activity with browser");
            }
            return true;
        });

        try {
            versionPref.setSummary(appVersion());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ListPreference langPref = (ListPreference)findPreference(KEY_LANG);
        if(langPref.getValue()!=null && langPref.getValue().equals("ru"))
            langPref.setSummary(getResources().getString(R.string.lang_ru));
        else
            langPref.setSummary(getResources().getString(R.string.lang_en));
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
            //getActivity().setResult(RESULT_NIGHTMODE_CHANGED);
        }
        else if(key.equals(KEY_LANG)){

            ListPreference langPref = (ListPreference)findPreference(key);
            String lang = langPref.getValue();
            Configuration cfg = new Configuration();
            cfg.locale = new Locale(lang);
            this.getResources().updateConfiguration(cfg,null);
            clearDatabase();

            getActivity().recreate();
            getActivity().setResult(Activity.RESULT_OK,getActivity().getIntent());// set ok to trigger mainActivity restart
        }

    }
    private void clearDatabase(){
        executor.execute(()->{
            try {
                database.clearAllTables();
            }
            catch (Exception ex){
//                Log.d("PostRepository update",ex.getLocalizedMessage());
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public String appVersion() throws PackageManager.NameNotFoundException {
        PackageInfo pInfo = getActivity().getPackageManager()
                .getPackageInfo(getActivity().getPackageName(), 0);
        String version = pInfo.versionName;
        return version;
    }
}
