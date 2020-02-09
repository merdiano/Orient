package com.tpsadvertising.orientnews.data;

import android.arch.paging.PagingRequestHelper;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import androidx.annotation.NonNull;

import com.tpsadvertising.orientnews.api.OrientNewsService;
import com.tpsadvertising.orientnews.api.ListingResponse;
import com.tpsadvertising.orientnews.room.Post;


import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by merdan on 8/9/18.
 */

public class PostBoundryCallback extends PagedList.BoundaryCallback<Post> {
    public int source = -1;
    public int page = 1;
    private OrientNewsService service;
    private PagingRequestHelper helper;
    LiveData<NetworkState> networkState;
    @Inject
    PostBoundryCallback(PagingRequestHelper helper, OrientNewsService service){
       this.helper = helper;
       this.service = service;
//       networkState = helper.createStatusLiveData();
    }

    @Override
    public void onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL,callback -> {
            if(source == -1)
                service.getRecentPosts(page).enqueue(createWebserviceCallback(callback));
            else
                service.getCategoryPosts(page,source).enqueue(createWebserviceCallback(callback));
        });
    }

    @Override
    public void onItemAtEndLoaded(@NonNull Post itemAtEnd) {

        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER,callback -> {
            if(source == -1)
                service.getRecentPosts(page).enqueue(createWebserviceCallback(callback));
            else
                service.getCategoryPosts(page,source).enqueue(createWebserviceCallback(callback));
        });
    }

    private void insertIntodb(Response<ListingResponse> response, PagingRequestHelper.Request.Callback it){

    }

    private Callback<ListingResponse> createWebserviceCallback(PagingRequestHelper.Request.Callback it){
        return new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
//                response.body().count
                insertIntodb(response,it);
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) {
                it.recordFailure(t);
            }
        };
    }
}
