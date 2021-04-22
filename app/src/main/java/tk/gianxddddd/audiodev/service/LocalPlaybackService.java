package tk.gianxddddd.audiodev.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import tk.gianxddddd.audiodev.R;
import tk.gianxddddd.audiodev.activity.LocalStreamActivity;
import tk.gianxddddd.audiodev.receiver.HeadphonesReceiver;
import tk.gianxddddd.audiodev.util.ApplicationUtil;
import tk.gianxddddd.audiodev.util.FileUtil;
import tk.gianxddddd.audiodev.util.ImageUtil;
import tk.gianxddddd.audiodev.util.IntegerUtil;
import tk.gianxddddd.audiodev.util.ListUtil;
import tk.gianxddddd.audiodev.util.Base64Util;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.albumArt;
import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.currentDuration;
import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.maxDuration;
import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.miniplayerAlbumArt;
import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.miniplayerPlayPause;
import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.miniplayerSeekbar;
import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.miniplayerSongArtist;
import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.miniplayerSongTitle;
import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.playPause;
import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.seekbarDuration;
import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.songArtist;
import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.songTitle;

public class LocalPlaybackService extends Service {

    public MediaPlayer mp;
    final IBinder musicBind = new MusicBinder();

    IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    HeadphonesReceiver headphonesReceiver = new HeadphonesReceiver();

    ArrayList<HashMap<String, Object>> musicData;
    HashMap<String, Object> sessionData;

    static final int NOTIFY_ID = 1;

    NotificationChannel notificationChannel;
    NotificationManager notificationManager;

    AudioManager audioManager;
    AudioManager.OnAudioFocusChangeListener audioChangeListener;

    Resources.Theme theme;
    Resources resources;

    public void onCreate() {
        super.onCreate();

        theme = getTheme();
        resources = getResources();

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
    public boolean onUnbind(Intent intent) {
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

        mp = MediaPlayer.create(this, Uri.fromFile(new File(Base64Util.decode(musicData.get(position).get("songData").toString()))));
        updateOnCompletionListener();

        Glide.with(this).asBitmap().load(ImageUtil.getAlbumArt(Base64Util.decode(musicData.get(position).get("songData").toString()), resources, theme)).into(albumArt);
        Glide.with(this).asBitmap().load(ImageUtil.getAlbumArt(Base64Util.decode(musicData.get(position).get("songData").toString()), resources, theme)).into(miniplayerAlbumArt);

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
        maxDuration.setText(String.valueOf((getMaxDuration() / 1000) / 60).concat(":".concat(new DecimalFormat("00").format((getMaxDuration() / 1000) % 60))));
        currentDuration.setText(String.valueOf((getCurrentPosition() / 1000) / 60).concat(":".concat(new DecimalFormat("00").format((getCurrentPosition() / 1000) % 60))));
        seekbarDuration.setMax(getMaxDuration());

        Intent notIntent = new Intent(this, LocalStreamActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT < 28) {
            Notification notification = new Notification.Builder(this)
                    .setContentTitle(musicData.get(position).get("songTitle").toString())
                    .setContentText("by ".concat(musicData.get(position).get("songArtist").toString()))
                    .setSmallIcon(R.drawable.ic_media_notification)
                    .setContentIntent(pendInt)
                    .build();

            startForeground(NOTIFY_ID, notification);

        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "libaudiodev");

            notificationChannel = new NotificationChannel("libaudiodev", "Music Player", NotificationManager.IMPORTANCE_LOW);
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.createNotificationChannel(notificationChannel);
            notificationBuilder.setNumber(0);

            Notification notification = notificationBuilder.setOngoing(true)
                    .setContentIntent(pendInt)
                    .setPriority(NotificationManager.IMPORTANCE_LOW)
                    .setNumber(0)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.drawable.ic_media_notification)
                    .setLargeIcon(ImageUtil.getAlbumArt(Base64Util.decode(musicData.get(position).get("songData").toString()), resources, theme))
                    .setContentText("by ".concat(musicData.get(position).get("songArtist").toString()))
                    .setContentTitle(musicData.get(position).get("songTitle").toString())
                    .build();

            startForeground(NOTIFY_ID, notification);
        }

    }

    public void initObjects() {
        if (FileUtil.doesExists(FileUtil.getPackageDir(this).concat("/song.json")) && FileUtil.isFile(FileUtil.getPackageDir(this).concat("/song.json"))) {
            musicData = ListUtil.getArrayListFromFile(FileUtil.getPackageDir(this).concat("/song.json"));

        } else {
            musicData = new ArrayList<>();
        }
        if (FileUtil.doesExists(FileUtil.getPackageDir(this).concat("/user/session.pref")) && FileUtil.isFile(FileUtil.getPackageDir(this).concat("/user/session.pref"))) {
            sessionData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir(this).concat("/user/session.pref"));

        } else {
            sessionData = new HashMap<>();
        }
    }

    public void loadAudioManager() {
        if (audioManager == null) {
            audioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));

            audioChangeListener = focusChange -> {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        if (!isPlaying()) {
                            playPause.performClick();
                        }

                        mp.setVolume(1.0f, 1.0f);

                        break;

                    case AudioManager.AUDIOFOCUS_LOSS:
                        if (isPlaying()) {
                            playPause.performClick();
                        }

                        mp.reset();
                        mp.release();

                        break;

                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        if (isPlaying()) {
                            playPause.performClick();
                        }

                        break;

                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        if (isPlaying()) {
                            mp.setVolume(0.1f, 0.1f);
                        }

                        break;
                }
            };
        }
    }

    public void updateOnCompletionListener() {
        mp.setOnCompletionListener(mp -> {
            playPause.setImageResource(R.drawable.ic_media_play);
            miniplayerPlayPause.setImageResource(R.drawable.ic_media_play);
            if (mp != null) {
                if (!sessionData.containsKey("sessionRepeatMode") || !sessionData.containsKey("sessionShuffleMode")) {
                    try {
                        if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1 < musicData.size()) {
                            sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                            createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
                            playPause.performClick();
                        }
                    } catch (Exception exception) {
                        ApplicationUtil.toast("Error loading audio file.", Toast.LENGTH_SHORT, this);
                        if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1 < musicData.size()) {
                            sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                            createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
                            playPause.performClick();
                        }
                    }
                } else {
                    if (sessionData.get("sessionRepeatMode").equals("0") && sessionData.get("sessionShuffleMode").equals("0")) {
                        try {
                            if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1 < musicData.size()) {
                                sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1));
                                FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                                createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
                                playPause.performClick();
                            }
                        } catch (Exception exception) {
                            ApplicationUtil.toast("Error loading audio file.", Toast.LENGTH_SHORT, this);
                            if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1 < musicData.size()) {
                                sessionData.put("profileSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1));
                                FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                                createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
                                playPause.performClick();
                            }
                        }
                    } else if (sessionData.get("sessionRepeatMode").equals("1")) {
                        seek(0);
                        if (Build.VERSION.SDK_INT >= 24) {
                            miniplayerSeekbar.setProgress(0, true);
                            seekbarDuration.setProgress(0, true);
                        } else {
                            miniplayerSeekbar.setProgress(0);
                            seekbarDuration.setProgress(0);
                        }
                        currentDuration.setText("0:00");
                    } else if (sessionData.get("sessionShuffleMode").equals("1")) {
                        int randomizer = IntegerUtil.getRandom(Integer.parseInt(sessionData.get("sessionSongPosition").toString()), musicData.size());
                        try {
                            if (randomizer < musicData.size()) {
                                sessionData.put("sessionSongPosition", String.valueOf(randomizer));
                                FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                                createLocalStream(randomizer);
                                playPause.performClick();
                            }
                        } catch (Exception exception) {
                            ApplicationUtil.toast("Error loading audio file.", Toast.LENGTH_SHORT, this);
                            if (randomizer < musicData.size()) {
                                sessionData.put("sessionSongPosition", String.valueOf(randomizer));
                                FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                                createLocalStream(randomizer);
                                playPause.performClick();
                            }
                        }
                    }
                }
            }
        });
    }

    public void startAudioFocus() {
        audioManager.requestAudioFocus(audioChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public void loseAudioFocus() {
        audioManager.abandonAudioFocus(audioChangeListener);
    }

    public void startHeadphoneReceiving() {
        registerReceiver(headphonesReceiver, intentFilter);
    }

    public void stopHeadphoneReceiving() {
        unregisterReceiver(headphonesReceiver);
    }

    public int getCurrentPosition() {
        return mp.getCurrentPosition();
    }

    public int getMaxDuration() {
        return mp.getDuration();
    }

    public boolean isPlaying() {
        return mp.isPlaying();
    }

    public void pause() {
        mp.pause();
    }

    public void seek(int position) {
        mp.seekTo(position);
    }

    public void play() {
        mp.start();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }
}
