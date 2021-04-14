package com.gianxd.audiodev.util;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.gianxd.audiodev.AudioDev;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class NetworkUtil {


    public static void sendMessageToDatabase(String path, String message) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(path);
        myRef.setValue(message);
    }

    public static void sendMessageToDatabase(String path, HashMap<String, Object> hashMap) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(path);
        myRef.setValue(hashMap);
    }

    public static boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (android.net.ConnectivityManager)AudioDev.applicationContext.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
