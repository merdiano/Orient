package com.tps.orientnews.injectors;

import android.support.v7.app.AppCompatActivity;

import com.tps.orientnews.ui.SearchActivity;

import dagger.Binds;
import dagger.Module;

/**
 * Created by merdan on 7/27/18.
 */
@Module(includes = {BaseActivityModule.class,NetworkModule.class,DatabaseModule.class})
public abstract class SearchActivityModule {
    @Binds
    abstract AppCompatActivity appCompatActivity(SearchActivity searchActivity);
}
