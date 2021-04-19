package com.gianxd.audiodev.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gianxd.audiodev.R;
import com.gianxd.audiodev.util.ApplicationUtil;
import com.gianxd.audiodev.util.FileUtil;
import com.gianxd.audiodev.util.ListUtil;

import java.util.ArrayList;
import java.util.HashMap;


public class LyricsEditorActivity extends  AppCompatActivity  { 

	private ArrayList<HashMap<String, Object>> musicData;
	private HashMap<String, Object> settingsData;
	
	private LinearLayout toolbar;
	private EditText lyrics;
	private ImageView back;
	private TextView bruh;
	private ImageView autoedit;
	private ImageView save;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lyrics_editor);
		initialize(savedInstanceState);
		com.google.firebase.FirebaseApp.initializeApp(this);
		initializeLogic();
	}
	
	private void initialize(Bundle savedInstanceState) {
		toolbar = (LinearLayout) findViewById(R.id.toolbar);
		lyrics = (EditText) findViewById(R.id.lyrics);
		back = (ImageView) findViewById(R.id.back);
		bruh = (TextView) findViewById(R.id.bruh);
		autoedit = (ImageView) findViewById(R.id.autoedit);
		save = (ImageView) findViewById(R.id.save);
		if (FileUtil.doesExists(FileUtil.getPackageDir().concat("/song.json")) && FileUtil.isFile(FileUtil.getPackageDir().concat("/song.json"))) {
			musicData = ListUtil.getArrayListFromFile(FileUtil.getPackageDir().concat("/song.json"));
		} else {
			musicData = new ArrayList<>();
		}
		if (FileUtil.doesExists(FileUtil.getPackageDir().concat("/user/settings.pref")) && FileUtil.isFile(FileUtil.getPackageDir().concat("/user/settings.pref"))) {
			settingsData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir().concat("/user/settings.pref"));
		} else {
			settingsData = new HashMap<>();
		}
		lyrics.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
				
			}
			
			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
				if (lyrics.getText().toString().length() < 1) {
					save.setEnabled(false);
					save.setAlpha((float)(0.5d));
				} else {
					save.setEnabled(true);
					save.setAlpha((float)(1.0d));
				}
			}
			
			@Override
			public void afterTextChanged(Editable editable) {
				
			}
		});
		
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
				back.setBackground(rippleButton);
				finish();
			}
		});

		autoedit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
				autoedit.setBackground(rippleButton);
			}
		});
		
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
				save.setBackground(rippleButton);
				try {
					musicData.get(Integer.parseInt(getIntent().getStringExtra("songPosition"))).put("songLyrics", lyrics.getText().toString());
					FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/song.json"), ListUtil.setArrayListToSharedJSON(musicData));
					ApplicationUtil.toast("Lyrics saved successfully.", Toast.LENGTH_SHORT);
					finish();
				} catch (Exception e) {
					ApplicationUtil.toast("Error saving lyrics.", Toast.LENGTH_SHORT);
				}
			}
		});
	}
	
	private void initializeLogic() {
		bruh.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/roboto_medium.ttf"), Typeface.NORMAL);
		if (Build.VERSION.SDK_INT >= 23) {
			if (settingsData.containsKey("settingsDarkMode")) {
				if (settingsData.get("settingsDarkMode").equals("true")) {
					setTheme(R.style.Theme_ArchoMusic_Dark);
					toolbar.setBackgroundColor(Color.parseColor("#1A1A1A"));
					lyrics.setBackgroundColor(Color.parseColor("#212121"));
					lyrics.setHintTextColor(Color.parseColor("#BDBDBD"));
					lyrics.setTextColor(Color.parseColor("#FFFFFF"));
					getWindow().setStatusBarColor(Color.parseColor("#1A1A1A"));
					getWindow().setNavigationBarColor(Color.parseColor("#1A1A1A"));
				} else {
					setTheme(R.style.Theme_ArchoMusic);
					toolbar.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
					getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
					getWindow().setNavigationBarColor(Color.parseColor("#FFFFFF"));
				}
			} else {
				setTheme(R.style.Theme_ArchoMusic);
				toolbar.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
				getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
				getWindow().setNavigationBarColor(Color.parseColor("#FFFFFF"));
			}
		} else {
			getWindow().setStatusBarColor(Color.parseColor("#000000"));
			getWindow().setNavigationBarColor(Color.parseColor("#000000"));
		}
		if (musicData.get(Integer.parseInt(getIntent().getStringExtra("songPosition"))).containsKey("songLyrics")) {
					if (musicData.get(Integer.parseInt(getIntent().getStringExtra("songPosition"))).get("songLyrics").toString().length() == 0) {
							// lyrics is added but empty cheems.
					} else {
						    lyrics.setText(musicData.get(Integer.parseInt(getIntent().getStringExtra("songPosition"))).get("songLyrics").toString());
				        }
		} else {
				// no lyrics found cheems.
		}
		save.setEnabled(false);
		save.setAlpha((float)(0.5d));
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			default:
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		finish();
	}
	
}
