package com.gianxd.audiodev.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gianxd.audiodev.util.ApplicationUtil;
import com.gianxd.audiodev.util.FileUtil;
import com.gianxd.audiodev.util.ListUtil;
import com.gianxd.audiodev.util.StringUtil;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.HashMap;

public class LauncherActivity extends AppCompatActivity {

    private HashMap<String, Object> settingsData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.i("MobileAds", initializationStatus.toString());
            }
        });
        (new initializeLocalFiles()).execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (ContextCompat.checkSelfPermission(ApplicationUtil.getAppContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(ApplicationUtil.getAppContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                (new mediaScanningTask()).execute();
            } else {
               startActivity(new Intent(ApplicationUtil.getAppContext(), SplashActivity.class));
               finish();
            }
        }
    }

    public class initializeLocalFiles extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            /* This AsyncTask class will perform data checking from local storage. */
        }

        @Override
        protected Void doInBackground(Void... path) {
            if (FileUtil.doesExists(FileUtil.getPackageDir().concat("/user")) && FileUtil.isDirectory(FileUtil.getPackageDir().concat("/user"))) {
                Log.v("LauncherActivity", "User data exists, ignoring creation task..");
            } else {
                Log.e("LauncherActivity", "User data not found, performing creation task..");
                FileUtil.createDirectory(FileUtil.getPackageDir().concat("/user/"));
                FileUtil.createFile(FileUtil.getPackageDir().concat("/user/profile.pref"));
                FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/profile.pref"), "{}");
                FileUtil.createFile(FileUtil.getPackageDir().concat("/user/settings.pref"));
                FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/settings.pref"), "{}");
                FileUtil.createFile(FileUtil.getPackageDir().concat("/user/session.pref"));
                FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), "{}");
                FileUtil.createFile(FileUtil.getPackageDir().concat("/user/online.pref"));
                FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/online.pref"), "{}");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            /* Update progress value */
        }

        @Override
        protected void onPostExecute(Void param) {
            if (ContextCompat.checkSelfPermission(ApplicationUtil.getAppContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(ApplicationUtil.getAppContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                (new mediaScanningTask()).execute();
            } else {
                ActivityCompat.requestPermissions(LauncherActivity.this, new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
            }
        }

    }

    public class mediaScanningTask extends AsyncTask<Void, Void, Void> {

        private ArrayList<HashMap<String, Object>> scanList;

        @Override
        protected void onPreExecute() {
            if (FileUtil.doesExists(FileUtil.getPackageDir().concat("/song.json")) && FileUtil.isFile(FileUtil.getPackageDir().concat("/song.json"))) {
                Log.v("LauncherActivity", "Song list exists, ignoring creation task..");
            } else {
                Log.e("LauncherActivity", "Song list not found, performing creation task..");
                FileUtil.createFile(FileUtil.getPackageDir().concat("/song.json"));
            }
            scanList = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... path) {
            if (ContextCompat.checkSelfPermission(ApplicationUtil.getAppContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(ApplicationUtil.getAppContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                String[] mediaProjection = {
                        android.provider.MediaStore.Audio.Media.ARTIST,
                        android.provider.MediaStore.Audio.Media.DATA,
                        android.provider.MediaStore.Audio.Media.TITLE,
                        android.provider.MediaStore.Audio.Media.ALBUM_ID
                };
                String orderBy = " " + android.provider.MediaStore.MediaColumns.DISPLAY_NAME;
                Cursor mediaCursor = ApplicationUtil.getAppContext().getContentResolver().query(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mediaProjection, null, null, orderBy);
                try {
                    if (mediaCursor.moveToFirst()) {
                        String name;
                        String data;
                        String artist;
                        do {
                            name = mediaCursor.getString(mediaCursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.TITLE));
                            data = mediaCursor.getString(mediaCursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.DATA));
                            artist = mediaCursor.getString(mediaCursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.ARTIST));
                            {

                                HashMap<String, Object> songDetails = new HashMap<>();
                                if (name.startsWith("<unknown>")) {
                                    name = "Unknown Title";
                                }
                                if (artist.startsWith("<unknown>")) {
                                    artist = "Unknown Artist";
                                }
                                songDetails.put("songTitle", name);
                                songDetails.put("songData", StringUtil.encodeString(data));
                                songDetails.put("songArtist", artist);
                                scanList.add(songDetails);
                            }
                        } while (mediaCursor.moveToNext());
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            } else {
                throw new RuntimeException("Failed to retrieve media files.");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(Void param){
            ListUtil.sortArrayList(scanList, "songTitle", false, true);
            FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/song.json"), ListUtil.setArrayListToSharedJSON(scanList));
            startActivity(new Intent(ApplicationUtil.getAppContext(), SplashActivity.class));
            finish();
        }

    }

}
