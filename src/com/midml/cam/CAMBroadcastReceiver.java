package com.midml.cam;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public final class CAMBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        final String action = intent.getAction();
        Log.d("CAM", "received action '" + action + '\'');

        if ("android.intent.action.BOOT_COMPLETED".equals(action))
        {
            final Intent startServiceIntent = new Intent(context, CarManagerService.class);
            startServiceIntent.putExtra("startReason", action);
            context.startService(startServiceIntent);
            Log.i("Autostart", "started");
        }
    }
}
