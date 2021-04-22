package tk.gianxddddd.audiodev;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import tk.gianxddddd.audiodev.activity.LauncherActivity;
import tk.gianxddddd.audiodev.util.ApplicationUtil;
import tk.gianxddddd.audiodev.util.FileUtil;
import tk.gianxddddd.audiodev.util.ListUtil;

import java.util.HashMap;

public class AudioDev extends Application {

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    @Override
    public void onCreate() {
        this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {

            Log.e("AudioDev", ApplicationUtil.getStackTrace(ex));

            HashMap<String, Object> settingsData;

            if (FileUtil.doesExists(FileUtil.getPackageDir(this).concat("/user/settings.pref")) && FileUtil.isFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"))) {
                settingsData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"));
            } else {
                settingsData = new HashMap<>();
            }

            if (settingsData.containsKey("settingsCaptureError")) {
                if (settingsData.get("settingsCaptureError").equals("true")) {
                    FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/crash.log"), ApplicationUtil.getStackTrace(ex));
                    startActivity(new Intent(getApplicationContext(), LauncherActivity.class));
                }
            } else {
                FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/crash.log"), ApplicationUtil.getStackTrace(ex));
                startActivity(new Intent(getApplicationContext(), LauncherActivity.class));
            }

            uncaughtExceptionHandler.uncaughtException(thread, ex);
        });

        super.onCreate();
    }
}
