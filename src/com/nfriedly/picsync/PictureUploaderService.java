package com.nfriedly.picsync;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
 * 3/17/12
 */
public class PictureUploaderService extends Service {
    private SharedPreferences prefs;
    public IBinder onBind(Intent intent) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String api_key = prefs.getString("api_key", "");
        String server = "https://picsync.heroku.com/upload";
        try {
            uploadImage(server, api_key, readPictureBase64(intent.getExtras().getString("path")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void uploadImage(String server, String api_key, String pictureBase64) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost( server );
        List<NameValuePair> postContent = new ArrayList<NameValuePair>(2);
        postContent.add(new BasicNameValuePair("api_key", api_key));
        postContent.add(new BasicNameValuePair("img", pictureBase64));
        httpPost.setEntity(new UrlEncodedFormEntity(postContent));
        HttpResponse response = httpClient.execute(httpPost);
        Toast.makeText(this, "Picture upload complete, status is " + response.getStatusLine(), Toast.LENGTH_SHORT).show();
        httpClient.getConnectionManager().shutdown();
    }

    private String readPictureBase64(String path) throws IOException {
        File file=new File(path);

        BufferedInputStream fIn = new BufferedInputStream(new FileInputStream(file));
        DataInputStream dataStream = new DataInputStream(fIn);

        byte[] pictureData = new byte[dataStream.available()];
        dataStream.read(pictureData);

        return new String(pictureData);
    }
}
