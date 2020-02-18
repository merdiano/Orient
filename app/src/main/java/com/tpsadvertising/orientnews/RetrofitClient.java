package com.tpsadvertising.orientnews;


import com.tpsadvertising.orientnews.api.NewsService;
import com.tpsadvertising.orientnews.api.OrientNewsService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://www.orient.tm/api/core/";
    private static RetrofitClient mInstance;
    private Retrofit retrofit;

    private RetrofitClient(){
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
    }

    public static synchronized RetrofitClient getmInstance(){
        if (mInstance==null){
            mInstance = new RetrofitClient();
        }
        return mInstance;
    }

    public OrientNewsService getApi(){
        return retrofit.create(OrientNewsService.class);
    }




}
