package tk.gianxddddd.audiodev.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class NetworkUtil {

    static FirebaseDatabase database = FirebaseDatabase.getInstance();

    public static void sendMessageToDatabase(String path, String message) {
        DatabaseReference myRef = database.getReference(path);
        myRef.setValue(message);
    }

    public static void sendMessageToDatabase(String path, HashMap<String, Object> hashMap) {
        DatabaseReference myRef = database.getReference(path);
        myRef.setValue(hashMap);
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (android.net.ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
