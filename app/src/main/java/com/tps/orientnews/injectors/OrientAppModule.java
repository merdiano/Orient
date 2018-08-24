package com.tps.orientnews.injectors;

import android.app.Activity;
import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.tps.orientnews.OrientApplication;
import com.tps.orientnews.api.PushService;

import com.tps.orientnews.room.AppDatabase;
import com.tps.orientnews.room.PostDao;
import com.tps.orientnews.ui.DetailActivity;
import com.tps.orientnews.ui.MainActivity;
import com.tps.orientnews.ui.PostActivity;
import com.tps.orientnews.ui.SearchActivity;

import javax.inject.Named;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import dagger.android.ContributesAndroidInjector;

import dagger.android.support.AndroidSupportInjectionModule;

import static com.tps.orientnews.OrientPrefs.ORIENT_PREF;

/**
 * Created by merdan on 7/12/18.
 */
@Module(includes = {AndroidSupportInjectionModule.class})
public abstract class OrientAppModule {

    @Binds
    abstract Application application(OrientApplication app);

    /**
     * Provides the injector for the {@link MainActivity}, which has access to the dependencies
     * provided by this application instance (singleton scoped objects).
     */
    @PerActivity
    @ContributesAndroidInjector(modules = {MainActivityModule.class})
    abstract MainActivity mainActivityInjector();

    @PerActivity
    @ContributesAndroidInjector(modules = PostActivityModule.class)
    abstract PostActivity postActivityInjector();

    @PerActivity
    @ContributesAndroidInjector(modules = DetailActivityModule.class)
    abstract DetailActivity detailActivityInjector();

    @Provides
    static SharedPreferences providePreferences(Application context){
        return  context.getSharedPreferences(ORIENT_PREF, Context
                .MODE_PRIVATE);
    }

    @ContributesAndroidInjector
    abstract PushService contributeMyService();

    @PerActivity
    @ContributesAndroidInjector(modules = SearchActivityModule.class)
    abstract SearchActivity searchActivityInjector();

    @Provides
    static AppDatabase provideDatabase(OrientApplication app){
        return Room.databaseBuilder(app,AppDatabase.class,"orient_db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

    }

    @Provides
    static com.tps.orientnews.room.CategoryDao provideRoomCategoryDao(AppDatabase database){
        return database.categoryDao();
    }

    @Provides
    static PostDao providePostDao(AppDatabase database){
        return database.postDao();
    }

    @Provides @Named("defaultPrefs")
    static SharedPreferences provideDefaultPreferences(Application context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}

