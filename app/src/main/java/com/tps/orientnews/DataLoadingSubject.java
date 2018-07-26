package com.tps.orientnews;

import com.tps.orientnews.models.Category;
import com.tps.orientnews.models.OrientPost;

import java.util.List;

/**
 * Created by merdan on 7/15/18.
 */

public interface DataLoadingSubject {
    boolean isDataLoading();
    void registerPostsCallback(PostLoadingCallbacks callbacks);
    void unregisterPostsCallback(PostLoadingCallbacks callbacks);
    void registerDataCallback(DataLoadingCallbacks callbacks);
    void unregisterDataCallback(DataLoadingCallbacks callbacks);

    interface PostLoadingCallbacks {
        void dataStartedLoading();
        void dataFinishedLoading();
    }

    interface DataLoadingCallbacks{
        void onCategoriesLoaded(List<Category> categories);
        void onPostsLoaded(List<OrientPost> posts);
    }
}
