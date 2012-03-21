package com.nfriedly.picsync;

import android.content.*;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * PictureReceiver
 * By nathan
 * 3/20/12
 */
public class PictureReceiver extends BroadcastReceiver {
    private SharedPreferences prefs;
    
    private String TAG = "picsync.PictureReceiver";

    public void onReceive(Context context, Intent intent) {

        boolean enabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("automatic_uploads", true);

        Log.d(TAG, "New picture received: " + intent.getData() + " Automatic uploads are " + (enabled ? "enabled" : "disabled"));

        if(!enabled) return;

        //Toast.makeText(context, "New picture dected! " + intent.getData().toString(), Toast.LENGTH_LONG).show();
        
        
        Intent uploadIntent = new Intent(context, PictureUploaderService.class);
        uploadIntent.setData(intent.getData());
        context.startService(uploadIntent);

    }




}
