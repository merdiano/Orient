package com.tps.orientnews.room;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListProvider;
import android.arch.paging.PagedList;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by merdan on 8/6/18.
 */
@Dao
public interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(Post... posts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertMany(List<Post> posts);

    @Delete
    public void delete(Post... posts);

    @Update
    public void update(Post... posts);

    @Query("Select * From post" +
//            " INNER JOIN user ON user.id = post.author_id" +
            " ORDER BY id DESC" +
            " Limit :limit" +
            " Offset :offset")
    public LiveData<List<Post>> loadPosts(int limit, int offset);

    @Query("Select * From post " +
//            " INNER JOIN user ON user.id = post.author_id" +
            " Where category_id=:categoryId" +
            " ORDER BY id DESC " +
            " LIMIT :limit" +
            " OFFSET :offset")
    LiveData<List<Post>> loadPosts(int limit, int offset, int categoryId);

    @Query("Select COUNT(*) FROM post")
    int count();

    @Query("Select COUNT(*) FROM post WHERE category_id=:categoryId")
    int count(int categoryId);

    @Query("Select * From post" +
            " INNER JOIN user ON user.uid = post.author_id" +
            " Where id=:id")
    LiveData<Post> get(int id);

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
            " ORDER BY id DESC ")
    DataSource.Factory<Integer,Post> loadPosts(int categoryId);

    @Query("Select * From post " +
//            " INNER JOIN user ON user.id = post.author_id" +
            " Where isFavorite = 1" +
            " ORDER BY id DESC ")
    DataSource.Factory<Integer,Post> loadFavoritePosts();

    @Query("Select * From post " +
//            " INNER JOIN user ON user.id = post.author_id" +
            " Where title Like :query" +
            " ORDER BY id DESC ")
    DataSource.Factory<Integer,Post> searchPosts(String query);
}
