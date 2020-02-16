package com.tpsadvertising.orientnews.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import android.database.sqlite.SQLiteConstraintException;
import androidx.annotation.MainThread;

import com.tpsadvertising.orientnews.api.OrientNewsService;
import com.tpsadvertising.orientnews.api.ListingResponse;
import com.tpsadvertising.orientnews.api.PostResponse;
import com.tpsadvertising.orientnews.injectors.PerActivity;
import com.tpsadvertising.orientnews.room.AppDatabase;
import com.tpsadvertising.orientnews.room.Post;
import com.tpsadvertising.orientnews.room.PostDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import kotlin.Unit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by merdan on 8/2/18.
 */
@PerActivity
public class PostRepository {
    private final OrientNewsService webService;
    private final PostDao postDao;
    private final Executor executor;
    private final AppDatabase database;
//    private static final int POSTS_PAGE_SIZE = 20;
    private PagedList.Config config;
    @Inject
    PostRepository(OrientNewsService service,
//                   PostDao postDao,
                   Executor executor,
                   AppDatabase database,
                   PagedList.Config config){
//        this.postDao = postDao;
        this.webService = service;
        this.executor = executor;
        this.config = config;
        this.database = database;
        this.postDao = database.postDao();
    }

    private Unit insertToDb(ListingResponse response){
        if(response == null||response.posts==null){
            return null;
        }
        List<Post> newPosts = new ArrayList<>();
        for (Post post : response.posts){
            if(post.title.isEmpty())
                continue;
            post = mapUrl(post);

            try {
                postDao.insertAuthor(post.author);

                post.authorId = post.author.id;
                postDao.insertCategory(post.category);
                post.categryId = post.category.id;
                newPosts.add(post);

            }

            catch (SQLiteConstraintException ex){
//                Log.d("category : "+post.category.id,"category exists "+ex.getLocalizedMessage());
            }

        }
        if(!newPosts.isEmpty())
            postDao.insertMany(newPosts);
        return  Unit.INSTANCE;
    }

    public void updatePost(Post post){
        executor.execute(() -> {
            try {
                postDao.update(post);

            }
            catch (Exception ex){
//                Log.d("PostRepository update",ex.getLocalizedMessage());
            }
        });
    }

    private Post mapUrl(Post post){
        if(post.thumbnail_images != null){
            if(post.thumbnail_images.large != null){
                post.thumbnail_images.largeUrl = post.thumbnail_images.large.url;
                post.thumbnail_images.largeWidh = post.thumbnail_images.large.width;
                post.thumbnail_images.largeHeght = post.thumbnail_images.large.height;
            }

            if(post.thumbnail_images.medium != null){
                post.thumbnail_images.mediumUrl = post.thumbnail_images.medium.url;
                post.thumbnail_images.mediumWidh = post.thumbnail_images.medium.width;
                post.thumbnail_images.mediumHeght = post.thumbnail_images.medium.height;
            }

            if(post.thumbnail_images.thumbnail != null){
                post.thumbnail_images.thumbnailUrl = post.thumbnail_images.thumbnail.url;
                post.thumbnail_images.thumbnailWidh = post.thumbnail_images.thumbnail.width;
                post.thumbnail_images.thumbnailHeght = post.thumbnail_images.thumbnail.height;
            }
        }

        return post;
    }

    public void insertPost(Post post){//todo same code twice
            post = mapUrl(post);

        try {
            postDao.insertAuthor(post.author);
            post.authorId = post.author.id;
            postDao.insertCategory(post.category);
            post.categryId = post.category.id;
            postDao.insert(post);

        }

        catch (SQLiteConstraintException ex){
//            Log.e("category : "+post.category.id,"category exists "+ex.getLocalizedMessage());
        }
    }

    public Listing<Post> favoritePosts(){
        MutableLiveData<NetworkState> ntState= new MutableLiveData<>();
        ntState.setValue(NetworkState.FAVORITE);
        return new Listing<Post>(new LivePagedListBuilder<>(postDao.loadFavoritePosts(),config)
                .build(),
                ntState,
                it->{return null;});
    }

    public Listing<Post> postsOfOrient(int source){
        DataSource.Factory<Integer,Post> datasourceFactory = (source==-1)?
                postDao.loadPosts():postDao.loadPosts(source);
        MyBoundryCallBack boundryCallBack = new MyBoundryCallBack(source,
//                page,
                webService,
                executor,
                this::insertToDb);

        //todo dirty code gowja pikirlenip uytget favoriler ucin ayratyn yaz
        return new Listing<Post>(new LivePagedListBuilder(datasourceFactory, config)
                .setBoundaryCallback(boundryCallBack)
                .build(),
                boundryCallBack.getNetworkState(),
                it -> {boundryCallBack.getHelper().retryAllFailed();return null ;});
    }

    @MainThread
    public LiveData<Post> getPost(Integer it) {
        loadPost(it);//todo meshaet dlya favorites
        return postDao.get(it);
    }

    @MainThread
    public LiveData<Integer> getNextPostId(int id){
        return postDao.getNextPostId(id);
    }

    @MainThread
    public LiveData<Integer> getPrevPostId(int id){
        return postDao.getPrevPostId(id);
    }

    public synchronized void loadPost(Integer postId) {
        webService.getPost(postId).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if(response.isSuccessful() && response.body().post!=null){
                    executor.execute(() -> {
                        Post oldPost = postDao.getPost(postId);
                        Post newPost = response.body().post;
                        if(oldPost != null)
                            newPost.isFavorite = oldPost.isFavorite;
                        insertPost(newPost);
                    });
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {

//                Log.e("load post","load post failed");
            }
        });
    }

    public Listing<Post> loadPostsOffline(){
        MutableLiveData<NetworkState> ntState= new MutableLiveData<>();
        ntState.setValue(NetworkState.FAVORITE);
        return new Listing<Post>(new LivePagedListBuilder<>(postDao.loadPostsOffline(),config)
                .build(),
                ntState,
                it->{return null;});
    }
}
