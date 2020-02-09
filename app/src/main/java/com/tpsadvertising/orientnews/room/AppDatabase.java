package com.tpsadvertising.orientnews.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Created by merdan on 8/6/18.
 */
@Database(entities = {Category.class,Post.class,User.class},version = 14,exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase{
    public abstract CategoryDao categoryDao();
    public abstract PostDao postDao();
}
