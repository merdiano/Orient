package com.tps.orientnews.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.database.sqlite.SQLiteConstraintException;
import android.support.annotation.MainThread;
import android.util.Log;

import com.tps.orientnews.api.OrientNewsService;
import com.tps.orientnews.api.ListingResponse;
import com.tps.orientnews.api.PostResponse;
import com.tps.orientnews.room.Post;
import com.tps.orientnews.room.PostDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

import kotlin.Unit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by merdan on 8/2/18.
 */
@Singleton
public class PostRepository {
    private final OrientNewsService webService;
    private final PostDao postDao;
    private final Executor executor;
    private static final int POSTS_PAGE_SIZE = 20;
    private PagedList.Config config;
    @Inject
    PostRepository(OrientNewsService service,
                   PostDao postDao,
                   Executor executor,
                   PagedList.Config config){
        this.postDao = postDao;
        this.webService = service;
        this.executor = executor;
        this.config = config;
    }

    private Unit insertToDb(ListingResponse response){
        if(response == null||response.posts==null){
            return null;
        }
        List<Post> newPosts = new ArrayList<>();
        for (Post post : response.posts){
            if(post.title.isEmpty())
                continue;
            try {
                post.thumbnail_images.largeUrl = post.thumbnail_images.large.url;
                post.thumbnail_images.mediumUrl = post.thumbnail_images.medium.url;
                post.thumbnail_images.thumbnailUrl = post.thumbnail_images.thumbnail.url;

                post.thumbnail_images.largeWidh = post.thumbnail_images.large.width;
                post.thumbnail_images.mediumWidh = post.thumbnail_images.medium.width;
                post.thumbnail_images.thumbnailWidh = post.thumbnail_images.thumbnail.width;

                post.thumbnail_images.largeHeght = post.thumbnail_images.large.height;
                post.thumbnail_images.mediumHeght = post.thumbnail_images.medium.height;
                post.thumbnail_images.thumbnailHeght = post.thumbnail_images.thumbnail.height;

            }
            catch (NullPointerException ex){
                Log.i("post : "+post.id,"images does not exists");
                continue;//skip it if no image;
            }

            try {
                postDao.insertAuthor(post.author);
                post.authorId = post.author.id;
                postDao.insertCategory(post.category);
                post.categryId = post.category.id;
                newPosts.add(post);

            }

            catch (SQLiteConstraintException ex){
                Log.d("category : "+post.category.id,"category exists "+ex.getLocalizedMessage());
            }

        }
        if(!newPosts.isEmpty())
            postDao.insertMany(newPosts);
        return  Unit.INSTANCE;
    }

    public void updatePost(Post post){
        postDao.update(post);
    }
    public void insertPost(Post post){//todo same code twice
        try {
            post.thumbnail_images.largeUrl = post.thumbnail_images.large.url;
            post.thumbnail_images.mediumUrl = post.thumbnail_images.medium.url;
            post.thumbnail_images.thumbnailUrl = post.thumbnail_images.thumbnail.url;

            post.thumbnail_images.largeWidh = post.thumbnail_images.large.width;
            post.thumbnail_images.mediumWidh = post.thumbnail_images.medium.width;
            post.thumbnail_images.thumbnailWidh = post.thumbnail_images.thumbnail.width;

            post.thumbnail_images.largeHeght = post.thumbnail_images.large.height;
            post.thumbnail_images.mediumHeght = post.thumbnail_images.medium.height;
            post.thumbnail_images.thumbnailHeght = post.thumbnail_images.thumbnail.height;

        }
        catch (NullPointerException ex){
            Log.i("post : "+post.id,"images does not exists");
        }

        try {
            postDao.insertAuthor(post.author);
            post.authorId = post.author.id;
            postDao.insertCategory(post.category);
            post.categryId = post.category.id;
            postDao.insert(post);

        }

        catch (SQLiteConstraintException ex){
            Log.d("category : "+post.category.id,"category exists "+ex.getLocalizedMessage());
        }
    }

    public Listing<Post> postsOfOrient(int source){
        DataSource.Factory<Integer,Post> datasourceFactory;
        int count = 0;
        if(source == 0){
            datasourceFactory = postDao.loadFavoritePosts();
        }
        else if(source==-1){
            datasourceFactory = postDao.loadPosts();
            count = postDao.count();
        }
        else
        {
            datasourceFactory =  postDao.loadPosts(source);
            count = postDao.count(source);
        }


        int page = (int) (Math.ceil((double) count / POSTS_PAGE_SIZE )+ 1);
        MyBoundryCallBack boundryCallBack = new MyBoundryCallBack(source,
                page,
                webService,
                executor,
                this::insertToDb);

        //todo dirty code gowja pikirlenip uytget
        return new Listing<Post>(new LivePagedListBuilder(datasourceFactory, config)
                .setBoundaryCallback(boundryCallBack).build(),
                boundryCallBack.getNetworkState(),
                it -> {boundryCallBack.getHelper().retryAllFailed();return null ;});
    }

    @MainThread
    public LiveData<Post> getPost(Integer it) {
        loadPost(it);
        return postDao.get(it);
    }


    public void loadPost(Integer postId) {
        webService.getPost(postId).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if(response.isSuccessful() && response.body().post!=null){
                    executor.execute(() -> {
                        insertPost(response.body().post);
                    });
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {

                Log.e("load post","load post failed");
            }
        });
    }
}
