package com.tps.orientnews.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.tps.orientnews.models.Image;
import com.tps.orientnews.models.OrientPost;

import java.io.File;


/**
 * Created by merdan on 6/10/18.
 */

public class ShareOrientImageTask extends AsyncTask<Void, Void, File> {
    private final Activity activity;
    private final OrientPost news;

    ShareOrientImageTask(Activity activity, OrientPost news) {
        this.activity = activity;
        this.news = news;
    }
    @Override
    protected File doInBackground(Void... voids) {
        Image assets = news.getThumbnail_images().getLarge();
        final String url = assets.getUrl();
        try {
            return Glide
                    .with(activity)
                    .load(url)
                    .downloadOnly(assets.getWidth(), assets.getHeight())//todo width height
                    .get();
        } catch (Exception ex) {
            Log.w("SHARE", "Sharing " + url + " failed", ex);
            return null;
        }
    }
    @Override
    protected void onPostExecute(File result) {
        if (result == null) { return; }
        // glide cache uses an unfriendly & extension-less name,
        // massage it based on the original
        String fileName = news.getUrl();
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        File renamed = new File(result.getParent(), fileName);
        result.renameTo(renamed);
        Uri uri = FileProvider.getUriForFile(activity, "com.tps.shareprovider", renamed);
        ShareCompat.IntentBuilder.from(activity)
                .setText(getShareText())
                .setType(getImageMimeType(fileName))
                .setSubject(news.getTitle())
                .setStream(uri)
                .startChooser();
    }

    private String getShareText() {
        return "“" + news.getTitle() + "” by " + news.author.fullname() + "\n" + news.getUrl();
    }

    private String getImageMimeType(@NonNull String fileName) {
        if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/jpeg";
    }
}
