package com.gianxd.audiodev.activity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gianxd.audiodev.R;
import com.gianxd.audiodev.util.ApplicationUtil;
import com.gianxd.audiodev.util.ListUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {
	
	private Timer timer = new Timer();

	private HashMap<String, Object> profileData;
	
	private LinearLayout mainLayout;
	private TextView logo;
	private ProgressBar loadanim;

	private TimerTask timerTask;
	private SharedPreferences savedData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		initialize(savedInstanceState);
		com.google.firebase.FirebaseApp.initializeApp(this);
		initializeLogic();
	}
	
	private void initialize(Bundle savedInstanceState) {
		mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
		logo = (TextView) findViewById(R.id.logo);
		loadanim = (ProgressBar) findViewById(R.id.loadanim);
		savedData = getSharedPreferences("savedData", Context.MODE_PRIVATE);
	}
	
	private void initializeLogic() {
		logo.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/leixo.ttf"), Typeface.BOLD);
		loadanim.setVisibility(View.GONE);
		loadanim.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);
		if (savedData.contains("savedProfileData")) {
			profileData = ListUtil.getHashMapFromSharedJSON(savedData, "savedProfileData");
		} else {
			profileData = new HashMap<>();
		}
		if (Build.VERSION.SDK_INT >= 23) {
			if (profileData.containsKey("profileDarkMode")) {
				if (!profileData.get("profileDarkMode").equals("true")) {
					getWindow().setStatusBarColor(Color.parseColor("#03A9F4"));
					getWindow().setNavigationBarColor(Color.parseColor("#03A9F4"));
				} else {
					setTheme(R.style.Theme_ArchoMusic_Dark);
					mainLayout.setBackgroundColor(Color.parseColor("#1A1A1A"));
					logo.setTextColor(Color.parseColor("#03A9F4"));
					getWindow().setStatusBarColor(Color.parseColor("#1A1A1A"));
					getWindow().setNavigationBarColor(Color.parseColor("#1A1A1A"));
				}
			} else {
				getWindow().setStatusBarColor(Color.parseColor("#03A9F4"));
				getWindow().setNavigationBarColor(Color.parseColor("#03A9F4"));
			}
		} else {
			getWindow().setStatusBarColor(Color.parseColor("#000000"));
			getWindow().setNavigationBarColor(Color.parseColor("#000000"));
		}

		timerTask = new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (profileData.containsKey("profileErrorTrace")) {
							BottomSheetDialog errorDialog = new BottomSheetDialog(SplashActivity.this);
							View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_debug, null);
							errorDialog.setContentView(dialogLayout);
							LinearLayout main = dialogLayout.findViewById(R.id.main);
							TextView title = dialogLayout.findViewById(R.id.title);
							TextView log = dialogLayout.findViewById(R.id.log);
							Button close = dialogLayout.findViewById(R.id.close);
							title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/roboto_medium.ttf"), Typeface.NORMAL);
							log.setText(profileData.get("profileErrorTrace").toString());
							close.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									if (!profileData.containsKey("profileDarkMode")) {
										RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
										view.setBackground(rippleButton);
									} else {
										if (profileData.get("profileDarkMode").equals("true")) {
											RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
											view.setBackground(rippleButton);
										} else {
											RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
											view.setBackground(rippleButton);
										}
									}
									profileData.remove("profileErrorTrace");
									savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).commit();
									errorDialog.dismiss();
									Intent intent = new Intent();
									intent.setClass(ApplicationUtil.getAppContext(), LocalStreamActivity.class);
									logo.setTransitionName("fade");
									ActivityOptions optionsCompat = ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this, logo, "fade");
									startActivity(intent, optionsCompat.toBundle());
								}
							});
							Double TopLeft = 20.0;
							Double TopRight = 20.0;
							Double BottomRight = 0.0;
							Double BottomLeft = 0.0;
							GradientDrawable roundedCorners = new GradientDrawable();
							roundedCorners.setShape(GradientDrawable.RECTANGLE);
							roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
							GradientDrawable roundedCorners2 = new GradientDrawable();
							roundedCorners2.setShape(GradientDrawable.RECTANGLE);
							roundedCorners2.setCornerRadius(20);
							if (!profileData.containsKey("profileDarkMode")) {
								roundedCorners.setColor(Color.parseColor("#FFFFFF"));
								roundedCorners2.setColor(Color.parseColor("#EEEEEE"));
							} else {
								if (profileData.get("profileDarkMode").equals("true")) {
									roundedCorners.setColor(Color.parseColor("#1A1A1A"));
									roundedCorners2.setColor(Color.parseColor("#212121"));
									log.setTextColor(Color.parseColor("#FFFFFF"));
									log.setHintTextColor(Color.parseColor("#BDBDBD"));
								} else {
									roundedCorners.setColor(Color.parseColor("#FFFFFF"));
									roundedCorners2.setColor(Color.parseColor("#EEEEEE"));
								}
							}
							((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
							log.setBackground(roundedCorners2);
							GradientDrawable gradientButton = new GradientDrawable();
							gradientButton.setColor(Color.parseColor("#03A9F4"));
							gradientButton.setCornerRadius(20);
							close.setBackground(gradientButton);
							errorDialog.setCancelable(false);
							errorDialog.show();
						} else {
							Intent intent = new Intent();
							intent.setClass(ApplicationUtil.getAppContext(), LocalStreamActivity.class);
							logo.setTransitionName("fade");
							ActivityOptions optionsCompat = ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this, logo, "fade");
							startActivity(intent, optionsCompat.toBundle());
						}
					}
				});
			}
		};
		timer.schedule(timerTask, (int)(1500));
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			default:
			break;
		}
	}
	
}
