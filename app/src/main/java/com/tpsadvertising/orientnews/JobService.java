package com.tpsadvertising.orientnews;

import android.app.job.JobParameters;
import android.content.SharedPreferences;
import android.util.Log;

import com.tpsadvertising.orientnews.api.ListingResponse;
import com.tpsadvertising.orientnews.room.Post;

import java.util.ArrayList;
import java.util.List;

import androidx.preference.PreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobService extends android.app.job.JobService {

    private static final String TAG = "JobService";
    private boolean jobCancelled = false;

    List<News> responseList = new ArrayList<>();

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job Started");

        doBackgroundWork(params);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;
    }

    private void doBackgroundWork(JobParameters params) {
        
        
        new Thread(() -> {


            if (jobCancelled){
                return;
            }

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            int id =  preferences.getInt("postid", 0);
            Log.d(TAG, "onCreate: " + id);

            Call<NewsResponse> call = RetrofitClient.getmInstance().getApi().getPostsForNotifiaction(41460,5);
            call.enqueue(new Callback<NewsResponse>() {
                @Override
                public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                    Log.d(TAG, "onResponse: " + response.code());
                    responseList = response.body().posts;

                    Log.d(TAG, "onResponse: list size" + responseList.size());

                }

                @Override
                public void onFailure(Call<NewsResponse> call, Throwable t) {
                    Log.d(TAG, "onFailure: " + t.getCause());
                }
            });


            Log.d(TAG, "Job Finished: ");
            jobFinished(params, false);
        }).start();

    }


}
