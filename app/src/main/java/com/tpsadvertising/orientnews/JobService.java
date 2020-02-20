package com.tpsadvertising.orientnews;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.tpsadvertising.orientnews.api.ListingResponse;
import com.tpsadvertising.orientnews.api.OrientNewsService;
import com.tpsadvertising.orientnews.data.PostRepository;
import com.tpsadvertising.orientnews.injectors.PerActivity;
import com.tpsadvertising.orientnews.room.Post;
import com.tpsadvertising.orientnews.ui.MainActivity;
import com.tpsadvertising.orientnews.ui.SettingsFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import dagger.android.AndroidInjection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@PerActivity
public class JobService extends android.app.job.JobService {

    private static final String TAG = "JobService";
    private boolean jobCancelled = false;

    List<Post> responseList = new ArrayList<>();

    NotificationManagerCompat notificationManagerCompat;

    Context context = this;

    @Inject
    OrientNewsService newsService;

    @Inject
    PostRepository postRepository;

    @Inject
    Executor executor;

    @Inject @Named("defaultPrefs")
    SharedPreferences preferences;

    @Override
    public boolean onStartJob(JobParameters params) {
        AndroidInjection.inject(this);
        Log.d(TAG, "Job Started");

        notificationManagerCompat = NotificationManagerCompat.from(this);

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


            Call<ListingResponse> call1 = newsService.getNewerPosts(41453, 5);
            call1.enqueue(new Callback<ListingResponse>() {
                @Override
                public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                    Log.d(TAG, "onResponse: " + response.code());
                    responseList = response.body().posts;



                    String content = "";

                    for (int i = 0; i < responseList.size(); i++){

                        int finalI = i;
                        executor.execute(() -> {
                            Post post = response.body().posts.get(finalI);

                            postRepository.insertPost(post);
                        });



                        content +=  (i+1) + ". " + responseList.get(i).title + "\n";
                    }

                    Intent intent = new Intent(context, MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    Notification notification = new NotificationCompat.Builder(context, "ChannelID")
                            .setSmallIcon(R.drawable.ic_launcherx)
                            .setContentTitle(context.getResources().getString(R.string.available_news))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent)
                            .build();

                    if(preferences.contains(SettingsFragment.KEY_PUSH)){
                        boolean pushIsActive = preferences.getBoolean(SettingsFragment.KEY_PUSH,true);
                        if(pushIsActive)
                            notificationManagerCompat.notify(1, notification);
                    }



                    Log.d(TAG, "onResponse: list size" + responseList.size());
                }

                @Override
                public void onFailure(Call<ListingResponse> call, Throwable t) {
                    Log.d(TAG, "onFailure: " + t.getCause());
                }
            });



            Log.d(TAG, "Job Finished: ");
            jobFinished(params, false);
        }).start();

    }


}
