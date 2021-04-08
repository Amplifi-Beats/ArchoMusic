package com.gianxd.audiodev.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.gianxd.audiodev.R;
import com.gianxd.audiodev.util.ApplicationUtil;
import com.gianxd.audiodev.util.ListUtil;
import com.gianxd.audiodev.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class FullVisualizerActivity extends AppCompatActivity {

    private ArrayList<HashMap<String, Object>> musicData;
    private HashMap<String, Object> profileData;

    private ImageView back;
    private BarVisualizer visualizer;

    private SharedPreferences savedData;
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_visualizer);
        initialize(savedInstanceState);
        com.google.firebase.FirebaseApp.initializeApp(this);
        initializeLogic();
    }

    private void initialize(Bundle savedInstanceState) {
        back = (ImageView) findViewById(R.id.back);
        visualizer = (BarVisualizer) findViewById(R.id.visualizer);
        savedData = getSharedPreferences("savedData", Context.MODE_PRIVATE);
        mp = new MediaPlayer();
        if (savedData.contains("savedMusicData")) {
            musicData = ListUtil.getArrayListFromSharedJSON(savedData, "savedMusicData");
        } else {
            musicData = new ArrayList<>();
        }
        if (savedData.contains("savedProfileData")) {
            profileData = ListUtil.getHashMapFromSharedJSON(savedData, "savedProfileData");
        } else {
            profileData = new HashMap<>();
        }
    }

    private void initializeLogic() {
        if (profileData.containsKey("profileSongPosition")) {
            mp = MediaPlayer.create(getApplicationContext(), Uri.fromFile(new File(StringUtil.decodeString(musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).get("songData").toString()))));
            if (musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).containsKey("songCurrentDuration")) {
                mp.seekTo(Integer.parseInt(musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).get("songCurrentDuration").toString()));
            }
            if (mp.getAudioSessionId() != -1) {
                visualizer.setAudioSessionId(mp.getAudioSessionId());
            }
            if (mp != null) {
                if (!mp.isPlaying()) {
                    ApplicationUtil.toast(getApplicationContext(), "Visualizer not visible, please resume/play the song.", Toast.LENGTH_LONG);
                }
            }
        } else {
            ApplicationUtil.toast(getApplicationContext(), "Failed to initialize Visualizer.", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onBackPressed() {
        if (mp != null && visualizer != null) {
            visualizer.release();
        }
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mp != null && visualizer != null) {
            visualizer.release();
        }
    }

}