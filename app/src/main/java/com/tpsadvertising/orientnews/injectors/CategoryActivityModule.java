package com.tpsadvertising.orientnews.injectors;

import android.app.Activity;
import androidx.paging.PagedList;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.InternetObservingSettings;
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.strategy.SocketInternetObservingStrategy;

import com.tpsadvertising.orientnews.room.Post;
import com.tpsadvertising.orientnews.ui.CategoryActivity;
import com.tpsadvertising.orientnews.ui.MainActivity;
import com.tpsadvertising.orientnews.ui.adapters.FeedAdapter;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by merdan on 7/13/18.
 */
@Module(includes = {BaseActivityModule.class})
public abstract  class CategoryActivityModule {
    /**
     * As per the contract specified in {@link BaseActivityModule}; "This must be included in all
     * activity modules, which must provide a concrete implementation of {@link AppCompatActivity}."
     * <p>
     * This provides the activity required to inject the
     *
     * @param categoryActivity the activity
     * @return the activity
     */
    @Binds
    abstract AppCompatActivity appCompatActivity(CategoryActivity categoryActivity);

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
