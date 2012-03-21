package com.nfriedly.picsync;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

/**
 * PictureUploaderService
 * By nathan
 *
 * todo: there's probably some things in here that need cleaned up, especially in the case of failures
 * 3/17/12
 */
public class PictureUploaderService extends Service {
    private SharedPreferences prefs;
    private static final String TAG = "picsync.PictureUploaderService";

    private static final String CRLF = "\r\n";
    private static final String BOUNDARY_BASE = "picsyncboundary";
    private static final String BOUNDARY = "--" + BOUNDARY_BASE + CRLF;
    private static final String CLOSE_BOUNDARY = "--" + BOUNDARY_BASE + "--" + CRLF;

    public void onCreate() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d(TAG, "Upload service started");
    }

    /**
     * starting in API level 5, onStart is deprecated and replaced with
     * public int onStartCommand (Intent intent, int flags, int startId)
     * @param intent
     * @param startId
     */
    public void onStart(Intent intent, int startId) {
        String api_key = prefs.getString("api_key", "");
        String server = "https://picsync.herokuapp.com/upload";

        Log.d(TAG, String.format("Upload starting with api_key %s and server %s", api_key, server));
        try {
            uploadImage(server, api_key, intent.getData());
        } catch (Throwable t) {
            Log.e(TAG, "Error uploading picture", t);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        onStart(intent, 0); // todo: determine what the startId should be or if 0 is good
        return null;
    }

    private void uploadImage(String server, String api_key, Uri picUri) throws IOException {

        HttpURLConnection conn = null;
        DataOutputStream uploadStream = null;
        InputStream imageStream = null;

        try {
            conn = (HttpURLConnection) new URL(server).openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY_BASE);
            uploadStream = new DataOutputStream(conn.getOutputStream());

            // send the api key
            uploadStream.writeBytes(BOUNDARY);
            uploadStream.writeBytes("Content-Disposition: form-data; name=\"api_key\"" + CRLF + CRLF);
            uploadStream.writeBytes(URLEncoder.encode(api_key, "UTF-8") + CRLF);

            // send the picture
            uploadStream.writeBytes(BOUNDARY);
            uploadStream.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"" + URLEncoder.encode(new Date().toString(), "UTF-8") + ".jpg\"" + CRLF);
            uploadStream.writeBytes("Content-Type: image/jpeg" + CRLF);
            uploadStream.writeBytes("Content-Transfer-Encoding: binary" + CRLF);
            uploadStream.writeBytes(CRLF);

            imageStream = getContentResolver().openInputStream(picUri);
            int maxBufferSize = 1024;
            int bufferSize = Math.min(imageStream.available(), maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            int bytesRead = imageStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                uploadStream.write(buffer, 0, bufferSize);
                bufferSize = Math.min(imageStream.available(), maxBufferSize);
                buffer = new byte[bufferSize];
                bytesRead = imageStream.read(buffer, 0, bufferSize);
            }
            uploadStream.writeBytes(CRLF);
            imageStream.close();

            uploadStream.writeBytes(CLOSE_BOUNDARY);
            uploadStream.flush();

            Log.d(TAG, "upload complete, status is" + conn.getResponseCode() + "\n response was \n" + conn.getResponseMessage());
        }
        finally {
            // cleanup - probably unnecessary unless something else went wrong
            try {
                if(uploadStream != null) uploadStream.close();
                if(imageStream != null) imageStream .close();
                if(conn != null) conn.disconnect();
                // System.gc(); // is this worthwhile?
            } catch(IOException ioe) {
                Log.d(TAG, "Error cleaning up", ioe);
            }
        }

    }
}
