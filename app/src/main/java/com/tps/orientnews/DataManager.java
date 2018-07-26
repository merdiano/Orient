package com.tps.orientnews;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tps.orientnews.api.CategoriesWrapper;
import com.tps.orientnews.api.OrientNewsService;
import com.tps.orientnews.api.PostsWrapper;
import com.tps.orientnews.injectors.PerActivity;
import com.tps.orientnews.models.Assets;
import com.tps.orientnews.models.Category;
import com.tps.orientnews.models.CategoryDao;
import com.tps.orientnews.models.DaoSession;
import com.tps.orientnews.models.OrientPost;
import com.tps.orientnews.models.OrientPostDao;
import com.tps.orientnews.ui.MainActivity;
import com.tps.orientnews.ui.adapters.FeedAdapter;
import com.tps.orientnews.ui.adapters.FilterAdapter;

import org.greenrobot.greendao.DaoException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by merdan on 7/14/18.
 */
@Singleton
public class DataManager extends BaseDataManager{

    DaoSession repository;

    OrientNewsService service;
    private Map<Long, Integer> pageIndexes;
    private Map<Long, Call> inflight;
    private static final int POSTS_LIMIT = 5;

    @Inject
    DataManager(OrientNewsService service, DaoSession daoSession){
        this.service = service;
        this.repository = daoSession;
        inflight = new HashMap<>();
    }

    @Override
    public void cancelLoading() {
        if (inflight.size() > 0) {
            for (Call call : inflight.values()) {
                call.cancel();
            }
            inflight.clear();
        }
    }

    public void loadCategories(){
//        loadStarted();
        List cats = repository.getCategoryDao().loadAll();

        if(cats == null || cats.size()==0){
            loadCatsFromNetwork();
        }
        else
        {
            loadCatsFinished(cats);
//            loadFinished();
        }

    }

    public OrientPost getPost(long post_id){
        return repository.getOrientPostDao().loadDeep(post_id);
    }

    private void loadCatsFromNetwork(){
        service.getCategories().enqueue(new Callback<CategoriesWrapper>() {
            @Override
            public void onResponse(Call<CategoriesWrapper> call, Response<CategoriesWrapper> response) {
                if(response.isSuccessful() && response.body()!=null){
                    List<Category> cats = response.body().categories;

                    try {
                        repository.getCategoryDao().insertInTx(cats);
                        loadCatsFinished(cats);
                        for(Category category : cats){
                            subscribeToSource(category);
                        }

                    }
                    catch (DaoException ex){
                        Log.d("categories insert",ex.getLocalizedMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<CategoriesWrapper> call, Throwable t) {
                //todo what if categories fales to load
                Log.d("Datamanager","load categories failed");
            }
        });
    }

    private void loadPostsfromNetwork(Category category){
        int lastPage = category.getLastDownloadedPage();
        if(lastPage!=0 && lastPage == category.getPages())
        {
            loadFailed(category.getId());
            return;
        }
        int page = lastPage+1;
        Call<PostsWrapper> postCall = service.getCategoryPosts(page,category.getId());
        postCall.enqueue(new Callback<PostsWrapper>() {
                @Override
                public void onResponse(Call<PostsWrapper> call, Response<PostsWrapper> response) {
                    if(response.isSuccessful() && response.body() !=null){
                        category.setLastDownloadedPage(page);
                        category.setPages(response.body().pages);
                        postsLoaded(response.body(),category);
                    }
                    else {
                        loadFailed(category.getId());
                    }
                }

                @Override
                public void onFailure(Call<PostsWrapper> call, Throwable t) {
                    loadFailed(category.getId());
                }
            });
        inflight.put(category.getId(), postCall);
    }

    private void postsLoaded(final PostsWrapper postsWrapper,final Category cat){

        inflight.remove(cat.getId());
        if(postsWrapper.posts == null ||postsWrapper.posts.isEmpty()){
            repository.update(cat);
            loadFailed(cat.getId());
            return;
        }

        List<OrientPost> loadedPosts = postsWrapper.posts;
        List<OrientPost> savedPosts = new ArrayList<>();
        for (OrientPost post : loadedPosts){
            if(post.getTitle().isEmpty())
                continue;
            OrientPost orientPost = repository.getOrientPostDao().loadDeep(post.getId());
            if(orientPost!=null)
                continue;

            try {

                repository.insert(post.thumbnail_images.large);
                repository.insert(post.thumbnail_images.medium);
                repository.insert(post.thumbnail_images.thumbnail);
            }
            catch (NullPointerException ex){
                Log.i("post : "+post.getId(),"images does not exists");
                continue;//skip it if no image;
            }
            catch (SQLiteConstraintException ex){
                Log.i("post : "+post.getId(),"insert images not successfull : "+ex.getLocalizedMessage());
            }

            try {
                repository.insert(post.author);
            }
            catch (SQLiteConstraintException ex){
                Log.d("author : "+post.author.getId(),"author exists "+ex.getLocalizedMessage());
            }
            try {
                post.thumbnail_images.setLargeImageId(post.thumbnail_images.large.getUrl());
                post.thumbnail_images.setMediumImageId(post.thumbnail_images.medium.getUrl());
                post.thumbnail_images.setThumbnailId(post.thumbnail_images.thumbnail.getUrl());
                repository.insert(post.thumbnail_images);
                post.setAuther_id(post.author.getId());
                post.setAssets_id(post.thumbnail_images.getId());
                post.setCategoryId(cat.getId());
                repository.insert(post);
                repository.update(cat);
                savedPosts.add(post);
            }
            catch (Exception ex){
                Log.i("post : "+post.getId(),"saving post : "+ex.getLocalizedMessage());
            }
        }
        if(sourceIsEnabled(cat.getId()))
            loadPostsFinished(savedPosts);
        loadFinished();
    }

    private void loadFailed(long key){
        loadFinished();
        inflight.remove(key);
    }

    public List<Category> getActiveCategories(){
       // repository.getCategoryDao().detachAll();
        return repository.getCategoryDao()
                .queryBuilder()
                .where(CategoryDao.Properties.Active.notEq(0))
                .list();
    }
    public void updateCategory(Category cat){
        repository.update(cat);
    }


    public void setupPageIndexes() {
        final List<Category> dateSources = getActiveCategories();
        pageIndexes = new HashMap<>(dateSources.size());
        for (Category source : dateSources) {
            pageIndexes.put(source.getId(), 0);
        }
    }

    private int getNextPageIndex(Long dataSource) {
        int nextPage = 1; // default to one – i.e. for newly added sources
        if (pageIndexes.containsKey(dataSource)) {
            nextPage = pageIndexes.get(dataSource) + 1;
        }
        pageIndexes.put(dataSource, nextPage);
        return nextPage;
    }

    private boolean sourceIsEnabled(Long key) {
        return pageIndexes.get(key) != 0;
    }

    public int getEnabledSourcesCount() {
        List cats = getActiveCategories();
        return cats!=null?cats.size():0;
    }

    public void loadAllDataSources() {
        List<Category> categories = getActiveCategories();
        for(Category category:categories){
            if(category.getActive()){
                loadStarted();
                loadSource(category);
            }
        }
    }

    private void loadSource(Category category){
        int page = getNextPageIndex(category.getId());
        int offset = (page-1)*POSTS_LIMIT;
        List<OrientPost> posts = repository.getOrientPostDao().queryBuilder()
                .where(OrientPostDao.Properties.CategoryId.eq(category.getId()))
                .offset(offset)
                .limit(POSTS_LIMIT)
                .list();
        if (posts!=null && !posts.isEmpty()){
            loadPostsFinished(posts);
            loadFinished();
        }
        else
            loadPostsfromNetwork(category);
    }

    public void onFilterChanged(Category changedFilter){
        if (changedFilter.active) {
            loadSource(changedFilter);
            subscribeToSource(changedFilter);
        } else { // filter deactivated
            final long key = changedFilter.getId();
            if (inflight.containsKey(key)) {
                final Call call = inflight.get(key);
                if (call != null) call.cancel();
                inflight.remove(key);
            }
            // clear the page index for the source
            pageIndexes.put(key, 0);
            unsubscribeToSource(changedFilter);
        }
    }
    private void subscribeToSource(Category source){
        FirebaseMessaging.getInstance().subscribeToTopic("cat"+source.getId())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (!task.isSuccessful()) {
                            Log.d("Firebase", "Subscribe failed");
                        }
                        Log.d("Firebase", "Subscribed");
                    }

                });
    }

    private void unsubscribeToSource(Category source){
        FirebaseMessaging.getInstance().unsubscribeFromTopic("cat"+source.getId())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (!task.isSuccessful()) {
                            Log.d("Firebase", "UnSubscribe failed");
                        }
                        Log.d("Firebase", "UnSubscribed");
                    }
                });
    }

}
