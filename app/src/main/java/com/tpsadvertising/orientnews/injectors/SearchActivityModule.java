package com.tpsadvertising.orientnews.injectors;

import androidx.appcompat.app.AppCompatActivity;

import com.tpsadvertising.orientnews.ui.SearchActivity;

import dagger.Binds;
import dagger.Module;

/**
 * Created by merdan on 7/27/18.
 */
@Module(includes = {BaseActivityModule.class,NetworkModule.class})
public abstract class SearchActivityModule {
    @Binds
    abstract AppCompatActivity appCompatActivity(SearchActivity searchActivity);
}
