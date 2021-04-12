package com.gianxd.audiodev.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gianxd.audiodev.R;
import com.gianxd.audiodev.util.ListUtil;

import java.util.HashMap;

import static com.gianxd.audiodev.AudioDev.applicationContext;


public class ExternalBrowserActivity extends  AppCompatActivity  {

	private HashMap<String, Object> profileData;

	private LinearLayout toolbar;
	private ProgressBar loadbar;
	private WebView web;
	private ImageView back;
	private TextView webtitle;
	private TextView weburl;

	private SharedPreferences savedData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_external_browser);
		initialize(savedInstanceState);
		com.google.firebase.FirebaseApp.initializeApp(this);
		initializeLogic();
	}
	
	private void initialize(Bundle savedInstanceState) {
		toolbar = (LinearLayout) findViewById(R.id.toolbar);
		loadbar = (ProgressBar) findViewById(R.id.loadbar);
		back = (ImageView) findViewById(R.id.back);
		web = (WebView) findViewById(R.id.web);
		web.getSettings().setJavaScriptEnabled(true);
		web.getSettings().setSupportZoom(true);
		webtitle = (TextView) findViewById(R.id.webtitle);
		weburl = (TextView) findViewById(R.id.weburl);
		savedData = applicationContext.getSharedPreferences("savedData", Context.MODE_PRIVATE);
		if (savedData.contains("savedProfileData")) {
			profileData = ListUtil.getHashMapFromSharedJSON(savedData, "savedProfileData");
		} else {
			profileData = new HashMap<>();
		}
		web.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView webView, String url, Bitmap favicon) {
				loadbar.setVisibility(View.VISIBLE);
				super.onPageStarted(webView, url, favicon);
			}
			@Override
			public void onPageFinished(WebView webView, String url) {
				loadbar.setVisibility(View.GONE);
				webtitle.setText(web.getTitle());
				if (!url.startsWith("file:///android_asset/")) {
					weburl.setText(url);
				}
				else {
					if (url.equals("file:///android_asset/LICENSE.html")) {
						weburl.setText("com.gianxd.audiodev/LICENSES");
					}
					if (url.equals("file:///android_asset/PRIVACY.html")) {
						weburl.setText("com.gianxd.audiodev/PRIVACY");
					}
				}
				super.onPageFinished(webView, url);
			}
		});
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
				back.setBackground(rippleButton);
				if (web != null) {
					web.destroy();
				}
				finish();
			}
		});
	}
	
	private void initializeLogic() {
		webtitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
		loadbar.setBackgroundColor(Color.parseColor("#03A9F4"));
		loadbar.setVisibility(View.GONE);
		loadbar.setElevation((float)10);
		toolbar.setElevation((float)10);
		if (Build.VERSION.SDK_INT >= 23) {
			if (profileData.containsKey("profileDarkMode")) {
				if (profileData.get("profileDarkMode").equals("true")) {
					setTheme(R.style.Theme_ArchoMusic_Dark);
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
		if (profileData.containsKey("profileDarkMode")) {
			if (profileData.get("profileDarkMode").equals("true")) {
				toolbar.setBackgroundColor(Color.parseColor("#1A1A1A"));
				weburl.setTextColor(Color.parseColor("#BDBDBD"));
			}
		}
		if (getIntent().getStringExtra("url") != null) {
			web.loadUrl(getIntent().getStringExtra("url"));
		}
		else {
			web.loadUrl("https://www.google.com");
		}
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
	public void onDestroy() {
		web.destroy();
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		if (web.canGoBack()) {
			web.goBack();
		} else {
			if (web != null) {
				web.destroy();
			}
			finish();
		}
	}
	
}
