package com.nyc.cloud.park;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by Yash on 22-12-2016.
 */

public class RegistrationIntentService extends IntentService {

    // abbreviated tag name
    private static final String TAG = "RegIntentService";
    public static final String MyPREFERENCES = "Quick Park" ;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Make a call to Instance API
        FirebaseInstanceId instanceID = FirebaseInstanceId.getInstance();
        String senderId = getResources().getString(R.string.gcm_defaultSenderId);
        try {
            SharedPreferences spreference = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            String FCMtoken = FirebaseInstanceId.getInstance().getToken();
            SharedPreferences.Editor edit = spreference.edit();
            edit.putString("FCMToken",FCMtoken);
            edit.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
