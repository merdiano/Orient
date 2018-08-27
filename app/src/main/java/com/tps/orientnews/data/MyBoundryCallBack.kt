package com.tps.orientnews.data

import android.arch.paging.PagedList
import android.arch.paging.PagingRequestHelper
import android.support.annotation.MainThread
import com.tps.orientnews.api.OrientNewsService
import com.tps.orientnews.api.ListingResponse
import com.tps.orientnews.room.Post
import com.tps.orientnews.utils.createStatusLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * Created by merdan on 8/10/18.
 */
class MyBoundryCallBack(
        private val source: Int,
        private var page: Int,
        private val webService: OrientNewsService,
        private val ioExecutor: Executor,
        private val handleResponse: (ListingResponse?) -> Unit)
    : PagedList.BoundaryCallback<Post>(){
    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()

    override fun onItemAtFrontLoaded(itemAtFront: Post) {
        // ignored, since we only ever append to what's in the DB
    }

    private fun insertItemsIntoDb(
            response: Response<ListingResponse>,
            it: PagingRequestHelper.Request.Callback){
        ioExecutor.execute {
            handleResponse(response.body())
            it.recordSuccess()
            page++
        }
    }

    @MainThread
    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL)
        {
            requestPosts(it);
        }
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: Post) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            requestPosts(it);
        }
    }
    private fun requestPosts(it:PagingRequestHelper.Request.Callback){
        if (source == -1)
            webService.getRecentPosts(page,20)
                    .enqueue(createWebserviceCallback(it))
        else
            webService.getCategoryPosts(page,source,20).enqueue(createWebserviceCallback(it))
    }
    private fun createWebserviceCallback(it: PagingRequestHelper.Request.Callback)
            : Callback<ListingResponse> {
        return object : Callback<ListingResponse> {
            override fun onFailure(
                    call: Call<ListingResponse>,
                    t: Throwable) {
                it.recordFailure(t)
            }

            override fun onResponse(
                    call: Call<ListingResponse>,
                    response: Response<ListingResponse>) {

                insertItemsIntoDb(response, it)
            }
        }
    }
}