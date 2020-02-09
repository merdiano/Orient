package com.tpsadvertising.orientnews.injectors;

import android.arch.paging.PagingRequestHelper;

import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import com.tpsadvertising.orientnews.viewmodels.DetailActivityViewModel;
import com.tpsadvertising.orientnews.viewmodels.MainActivityViewModel;
import com.tpsadvertising.orientnews.viewmodels.SearchViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import java.util.concurrent.Executor;

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
    abstract ViewModel mainActivityViewModel(MainActivityViewModel mainViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DetailActivityViewModel.class)
    abstract ViewModel detailActivityViewModel(DetailActivityViewModel detailViewModel);



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
