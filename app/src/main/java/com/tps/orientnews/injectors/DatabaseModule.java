package com.tps.orientnews.injectors;

import com.tps.orientnews.OrientApplication;
import com.tps.orientnews.models.DaoMaster;
import com.tps.orientnews.models.DaoSession;

import org.greenrobot.greendao.database.Database;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by merdan on 7/13/18.
 */
@Module
public class DatabaseModule {

    @Singleton
    @Provides
    DaoSession provideDaoSession(OrientApplication app){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(app, "orient-db");
        Database db = helper.getWritableDb();
        return new DaoMaster(db).newSession();
    }
}
