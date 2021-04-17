package com.gianxd.audiodev.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.gianxd.audiodev.AudioDev;
import com.gianxd.audiodev.R;
import com.gianxd.audiodev.util.ApplicationUtil;
import com.gianxd.audiodev.util.IntegerUtil;
import com.gianxd.audiodev.util.ListUtil;
import com.gianxd.audiodev.util.StringUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {
	
	private Timer timer = new Timer();
	
	private ArrayList<HashMap<String, Object>> musicData;
	private HashMap<String, Object> profileData;
	
	private LinearLayout mainLayout;
	private TextView logo;
	private ProgressBar loadanim;
	
	private Intent intent = new Intent();
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
		Log.i("SplashActivity", "Preparing for saved data init..");
		logo.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/leixo.ttf"), Typeface.BOLD);
		loadanim.setVisibility(View.GONE);
		loadanim.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);
		musicData = new ArrayList<>();
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
					Log.i("SplashActivity", "Dark mode was enabled from a saved context, Making em GOOOOOD.");
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
						if (savedData.contains("savedProfileData")) {
							if (savedData.contains("savedMusicData")) {
								int randomizer = IntegerUtil.getRandom((int)(0), (int)(1));
								if (randomizer == 0) {
									intent.setClass(getApplicationContext(), LocalStreamActivity.class);
									logo.setTransitionName("fade");
									android.app.ActivityOptions optionsCompat = android.app.ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this, logo, "fade");
									startActivity(intent, optionsCompat.toBundle());
								} else {
									if (randomizer == 1) {
										if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
										&& ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
											scanMedia();
										} else {
											ActivityCompat.requestPermissions(SplashActivity.this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO }, 1);
										}
									}
								}
							} else {
								if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
								&& ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
									scanMedia();
								} else {
									ActivityCompat.requestPermissions(SplashActivity.this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO }, 1);
								}
							}
						} else {
							BottomSheetDialog createProfile = new BottomSheetDialog(SplashActivity.this);
							View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_create_a_profile, null);
							createProfile.setContentView(dialogLayout);
							LinearLayout main = dialogLayout.findViewById(R.id.main);
							TextView title = dialogLayout.findViewById(R.id.title);
							ImageView profile_icon = dialogLayout.findViewById(R.id.profile_icon);
							EditText profile_name = dialogLayout.findViewById(R.id.profile_name);
							Button create = dialogLayout.findViewById(R.id.create);
							title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
							profile_icon.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor("#BDBDBD")}), null, null);
										view.setBackground(rippleButton);
										BottomSheetDialog pfpDialog = new BottomSheetDialog(SplashActivity.this);
										View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_create_a_profile_icon, null);
										pfpDialog.setContentView(dialogLayout);
										LinearLayout main = dialogLayout.findViewById(R.id.main);
										TextView title = dialogLayout.findViewById(R.id.title);
										ImageView profile_picture = dialogLayout.findViewById(R.id.profile_icon);
										EditText url = dialogLayout.findViewById(R.id.url);
										Button finish = dialogLayout.findViewById(R.id.finish);
										Button cancel = dialogLayout.findViewById(R.id.cancel);
										if (savedData.contains("savedProfileData")) {
											if (profileData.containsKey("profilePicture")) {
												Glide.with(getApplicationContext()).load(profileData.get("profilePicture").toString()).into(profile_picture);
												url.setText(profileData.get("profilePicture").toString());
											}
										}
										title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
										url.addTextChangedListener(new TextWatcher() {
											@Override
											public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
												// DO NOTHING
											}

											@Override
											public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
												if (!(url.getText().toString().length() == 0)) {
													Glide.with(getApplicationContext()).load(url.getText().toString()).into(profile_picture);
												} else {
													Glide.with(getApplicationContext()).load(R.drawable.ic_profile_icon).into(profile_picture);
												}
											}

											@Override
											public void afterTextChanged(Editable editable) {
												// DO NOTHING
											}
										});
										finish.setOnClickListener(new View.OnClickListener() {
											@Override
											public void onClick(View view) {
												if (url.getText().toString().length() > 0) {
													if (url.getText().toString().equals(profileData.get("profileName").toString())) {
														pfpDialog.dismiss();
													} else {
														String pfpUrl = url.getText().toString();
														profileData.put("profilePicture", pfpUrl);
														savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).commit();
														ApplicationUtil.toast("Set profile picture successfully.", Toast.LENGTH_SHORT);
														Glide.with(getApplicationContext()).load(url.getText().toString()).into(profile_icon);
														pfpDialog.dismiss();
													}
												} else {
													url.setError("Path/URI should not be blank.");
												}
											}
										});
										cancel.setOnClickListener(new View.OnClickListener() {
											@Override
											public void onClick(View view) {
												if (!profileData.containsKey("profileDarkMode")) {
													android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
													view.setBackground(rippleButton);
												} else {
													if (profileData.get("profileDarkMode").equals("true")) {
														android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#1A1A1A")), null);
														view.setBackground(rippleButton);
													} else {
														android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
														view.setBackground(rippleButton);
													}
												}
												pfpDialog.dismiss();
											}
										});
										Double TopLeft = 20.0;
										Double TopRight = 20.0;
										Double BottomRight = 0.0;
										Double BottomLeft = 0.0;
										android.graphics.drawable.GradientDrawable roundedCorners = new android.graphics.drawable.GradientDrawable();
										roundedCorners.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
										roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
										android.graphics.drawable.GradientDrawable roundedCorners2 = new android.graphics.drawable.GradientDrawable();
										roundedCorners2.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
										roundedCorners2.setCornerRadius(20);
										if (!profileData.containsKey("profileDarkMode")) {
											roundedCorners.setColor(Color.parseColor("#FFFFFF"));
											roundedCorners2.setColor(Color.parseColor("#EEEEEE"));
										} else {
											if (profileData.get("profileDarkMode").equals("true")) {
												roundedCorners.setColor(Color.parseColor("#1A1A1A"));
												roundedCorners2.setColor(Color.parseColor("#212121"));
												url.setTextColor(Color.parseColor("#FFFFFF"));
												url.setHintTextColor(Color.parseColor("#BDBDBD"));
											} else {
												roundedCorners.setColor(Color.parseColor("#FFFFFF"));
												roundedCorners2.setColor(Color.parseColor("#EEEEEE"));
											}
										}
										((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
										url.setBackground(roundedCorners2);
										android.graphics.drawable.GradientDrawable gradientButton = new android.graphics.drawable.GradientDrawable();
										gradientButton.setColor(Color.parseColor("#03A9F4"));
										gradientButton.setCornerRadius(20);
										finish.setBackground(gradientButton);
										cancel.setBackground(gradientButton);
										pfpDialog.show();
									}
							});
							create.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
											if (profile_name.getText().toString().length() > 0) {
													HashMap<String, Object> tempProfileData = new HashMap<>();
													String profileName = profile_name.getText().toString();
													profileData.put("profileName", profileName);
												    savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).commit();
													createProfile.dismiss();
													intent.setClass(AudioDev.applicationContext, SplashActivity.class);
													startActivity(intent);
													finish();
											} else {
												    profile_name.setError("Profile name should not be blank.");
											}
									}
							});
							Double TopLeft = 20.0;
							Double TopRight = 20.0;
							Double BottomRight = 0.0;
							Double BottomLeft = 0.0;
							android.graphics.drawable.GradientDrawable roundedCorners = new android.graphics.drawable.GradientDrawable();
							roundedCorners.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
							roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
							android.graphics.drawable.GradientDrawable roundedCorners2 = new android.graphics.drawable.GradientDrawable();
							roundedCorners2.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
							roundedCorners2.setCornerRadius(20);
							if (!profileData.containsKey("profileDarkMode")) {
								roundedCorners.setColor(Color.parseColor("#FFFFFF"));
								roundedCorners2.setColor(Color.parseColor("#EEEEEE"));
							} else {
								if (profileData.get("profileDarkMode").equals("true")) {
									roundedCorners.setColor(Color.parseColor("#1A1A1A"));
									roundedCorners2.setColor(Color.parseColor("#212121"));
									profile_name.setTextColor(Color.parseColor("#FFFFFF"));
									profile_name.setHintTextColor(Color.parseColor("#BDBDBD"));
								} else {
									roundedCorners2.setColor(Color.parseColor("#EEEEEE"));
									roundedCorners.setColor(Color.parseColor("#FFFFFF"));
								}
							}
							((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
							profile_name.setBackground(roundedCorners2);
							android.graphics.drawable.GradientDrawable gradientButton = new android.graphics.drawable.GradientDrawable();
							gradientButton.setColor(Color.parseColor("#03A9F4"));
							gradientButton.setCornerRadius(20);
							create.setBackground(gradientButton);
							createProfile.setCancelable(false);
							createProfile.show();
						}
					}
				});
			}
		};
		timer.schedule(timerTask, (int)(1500));
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
			if (requestCode == 1) {
					if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
							if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
									scanMedia();
							} else {
								    scanMedia();
							}
					} else {
						   scanMedia();
					}
			}
	}
	
	{
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			default:
			break;
		}
	}

	public void scanMedia () {
		(new MediaScanTask()).execute();
	}
	
	private class MediaScanTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			if (musicData != null) {
				musicData.clear();
			}
			loadanim.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Void doInBackground(Void... path) {
			if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
			&& ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
				String[] mediaProjection = {
					            android.provider.MediaStore.Audio.Media._ID, 
								android.provider.MediaStore.Audio.Media.ARTIST,
								android.provider.MediaStore.Audio.Media.DATA,
								android.provider.MediaStore.Audio.Media.TITLE,
					            android.provider.MediaStore.Audio.Media.ALBUM_ID
				};
				String orderBy = " " + android.provider.MediaStore.MediaColumns.DISPLAY_NAME;
				android.database.Cursor cursor = getApplicationContext().getContentResolver().query(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mediaProjection, null, null, orderBy);
				try {
					if (cursor.moveToFirst()) {
						long _id;
						String name;
						String data;
						String artist;
						do {
							_id = cursor.getLong(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media._ID));
							name = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.TITLE));
							data = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.DATA));
							artist = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.ARTIST));
							{
														
								HashMap<String, Object> songDetails = new HashMap<>();
								if (name.startsWith("<unknown>")) {
									name = "Unknown Title";
								}
								if (artist.startsWith("<unknown>")) {
									artist = "Unknown Artist";
								}
								songDetails.put("songTitle", name);
								songDetails.put("songData", StringUtil.encodeString(data));
								songDetails.put("songArtist", artist);
								songDetails.put("id", _id);
								musicData.add(songDetails);
							}
						} while (cursor.moveToNext());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}

		@Override
		protected void onPostExecute(Void param){
			if (savedData.contains("savedMusicData")) {
				ArrayList<HashMap<String, Object>> tempMusicData = ListUtil.getArrayListFromSharedJSON(savedData, "savedMusicData");
				if (!Objects.equals(musicData, tempMusicData)) {
					savedData.edit().putString("savedMusicData", ListUtil.setArrayListToSharedJSON(musicData)).commit();
				}
				loadanim.setVisibility(View.GONE);
			} else {
				savedData.edit().putString("savedMusicData", ListUtil.setArrayListToSharedJSON(musicData)).commit();
			}
			timerTask = new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							intent.setClass(AudioDev.applicationContext, SplashActivity.class);
							startActivity(intent);
							finish();
						}
					});
				}
			};
			timer.schedule(timerTask, (int)(1500));
		}
	}
	
	
	
}
