package com.tps.orientnews.room;

import android.arch.lifecycle.LiveData;
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

    @Query("Select COUNT(*) FROM category")
    public int count();
}
