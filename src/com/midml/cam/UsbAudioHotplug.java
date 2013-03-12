package com.midml.cam;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


final class UsbAudioHotplug
{
    UsbAudioHotplug(Context context)
    {
        _context = context;
    }


    synchronized void notifyPcmUEvent(SoundDevice device, String action)
    {
        boolean adding = "add".equals(action);
        if (adding)
        {
            _uEventAudioDevices.add(device);
        }
        else
        {
            _uEventAudioDevices.remove(device);
        }

        sendAudioIntent(device, adding);
    }


    private void sendAudioIntent(SoundDevice device, boolean adding)
    {
        _context.sendBroadcast(_AUDIO_NOISY_INTENT);

        final Intent i = getUsbPlugEventFor(device);
        if (adding)
        {
            i.putExtra("state", 1);
        }
        else
        {
            i.putExtra("state", 0);
        }

        _context.sendStickyBroadcast(i);
        Log.d(_TAG, "broadcast sticky intent    " + i.toString());
    }





    private static Intent getUsbPlugEventFor(SoundDevice device)
    {
        final Intent intent = new Intent("android.intent.action.USB_AUDIO_ACCESSORY_PLUG");
        intent.putExtra("card", device.getCard());
        intent.putExtra("device", device.getDeviceNumber());
        intent.putExtra("channels", device.getChannels());

        return intent;
    }



    void startAtBoot() throws IOException
    {
        int lastCardFound = -1;
        final BufferedReader br = new BufferedReader(new FileReader(_ASOUND_PCM_FILE));
        try
        {
            for (String l = br.readLine(); l != null; l = br.readLine())
            {
                final Matcher m = _ASOUND_PCM_MATCHER.matcher(l);
                if (m.find())
                {
                    final int card = Integer.parseInt(m.group(1));

                    if (lastCardFound != card)
                    {
                        lastCardFound  = card;
                        final int devnum = Integer.parseInt(m.group(2));
                        final SoundDevice sd = new SoundDevice(card, devnum, 2);
                        Log.d(_TAG, "discovered new audio playback " + sd);

                        _bootUsbAudioDevices.add(sd);
                        sendAudioIntent(sd, true);
                    }
                    else
                    {
                        Log.d(_TAG, "already discovered this card  " + m.group(1) + " device  " + m.group(2));
                    }
                }
            }
        }
        finally
        {
            br.close();
        }
    }


    private final Context _context;

    private static final Intent _AUDIO_NOISY_INTENT = new Intent("android.media.AUDIO_BECOMING_NOISY");

    private static final Pattern _ASOUND_PCM_MATCHER = Pattern.compile("^([0-9]+)-([0-9]+): USB Audio.*playback");
    private static final String  _ASOUND_PCM_FILE = "/proc/asound/pcm";

    private final LinkedList<SoundDevice> _uEventAudioDevices = new LinkedList<SoundDevice>();
    private final LinkedList<SoundDevice> _bootUsbAudioDevices = new LinkedList<SoundDevice>();


    private static final String  _TAG = "CAMMS-UAH";
}
