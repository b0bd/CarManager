package com.midml.cam;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public final class CarManagerService extends Service
{
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(_TAG, "service has been started. starting usb observer");
        _hotplug = new UsbAudioHotplug(this);
        new UsbAudioObserver(this).start();

        final IntentFilter vf = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(new AudioVolumeMonitor(this), vf);

        final IntentFilter pf = new IntentFilter();
        pf.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        pf.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        registerReceiver(new PowerConnectionMonitor(this), pf);

        startWithIntent(intent);
        return START_STICKY;
    }



    void notifyPcmUEvent(SoundDevice device, String action)
    {
        _hotplug.notifyPcmUEvent(device, action);
    }


    private void startWithIntent(Intent intent)
    {
        if (intent != null)
        {
            final String intentStartReason = intent.getStringExtra("startReason");

            if ("android.intent.action.BOOT_COMPLETED".equals(intentStartReason))
            {
                try
                {
                    _hotplug.startAtBoot();
                }
                catch (IOException ioe)
                {
                    Log.e(_TAG, "Unable to register audio devices at boot", ioe);
                }
            }
        }
    }




    private static final String          _TAG = "CAMMS";
    private              UsbAudioHotplug _hotplug = null;
}
