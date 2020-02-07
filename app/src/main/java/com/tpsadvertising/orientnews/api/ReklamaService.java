package com.tpsadvertising.orientnews.api;

import com.tpsadvertising.orientnews.room.Reklama;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ReklamaService {
    @GET("get_banner.php")
    Call<List<Reklama>> getAds();
}
