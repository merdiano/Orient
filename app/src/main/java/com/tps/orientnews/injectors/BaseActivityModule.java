package com.tps.orientnews.injectors;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.util.ViewPreloadSizeProvider;

import com.tps.orientnews.room.Post;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by merdan on 7/13/18.
 */
@Module
public abstract class BaseActivityModule {
    @Binds
    abstract Activity activity(AppCompatActivity appCompatActivity);

    @Binds
    abstract Context activityContext(Activity activity);

    @Provides
    static ViewPreloadSizeProvider<Post> viewPreloadSizeProvider(){
        return  new ViewPreloadSizeProvider<>();
    }
}
