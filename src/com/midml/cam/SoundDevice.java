package com.midml.cam;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SoundDevice
{
    SoundDevice(int card, int deviceNumber, int channels)
    {
        _card = card;
        _deviceNumber = deviceNumber;
        _channels = channels;
    }


    static boolean isPcmPlaybackPath(String path)
    {
        return _PCM_PATTERN.matcher(path).find();
    }


    static SoundDevice fromPcmPath(String path)
    {
        final Matcher m = _PCM_PATTERN.matcher(path);

        if (!m.find())
        {
            throw new IllegalArgumentException("path '" + path  + "' is not a valid pattern");
        }

        final int card = Integer.parseInt(m.group(1));
        final int deviceNumber = Integer.parseInt(m.group(2));

        return new SoundDevice(card, deviceNumber, 2);
    }



    int getCard()
    {
        return _card;
    }


    int getDeviceNumber()
    {
        return _deviceNumber;
    }


    int getChannels()
    {
        return _channels;
    }


    @Override
    public String toString()
    {
        return "SoundDevice{" +
                "_card=" + _card +
                ", _deviceNumber=" + _deviceNumber +
                ", _channels=" + _channels +
                '}';
    }


    private final int _card;
    private final int _deviceNumber;
    private final int _channels;


    private static final Pattern _PCM_PATTERN = Pattern.compile("pcmC([0-9]+)D([0-9]+)p$");
}
