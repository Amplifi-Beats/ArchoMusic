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
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.gianxd.audiodev.R;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class IntentFilterActivity extends  AppCompatActivity  { 
	
	private Timer _timer = new Timer();
	
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
	private TimerTask timer;
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
						timer = new TimerTask() {
							@Override
							public void run() {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										currentDuration.setText(String.valueOf((long)((mp.getCurrentPosition() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((mp.getCurrentPosition() / 1000) % 60))));
										seekbarDuration.setProgress((int)mp.getCurrentPosition());
									}
								});
							}
						};
						_timer.scheduleAtFixedRate(timer, (int)(0), (int)(1000));
						mp.start();
					}
					else {
						miniplayerPlayPause.setImageResource(R.drawable.ic_media_play);
						mp.pause();
						if (timer != null) {
							timer.cancel();
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
				currentDuration.setText(String.valueOf((long)((seekbarDuration.getProgress() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((seekbarDuration.getProgress() / 1000) % 60))));
				seekbarDuration.setProgress((int)seekbarDuration.getProgress());
				mp.seekTo((int)(seekbarDuration.getProgress()));
			}
		});
	}
	
	private void initializeLogic() {
		_startupUI();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		try {
			Intent intent = getIntent();
			Uri data = intent.getData();
			_startupMP(data);
		} catch (Exception e){
			com.gianxd.musicdev.MusicDevUtil.showMessage(getApplicationContext(), "Failed to play selected audio file.");
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
	public void _startupUI () {
		logoName.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/leixo.ttf"), Typeface.BOLD);
		miniplayerPlayPause.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
	}
	
	
public void _startupMP (Uri data) {
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
		audioManager.requestAudioFocus(audioChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		try {
				MediaMetadataRetriever artRetriever = new MediaMetadataRetriever();
				artRetriever.setDataSource(data.toString());
				byte[] album_art = artRetriever.getEmbeddedPicture(); 
				if( album_art != null ){
						Bitmap bitmapArt = BitmapFactory.decodeByteArray(album_art, 0, album_art.length); 
						Glide.with(getApplicationContext())
.asBitmap().load(bitmapArt).into(miniplayerAlbumArt);
				} else {
						Glide.with(getApplicationContext()).asBitmap().load(R.drawable.ic_media_album_art).into(miniplayerAlbumArt);
				}
		} catch (Exception e) {
				// apply default image art if song has no album art
			    Glide.with(getApplicationContext()).asBitmap().load(R.drawable.ic_media_album_art).into(miniplayerAlbumArt);
		}
		miniplayerSongTitle.setText(data.getLastPathSegment());
		maxDuration.setText(String.valueOf((long)((mp.getDuration() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((mp.getDuration() / 1000) % 60))));
		currentDuration.setText(String.valueOf((long)((mp.getCurrentPosition() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((mp.getCurrentPosition() / 1000) % 60))));
		seekbarDuration.setMax((int)mp.getDuration());
		seekbarDuration.setProgress((int)mp.getCurrentPosition());
		miniplayerPlayPause.performClick();
	}
	
	
	public void _javaReferences () {
	}
	
	
	public void _xmlReferences () {
		
	}
	
	
	
}
