package com.gianxd.audiodev.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import static com.gianxd.audiodev.activity.LocalStreamActivity.playPause;

public class HeadphonesReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            playPause.performClick();
        }
    }
}