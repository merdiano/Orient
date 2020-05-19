package com.tpsadvertising.orientnews.injectors;

import android.app.Application;
import android.app.NotificationManager;
import androidx.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.tpsadvertising.orientnews.JobService;
import com.tpsadvertising.orientnews.OrientApplication;
import com.tpsadvertising.orientnews.R;
import com.tpsadvertising.orientnews.api.PushService;

import com.tpsadvertising.orientnews.room.AppDatabase;
import com.tpsadvertising.orientnews.room.Category;
import com.tpsadvertising.orientnews.room.PostDao;
import com.tpsadvertising.orientnews.ui.CategoryActivity;
import com.tpsadvertising.orientnews.ui.DetailActivity;
import com.tpsadvertising.orientnews.ui.MainActivity;
import com.tpsadvertising.orientnews.ui.SearchActivity;
import com.tpsadvertising.orientnews.ui.SettingsFragment;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import dagger.android.ContributesAndroidInjector;

import dagger.android.support.AndroidSupportInjectionModule;

import static com.tpsadvertising.orientnews.OrientPrefs.ORIENT_PREF;

/**
 * Created by merdan on 7/12/18.
 */
@Module(includes = {AndroidSupportInjectionModule.class})
public abstract class OrientAppModule {

    @Binds
    abstract Application application(OrientApplication app);

    /**
     * Provides the injector for the {@link MainActivity}, which has access to the dependencies
     * provided by this application instance (singleton scoped objects).
     */
    @PerActivity
    @ContributesAndroidInjector(modules = {MainActivityModule.class,NetworkModule.class})
    abstract MainActivity mainActivityInjector();

    @PerActivity
    @ContributesAndroidInjector(modules = {CategoryActivityModule.class,NetworkModule.class})
    abstract CategoryActivity categoryActivityInjector();

    @PerActivity
    @ContributesAndroidInjector(modules = NetworkModule.class)
    abstract SettingsFragment settingsFragmentInjector();

    @PerActivity
    @ContributesAndroidInjector(modules = {DetailActivityModule.class,NetworkModule.class})
    abstract DetailActivity detailActivityInjector();

    @Provides
    static SharedPreferences providePreferences(Application context){
        return  context.getSharedPreferences(ORIENT_PREF, Context
                .MODE_PRIVATE);
    }
    @PerActivity
    @ContributesAndroidInjector(modules = NetworkModule.class)
    abstract PushService contributeMyService();

    @PerActivity
    @ContributesAndroidInjector(modules = NetworkModule.class)
    abstract JobService contributeMyJobService();

    @PerActivity
    @ContributesAndroidInjector(modules = SearchActivityModule.class)
    abstract SearchActivity searchActivityInjector();

    @Provides
    @Singleton
    static AppDatabase provideDatabase(OrientApplication app){
        return Room.databaseBuilder(app,AppDatabase.class,"orient_db")
                .fallbackToDestructiveMigration()
//                .allowMainThreadQueries()
                .build();

    }

    @Provides
    static com.tpsadvertising.orientnews.room.CategoryDao provideRoomCategoryDao(AppDatabase database){
        return database.categoryDao();
    }

    @Provides
    static PostDao providePostDao(AppDatabase database){
        return database.postDao();

    }

    @Provides @Named("defaultPrefs")
    static SharedPreferences provideDefaultPreferences(Application context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    static NotificationManager provideNotificationManager(Application application){
        return (NotificationManager)application.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Provides
    static NotificationCompat.Builder provideNotificationBuilder(Application application){
        return new NotificationCompat
                .Builder(application, PushService.ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo_inverse)  //a resource for your custom small icon
                .setAutoCancel(true)  //dismisses the notification on click
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    }
    @Provides
    static Executor provideExecuter(){
        return Executors.newSingleThreadExecutor();
    }
}

