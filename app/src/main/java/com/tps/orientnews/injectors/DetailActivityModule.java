package com.tps.orientnews.injectors;

import android.support.v7.app.AppCompatActivity;

import com.tps.orientnews.ui.DetailActivity;

import dagger.Binds;
import dagger.Module;

/**
 * Created by merdan on 8/22/18.
 */
@Module(includes = {BaseActivityModule.class})
public abstract class DetailActivityModule {
    @Binds
    abstract AppCompatActivity appCompatActivity(DetailActivity postActivity);
}
