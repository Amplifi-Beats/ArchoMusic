package com.gianxd.audiodev.activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.gianxd.audiodev.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class PlaybackService extends Service {

	/* I was lazy to fix these stupid errors so I moved the class here. If you want to fix it, please download the Github repo and then make a directory and put this class there */

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
		PlaybackService getService() { 
			return PlaybackService.this;
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
				LocalStreamActivity.playPause.performClick();
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
	
	public void createLocalStream(int position){
       if (mp != null) {
		   audioManager.abandonAudioFocus(audioChangeListener);
	       if (isPlaying()) {
			   LocalStreamActivity.playPause.performClick();
	       }
	       mp.reset();
	       mp.release();
       }
	   if (audioManager == null) {
		   audioManager = ((AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
	   }
	   String decodedData = "";
       savedData.edit().putString("savedSongPosition", String.valueOf(position)).apply();
	    if (!musicData.get(position).get("songData").toString().startsWith("/")) {
			try {
				decodedData = new String(android.util.Base64.decode(musicData.get(position).get("songData").toString(), android.util.Base64.DEFAULT), "UTF-8");
			} catch (Exception e) {
			    /* do nothing if it crashes. :cheems: */
			}
			mp = MediaPlayer.create(getApplicationContext(), Uri.fromFile(new java.io.File(decodedData)));
		} else {
		    mp = MediaPlayer.create(getApplicationContext(), Uri.fromFile(new java.io.File(musicData.get(position).get("songData").toString())));
		}
		mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				LocalStreamActivity.playPause.setImageResource(R.drawable.ic_media_play);
		        LocalStreamActivity.miniplayerPlayPause.setImageResource(R.drawable.ic_media_play);
				try {
					if (position + 1 < musicData.size()) {
						createLocalStream(position + 1);
						LocalStreamActivity.playPause.performClick();
					}
				} catch (Exception e) {
				    if (position + 1 < musicData.size()) {
						createLocalStream(position + 1);
						LocalStreamActivity.playPause.performClick();
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
						LocalStreamActivity.playPause.performClick();
					}
					break;
				}
			}
		};
		audioManager.requestAudioFocus(audioChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
       try {
	       MediaMetadataRetriever artRetriever = new MediaMetadataRetriever(); 
	       if (!musicData.get(position).get("songData").toString().startsWith("/")) {
			   artRetriever.setDataSource(decodedData);
		   } else {
		       artRetriever.setDataSource(musicData.get(position).get("songData").toString());
		   }
	       byte[] album_art = artRetriever.getEmbeddedPicture(); 
	       if( album_art != null ){
		       Bitmap bitmapArt = BitmapFactory.decodeByteArray(album_art, 0, album_art.length); 
		       Glide.with(getApplicationContext()).asBitmap().load(bitmapArt).into(LocalStreamActivity.albumArt);
		       Glide.with(getApplicationContext()).asBitmap().load(bitmapArt).into(LocalStreamActivity.miniplayerAlbumArt);
	       } else { 
		       Glide.with(getApplicationContext()).asBitmap().load(R.drawable.ic_media_album_art).into(LocalStreamActivity.albumArt);
		       Glide.with(getApplicationContext()).asBitmap().load(R.drawable.ic_media_album_art).into(LocalStreamActivity.miniplayerAlbumArt);
	       }
       } catch (Exception e) {
	       /* apply default image art if song has no album art */
		   Glide.with(getApplicationContext()).asBitmap().load(R.drawable.ic_media_album_art).into(LocalStreamActivity.albumArt);
		   Glide.with(getApplicationContext()).asBitmap().load(R.drawable.ic_media_album_art).into(LocalStreamActivity.miniplayerAlbumArt);
       }
       LocalStreamActivity.songTitle.setText(musicData.get(position).get("songTitle").toString());
       LocalStreamActivity.songArtist.setText(musicData.get(position).get("songArtist").toString());
       LocalStreamActivity.miniplayerSongTitle.setText(musicData.get(position).get("songTitle").toString());
       LocalStreamActivity.miniplayerSongArtist.setText(musicData.get(position).get("songArtist").toString());
       LocalStreamActivity.miniplayerSeekbar.setMax(getMaxDuration());
       LocalStreamActivity.miniplayerSeekbar.setProgress(getCurrentPosition());
       LocalStreamActivity.maxDuration.setText(String.valueOf((long)((getMaxDuration() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((getMaxDuration() / 1000) % 60))));
       LocalStreamActivity.currentDuration.setText(String.valueOf((long)((getCurrentPosition() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((getCurrentPosition() / 1000) % 60))));
       LocalStreamActivity.seekbarDuration.setMax(getMaxDuration());
       LocalStreamActivity.seekbarDuration.setProgress(getCurrentPosition());
	   Intent notIntent = new Intent(this, LocalStreamActivity.class);
	   notIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	   PendingIntent pendInt = PendingIntent.getActivity(this, 0,
	   notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	   if (Build.VERSION.SDK_INT < 28 ) {
		   Notification notification = new Notification.Builder(getApplicationContext())
				.setContentTitle(musicData.get(position).get("songTitle").toString())
				.setContentText("by ".concat(musicData.get(position).get("songArtist").toString()))
			    .setSmallIcon(R.mipmap.ic_launcher)
				.setContentIntent(pendInt)
				.build();
		   startForeground(NOTIFY_ID, notification);
	   } else {
	       NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "libaudiodev");
		   notificationChannel = new NotificationChannel("libaudiodev", "AudioSession", NotificationManager.IMPORTANCE_LOW);
		   notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
           assert notificationManager != null;
		   assert notificationChannel != null;
           notificationManager.createNotificationChannel(notificationChannel);
		   notificationBuilder.setNumber(0);
	       if (!musicData.get(position).get("songData").toString().startsWith("/")) {
			   Notification notification = notificationBuilder.setOngoing(true)
		       .setContentIntent(pendInt)
               .setPriority(NotificationManager.IMPORTANCE_LOW)
		       .setNumber(0)
               .setCategory(Notification.CATEGORY_SERVICE)
			   .setSmallIcon(R.mipmap.ic_launcher)
			   .setLargeIcon(getAlbumArt(decodedData))
			   .setContentText("by ".concat(musicData.get(position).get("songArtist").toString()))
               .setContentTitle(musicData.get(position).get("songTitle").toString())
			   .build();
			   startForeground(NOTIFY_ID, notification);
		   } else {
		       Notification notification = notificationBuilder.setOngoing(true)
		       .setContentIntent(pendInt)
               .setPriority(NotificationManager.IMPORTANCE_LOW)
		       .setNumber(0)
               .setCategory(Notification.CATEGORY_SERVICE)
			   .setSmallIcon(R.mipmap.ic_launcher)
			   .setLargeIcon(getAlbumArt(musicData.get(position).get("songData").toString()))
			   .setContentText("by ".concat(musicData.get(position).get("songArtist").toString()))
               .setContentTitle(musicData.get(position).get("songTitle").toString())
			   .build();
			   startForeground(NOTIFY_ID, notification);
	       }
	   }
		
	}
	
	public void initArrayList() {
		savedData = getSharedPreferences("savedData", Context.MODE_PRIVATE);
		musicData = new Gson().fromJson(savedData.getString("savedMusicData", ""), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
	}
	
	public int getCurrentPosition(){
		return mp.getCurrentPosition();
	}
	
	public int getMaxDuration(){
		return mp.getDuration();
	}
	
	public Bitmap getAlbumArt(String path) {
		Bitmap bitmapArt;
		MediaMetadataRetriever artRetriever = new MediaMetadataRetriever(); 
        artRetriever.setDataSource(path); 
        byte[] album_art = artRetriever.getEmbeddedPicture(); 
        if( album_art != null ){
            bitmapArt = BitmapFactory.decodeByteArray(album_art, 0, album_art.length);
        } else {
			bitmapArt = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_media_album_art)).getBitmap();
	    }
		return bitmapArt;
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
