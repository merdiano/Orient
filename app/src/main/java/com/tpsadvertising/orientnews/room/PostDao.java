package com.tpsadvertising.orientnews.room;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.PagedList;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Created by merdan on 8/6/18.
 */
@Dao
public interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Post... posts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMany(List<Post> posts);

    @Delete
    void delete(Post... posts);

    @Update
    void update(Post... posts);


    @Query("Select * From post" +
            " INNER JOIN user ON user.uid = post.author_id" +
            " Where id=:id")
    LiveData<Post> get(int id);

    @Query("Select id From post Where id<:postId  ORDER BY date DESC Limit 1")
    LiveData<Integer> getNextPostId(int postId);

    @Query("Select id From post Where id>:postId  ORDER BY date DESC Limit 1")
    LiveData<Integer> getPrevPostId(int postId);



    @Query("Select * From post Where id=:id")
    Post getPost(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAuthor(User author);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategory(Category category);

    @Query("Select * From post" +
//            " INNER JOIN user ON user.id = post.author_id" +
            " ORDER BY id DESC")
    DataSource.Factory<Integer,Post> loadPosts();

    @Query("Select * From post " +
//            " INNER JOIN user ON user.id = post.author_id" +
            " Where category_id=:categoryId" +
            " ORDER BY date DESC ")
    DataSource.Factory<Integer,Post> loadPosts(int categoryId);

    @Query("Select * From post " +
//            " INNER JOIN user ON user.id = post.author_id" +
            " Where isFavorite = 1" +
            " ORDER BY date DESC ")
    DataSource.Factory<Integer,Post> loadFavoritePosts();

    @Query("Select * From post " +
//            " INNER JOIN user ON user.id = post.author_id" +
            " Where title Like :query" +
            " ORDER BY date DESC ")
    DataSource.Factory<Integer,Post> searchPosts(String query);

    @Query("Select * From post")
    DataSource.Factory<Integer,Post> loadPostsOffline();
}
