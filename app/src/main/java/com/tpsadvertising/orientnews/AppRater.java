package com.tpsadvertising.orientnews;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

public class AppRater {

    private final static int DAYS_UNTIL_PROMPT = 0;//Min number of days
    private final static int LAUNCHES_UNTIL_PROMPT = 0;//Min number of launches

    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showDialog(mContext, editor);
            }
        }

        editor.commit();
    }



    private static void showDialog(Context context, SharedPreferences.Editor editor){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // set the custom layout
        final View customLayout = LayoutInflater.from(context).inflate(R.layout.custom_dialog, null);
        builder.setView(customLayout);
        // add a button

        TextView yesBtn = customLayout.findViewById(R.id.dialog_rating_button_positive);

        TextView noBtn = customLayout.findViewById(R.id.dialog_rating_button_negative);

        AlertDialog dialog = builder.create();
        dialog.show();

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                context.startActivity(new Intent(Int ent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));

                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + context.getPackageName())));
                } catch (ActivityNotFoundException e) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
                }

                dialog.dismiss();
            }
        });


        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });



    }
}
