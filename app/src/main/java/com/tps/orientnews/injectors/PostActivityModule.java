package com.tps.orientnews.injectors;

import android.support.v7.app.AppCompatActivity;

import com.tps.orientnews.ui.MainActivity;
import com.tps.orientnews.ui.PostActivity;

import dagger.Binds;
import dagger.Module;

/**
 * Created by merdan on 7/19/18.
 */
@Module(includes = {BaseActivityModule.class,NetworkModule.class,DatabaseModule.class})
public abstract  class PostActivityModule  {
    @Binds
    abstract AppCompatActivity appCompatActivity(PostActivity postActivity);
}
