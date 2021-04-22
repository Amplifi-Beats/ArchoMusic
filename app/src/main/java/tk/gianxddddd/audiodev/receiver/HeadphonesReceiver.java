package tk.gianxddddd.audiodev.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import tk.gianxddddd.audiodev.activity.LocalStreamActivity;

public class HeadphonesReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            LocalStreamActivity.playPause.performClick();
        }
    }
}