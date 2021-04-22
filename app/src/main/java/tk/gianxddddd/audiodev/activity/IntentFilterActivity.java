package tk.gianxddddd.audiodev.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import tk.gianxddddd.audiodev.R;
import tk.gianxddddd.audiodev.util.ApplicationUtil;
import tk.gianxddddd.audiodev.util.ImageUtil;
import com.google.firebase.FirebaseApp;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class IntentFilterActivity extends AppCompatActivity {

    final Timer timer = new Timer();

    TextView logoName;

    LinearLayout miniPlayer;
    ImageView miniPlayerPlayPause;
    ImageView miniPlayerAlbumArt;
    TextView miniPlayerSongTitle;

    TextView currentDuration;
    SeekBar seekBarDuration;
    TextView maxDuration;

    MediaPlayer mp;

    AudioManager audioManager;
    AudioManager.OnAudioFocusChangeListener audioChangeListener;

    TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_intent_filter);

        initialize();
        FirebaseApp.initializeApp(this);
        initializeLogic();
    }

    private void initialize() {
        logoName = findViewById(R.id.logoName);
        miniPlayer = findViewById(R.id.miniplayer);
        miniPlayerPlayPause = findViewById(R.id.miniplayerPlayPause);
        miniPlayerAlbumArt = findViewById(R.id.miniplayerAlbumArt);
        miniPlayerSongTitle = findViewById(R.id.miniplayerSongTitle);
        currentDuration = findViewById(R.id.currentDuration);
        seekBarDuration = findViewById(R.id.seekbarDuration);
        maxDuration = findViewById(R.id.maxDuration);

        miniPlayerPlayPause.setOnClickListener(view -> {
            if (mp != null) {
                if (!mp.isPlaying()) {
                    miniPlayerPlayPause.setImageResource(R.drawable.ic_media_pause);

                    timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(() -> {
                                currentDuration.setText(String.valueOf((mp.getCurrentPosition() / 1000) / 60).concat(":".concat(new DecimalFormat("00").format((mp.getCurrentPosition() / 1000) % 60))));
                                seekBarDuration.setProgress(mp.getCurrentPosition());
                            });
                        }
                    };

                    timer.scheduleAtFixedRate(timerTask, 0, 1000);
                    mp.start();

                } else {
                    miniPlayerPlayPause.setImageResource(R.drawable.ic_media_play);
                    mp.pause();

                    if (timerTask != null) {
                        timerTask.cancel();
                    }
                }
            }
        });

        seekBarDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                currentDuration.setText(String.valueOf((seekBarDuration.getProgress() / 1000) / 60).concat(":".concat(new DecimalFormat("00").format((seekBarDuration.getProgress() / 1000) % 60))));
                seekBarDuration.setProgress(seekBarDuration.getProgress());
                mp.seekTo(seekBarDuration.getProgress());
            }
        });
    }

    private void initializeLogic() {
        startupUI();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        Intent intent = getIntent();
        Uri data = intent.getData();

        try {
            startupMP(data);
        } catch (Exception e) {
            ApplicationUtil.toast("Error loading audio file.", Toast.LENGTH_LONG, this);
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mp != null) {
            audioManager.abandonAudioFocus(audioChangeListener);

            if (mp.isPlaying()) {
                miniPlayer.performClick();
            }

            mp.reset();
            mp.release();
        }
    }

    public void startupUI() {
        logoName.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/leixo.ttf"), Typeface.BOLD);
        miniPlayerPlayPause.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
    }


    public void startupMP(Uri data) {
        if (mp != null) {
            audioManager.abandonAudioFocus(audioChangeListener);

            if (mp.isPlaying()) {
                miniPlayerPlayPause.performClick();
            }

            mp.reset();
            mp.release();
        }

        if (audioManager == null) {
            audioManager = ((AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
        }

        mp = MediaPlayer.create(getApplicationContext(), data);
        mp.setOnCompletionListener(mp -> finish());

        audioChangeListener = focusChange -> {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                if (mp.isPlaying()) {
                    miniPlayerPlayPause.performClick();
                }
            }
        };

        Glide.with(this).asBitmap().load(ImageUtil.getAlbumArt(data, this)).into(miniPlayerAlbumArt);

        audioManager.requestAudioFocus(audioChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        miniPlayerSongTitle.setText(data.getLastPathSegment());
        maxDuration.setText(String.valueOf((mp.getDuration() / 1000) / 60).concat(":".concat(new DecimalFormat("00").format((mp.getDuration() / 1000) % 60))));
        currentDuration.setText(String.valueOf((mp.getCurrentPosition() / 1000) / 60).concat(":".concat(new DecimalFormat("00").format((mp.getCurrentPosition() / 1000) % 60))));

        seekBarDuration.setMax(mp.getDuration());
        seekBarDuration.setProgress(mp.getCurrentPosition());

        miniPlayerPlayPause.performClick();
    }

}
