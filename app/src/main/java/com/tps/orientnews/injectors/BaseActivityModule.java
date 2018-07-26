package com.tps.orientnews.injectors;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.tps.orientnews.DataManager;
import com.tps.orientnews.api.OrientNewsService;
import com.tps.orientnews.models.DaoSession;
import com.tps.orientnews.ui.adapters.FilterAdapter;

import dagger.Binds;
import dagger.Module;

/**
 * Created by merdan on 7/13/18.
 */
@Module
public abstract class BaseActivityModule {
    @Binds
    abstract Activity activity(AppCompatActivity appCompatActivity);

    @Binds
    abstract Context activityContext(Activity activity);


}
