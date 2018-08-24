package com.tps.orientnews.api;

import com.tps.orientnews.room.Post;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by merdan on 5/14/18.
 */

public interface OrientNewsService {

    /*Recent posts*/
    @GET("get_recent_posts")
    Call<PostsResponse> getRecentPosts(@Query("page") Integer page);
    /*Posts related to categroy*/
    @GET("get_category_posts")
    Call<PostsResponse> getCategoryPosts(@Query("page") Integer page,
                                         @Query("id") Integer id);/*Recent posts*/

    @GET("get_recent_posts")
    Call<PostsResponse> getRecentPosts(@Query("page") Integer page,
                                       @Query("count") Integer limit);
    /*Posts related to categroy*/
    @GET("get_category_posts")
    Call<PostsResponse> getCategoryPosts(@Query("page") Integer page,
                                         @Query("id") Integer id,
                                         @Query("count") Integer limit);

    /*Get single post given by id*/
    @GET("get_post")
    Call<Post> getPost(@Query("post_id") long postId);

    //orient.tm/api/core/get_category_index
    @GET("get_category_index")
    Call<CategoriesWrapper> getCategories();

    @GET("get_posts_down")
    Call<PostsResponse> getOlderPosts(@Query("postid") Integer postId,
                                      @Query("categoryid") Integer categoryId,
                                      @Query("count") Integer count);
    @GET("get_posts_up")
    Call<PostsResponse> getNewerPosts(@Query("postid") Integer postId,
                                      @Query("categoryid") Integer categoryId,
                                      @Query("count") Integer count);
}
