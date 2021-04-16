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
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.gianxd.audiodev.R;
import com.gianxd.audiodev.activity.LocalStreamActivity;
import com.gianxd.audiodev.util.ApplicationUtil;
import com.gianxd.audiodev.util.ImageUtil;
import com.gianxd.audiodev.util.IntegerUtil;
import com.gianxd.audiodev.util.ListUtil;
import com.gianxd.audiodev.util.StringUtil;

import java.io.File;
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
	private ArrayList<HashMap<String, Object>> musicData;
	private HashMap<String, Object> profileData;
	private static final int NOTIFY_ID = 1;
	private NotificationChannel notificationChannel;
	private NotificationManager notificationManager;
	private AudioManager audioManager;
	private AudioManager.OnAudioFocusChangeListener audioChangeListener;
	private SharedPreferences savedData;
	
	public void onCreate(){
		super.onCreate();
		initObjects();
		loadAudioManager();
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
	    loadAudioManager();
		mp = MediaPlayer.create(getApplicationContext(), Uri.fromFile(new File(StringUtil.decodeString(musicData.get(position).get("songData").toString()))));
		updateOnCompletionListener();
		Glide.with(getApplicationContext()).asBitmap().load(ImageUtil.getAlbumArt(StringUtil.decodeString(musicData.get(position).get("songData").toString()))).into(albumArt);
		Glide.with(getApplicationContext()).asBitmap().load(ImageUtil.getAlbumArt(StringUtil.decodeString(musicData.get(position).get("songData").toString()))).into(miniplayerAlbumArt);
		songTitle.setText(musicData.get(position).get("songTitle").toString());
		songArtist.setText(musicData.get(position).get("songArtist").toString());
		miniplayerSongTitle.setText(musicData.get(position).get("songTitle").toString());
		miniplayerSongArtist.setText(musicData.get(position).get("songArtist").toString());
		if (Build.VERSION.SDK_INT >= 24) {
			miniplayerSeekbar.setProgress(getCurrentPosition(), true);
			seekbarDuration.setProgress(getCurrentPosition(), true);
		} else {
			miniplayerSeekbar.setProgress(getCurrentPosition());
			seekbarDuration.setProgress(getCurrentPosition());
		}
		miniplayerSeekbar.setMax(getMaxDuration());
		maxDuration.setText(String.valueOf((int)((getMaxDuration() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((getMaxDuration() / 1000) % 60))));
		currentDuration.setText(String.valueOf((int)((getCurrentPosition() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((getCurrentPosition() / 1000) % 60))));
		seekbarDuration.setMax(getMaxDuration());
	    Intent notIntent = new Intent(this, LocalStreamActivity.class);
	    notIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	    if (Build.VERSION.SDK_INT < 28 ) {
		    Notification notification = new Notification.Builder(getApplicationContext())
				 .setContentTitle(musicData.get(position).get("songTitle").toString())
				 .setContentText("by ".concat(musicData.get(position).get("songArtist").toString()))
			     .setSmallIcon(R.drawable.ic_media_notification)
				 .setContentIntent(pendInt)
				. build();
		    startForeground(NOTIFY_ID, notification);
	    } else {
	        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "libaudiodev");
		    notificationChannel = new NotificationChannel("libaudiodev", "Music Player", NotificationManager.IMPORTANCE_LOW);
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
				    .setSmallIcon(R.drawable.ic_media_notification)
				    .setLargeIcon(ImageUtil.getAlbumArt(StringUtil.decodeString(musicData.get(position).get("songData").toString())))
				    .setContentText("by ".concat(musicData.get(position).get("songArtist").toString()))
				    .setContentTitle(musicData.get(position).get("songTitle").toString())
				    .build();
		    startForeground(NOTIFY_ID, notification);
	    }
		
	}
	
	public void initObjects() {
		savedData = getSharedPreferences("savedData", Context.MODE_PRIVATE);
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

	public void loadAudioManager() {
		if (audioManager == null) {
			audioManager = ((AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
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
		}
	}

	public void startAudioFocus() {
		audioManager.requestAudioFocus(audioChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
	}

	public void loseAudioFocus() {
		audioManager.abandonAudioFocus(audioChangeListener);
	}

	public void updateOnCompletionListener() {
		mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				playPause.setImageResource(R.drawable.ic_media_play);
				miniplayerPlayPause.setImageResource(R.drawable.ic_media_play);
				if (!profileData.containsKey("profileRepeatMode") || !profileData.containsKey("profileShuffleMode")) {
					try {
						if (Integer.parseInt(profileData.get("profileSongPosition").toString()) + 1< musicData.size()) {
							profileData.put("profileSongPosition", String.valueOf(Integer.parseInt(profileData.get("profileSongPosition").toString()) + 1));
							savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).commit();
							createLocalStream(Integer.parseInt(profileData.get("profileSongPosition").toString()));
							playPause.performClick();
						}
					} catch (Exception exception) {
						if (Integer.parseInt(profileData.get("profileSongPosition").toString()) + 1 < musicData.size()) {
							profileData.put("profileSongPosition", String.valueOf(Integer.parseInt(profileData.get("profileSongPosition").toString()) + 1));
							savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).commit();
							createLocalStream(Integer.parseInt(profileData.get("profileSongPosition").toString()));
							playPause.performClick();
						}
					}
				} else {
					if (profileData.get("profileRepeatMode").equals("0") && profileData.get("profileShuffleMode").equals("0")) {
						try {
							if (Integer.parseInt(profileData.get("profileSongPosition").toString()) + 1 < musicData.size()) {
								profileData.put("profileSongPosition", String.valueOf(Integer.parseInt(profileData.get("profileSongPosition").toString()) + 1));
								savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).commit();
								createLocalStream(Integer.parseInt(profileData.get("profileSongPosition").toString()));
								playPause.performClick();
							}
						} catch (Exception exception) {
							if (Integer.parseInt(profileData.get("profileSongPosition").toString()) + 1 < musicData.size()) {
								profileData.put("profileSongPosition", String.valueOf(Integer.parseInt(profileData.get("profileSongPosition").toString()) + 1));
								savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).commit();
								createLocalStream(Integer.parseInt(profileData.get("profileSongPosition").toString()));
								playPause.performClick();
							}
						}
					} else if (profileData.get("profileRepeatMode").equals("1")) {
						seek(0);
						if (Build.VERSION.SDK_INT >= 24) {
							miniplayerSeekbar.setProgress(0, true);
							seekbarDuration.setProgress(0, true);
						} else {
							miniplayerSeekbar.setProgress(0);
							seekbarDuration.setProgress(0);
						}
						currentDuration.setText("0:00");
					} else if (profileData.get("profileShuffleMode").equals("1")) {
						try {
							if (IntegerUtil.getRandom(Integer.parseInt(profileData.get("profileSongPosition").toString()), musicData.size()) < musicData.size()) {
								profileData.put("profileSongPosition", String.valueOf(IntegerUtil.getRandom(Integer.parseInt(profileData.get("profileSongPosition").toString()), musicData.size())));
								savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).commit();
								createLocalStream(Integer.parseInt(profileData.get("profileSongPosition").toString()));
								playPause.performClick();
							}
						} catch (Exception exception) {
							if (IntegerUtil.getRandom(Integer.parseInt(profileData.get("profileSongPosition").toString()), musicData.size()) < musicData.size()) {
								profileData.put("profileSongPosition", String.valueOf(IntegerUtil.getRandom(Integer.parseInt(profileData.get("profileSongPosition").toString()), musicData.size())));
								savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).commit();
								createLocalStream(Integer.parseInt(profileData.get("profileSongPosition").toString()));
								playPause.performClick();
							}
						}
					}
				}
			}
		});
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
