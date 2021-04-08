package com.gianxd.audiodev;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import java.util.HashMap;

import com.gianxd.audiodev.activity.LauncherActivity;
import com.gianxd.audiodev.util.ApplicationUtil;
import com.gianxd.audiodev.util.ListUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AudioDev extends Application {

    public static Context applicationContext;
	private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
	private HashMap<String, Object> profileData;
	public static volatile Handler applicationHandler;
	private SharedPreferences savedData;
	
	@Override
	public void onCreate() {
        applicationContext = this;
		applicationHandler = new Handler(AudioDev.applicationContext.getMainLooper());
		savedData = getSharedPreferences("savedData", Context.MODE_PRIVATE);
		if (!savedData.contains("savedProfileData")) {
			profileData = new HashMap<>();
		} else {
			profileData = ListUtil.getHashMapFromSharedJSON(savedData, "savedProfileData");
		}
		this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				profileData.put("profileErrorTrace", ApplicationUtil.getStackTrace(ex));
				savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).apply();
				startActivity(new Intent(getApplicationContext(), LauncherActivity.class));
				uncaughtExceptionHandler.uncaughtException(thread, ex);
			}
		});
		super.onCreate();
	}

}
