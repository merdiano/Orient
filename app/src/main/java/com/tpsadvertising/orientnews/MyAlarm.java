package com.tpsadvertising.orientnews;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tpsadvertising.orientnews.ui.MainActivity;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class MyAlarm extends BroadcastReceiver {

    private static final String TAG = "JobService";

//    Context context = new MainActivity();

    //the method will be fired when the alarm is triggerred


    @Override
    public void onReceive(Context context, Intent intent) {


        //you can check the log that it is fired
        //Here we are actually not doing anything
        //but you can do any task here that you want to be done at a specific time everyday
        Log.d("JobService", "Alarm just fired");
        startBackgroundService(context);
    }

    private void startBackgroundService(Context context){

        ComponentName componentName = new ComponentName(context, JobService.class);
        JobInfo jobInfo = new JobInfo.Builder(123, componentName)
                .setRequiresCharging(false)
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build();
        JobScheduler jobScheduler = (JobScheduler)context.getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = jobScheduler.schedule(jobInfo);

        if (resultCode == JobScheduler.RESULT_SUCCESS){
            Log.d(TAG, "Job Scheduled");
        }
        else {
            Log.d(TAG, "Job Scheduling failed");
        }
    }

}
