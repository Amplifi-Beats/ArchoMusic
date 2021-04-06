package com.gianxd.audiodev.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.gianxd.audiodev.R;
import com.gianxd.audiodev.util.ApplicationUtil;
import com.gianxd.audiodev.util.ImageUtil;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class IntentFilterActivity extends  AppCompatActivity  { 
	
	private Timer timer = new Timer();
	
	private LinearLayout main;
	private TextView logoName;
	private LinearLayout miniplayer;
	private CardView miniplayerCorneredView;
	private LinearLayout bruz;
	private ImageView miniplayerPlayPause;
	private ImageView miniplayerAlbumArt;
	private TextView miniplayerSongTitle;
	private LinearLayout bb;
	private TextView currentDuration;
	private SeekBar seekbarDuration;
	private TextView maxDuration;
	
	private MediaPlayer mp;
    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener audioChangeListener;
	private TimerTask timerTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_intent_filter);
		initialize(savedInstanceState);
		com.google.firebase.FirebaseApp.initializeApp(this);
		initializeLogic();
	}
	
	private void initialize(Bundle savedInstanceState) {
		main = (LinearLayout) findViewById(R.id.main);
		logoName = (TextView) findViewById(R.id.logoName);
		miniplayer = (LinearLayout) findViewById(R.id.miniplayer);
		miniplayerCorneredView = (CardView) findViewById(R.id.miniplayerCorneredView);
		bruz = (LinearLayout) findViewById(R.id.bruz);
		miniplayerPlayPause = (ImageView) findViewById(R.id.miniplayerPlayPause);
		miniplayerAlbumArt = (ImageView) findViewById(R.id.miniplayerAlbumArt);
		miniplayerSongTitle = (TextView) findViewById(R.id.miniplayerSongTitle);
		bb = (LinearLayout) findViewById(R.id.bb);
		currentDuration = (TextView) findViewById(R.id.currentDuration);
		seekbarDuration = (SeekBar) findViewById(R.id.seekbarDuration);
		maxDuration = (TextView) findViewById(R.id.maxDuration);
		miniplayerPlayPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (mp != null) {
					if (!mp.isPlaying()) {
						miniplayerPlayPause.setImageResource(R.drawable.ic_media_pause);
						timerTask = new TimerTask() {
							@Override
							public void run() {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										currentDuration.setText(String.valueOf((int)((mp.getCurrentPosition() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((mp.getCurrentPosition() / 1000) % 60))));
										seekbarDuration.setProgress((int)mp.getCurrentPosition());
									}
								});
							}
						};
						timer.scheduleAtFixedRate(timerTask, (int)(0), (int)(1000));
						mp.start();
					}
					else {
						miniplayerPlayPause.setImageResource(R.drawable.ic_media_play);
						mp.pause();
						if (timerTask != null) {
							timerTask.cancel();
						}
					}
				}
			}
		});
		
		seekbarDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				currentDuration.setText(String.valueOf((int)((seekbarDuration.getProgress() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((seekbarDuration.getProgress() / 1000) % 60))));
				seekbarDuration.setProgress((int)seekbarDuration.getProgress());
				mp.seekTo((int)(seekbarDuration.getProgress()));
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
		} catch (Exception e){
		    ApplicationUtil.toast(getApplicationContext(), "Error loading audio file.", Toast.LENGTH_LONG);
            finish();
		}
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
		switch (_requestCode) {
			default:
			break;
		}
	}
	
	
	@Override
	public void onPause() {
		super.onPause();
		if (mp != null) {
			audioManager.abandonAudioFocus(audioChangeListener);
			if (mp.isPlaying()) {
				miniplayer.performClick();
			}
			mp.reset();
			mp.release();
		}
	}
	public void startupUI() {
		logoName.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/leixo.ttf"), Typeface.BOLD);
		miniplayerPlayPause.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
	}
	
	
	public void startupMP(Uri data) {
		if (mp != null) {
			audioManager.abandonAudioFocus(audioChangeListener);
			if (mp.isPlaying()) {
				miniplayerPlayPause.performClick();
			}
			mp.reset();
			mp.release();
		}
		if (audioManager == null) {
			audioManager = ((AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
		}
		mp = MediaPlayer.create(getApplicationContext(), data);
		mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
						finish();
				}
		});
		audioChangeListener = new AudioManager.OnAudioFocusChangeListener() {
				@Override
			    public void onAudioFocusChange(int focusChange) {
						switch (focusChange) {
								case AudioManager.AUDIOFOCUS_LOSS:
								    if (mp.isPlaying()) {
											miniplayerPlayPause.performClick();
									}
								    break;
						}
				}
		};
		Glide.with(getApplicationContext()).asBitmap().load(ImageUtil.getAlbumArt(getApplicationContext(), data, getResources())).into(miniplayerAlbumArt);
		audioManager.requestAudioFocus(audioChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		miniplayerSongTitle.setText(data.getLastPathSegment());
		maxDuration.setText(String.valueOf((int)((mp.getDuration() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((mp.getDuration() / 1000) % 60))));
		currentDuration.setText(String.valueOf((int)((mp.getCurrentPosition() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((mp.getCurrentPosition() / 1000) % 60))));
		seekbarDuration.setMax((int)mp.getDuration());
		seekbarDuration.setProgress((int)mp.getCurrentPosition());
		miniplayerPlayPause.performClick();
	}
	
	
	public void _javaReferences () {
	}
	
	
	public void _xmlReferences () {
		
	}
	
	
	
}
