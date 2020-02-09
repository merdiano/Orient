package com.tpsadvertising.orientnews.api;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.tpsadvertising.orientnews.R;
import com.tpsadvertising.orientnews.data.PostRepository;

import com.tpsadvertising.orientnews.injectors.PerActivity;
import com.tpsadvertising.orientnews.ui.DetailActivity;
import com.tpsadvertising.orientnews.ui.SettingsFragment;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.android.AndroidInjection;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
@PerActivity
public class PushService extends FirebaseMessagingService {
    private static final String TAG = "Pushservce";
    public static final String ADMIN_CHANNEL_ID = "Pushservce";
    @Inject NotificationManager notificationManager;
    @Inject NotificationCompat.Builder notificationBuilder;
    @Inject Gson gson;
    @Inject PostRepository repository;
    @Inject @Named("defaultPrefs")
    SharedPreferences preferences;
    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override public void onMessageReceived(RemoteMessage remoteMessage) {

        //Setting up Notification channels for android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels();
        }

        int notificationId = new Random().nextInt(60000);
        notificationBuilder
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("message"));

        try {
            Integer postId = gson.fromJson(remoteMessage.getData().get("id"), Integer.class);
            //Post post = gson.fromJson(remoteMessage.getData().get("post"), Post.class);
            if(postId!=null && postId!=0)//15071
                repository.loadPost(postId);

            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_POST,postId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(intent);

//            PendingIntent pendingIntent = stackBuilder.getPendingIntent(
//                    0,
//                    PendingIntent.FLAG_UPDATE_CURRENT
//            );

            PendingIntent pendingIntent = PendingIntent.getActivity(this, postId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(pendingIntent);
            notificationId = postId;

        }
        catch (Exception ex){
            Log.e(TAG,ex.getLocalizedMessage());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels();
        }
        if(preferences.contains(SettingsFragment.KEY_PUSH)){
            boolean pushIsActive = preferences.getBoolean(SettingsFragment.KEY_PUSH,true);
            if(pushIsActive)
                notificationManager.notify(notificationId , notificationBuilder.build());
        }
        else
            notificationManager.notify(notificationId, notificationBuilder.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(){
        CharSequence adminChannelName = getString(R.string.notifications_admin_channel_name);
        String adminChannelDescription = getString(R.string.notifications_admin_channel_description);

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }
}
