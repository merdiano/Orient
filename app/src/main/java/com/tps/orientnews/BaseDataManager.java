package com.tps.orientnews;

import com.tps.orientnews.models.Category;
import com.tps.orientnews.models.OrientPost;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by merdan on 7/15/18.
 */

public abstract class BaseDataManager implements DataLoadingSubject{
    private final AtomicInteger loadingCount;
    private List<PostLoadingCallbacks> loadingCallbacks;
    private DataLoadingCallbacks dataLoadingCallbacks;
    BaseDataManager(){
        loadingCount = new AtomicInteger(0);
    }
    @Override
    public boolean isDataLoading() {
        return loadingCount.get() > 0;
    }

    public void loadCatsFinished(List<Category> cats){
        if(dataLoadingCallbacks!= null)
            dataLoadingCallbacks.onCategoriesLoaded(cats);
    }
    public void loadPostsFinished(List<OrientPost> posts){
        if(dataLoadingCallbacks!= null && !posts.isEmpty())
            dataLoadingCallbacks.onPostsLoaded(posts);
    }
    @Override
    public void registerDataCallback(DataLoadingCallbacks callbacks) {
        this.dataLoadingCallbacks = callbacks;
    }

    @Override
    public void unregisterDataCallback(DataLoadingCallbacks callbacks) {
        this.dataLoadingCallbacks = null;
    }
    public abstract void cancelLoading();
    @Override
    public void registerPostsCallback(PostLoadingCallbacks callback) {
        if (loadingCallbacks == null) {
            loadingCallbacks = new ArrayList<>(1);
        }
        loadingCallbacks.add(callback);
    }

    @Override
    public void unregisterPostsCallback(PostLoadingCallbacks callback) {
        if (loadingCallbacks != null && loadingCallbacks.contains(callback)) {
            loadingCallbacks.remove(callback);
        }
    }
    protected void loadStarted() {
        if (0 == loadingCount.getAndIncrement()) {
            dispatchLoadingStartedCallbacks();
        }
    }

    protected void loadFinished() {
        if (0 == loadingCount.decrementAndGet()) {
            dispatchLoadingFinishedCallbacks();
        }
    }
    protected void dispatchLoadingStartedCallbacks() {
        if (loadingCallbacks == null || loadingCallbacks.isEmpty()) return;
        for (PostLoadingCallbacks loadingCallback : loadingCallbacks) {
            loadingCallback.dataStartedLoading();
        }
    }

    protected void dispatchLoadingFinishedCallbacks() {
        if (loadingCallbacks == null || loadingCallbacks.isEmpty()) return;
        for (PostLoadingCallbacks loadingCallback : loadingCallbacks) {
            loadingCallback.dataFinishedLoading();
        }
    }

    protected void resetLoadingCount() {
        loadingCount.set(0);
    }


}
