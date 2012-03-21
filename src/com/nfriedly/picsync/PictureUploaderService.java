package com.nfriedly.picsync;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PictureUploaderService
 * By nathan
 *
 * todo: there's probably some things in here that need cleaned up, especially in the case of failures
 * 3/17/12
 */
public class PictureUploaderService extends Service {
    private SharedPreferences prefs;
    private String TAG = "picsync.PictureUploaderService";

    public void onCreate() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d(TAG, "Upload service started");
    }

    /**
     * per the android docs, onStart is deprecated and replaced with 
     * public int onStartCommand (Intent intent, int flags, int startId) starting in API level 5
     * @param intent
     */
    public void onStart(Intent intent) {
        String api_key = prefs.getString("api_key", "");
        String server = "https://picsync.heroku.com/upload";

        Log.d(TAG, String.format("Upload starting with api_key %s and server %s", api_key, server));
        try {
            uploadImage(server, api_key, readPictureData(this, intent.getData()));
        } catch (IOException e) {
            Log.e(TAG, "Exception uploading picture", e);
        }
    }

    private void uploadImage(String server, String api_key, String pictureBase64) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost( server );
        List<NameValuePair> postContent = new ArrayList<NameValuePair>(2);
        postContent.add(new BasicNameValuePair("api_key", api_key));
        postContent.add(new BasicNameValuePair("img", pictureBase64));
        httpPost.setEntity(new UrlEncodedFormEntity(postContent));
        HttpResponse response = httpClient.execute(httpPost);
        httpClient.getConnectionManager().shutdown();
        Log.d(TAG, "Upload complet, response status is " + response.getStatusLine());
    }

    private String readPictureData(Context context, Uri picUri) throws IOException {

        ContentResolver cr = new ContentResolver(context) {
            @Override
            public void notifyChange(Uri uri, ContentObserver observer) {
            }

            @Override
            public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork) {
            }

            @Override
            public void startSync(Uri uri, Bundle extras) {
            }

            @Override
            public void cancelSync(Uri uri) {
            }
        };

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cur = cr.query(picUri, projection, null, null, null);
        cur.moveToFirst();
        String path = cur.getString(cur.getColumnIndex(MediaStore.Images.Media.DATA));

        File file = new File(path);

        BufferedInputStream fIn = new BufferedInputStream(new FileInputStream(file));
        DataInputStream dataStream = new DataInputStream(fIn);

        byte[] pictureData = new byte[dataStream.available()];
        dataStream.read(pictureData);

        return new String(pictureData);
    }

    /**
     * todo: determine if this is really needed and impliment it if so
     * @param intent
     * @return
     */
    public IBinder onBind (Intent intent) {
        Log.d(TAG, "onBind called, redirecting to onStart");
        onStart(intent);
        return null;
    }
}
