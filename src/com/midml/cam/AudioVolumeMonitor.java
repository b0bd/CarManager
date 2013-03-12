package com.midml.cam;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import java.util.List;

public final class AudioVolumeMonitor extends BroadcastReceiver
{
    AudioVolumeMonitor(Context context)
    {
        _context         = context;
        _activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        _audioManager    = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }


    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(_TAG, "got intent "  + intent.getAction());
        if ("android.media.VOLUME_CHANGED_ACTION".equals(intent.getAction()))
        {
            final int stream = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
            if (AudioManager.STREAM_NOTIFICATION == stream)
            {
                final int prevVol = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", -1);
                final int currVol = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1);

                if (currVol < prevVol)
                {
                    Log.d(_TAG, "someone changed the notification volume lower from " + prevVol + " to " + currVol);

                    final String currentActivity = getCurrentActivity();
                    if (isPermittedVolumeChangeApp(currentActivity))
                    {
                        Log.d(_TAG, "detected activity '" + currentActivity + "' " +
                                    "allowed to change the notification volume");
                    }
                    else
                    {
                        Log.d(_TAG, "detected activity '" + currentActivity + "' " +
                                    "is not known as an activity " +
                                    "allowed to change the notification volume, resetting to " + prevVol);

                        _audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, prevVol, 0);
                    }
                }
            }
        }
    }


    private String getCurrentActivity()
    {
        String result = "";
        try
        {
            final List<ActivityManager.RunningTaskInfo> runningTaskInfos = _activityManager.getRunningTasks(1);
            if (runningTaskInfos.size() > 0)
            {
                final ActivityManager.RunningTaskInfo rti = runningTaskInfos.iterator().next();
                if (rti != null && rti.topActivity != null)
                {
                    result = rti.topActivity.getClassName();
                }
            }
        }
        catch (Exception e)
        {
            result = "";
        }

        return result;
    }


    private static boolean isPermittedVolumeChangeApp(String appName)
    {
        final boolean result;
        if (appName == null)
        {
            result = false;
        }
        else
        {
            result = appName.startsWith("com.android.settings.");
        }
        return result;
    }

    private static final String          _TAG = "CAMMS-AVM";

    private        final Context         _context;
    private        final ActivityManager _activityManager;
    private        final AudioManager    _audioManager;
}
