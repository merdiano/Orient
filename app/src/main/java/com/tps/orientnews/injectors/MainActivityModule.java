package com.tps.orientnews.injectors;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.tps.orientnews.DataManager;
import com.tps.orientnews.OrientApplication;
import com.tps.orientnews.api.OrientNewsService;
import com.tps.orientnews.models.DaoMaster;
import com.tps.orientnews.models.DaoSession;
import com.tps.orientnews.models.OrientPost;
import com.tps.orientnews.ui.MainActivity;
import com.tps.orientnews.ui.adapters.FeedAdapter;
import com.tps.orientnews.ui.adapters.FilterAdapter;

import org.greenrobot.greendao.database.Database;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;
import retrofit2.Retrofit;

/**
 * Created by merdan on 7/13/18.
 */
@Module(includes = {BaseActivityModule.class,NetworkModule.class,DatabaseModule.class})
public abstract  class MainActivityModule {
    /**
     * As per the contract specified in {@link BaseActivityModule}; "This must be included in all
     * activity modules, which must provide a concrete implementation of {@link AppCompatActivity}."
     * <p>
     * This provides the activity required to inject the
     *
     * @param mainActivity the activity
     * @return the activity
     */
    @Binds
    abstract AppCompatActivity appCompatActivity(MainActivity mainActivity);

//    @Binds
//    @PerActivity
//    abstract DataManager dataManager(DataManager dataManager);

//    @Binds
//    @PerActivity
//    abstract FilterAdapter filterAdapter(FilterAdapter filterAdapter);

//    @Binds
//    @PerActivity
//    abstract FeedAdapter feedAdapter(FeedAdapter feedAdapter);


    @Provides
    static ViewPreloadSizeProvider<OrientPost> viewPreloadSizeProvider(){
        return  new ViewPreloadSizeProvider<>();
    }

    @Provides
    static RecyclerViewPreloader<OrientPost> provideRecyclerViewPreloader(
            Activity activity,FeedAdapter adapter,ViewPreloadSizeProvider<OrientPost> preloadSizeProvider)
    {

        return new RecyclerViewPreloader<>(activity,adapter,preloadSizeProvider,4);

    }
}
