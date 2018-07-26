package com.tps.orientnews.injectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tps.orientnews.DataManager;
import com.tps.orientnews.api.CategoriesWrapper;
import com.tps.orientnews.api.OrientNewsService;
import com.tps.orientnews.api.PostsWrapper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by merdan on 7/13/18.
 */
@Module
public abstract class NetworkModule {
    private static final String ENDPOINT = "http://www.orient.tm/api/core/";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";//2018-06-09 17:17:33
    @Provides
    @Singleton
    static Retrofit provideRetrofit(Gson gson) {

//        final OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(new AuthInterceptor(getAccessToken()))
//                .build();
        return new Retrofit.Builder()
                .baseUrl(ENDPOINT)
//                .client(client)
//                .addConverterFactory(new DenvelopingConverter(gson))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

    }


    @Provides
    @Singleton
    static OrientNewsService provideNewsService(Retrofit retrofit)
    {
        return retrofit.create(OrientNewsService.class);
    }

    @Provides
    @Singleton
    static  Gson provideGson(){
        return  new GsonBuilder()
                .setDateFormat(DATE_FORMAT)
                .create();

    }

}
