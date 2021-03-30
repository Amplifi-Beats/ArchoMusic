package com.gianxd.audiodev;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import java.util.HashMap;

import com.gianxd.audiodev.activity.LauncherActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AudioDev extends Application {

    public static Context applicationContext;
	private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
	public static volatile Handler applicationHandler;
	private SharedPreferences savedData;
	
	@Override
	public void onCreate() {
        applicationContext = this;
		applicationHandler = new Handler(AudioDev.applicationContext.getMainLooper());
		savedData = getSharedPreferences("savedData", Context.MODE_PRIVATE);
		this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				HashMap<String, Object> profileData = new Gson().fromJson(savedData.getString("savedProfileData", ""), new TypeToken<HashMap<String, Object>>(){}.getType());
				profileData.put("profileErrorTrace", getStackTrace(ex));
				savedData.edit().putString("savedProfileData", new Gson().toJson(profileData)).apply();
				Intent intent = new Intent(getApplicationContext(), LauncherActivity.class);
				startActivity(intent);
				uncaughtExceptionHandler.uncaughtException(thread, ex);
			}
		});
		super.onCreate();
	}

	private String getStackTrace(Throwable th){
		final Writer result = new StringWriter();

		final PrintWriter printWriter = new PrintWriter(result);
		Throwable cause = th;

		while(cause != null){
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		final String stacktraceAsString = result.toString();
		printWriter.close();

		return stacktraceAsString;
	}
}
