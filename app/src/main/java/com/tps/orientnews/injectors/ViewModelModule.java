package com.tps.orientnews.injectors;

import android.arch.lifecycle.ViewModel;
import android.arch.paging.PagedList;
import android.arch.paging.PagingRequestHelper;

import com.tps.orientnews.viewmodels.MainActivityViewModel;
import com.tps.orientnews.viewmodels.MainViewModel;
import com.tps.orientnews.viewmodels.SearchViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by merdan on 8/2/18.
 */
@Module
public abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel.class)
    abstract ViewModel mainViewModel(SearchViewModel searchViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel.class)
    abstract ViewModel mainActivityViewModel(MainActivityViewModel userViewModel);


    @Provides
    static Executor provideExecuter(){
        return Executors.newSingleThreadExecutor();
    }

    @Provides
    static PagedList.Config providePagedListConfig(){
        return new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(20)
                .setPageSize(10)
                .setPrefetchDistance(3)
                .build();
    }

    @Provides
    static PagingRequestHelper provideRequestHelper(Executor ioExecutor){
        return new PagingRequestHelper(ioExecutor);
    }
}
