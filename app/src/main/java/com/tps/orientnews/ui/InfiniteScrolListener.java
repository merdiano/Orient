package com.tps.orientnews.ui;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tps.orientnews.DataLoadingSubject;

/**
 * Created by merdan on 7/16/18.
 */

public abstract class InfiniteScrolListener extends RecyclerView.OnScrollListener{
    private static final int VISIBLE_THRESHOLD = 5;
    LinearLayoutManager linearLayoutManager;
    DataLoadingSubject dataLoadingSubject;
    InfiniteScrolListener(LinearLayoutManager layoutManager, DataLoadingSubject subject){
        this.linearLayoutManager = layoutManager;
        this.dataLoadingSubject = subject;
    }
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

        if(dy<0||dataLoadingSubject.isDataLoading())return;
        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = linearLayoutManager.getItemCount();
        int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
        if (totalItemCount - visibleItemCount <= firstVisibleItem + VISIBLE_THRESHOLD) {
            recyclerView.post(loadMoreRunnable);
        }
    }

    private Runnable loadMoreRunnable = new Runnable() {
        @Override
        public void run() {
            onLoadMore();
        }
    };
    abstract void onLoadMore();
}
