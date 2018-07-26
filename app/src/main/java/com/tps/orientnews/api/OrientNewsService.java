package com.tps.orientnews.api;

import com.tps.orientnews.models.OrientPost;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by merdan on 5/14/18.
 */

public interface OrientNewsService {

    /*Recent posts*/
    @GET("get_recent_posts")
    Call<PostsWrapper> getRecentPosts(@Query("page") Integer page);
    /*Posts related to categroy*/
    @GET("get_category_posts")
    Call<PostsWrapper> getCategoryPosts(@Query("page") Integer page, @Query("id") Long id);

    /*Get single post given by id*/
    @GET("get_post")
    Call<OrientPost> getPost(@Query("post_id") long postId);

    //orient.tm/api/core/get_category_index
    @GET("get_category_index")
    Call<CategoriesWrapper> getCategories();
}
