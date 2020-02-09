package com.tpsadvertising.orientnews.data;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

/**
 * Created by merdan on 8/9/18.
 */

public class Listing<T> {
    public LiveData<PagedList<T>> pagedList;
    public LiveData<NetworkState> networkState;
    public Function<Void,Void> retry;
    Listing(LiveData<PagedList<T>> pagedList,
            LiveData<NetworkState> networkState,
            Function<Void,Void> retry){
        this.networkState = networkState;
        this.pagedList = pagedList;
        this.retry = retry;
    }
}
