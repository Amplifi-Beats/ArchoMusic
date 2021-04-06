package com.gianxd.audiodev.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.gianxd.audiodev.R;
import com.gianxd.audiodev.activity.LocalStreamActivity;
import com.gianxd.audiodev.util.ImageUtil;
import com.gianxd.audiodev.util.ListUtil;
import com.gianxd.audiodev.util.StringUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import static com.gianxd.audiodev.activity.LocalStreamActivity.albumArt;
import static com.gianxd.audiodev.activity.LocalStreamActivity.currentDuration;
import static com.gianxd.audiodev.activity.LocalStreamActivity.maxDuration;
import static com.gianxd.audiodev.activity.LocalStreamActivity.miniplayerAlbumArt;
import static com.gianxd.audiodev.activity.LocalStreamActivity.miniplayerPlayPause;
import static com.gianxd.audiodev.activity.LocalStreamActivity.miniplayerSeekbar;
import static com.gianxd.audiodev.activity.LocalStreamActivity.miniplayerSongArtist;
import static com.gianxd.audiodev.activity.LocalStreamActivity.miniplayerSongTitle;
import static com.gianxd.audiodev.activity.LocalStreamActivity.playPause;
import static com.gianxd.audiodev.activity.LocalStreamActivity.seekbarDuration;
import static com.gianxd.audiodev.activity.LocalStreamActivity.songArtist;
import static com.gianxd.audiodev.activity.LocalStreamActivity.songTitle;

public class LocalPlaybackService extends Service {

	public MediaPlayer mp;
	private final IBinder musicBind = new MusicBinder();
	private ArrayList<HashMap<String, Object>> musicData = new ArrayList<>();
	private static final int NOTIFY_ID = 1;
	private NotificationChannel notificationChannel;
	private NotificationManager notificationManager;
	private AudioManager audioManager;
	private AudioManager.OnAudioFocusChangeListener audioChangeListener;
	private SharedPreferences savedData;
	
	public void onCreate(){
		super.onCreate();
		initArrayList();
	}
	
	public class MusicBinder extends Binder {
		public LocalPlaybackService getService() {
			return LocalPlaybackService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return musicBind;
	}
	
	@Override
	public boolean onUnbind(Intent intent){
		return false;
	}
	
	@Override
	public void onTaskRemoved(Intent intent) {
		if (mp != null) {
			audioManager.abandonAudioFocus(audioChangeListener);
			if (isPlaying()) {
				playPause.performClick();
			}
			mp.reset();
			mp.release();
		}
		endService();
	}
	
	public void endService() {
		if (notificationManager != null) {
			notificationManager.cancelAll();
			
		}
		stopSelf();
	}
	
	public void createLocalStream(int position) {
        if (mp != null) {
		    audioManager.abandonAudioFocus(audioChangeListener);
	        if (isPlaying()) {
			    playPause.performClick();
	        }
	        mp.reset();
	        mp.release();
        }
	    if (audioManager == null) {
		    audioManager = ((AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
	    }
		mp = MediaPlayer.create(getApplicationContext(), Uri.fromFile(new File(StringUtil.decodeString(musicData.get(position).get("songData").toString()))));
		mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				HashMap<String, Object> profileData;
				if (savedData.contains("savedProfileData")) {
					profileData = ListUtil.getHashMapFromSharedJSON(savedData, "savedProfileData");
				} else {
					profileData = new HashMap<>();
				}
				playPause.setImageResource(R.drawable.ic_media_play);
		        miniplayerPlayPause.setImageResource(R.drawable.ic_media_play);
				try {
					if (position + 1 < musicData.size()) {
						profileData.put("lastSongItemPosition", String.valueOf(position + 1));
						savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).apply();
						createLocalStream(position + 1);
						playPause.performClick();
					}
				} catch (Exception e) {
				    if (position + 1 < musicData.size()) {
						profileData.put("lastSongItemPosition", String.valueOf(position + 1));
						savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).apply();
						createLocalStream(position + 1);
						playPause.performClick();
					}
			    }
			}
		});
		audioChangeListener = new AudioManager.OnAudioFocusChangeListener() {
			@Override
			public void onAudioFocusChange(int focusChange) {
				switch (focusChange) {
				case AudioManager.AUDIOFOCUS_LOSS:
					if (isPlaying()) {
						playPause.performClick();
					}
					break;
				}
			}
		};
		audioManager.requestAudioFocus(audioChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		Glide.with(getApplicationContext()).asBitmap().load(ImageUtil.getAlbumArt(musicData.get(position).get("songData").toString(), getResources())).into(albumArt);
		Glide.with(getApplicationContext()).asBitmap().load(ImageUtil.getAlbumArt(musicData.get(position).get("songData").toString(), getResources())).into(miniplayerAlbumArt);
        songTitle.setText(musicData.get(position).get("songTitle").toString());
        songArtist.setText(musicData.get(position).get("songArtist").toString());
        miniplayerSongTitle.setText(musicData.get(position).get("songTitle").toString());
        miniplayerSongArtist.setText(musicData.get(position).get("songArtist").toString());
        miniplayerSeekbar.setMax(getMaxDuration());
        miniplayerSeekbar.setProgress(getCurrentPosition());
        maxDuration.setText(String.valueOf((long)((getMaxDuration() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((getMaxDuration() / 1000) % 60))));
        currentDuration.setText(String.valueOf((long)((getCurrentPosition() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((getCurrentPosition() / 1000) % 60))));
        seekbarDuration.setMax(getMaxDuration());
        seekbarDuration.setProgress(getCurrentPosition());
	    Intent notIntent = new Intent(this, LocalStreamActivity.class);
	    notIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	    if (Build.VERSION.SDK_INT < 28 ) {
		    Notification notification = new Notification.Builder(getApplicationContext())
				 .setContentTitle(musicData.get(position).get("songTitle").toString())
				 .setContentText("by ".concat(musicData.get(position).get("songArtist").toString()))
			     .setSmallIcon(R.mipmap.ic_launcher_round)
				 .setContentIntent(pendInt)
				. build();
		    startForeground(NOTIFY_ID, notification);
	    } else {
	        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "libaudiodev");
		    notificationChannel = new NotificationChannel("libaudiodev", "AudioSession", NotificationManager.IMPORTANCE_LOW);
		    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert notificationManager != null;
		    assert notificationChannel != null;
            notificationManager.createNotificationChannel(notificationChannel);
		    notificationBuilder.setNumber(0);
		    Notification notification = notificationBuilder.setOngoing(true)
				    .setContentIntent(pendInt)
				    .setPriority(NotificationManager.IMPORTANCE_LOW)
				    .setNumber(0)
				    .setCategory(Notification.CATEGORY_SERVICE)
				    .setSmallIcon(R.mipmap.ic_launcher_round)
				    .setLargeIcon(ImageUtil.getAlbumArt(StringUtil.decodeString(musicData.get(position).get("songData").toString()), getResources()))
				    .setContentText("by ".concat(musicData.get(position).get("songArtist").toString()))
				    .setContentTitle(musicData.get(position).get("songTitle").toString())
				    .build();
		    startForeground(NOTIFY_ID, notification);
	    }
		
	}
	
	public void initArrayList() {
		savedData = getSharedPreferences("savedData", Context.MODE_PRIVATE);
		musicData = ListUtil.getArrayListFromSharedJSON(savedData, "savedMusicData");
	}
	
	public int getCurrentPosition(){
		return mp.getCurrentPosition();
	}
	
	public int getMaxDuration(){
		return mp.getDuration();
	}
	
	public boolean isPlaying(){
		return mp.isPlaying();
	}
	
	public void pause(){
		mp.pause();
	}
	
	public void seek(int position){
		mp.seekTo(position);
	}
	
	public void play(){
		mp.start();
	}
	
	@Override
	public void onDestroy() {
		stopForeground(true);
	}
	
}
