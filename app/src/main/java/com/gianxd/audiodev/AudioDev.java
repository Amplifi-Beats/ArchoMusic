package com.gianxd.audiodev;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;

import com.gianxd.audiodev.activity.LauncherActivity;
import com.gianxd.audiodev.util.ApplicationUtil;
import com.gianxd.audiodev.util.FileUtil;
import com.gianxd.audiodev.util.ListUtil;

import java.util.HashMap;

public class AudioDev extends Application {

    public static Context applicationContext;
    public static Resources applicationResources;
	private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
	public static volatile Handler applicationHandler;
	
	@Override
	public void onCreate() {
        applicationContext = this;
        applicationResources = getResources();
		applicationHandler = new Handler(ApplicationUtil.getAppContext().getMainLooper());
		this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				Log.e("AudioDev", ApplicationUtil.getStackTrace(ex));
				HashMap<String, Object> settingsData;
				if (FileUtil.doesExists(FileUtil.getPackageDir().concat("/user/settings.pref")) && FileUtil.isFile(FileUtil.getPackageDir().concat("/user/settings.pref"))) {
					settingsData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir().concat("/user/settings.pref"));
				} else {
					settingsData = new HashMap<>();
				}
				if (settingsData.containsKey("settingsCaptureError")) {
					if (settingsData.get("settingsCaptureError").equals("true")) {
						FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/crash.log"), ApplicationUtil.getStackTrace(ex));
						startActivity(new Intent(getApplicationContext(), LauncherActivity.class));
					}
				} else {
					FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/crash.log"), ApplicationUtil.getStackTrace(ex));
					startActivity(new Intent(getApplicationContext(), LauncherActivity.class));
				}
				uncaughtExceptionHandler.uncaughtException(thread, ex);
			}
		});
		super.onCreate();
	}

}
