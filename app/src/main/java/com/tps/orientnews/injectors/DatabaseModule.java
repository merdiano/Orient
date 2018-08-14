package com.tps.orientnews.injectors;

import android.arch.persistence.room.Room;

import com.tps.orientnews.OrientApplication;

import com.tps.orientnews.room.AppDatabase;
import com.tps.orientnews.room.PostDao;


import dagger.Module;
import dagger.Provides;

/**
 * Created by merdan on 7/13/18.
 */
@Module
public class DatabaseModule {

//    @Singleton
//    @Provides
//    DaoSession provideDaoSession(OrientApplication app){
//        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(app, "orient-db");
//        Database db = helper.getWritableDb();
//        return new DaoMaster(db).newSession();
//    }
//
//    @Provides
//    CategoryDao provideCategoryDao(DaoSession session){
//        return session.getCategoryDao();
//    }
//
//    @Provides
//    OrientPostDao provideOrientPostDao(DaoSession session){
//        return session.getOrientPostDao();
//    }

    @Provides
    AppDatabase provideDatabase(OrientApplication app){
        return Room.databaseBuilder(app,AppDatabase.class,"orient_db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

    }

    @Provides
    com.tps.orientnews.room.CategoryDao provideRoomCategoryDao(AppDatabase database){
        return database.categoryDao();
    }

    @Provides
    PostDao providePostDao(AppDatabase database){
        return database.postDao();
    }
}
