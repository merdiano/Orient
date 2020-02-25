package com.tpsadvertising.orientnews;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
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


        new Thread(new Runnable() {
            @Override
            public void run() {


                if (jobCancelled){
                    return;
                }

                Post post = postRepository.getLastPost();
                int postId = post.id;
                Log.d(TAG, "doBackgroundWork: post id " + postId);

                Call<ListingResponse> call1 = newsService.getNewerPosts(41453, 20);
                call1.enqueue(new Callback<ListingResponse>() {
                    @Override
                    public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                        Log.d(TAG, "onResponse: " + response.code());
                        responseList = response.body().posts;

                        String content = "";

                        if (responseList.size() < 5){
                            for (int i = 0; i < 5; i++){

                                int finalI = i;
                                executor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        Post post = response.body().posts.get(finalI);

                                        postRepository.insertPost(post);
                                    }
                                });



                                content +=  (i+1) + ". " + responseList.get(i).title + "\n";
                            }
                        }else {
                            for (int i = 0; i < responseList.size(); i++){

                                int finalI = i;
                                executor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        Post post = response.body().posts.get(finalI);

                                        postRepository.insertPost(post);
                                    }
                                });



                                content +=  (i+1) + ". " + responseList.get(i).title + "\n";
                            }
                        }

                        Intent intent = new Intent(context, MainActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        Notification notification = new NotificationCompat.Builder(context, "ChannelID")
                                .setSmallIcon(R.drawable.ic_launcherx)
                                .setContentTitle("Orient News")
                                .setContentText(context.getResources().getString(R.string.available_news))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent)
                                .build();

//                        Log.d(TAG, "onResponse: " + content);

                        if(preferences.contains(SettingsFragment.KEY_PUSH)){
                            boolean pushIsActive = preferences.getBoolean(SettingsFragment.KEY_PUSH,true);
                            if(pushIsActive)
                                showNotification(context, content, intent);
//                                notificationManagerCompat.notify(1, notification);

                        }



                        Log.d(TAG, "onResponse: list size" + responseList.size());
                    }

                    @Override
                    public void onFailure(Call<ListingResponse> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t.getCause());
                    }
                });


//            Call<NewsResponse> call = RetrofitClient.getmInstance().getApi().getPostsForNotifiaction(id,5);
//            call.enqueue(new Callback<NewsResponse>() {
//                @Override
//                public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
//                    Log.d(TAG, "onResponse: " + response.code());
//                    responseList = response.body().posts;
//
//                        Notification notification = new NotificationCompat.Builder(context, "ChannelID")
//                                .setSmallIcon(R.drawable.ic_launcherx)
//                                .setContentTitle(responseList.get(i).getTitle())
//                                .setContentText(responseList.get(i).getContent())
//                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
//                                .build();
//
//                        notificationManagerCompat.notify(i, notification);
//
//
//                    Log.d(TAG, "onResponse: list size" + responseList.size());
//
//                }
//
//                @Override
//                public void onFailure(Call<NewsResponse> call, Throwable t) {
//                    Log.d(TAG, "onFailure: " + t.getCause());
//                }
//            });


                Log.d(TAG, "Job Finished: ");
                jobFinished(params, false);
            }
        }).start();
    }

    public void showNotification(Context context,String body, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = 1;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcherx)
                .setContentTitle("Orient News")
                .setContentText(context.getResources().getString(R.string.available_news))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(notificationId, mBuilder.build());
    }

}
