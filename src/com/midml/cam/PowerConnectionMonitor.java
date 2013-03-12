package com.midml.cam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

public final class PowerConnectionMonitor extends BroadcastReceiver implements AudioManager.OnAudioFocusChangeListener
{
    PowerConnectionMonitor(Context context)
    {
        _audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }


    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent != null)
        {
            final String action = intent.getAction();
            Log.d(_TAG, "got power intent '" + action + '\'');

            if ("android.intent.action.ACTION_POWER_CONNECTED".equals(action))
            {
                processPowerConnected();
            }
            else if ("android.intent.action.ACTION_POWER_DISCONNECTED".equals(action))
            {
                processPowerDisconnected();
            }
        }
    }


    private void processPowerConnected()
    {
        releaseAudioFocus();
    }


    private void processPowerDisconnected()
    {
        grabAudioFocus();
    }


    private void grabAudioFocus()
    {
        final int result = _audioManager.requestAudioFocus(this,
                                                           AudioManager.STREAM_MUSIC,
                                                           AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        Log.d(_TAG,
              "tried to gain temp audio focus, result: " + result + ", " +
              ((AudioManager.AUDIOFOCUS_REQUEST_GRANTED==result)?"granted":"failed"));

        if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED==result)
        {
            _haveFocus = true;
            _handler.postDelayed(
                    new Runnable()
                    {
                        public void run()
                        {
                            gainPermAudioFocus();
                        }
                    },
                    100);
        }
    }


    private void gainPermAudioFocus()
    {
        if (_haveFocus)
        {
            final int result = _audioManager.requestAudioFocus(this,
                                                               AudioManager.STREAM_MUSIC,
                                                               AudioManager.AUDIOFOCUS_GAIN);

            Log.d(_TAG,
                  "tried to gain perm audio focus, result: " + result + ", " +
                  ((AudioManager.AUDIOFOCUS_REQUEST_GRANTED==result)?"granted":"failed"));

            if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED==result && _haveFocus)
            {
                Log.d(_TAG, "posting a delayed focus release");
                _handler.postDelayed(
                        new Runnable()
                        {
                            public void run()
                            {
                                releaseAudioFocus();
                            }
                        },
                2000);
            }
        }
    }


    private void releaseAudioFocus()
    {
        Log.d(_TAG, "trying to release audio focus, _haveFocus=" + _haveFocus);
        if (_haveFocus)
        {
            Log.d(_TAG, "calling abandonAudioFocus");
            _audioManager.abandonAudioFocus(this);
            _haveFocus = false;
        }
    }


    @Override
    public void onAudioFocusChange(int focusChange)
    {
        switch (focusChange)
        {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.d("TAG", "onAudioFocusChange AUDIOFOCUS_GAIN");
                _haveFocus = true;
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                Log.d(_TAG, "onAudioFocusChange AUDIOFOCUS_LOSS");
                releaseAudioFocus();
                break;
        }
    }


    private static final String       _TAG            = "CAMMS-PCM";
    private volatile     boolean      _haveFocus      = false;

    private final        Handler      _handler        = new Handler();
    private final        AudioManager _audioManager;
}
