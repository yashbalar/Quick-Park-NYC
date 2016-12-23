package com.nyc.cloud.park;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nyc.cloud.park.MapsActivity;
import com.nyc.cloud.park.R;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "FROM:" + remoteMessage.getFrom());

        //Check if the message contains data
        if(remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data: " + remoteMessage.getData().get("default"));
        }

        Map<String, String> params = remoteMessage.getData();
        try {
            JSONObject object = new JSONObject(params.get("default"));

            Intent intent = new Intent();
            intent.setAction("PARKING_DATA_FROM_SERVER");
            intent.putExtra("PARKING_SPOTS", object.toString());
            sendBroadcast(intent);
        }catch (Exception e){
            Log.d(TAG, "Mesage body:" + e.toString());
        }

    }

}
