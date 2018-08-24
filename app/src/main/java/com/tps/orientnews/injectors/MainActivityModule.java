package com.tps.orientnews.injectors;

import android.app.Activity;
import android.arch.paging.PagedList;
import android.arch.paging.PagingRequestHelper;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.InternetObservingSettings;
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.strategy.SocketInternetObservingStrategy;

import com.tps.orientnews.room.Post;
import com.tps.orientnews.ui.MainActivity;
import com.tps.orientnews.ui.adapters.FeedAdapter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by merdan on 7/13/18.
 */
@Module(includes = {BaseActivityModule.class,NetworkModule.class})
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



    @PerActivity
    @Provides
    static RecyclerViewPreloader<Post> provideRecyclerViewPreloader(
            Activity activity,FeedAdapter adapter,ViewPreloadSizeProvider<Post> preloadSizeProvider)
    {

        return new RecyclerViewPreloader<>(activity,adapter,preloadSizeProvider,4);

    }
    @PerActivity
    @Provides
    static InternetObservingSettings provideInternetSettings(){
        return InternetObservingSettings
                .host("www.orient.tm")
                .strategy(new SocketInternetObservingStrategy())
                .build();
    }
    @PerActivity
    @Provides
    static Observable<Boolean> provideConnectionState(InternetObservingSettings settings){
        return ReactiveNetwork.observeInternetConnectivity(settings)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }




}
