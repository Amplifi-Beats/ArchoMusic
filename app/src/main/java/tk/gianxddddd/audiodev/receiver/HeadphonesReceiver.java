package tk.gianxddddd.audiodev.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;

import tk.gianxddddd.audiodev.activity.LocalStreamActivity;
import tk.gianxddddd.audiodev.service.LocalPlaybackService;

public class HeadphonesReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            LocalStreamActivity.playPause.performClick();
        }
    }


}