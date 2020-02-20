package com.tpsadvertising.orientnews;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class NotificationHelper extends ContextWrapper {

    public static final String ChannelID = "ChannelID";
    public static final String ChannelName = "Channel 1";

    public NotificationHelper(Context base) {
        super(base);
        createChannel();
    }

    private void createChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(ChannelID, ChannelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("This is Channel 1");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
