package com.tpsadvertising.orientnews.injectors;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tpsadvertising.orientnews.api.NewsService;
import com.tpsadvertising.orientnews.api.OrientNewsService;
import com.tpsadvertising.orientnews.api.ReklamaService;
import com.tpsadvertising.orientnews.ui.MainActivity;

import java.util.Locale;

import javax.inject.Inject;

import androidx.core.os.LocaleListCompat;
import androidx.preference.PreferenceManager;
import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by merdan on 7/13/18.
 */
@Module
public abstract class NetworkModule {
    private static final String ENDPOINT = "https://www.orient.tm/api/core/";
    private static final String ADVERTSPOINT ="http://tpsadvertising.com/api/" ;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";//2018-06-09 17:17:33


//    @Inject SharedPreferences preferences;

//    @Provides
//    @Singleton
//    static Retrofit provideRetrofit(Gson gson) {
//
////        final OkHttpClient client = new OkHttpClient.Builder()
////                .addInterceptor(new AuthInterceptor(getAccessToken()))
////                .build();
//        return new Retrofit.Builder()
//                .baseUrl(ENDPOINT)
////                .client(client)
////                .addConverterFactory(new DenvelopingConverter(gson))
//                .addConverterFactory(GsonConverterFactory.create(gson))
//                .build();
//
//    }

    @PerActivity
    @Provides
    static Retrofit provideRetrofit(Gson gson){
        String lang = Locale.getDefault().getLanguage();


        Log.d("Lang", "provideRetrofit: " + lang);

        //todo problemly dil uytgese garyshyara
        String base_url = lang.equals("en")?ENDPOINT+"en/":ENDPOINT;
        return new Retrofit.Builder()
                .baseUrl(base_url)
//                .client(client)
//                .addConverterFactory(new DenvelopingConverter(gson))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides
    static OrientNewsService provideNewsService(Retrofit retrofit)
    {
        //retrofit.
        return retrofit.create(OrientNewsService.class);
    }

    @Provides
    static NewsService providesNewsServiceEn(Retrofit retrofit){
        return retrofit.create(NewsService.class);
    }

    @Provides
    static ReklamaService provideReklamaService(Gson gson){
        Retrofit tpsRetrofit = new Retrofit.Builder()
                .baseUrl(ADVERTSPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return tpsRetrofit.create(ReklamaService.class);
    }

    @Provides
    static  Gson provideGson(){
        return  new GsonBuilder()
                .setDateFormat(DATE_FORMAT)
                .disableHtmlEscaping()
                .create();
    }

}
