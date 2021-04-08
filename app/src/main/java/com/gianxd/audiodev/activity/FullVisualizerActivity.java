package com.gianxd.audiodev.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.RippleDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.gianxd.audiodev.R;
import com.gianxd.audiodev.service.LocalPlaybackService;
import com.gianxd.audiodev.util.ApplicationUtil;
import com.gianxd.audiodev.util.ImageUtil;
import com.gianxd.audiodev.util.ListUtil;
import com.gianxd.audiodev.util.StringUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class FullVisualizerActivity extends AppCompatActivity {

    private ServiceConnection musicConnection;
    private LocalPlaybackService playbackSrv;
    private Intent playIntent;
    private boolean musicBound = false;

    private ImageView back;
    private BarVisualizer visualizer;

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
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
                view.setBackground(rippleButton);
                finish();
            }
        });
    }

    private void initializeLogic() {
        musicConnection = new ServiceConnection(){
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LocalPlaybackService.MusicBinder binder = (LocalPlaybackService.MusicBinder)service;
                playbackSrv = binder.getService();
                musicBound = true;
                if (playbackSrv.mp != null && playbackSrv.isPlaying()) {
                    if (playbackSrv.mp.getAudioSessionId() != -1) {
                        visualizer.setAudioSessionId(playbackSrv.mp.getAudioSessionId());
                    }
                } else  if (playbackSrv.mp != null && !playbackSrv.isPlaying()){
                    ApplicationUtil.toast(getApplicationContext(), "Visualizer not visible, please resume/play the song.", Toast.LENGTH_LONG);
                } else if (playbackSrv.mp != null) {
                    ApplicationUtil.toast(getApplicationContext(), "Failed to initialize Visualizer.", Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicBound = false;
            }
        };
        if (playIntent == null) {
            playIntent = new Intent(this, LocalPlaybackService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        } else {
            if (playbackSrv != null) {
                playIntent = new Intent(this, LocalPlaybackService.class);
                unbindService(musicConnection);
                stopService(playIntent);
                // Restart service
                bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
                startService(playIntent);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (playbackSrv.mp != null && visualizer != null) {
            visualizer.release();
        }
        finish();
    }

    @Override
    public void onDestroy() {
        if (playbackSrv.mp != null && visualizer != null) {
            visualizer.release();
        }
        super.onDestroy();
    }

}