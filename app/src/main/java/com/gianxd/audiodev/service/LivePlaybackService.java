package com.gianxd.audiodev.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class LivePlaybackService extends Service {

    private final IBinder liveMusicBind = new LiveMusicBinder();
    private static final int NOTIFY_ID = 1;
    private NotificationChannel notificationChannel;
    private NotificationManager notificationManager;

    public void onCreate() {
        super.onCreate();
    }

    public class LiveMusicBinder extends Binder {
        public LivePlaybackService getService() {
            return LivePlaybackService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return liveMusicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        return false;
    }

}
