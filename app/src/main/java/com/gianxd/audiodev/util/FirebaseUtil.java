package com.gianxd.audiodev.util;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.gianxd.audiodev.AudioDev;

public class FirebaseUtil {

    public static boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (android.net.ConnectivityManager)AudioDev.applicationContext.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
