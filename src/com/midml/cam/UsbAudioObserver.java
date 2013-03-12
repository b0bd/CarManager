package com.midml.cam;

import android.util.Log;

public final class UsbAudioObserver extends android.os.UEventObserver
{
    UsbAudioObserver(CarManagerService service)
    {
        _service = service;
    }

    void start()
    {
        Log.v(_TAG, "starting");
        startObserving("MAJOR=116");
        Log.v(_TAG, "observers registered");
    }

    @Override
    public void onUEvent(UEvent uEvent)
    {
        Log.v(_TAG, "received Event '" + uEvent.toString() + '\'');

        final String path = uEvent.get("DEVNAME");
        if (SoundDevice.isPcmPlaybackPath(path))
        {
            _service.notifyPcmUEvent(SoundDevice.fromPcmPath(path), uEvent.get("ACTION"));
        }
        else
        {
            Log.v(_TAG, "observer path '" + path + "' did not match");
        }
    }


    private final CarManagerService _service;

    private static final String _TAG = "CAMMS-UAO";
}
