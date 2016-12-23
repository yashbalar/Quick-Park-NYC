package com.nyc.cloud.park;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseInsIDService";
    public static final String MyPREFERENCES = "Quick Park" ;

    @Override
    public void onTokenRefresh() {
        //Get updated token
        SharedPreferences spreference = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences.Editor edit = spreference.edit();
        edit.putString("FCMToken",refreshedToken);
        edit.commit();
        Log.d(TAG, "New Token: " + refreshedToken);

        //You can save the token into third party server to do anything you want
    }
}
