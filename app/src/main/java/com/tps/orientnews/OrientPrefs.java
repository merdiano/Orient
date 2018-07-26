package com.tps.orientnews;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * Created by merdan on 6/9/18.
 * Storing Orient user prefs
 */
@Singleton
public class OrientPrefs {
    public static final String ORIENT_PREF = "ORIENT_PREF";
    private static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";
    private static final String KEY_USER_ID = "KEY_USER_ID";
    private static final String KEY_USER_NAME = "KEY_USER_NAME";
    private static final String KEY_NIGHT_MODE = "KEY_NIGHT_MODE";
    private static final String KEY_FONT_SIZE = "KEY_FONT_SIZE";
//    private static final String KEY_USER_USERNAME = "KEY_USER_USERNAME";
//    private static final String KEY_USER_AVATAR = "KEY_USER_AVATAR";
    private static volatile OrientPrefs singleton;
    private final SharedPreferences prefs;
    private String accessToken;
    private boolean isLoggedIn = false;
    private long userId;
    private String userName;
    private List<OrientLoginStatusListener> loginStatusListeners;
    public interface OrientLoginStatusListener{
        void onOrientLogin();
        void onOrientLogout();
    }
    @Inject
    OrientPrefs(SharedPreferences prefs) {
        //todo move injection to dagger module
        this.prefs = prefs;
        accessToken = prefs.getString(KEY_ACCESS_TOKEN, null);
        isLoggedIn = !TextUtils.isEmpty(accessToken);
        if (isLoggedIn) {
            userId = prefs.getLong(KEY_USER_ID, 0l);
            userName = prefs.getString(KEY_USER_NAME, null);
//            userUsername = prefs.getString(KEY_USER_USERNAME, null);
//            userAvatar = prefs.getString(KEY_USER_AVATAR, null);
//            userType = prefs.getString(KEY_USER_TYPE, null);
        }
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    public void setAccessToken(String accessToken) {
        if (!TextUtils.isEmpty(accessToken)) {
            this.accessToken = accessToken;
            isLoggedIn = true;
            prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply();
            dispatchLoginEvent();
        }
    }


    public int getFontSizePref(){
        return prefs.getInt(KEY_FONT_SIZE,100);
    }
    public void setFontSizePref(int size){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_FONT_SIZE,size);
        editor.apply();
    }
    public boolean getNightModePref(){
        return  prefs.getBoolean(KEY_NIGHT_MODE,false);
    }
    public void setNightModePref(boolean checked){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_NIGHT_MODE, checked);
        editor.apply();
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }


    public void logout() {
        isLoggedIn = false;
        accessToken = null;
        userId = 0l;
        userName = null;
//        userUsername = null;
//        userAvatar = null;
//        userType = null;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACCESS_TOKEN, null);
        editor.putLong(KEY_USER_ID, 0l);
        editor.putString(KEY_USER_NAME, null);
//        editor.putString(KEY_USER_AVATAR, null);
//        editor.putString(KEY_USER_TYPE, null);
        editor.apply();

        dispatchLogoutEvent();
    }

    public void addLoginStatusListener(OrientLoginStatusListener listener) {
        if (loginStatusListeners == null) {
            loginStatusListeners = new ArrayList<>();
        }
        loginStatusListeners.add(listener);
    }

    public void removeLoginStatusListener(OrientLoginStatusListener listener) {
        if (loginStatusListeners != null) {
            loginStatusListeners.remove(listener);
        }
    }
    private void dispatchLoginEvent() {
        if (loginStatusListeners != null && !loginStatusListeners.isEmpty()) {
            for (OrientLoginStatusListener listener: loginStatusListeners) {
                listener.onOrientLogin();
            }
        }
    }

    private void dispatchLogoutEvent() {
        if (loginStatusListeners != null && !loginStatusListeners.isEmpty()) {
            for (OrientLoginStatusListener listener : loginStatusListeners) {
                listener.onOrientLogout();
            }
        }
    }
}
