package com.tpsadvertising.orientnews.room;

import androidx.lifecycle.LiveData;
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
public interface CategoryDao {
    @Query("Select * From category")
    public LiveData<List<Category>> loadAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(Category category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertMany(List<Category> categoryList);

    @Update
    public void update(Category... categories);

    @Delete void delete(Category... categories);

    @Query("Delete From category")
    void deleteAll();

    @Query("Select COUNT(*) FROM category")
    public int count();
}
