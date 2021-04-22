package tk.gianxddddd.audiodev.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import tk.gianxddddd.audiodev.R;
import tk.gianxddddd.audiodev.service.LocalPlaybackService;
import tk.gianxddddd.audiodev.util.ApplicationUtil;
import com.google.firebase.FirebaseApp;

public class FullVisualizerActivity extends AppCompatActivity {

    LocalPlaybackService playbackSrv;
    Intent playIntent;
    boolean musicBound = false;

    BarVisualizer visualizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_visualizer);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);

        initialize();
        FirebaseApp.initializeApp(this);
        initializeLogic();
    }

    private void initialize() {
        ImageView back = findViewById(R.id.back);
        visualizer = findViewById(R.id.visualizer);

        back.setOnClickListener(view -> {
            RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
            view.setBackground(rippleButton);

            finish();
        });
    }

    private void initializeLogic() {
        ServiceConnection musicConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LocalPlaybackService.MusicBinder binder = (LocalPlaybackService.MusicBinder) service;

                playbackSrv = binder.getService();
                musicBound = true;

                if (playbackSrv.mp != null && playbackSrv.isPlaying()) {
                    if (playbackSrv.mp.getAudioSessionId() != -1) {
                        visualizer.setAudioSessionId(playbackSrv.mp.getAudioSessionId());
                    }

                } else if (playbackSrv.mp != null && !playbackSrv.isPlaying()) {
                    ApplicationUtil.toast("Visualizer not visible, please resume/play the song.", Toast.LENGTH_LONG, FullVisualizerActivity.this);

                } else if (playbackSrv.mp != null) {
                    ApplicationUtil.toast("Failed to initialize Visualizer.", Toast.LENGTH_LONG, FullVisualizerActivity.this);
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