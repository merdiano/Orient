package com.tps.orientnews.api;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.tps.orientnews.R;
import com.tps.orientnews.data.PostRepository;
import com.tps.orientnews.injectors.DaggerOrientAppComponent;

import com.tps.orientnews.room.Post;
import com.tps.orientnews.ui.DetailActivity;
import com.tps.orientnews.ui.SettingsFragment;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.android.AndroidInjection;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
@Singleton
public class PushService extends FirebaseMessagingService {
    private static final String TAG = "Pushservce";
    private static final String ADMIN_CHANNEL_ID = "Pushservce";
    NotificationManager notificationManager;

    @Inject
    Gson gson;
    @Inject
    PostRepository repository;
    @Inject @Named("defaultPrefs")
    SharedPreferences preferences;
    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override public void onMessageReceived(RemoteMessage remoteMessage) {

        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Setting up Notification channels for android O and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels();
        }
        int notificationId = new Random().nextInt(60000);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat
                .Builder(this, ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo_inverse)  //a resource for your custom small icon
                .setContentTitle(remoteMessage.getData().get("title")) //the "title" value you sent in your notification
                .setContentText(remoteMessage.getData().get("message")) //ditto
                .setAutoCancel(true)  //dismisses the notification on click
                .setSound(defaultSoundUri);
        try {
            Integer postId = gson.fromJson(remoteMessage.getData().get("id"), Integer.class);
            //Post post = gson.fromJson(remoteMessage.getData().get("post"), Post.class);
            if(postId!=null && postId!=0)
                repository.loadPost(postId);

            Intent intent = new Intent(this, DetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(DetailActivity.EXTRA_POST,postId);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            notificationBuilder.setContentIntent(pendingIntent);

        }
        catch (Exception ex){
            Log.e(TAG,ex.getLocalizedMessage());
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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
        CharSequence adminChannelName = "getString(R.string.notifications_admin_channel_name)";
        String adminChannelDescription = "getString(R.string.notifications_admin_channel_description)";

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
