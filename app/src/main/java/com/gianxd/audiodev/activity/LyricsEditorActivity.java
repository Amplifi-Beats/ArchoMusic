package com.gianxd.audiodev.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.gianxd.audiodev.util.ListUtil;

import java.util.ArrayList;
import java.util.HashMap;

import static com.gianxd.audiodev.AudioDev.applicationContext;


public class LyricsEditorActivity extends  AppCompatActivity  { 

	private ArrayList<HashMap<String, Object>> musicData;
	private HashMap<String, Object> profileData;
	
	private LinearLayout toolbar;
	private EditText lyrics;
	private ImageView back;
	private TextView bruh;
	private ImageView save;
	
	private SharedPreferences savedData;

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
		save = (ImageView) findViewById(R.id.save);
		savedData = applicationContext.getSharedPreferences("savedData", Activity.MODE_PRIVATE);
		if (savedData.contains("savedMusicData")) {
			musicData = ListUtil.getArrayListFromSharedJSON(savedData, "savedMusicData");
		} else {
			musicData = new ArrayList<>();
		}
		if (savedData.contains("savedProfileData")) {
			profileData = ListUtil.getHashMapFromSharedJSON(savedData, "savedProfileData");
		} else {
			profileData = new HashMap<>();
		}
		lyrics.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence charSequence, int _param2, int _param3, int _param4) {
				if (lyrics.getText().toString().length() < 1) {
					save.setEnabled(false);
					save.setAlpha((float)(0.5d));
				} else {
					save.setEnabled(true);
					save.setAlpha((float)(1.0d));
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence charSequence, int _param2, int _param3, int _param4) {
				
			}
			
			@Override
			public void afterTextChanged(Editable editable) {
				
			}
		});
		
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
				back.setBackground(rippleButton);
				finish();
			}
		});
		
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
				save.setBackground(rippleButton);
				try {
					musicData.get(Integer.parseInt(getIntent().getStringExtra("songPosition"))).put("songLyrics", lyrics.getText().toString());
					savedData.edit().putString("savedMusicData", ListUtil.setArrayListToSharedJSON(musicData)).apply();
					ApplicationUtil.toast(getApplicationContext(), "Lyrics saved successfully.", Toast.LENGTH_SHORT);
					finish();
				} catch (Exception e) {
					ApplicationUtil.toast(getApplicationContext(), "Error saving lyrics.", Toast.LENGTH_SHORT);
				}
			}
		});
	}
	
	private void initializeLogic() {
		bruh.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/roboto_medium.ttf"), Typeface.NORMAL);
		if (Build.VERSION.SDK_INT >= 23) {
			if (profileData.containsKey("profileDarkMode")) {
				if (profileData.get("profileDarkMode").equals("true")) {
					getWindow().setStatusBarColor(Color.parseColor("#1A1A1A"));
					getWindow().setNavigationBarColor(Color.parseColor("#1A1A1A"));
				} else {
					toolbar.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
					getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
					getWindow().setNavigationBarColor(Color.parseColor("#FFFFFF"));
				}
			} else {
				toolbar.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
				getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
				getWindow().setNavigationBarColor(Color.parseColor("#FFFFFF"));
			}
		} else {
			getWindow().setStatusBarColor(Color.parseColor("#000000"));
			getWindow().setNavigationBarColor(Color.parseColor("#000000"));
		}
		if (profileData.containsKey("profileDarkMode")) {
			if (profileData.get("profileDarkMode").equals("true")) {
				toolbar.setBackgroundColor(Color.parseColor("#1A1A1A"));
				lyrics.setBackgroundColor(Color.parseColor("#212121"));
				lyrics.setHintTextColor(Color.parseColor("#BDBDBD"));
				lyrics.setTextColor(Color.parseColor("#FFFFFF"));
			}
		}
		if (musicData.get((int)Integer.parseInt(getIntent().getStringExtra("songPosition"))).containsKey("songLyrics")) {
					if (musicData.get((int)Integer.parseInt(getIntent().getStringExtra("songPosition"))).get("songLyrics").toString().length() == 0) {
							// lyrics is added but empty cheems.
					} else {
						    lyrics.setText(musicData.get((int)Integer.parseInt(getIntent().getStringExtra("songPosition"))).get("songLyrics").toString());
				        }
		} else {
				// no lyrics found cheems.
		}
		save.setEnabled(false);
		save.setAlpha((float)(0.5d));
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
		switch (_requestCode) {
			default:
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		finish();
	}
	
}
