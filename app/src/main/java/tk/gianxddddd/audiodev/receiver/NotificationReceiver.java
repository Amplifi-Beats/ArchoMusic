package tk.gianxddddd.audiodev.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import tk.gianxddddd.audiodev.util.ApplicationUtil;

import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.repeat;
import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.skipBackward;
import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.playPause;
import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.skipForward;
import static tk.gianxddddd.audiodev.activity.LocalStreamActivity.shuffle;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getStringExtra("onClick").equals("repeat")) {
            repeat.performClick();
        } else if (intent.getStringExtra("onClick").equals("previous")) {
            skipBackward.performClick();
        } else if (intent.getStringExtra("onClick").equals("playpause")) {
            playPause.performClick();
        } else if (intent.getStringExtra("onClick").equals("next")) {
            skipForward.performClick();
        } else if (intent.getStringExtra("onClick").equals("shuffle")) {
            shuffle.performClick();
        }
    }
}