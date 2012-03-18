package com.nfriedly.picsync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BootReciever
 * By nathan
 * 3/17/12
 */
public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, PictureObserverService.class);
        context.startService(service);
    }
}
