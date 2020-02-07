package com.tpsadvertising.orientnews.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NewsService {
    /*Recent posts*/
    @GET("{lang}get_recent_posts")
    Call<ListingResponse> getRecentPosts(@Path("lang") String lang, @Query("page") Integer page);
    /*Posts related to categroy*/
    @GET("{lang}/get_category_posts")
    Call<ListingResponse> getCategoryPosts(@Path("lang") String lang,@Query("page") Integer page,
                                           @Query("id") Integer id);/*Recent posts*/

    @GET("{lang}get_recent_posts")
    Call<ListingResponse> getRecentPosts(@Path("lang") String lang,@Query("page") Integer page,
                                         @Query("count") Integer limit);
    /*Posts related to categroy*/
    @GET("{lang}get_category_posts")
    Call<ListingResponse> getCategoryPosts(@Path("lang") String lang,@Query("page") Integer page,
                                           @Query("id") Integer id,
                                           @Query("count") Integer limit);

    /*Get single post given by id*/
    @GET("{lang}get_post")
    Call<PostResponse> getPost(@Path("lang") String lang,@Query("id") Integer postId);

    //orient.tm/api/core/get_category_index
    @GET("{lang}get_category_index")
    Call<CategoriesWrapper> getCategories();

    @GET("{lang}get_posts_down")
    Call<ListingResponse> getOlderPosts(@Path("lang") String lang,@Query("postid") Integer postId,
                                        @Query("count") Integer count);
    @GET("{lang}get_posts_down")
    Call<ListingResponse> getOlderCategoryPosts(@Path("lang") String lang,@Query("postid") Integer postId,
                                                @Query("categoryid") Integer categoryId,
                                                @Query("count") Integer count);
    @GET("{lang}get_posts_up")
    Call<ListingResponse> getNewerPosts(@Path("lang") String lang,@Query("postid") Integer postId,
//                                        @Query("categoryid") Integer categoryId,
                                        @Query("count") Integer count);
}
