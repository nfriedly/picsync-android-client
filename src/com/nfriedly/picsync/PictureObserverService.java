package com.nfriedly.picsync;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.os.FileObserver;
import android.preference.PreferenceManager;

/**
 * Watches for new .jpg files created anywhere and uploads them if we're enabled
 *
 * By nathan
 * 3/17/12
 */
public class PictureObserverService extends Service  {
    private FileObserver observer;
    private SharedPreferences prefs;

    public IBinder onBind(Intent intent) {
        // todo: consider only watching Environment.getExternalStorageDirectory() or some other non-root dir
        String root_path = "/";
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        observer = new FileObserver(root_path, FileObserver.CREATE) {
            @Override
            public void onEvent(int mask, String path) {
                if(!path.endsWith(".jpg") || !prefs.getBoolean("automatic_uploads", true)) return;
                else startUpload(path);
            }
        };
        return null;
    }
    
    private void startUpload(String path){
        Intent service = new Intent(this, PictureUploaderService.class);
        service.putExtra("path", path);
        startService(service);
    }

}



