package com.gianxd.audiodev.activity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.gianxd.audiodev.R;
import com.gianxd.audiodev.service.LocalPlaybackService;
import com.gianxd.audiodev.service.LocalPlaybackService.MusicBinder;
import com.gianxd.audiodev.util.ApplicationUtil;
import com.gianxd.audiodev.util.FileUtil;
import com.gianxd.audiodev.util.ImageUtil;
import com.gianxd.audiodev.util.IntegerUtil;
import com.gianxd.audiodev.util.ListUtil;
import com.gianxd.audiodev.util.StringUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class LocalStreamActivity extends  AppCompatActivity  {
	
	private Timer timer = new Timer();
	private ArrayList<HashMap<String, Object>> musicData;
	private HashMap<String, Object> profileData;
	private HashMap<String, Object> sessionData;
	private HashMap<String, Object> settingsData;

	private ServiceConnection musicConnection;
	private LocalPlaybackService playbackSrv;
	private Intent playIntent;
	private boolean musicBound = false;

	private LinearLayout top;
	private LinearLayout main;
	public static ProgressBar miniplayerSeekbar;
	private LinearLayout miniplayer;
	private TextView logoName;
	private TabLayout tabNavigation;
	private ImageView menu;
	private SwipeRefreshLayout listRefresh;
	private ProgressBar listLoadBar;
	private TextView listEmptyMsg;
	private LinearLayout player;
	private RecyclerView songList;
    public static ImageView albumArt;
    public static TextView songTitle;
    public static TextView songArtist;
    public static TextView currentDuration;
    public static SeekBar seekbarDuration;
    public static TextView maxDuration;
    public ImageView skipBackward;
    public static ImageView playPause;
    public ImageView skipForward;
	private ImageView miniplayerSkipPrev;
    public static ImageView miniplayerPlayPause;
	private ImageView miniplayerSkipNext;
	private ImageView repeat;
	private ImageView shuffle;
	public static ImageView miniplayerAlbumArt;
    public static TextView miniplayerSongTitle;
    public static TextView miniplayerSongArtist;

	private TimerTask timerTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_local_streaming);
		initialize(savedInstanceState);
		com.google.firebase.FirebaseApp.initializeApp(this);
	}
	
	private void initialize(Bundle savedInstanceState) {
		top = (LinearLayout) findViewById(R.id.up);
		main = (LinearLayout) findViewById(R.id.main);
		miniplayerSeekbar = (ProgressBar) findViewById(R.id.miniplayerSeekbar);
		miniplayer = (LinearLayout) findViewById(R.id.miniplayer);
		logoName = (TextView) findViewById(R.id.logoName);
		tabNavigation = (TabLayout) findViewById(R.id.tabNavigation);
		menu = (CircleImageView) findViewById(R.id.menu);
		listRefresh = (SwipeRefreshLayout) findViewById(R.id.listRefresh);
		listLoadBar = (ProgressBar) findViewById(R.id.listLoadBar);
		listEmptyMsg = (TextView) findViewById(R.id.listEmptyMsg);
		songList = (RecyclerView) findViewById(R.id.songList);
		player = (LinearLayout) findViewById(R.id.player);
		albumArt = (ImageView) findViewById(R.id.albumArt);
		songTitle = (TextView) findViewById(R.id.songTitle);
		songArtist = (TextView) findViewById(R.id.songArtist);
		currentDuration = (TextView) findViewById(R.id.currentDuration);
		seekbarDuration = (SeekBar) findViewById(R.id.seekbarDuration);
		maxDuration = (TextView) findViewById(R.id.maxDuration);
		skipBackward = (ImageView) findViewById(R.id.skipBackward);
		playPause = (ImageView) findViewById(R.id.playPause);
		skipForward = (ImageView) findViewById(R.id.skipForward);
		miniplayerSkipPrev = (ImageView) findViewById(R.id.miniplayerSkipPrev);
		miniplayerPlayPause = (ImageView) findViewById(R.id.miniplayerPlayPause);
		miniplayerSkipNext = (ImageView) findViewById(R.id.miniplayerSkipNext);
		miniplayerAlbumArt = (ImageView) findViewById(R.id.miniplayerAlbumArt);
		miniplayerSongTitle = (TextView) findViewById(R.id.miniplayerSongTitle);
		miniplayerSongArtist = (TextView) findViewById(R.id.miniplayerSongArtist);
		repeat = (ImageView) findViewById(R.id.repeat);
		shuffle = (ImageView) findViewById(R.id.shuffle);
		tabNavigation.addTab(tabNavigation.newTab().setIcon(R.drawable.ic_tabnav_library));
		tabNavigation.addTab(tabNavigation.newTab().setIcon(R.drawable.ic_tabnav_nowplaying));
		listLoadBar.setVisibility(View.GONE);
		if (FileUtil.doesExists(FileUtil.getPackageDir().concat("/song.json")) && FileUtil.isFile(FileUtil.getPackageDir().concat("/song.json"))) {
			musicData = ListUtil.getArrayListFromFile(FileUtil.getPackageDir().concat("/song.json"));
			songList.setAdapter(new SongListAdapter(musicData));
			if (!musicData.isEmpty()) {
				listEmptyMsg.setVisibility(View.GONE);
				songList.setVisibility(View.VISIBLE);
			} else {
				listEmptyMsg.setVisibility(View.VISIBLE);
				songList.setVisibility(View.GONE);
			}
			connectToLocalPlaybackService();
		}
		if (FileUtil.doesExists(FileUtil.getPackageDir().concat("/user/profile.pref")) && FileUtil.isFile(FileUtil.getPackageDir().concat("/user/profile.pref"))) {
			profileData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir().concat("/user/profile.pref"));
		} else {
			profileData = new HashMap<>();
		}
		if (FileUtil.doesExists(FileUtil.getPackageDir().concat("/user/session.pref")) && FileUtil.isFile(FileUtil.getPackageDir().concat("/user/session.pref"))) {
			sessionData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir().concat("/user/session.pref"));
		} else {
			sessionData = new HashMap<>();
		}
		if (FileUtil.doesExists(FileUtil.getPackageDir().concat("/user/settings.pref")) && FileUtil.isFile(FileUtil.getPackageDir().concat("/user/settings.pref"))) {
			settingsData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir().concat("/user/settings.pref"));
		} else {
			settingsData = new HashMap<>();
		}
		if (!settingsData.containsKey("settingsDarkMode")) {
			settingsData.put("settingsDarkMode", "false");
			FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/settings.pref"), ListUtil.setHashMapToSharedJSON(settingsData));
		}
		if (!sessionData.containsKey("sessionToggleIntro")) {
			BottomSheetDialog introDialog = new BottomSheetDialog(LocalStreamActivity.this);
			View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_introduction, null);
			introDialog.setContentView(dialogLayout);
			LinearLayout main = dialogLayout.findViewById(R.id.main);
			TextView title = dialogLayout.findViewById(R.id.title);
			ImageView logo = dialogLayout.findViewById(R.id.logo);
			TextView quote = dialogLayout.findViewById(R.id.quote);
			TextView description = dialogLayout.findViewById(R.id.description);
			Button close = dialogLayout.findViewById(R.id.close);
			title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/roboto_medium.ttf"), Typeface.NORMAL);
			close.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (!settingsData.containsKey("settingsDarkMode")) {
						RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
						view.setBackground(rippleButton);
					} else {
						if (settingsData.get("settingsDarkMode").equals("true")) {
							RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
							view.setBackground(rippleButton);
						} else {
							RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
							view.setBackground(rippleButton);
						}
					}
					sessionData.put("sessionToggleIntro", "0");
					FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
					introDialog.dismiss();
				}
			});
			Double TopLeft = 20.0;
			Double TopRight = 20.0;
			Double BottomRight = 0.0;
			Double BottomLeft = 0.0;
			GradientDrawable roundedCorners = new GradientDrawable();
			roundedCorners.setShape(GradientDrawable.RECTANGLE);
			roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
			if (!settingsData.containsKey("settingsDarkMode")) {
				roundedCorners.setColor(Color.parseColor("#FFFFFF"));
			} else {
				if (settingsData.get("settingsDarkMode").equals("true")) {
					roundedCorners.setColor(Color.parseColor("#1A1A1A"));
					description.setTextColor(Color.parseColor("#FFFFFF"));
				} else {
					roundedCorners.setColor(Color.parseColor("#FFFFFF"));
				}
			}
			((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);;
			GradientDrawable gradientButton = new GradientDrawable();
			gradientButton.setColor(Color.parseColor("#03A9F4"));
			gradientButton.setCornerRadius(20);
			close.setBackground(gradientButton);
			introDialog.setCancelable(false);
			introDialog.show();
		}
		if (sessionData.containsKey("sessionNavigationIndex")) {
			if (sessionData.get("sessionNavigationIndex").equals("0")) {
				tabNavigation.getTabAt(0).select();
				listRefresh.setVisibility(View.VISIBLE);
				miniplayer.setVisibility(View.VISIBLE);
				player.setVisibility(View.GONE);
				miniplayerSeekbar.setVisibility(View.VISIBLE);
			} else if (sessionData.get("sessionNavigationIndex").equals("1")) {
				tabNavigation.getTabAt(1).select();
				listRefresh.setVisibility(View.GONE);
				player.setVisibility(View.VISIBLE);
				miniplayer.setVisibility(View.GONE);
				miniplayerSeekbar.setVisibility(View.GONE);
			}
		} else {
			profileData.put("sessionNavigationIndex", "0");
			FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
			tabNavigation.getTabAt(0).select();
			listRefresh.setVisibility(View.VISIBLE);
			player.setVisibility(View.GONE);
			miniplayer.setVisibility(View.VISIBLE);
			miniplayerSeekbar.setVisibility(View.VISIBLE);
		}
		if (profileData.containsKey("profilePicture")) {
			if (!profileData.get("profilePicture").toString().equals("")) {
				Glide.with(ApplicationUtil.getAppContext()).load(profileData.get("profilePicture").toString()).into(menu);
			} else {
				Glide.with(ApplicationUtil.getAppContext()).load(R.drawable.ic_profile_icon).into(menu);
			}
		}
		if (sessionData.containsKey("sessionRepeatMode")) {
			if (sessionData.get("sessionRepeatMode").equals("0")) {
				if (Build.VERSION.SDK_INT >= 23) {
					repeat.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
				} else {
					repeat.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
				}
			} else if (sessionData.get("sessionRepeatMode").equals("1")) {
				if (Build.VERSION.SDK_INT >= 23) {
					repeat.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
				} else {
					repeat.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
				}
			}
		} else {
			if (Build.VERSION.SDK_INT >= 23) {
				repeat.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
			} else {
				repeat.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
			}
			sessionData.put("sessionRepeatMode", "0");
			FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
		}
		if (sessionData.containsKey("sessionShuffleMode")) {
			if (sessionData.get("sessionShuffleMode").equals("0")) {
				if (Build.VERSION.SDK_INT >= 23) {
					shuffle.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
				} else {
					shuffle.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
				}
			} else if (sessionData.get("sessionShuffleMode").equals("1")) {
				if (Build.VERSION.SDK_INT >= 23) {
					shuffle.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
				} else {
					shuffle.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
				}
			}
		} else {
			if (Build.VERSION.SDK_INT >= 23) {
				shuffle.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
			} else {
				shuffle.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
			}
			sessionData.put("sessionShuffleMode", "0");
			FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
		}
		registerListeners();
		startupUI();
	}

	private void registerListeners() {
		menu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
				menu.setBackground(rippleButton);
				BottomSheetDialog menuDialog = new BottomSheetDialog(LocalStreamActivity.this);
				View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_menu, null);
				menuDialog.setContentView(dialogLayout);
				LinearLayout main = dialogLayout.findViewById(R.id.main);
				TextView title = dialogLayout.findViewById(R.id.title);
				ImageView about = dialogLayout.findViewById(R.id.about);
				LinearLayout profile = dialogLayout.findViewById(R.id.profile);
				ImageView profile_icon = dialogLayout.findViewById(R.id.profile_icon);
				TextView profile_name = dialogLayout.findViewById(R.id.profile_name);
				TextView live_stream_name = dialogLayout.findViewById(R.id.live_stream_name);
				TextView visualizer_name = dialogLayout.findViewById(R.id.visualizer_name);
				TextView settings_name = dialogLayout.findViewById(R.id.settings_name);
				LinearLayout live_streaming = dialogLayout.findViewById(R.id.live_streaming);
				LinearLayout settings = dialogLayout.findViewById(R.id.settings);
				LinearLayout visualizer = dialogLayout.findViewById(R.id.visualizer);
				title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
				if (profileData.containsKey("profileName")) {
					profile_name.setText(profileData.get("profileName").toString());
				}
				if (profileData.containsKey("profilePicture")) {
					if (!profileData.get("profilePicture").toString().equals("")) {
						Glide.with(ApplicationUtil.getAppContext()).load(profileData.get("profilePicture").toString()).into(profile_icon);
					} else {
						Glide.with(ApplicationUtil.getAppContext()).load(R.drawable.ic_profile_icon).into(profile_icon);
					}
				}
				profile.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!settingsData.containsKey("settingsDarkMode")) {
							RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
							view.setBackground(rippleButton);
						} else {
							if (settingsData.get("settingsDarkMode").equals("true")) {
								RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
								view.setBackground(rippleButton);
							} else {
								RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
								view.setBackground(rippleButton);
							}
						}
						BottomSheetDialog createProfileDialog = new BottomSheetDialog(LocalStreamActivity.this);
						View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_create_a_profile, null);
						createProfileDialog.setContentView(dialogLayout);
						LinearLayout main = dialogLayout.findViewById(R.id.main);
						TextView title = dialogLayout.findViewById(R.id.title);
						ImageView profile_icon = dialogLayout.findViewById(R.id.profile_icon);
						EditText profile_name = dialogLayout.findViewById(R.id.profile_name);
						Button create = dialogLayout.findViewById(R.id.create);
						if (profileData.containsKey("profileName")) {
							profile_name.setText(profileData.get("profileName").toString());
						}
						if (profileData.containsKey("profilePicture")) {
							if (!profileData.get("profilePicture").toString().equals("")) {
								Glide.with(ApplicationUtil.getAppContext()).load(profileData.get("profilePicture").toString()).into(profile_icon);
							} else {
								Glide.with(ApplicationUtil.getAppContext()).load(R.drawable.ic_profile_icon).into(profile_icon);
							}
						}
						title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
						profile_icon.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor("#BDBDBD")}), null, null);
								view.setBackground(rippleButton);
								BottomSheetDialog pfpDialog = new BottomSheetDialog(LocalStreamActivity.this);
								View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_create_a_profile_icon, null);
								pfpDialog.setContentView(dialogLayout);
								LinearLayout main = dialogLayout.findViewById(R.id.main);
								TextView title = dialogLayout.findViewById(R.id.title);
								ImageView profile_picture = dialogLayout.findViewById(R.id.profile_icon);
								EditText url = dialogLayout.findViewById(R.id.url);
								Button finish = dialogLayout.findViewById(R.id.finish);
								Button cancel = dialogLayout.findViewById(R.id.cancel);
								if (profileData.containsKey("profilePicture")) {
									Glide.with(ApplicationUtil.getAppContext()).load(profileData.get("profilePicture").toString()).into(profile_picture);
									url.setText(profileData.get("profilePicture").toString());
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
											Glide.with(ApplicationUtil.getAppContext()).load(url.getText().toString()).into(profile_picture);
										} else {
											Glide.with(ApplicationUtil.getAppContext()).load(R.drawable.ic_profile_icon).into(profile_picture);
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
										if (profileData.containsKey("profilePicture")) {
											if (url.getText().toString().equals(profileData.get("profileName").toString())) {
												pfpDialog.dismiss();
											} else {
												String pfpUrl = url.getText().toString();
												profileData.put("profilePicture", pfpUrl);
												FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/profile.pref"), ListUtil.setHashMapToSharedJSON(profileData));
												if (!pfpUrl.equals("")) {
													Glide.with(ApplicationUtil.getAppContext()).load(pfpUrl).into(profile_icon);
													Glide.with(ApplicationUtil.getAppContext()).load(pfpUrl).into(menu);
												} else {
													Glide.with(ApplicationUtil.getAppContext()).load(R.drawable.ic_profile_icon).into(profile_icon);
													Glide.with(ApplicationUtil.getAppContext()).load(R.drawable.ic_profile_icon).into(menu);
												}
												tabNavigation.getTabAt(0).select();
												menuDialog.dismiss();
												pfpDialog.dismiss();
											}
										} else {
											if (url.getText().toString().equals(profileData.get("profileName").toString())) {
												pfpDialog.dismiss();
											} else {
												String pfpUrl = url.getText().toString();
												profileData.put("profilePicture", pfpUrl);
												FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/profile.pref"), ListUtil.setHashMapToSharedJSON(profileData));
												if (!pfpUrl.equals("")) {
													Glide.with(ApplicationUtil.getAppContext()).load(pfpUrl).into(profile_icon);
													Glide.with(ApplicationUtil.getAppContext()).load(pfpUrl).into(menu);
												} else {
													Glide.with(ApplicationUtil.getAppContext()).load(R.drawable.ic_profile_icon).into(profile_icon);
													Glide.with(ApplicationUtil.getAppContext()).load(R.drawable.ic_profile_icon).into(menu);
												}
												tabNavigation.getTabAt(0).select();
												menuDialog.dismiss();
												pfpDialog.dismiss();
											}
										}
									}
								});
								cancel.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										if (!settingsData.containsKey("settingsDarkMode")) {
											RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
											view.setBackground(rippleButton);
										} else {
											if (settingsData.get("settingsDarkMode").equals("true")) {
												RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
												view.setBackground(rippleButton);
											} else {
												RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
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
								GradientDrawable roundedCorners = new GradientDrawable();
								roundedCorners.setShape(GradientDrawable.RECTANGLE);
								roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
								GradientDrawable roundedCorners2 = new GradientDrawable();
								roundedCorners2.setShape(GradientDrawable.RECTANGLE);
								roundedCorners2.setCornerRadius(20);
								if (!settingsData.containsKey("settingsDarkMode")) {
									roundedCorners.setColor(Color.parseColor("#FFFFFF"));
									roundedCorners2.setColor(Color.parseColor("#EEEEEE"));
								} else {
									if (settingsData.get("settingsDarkMode").equals("true")) {
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
								GradientDrawable gradientButton = new GradientDrawable();
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
									if (profileData.containsKey("profileName")) {
										if (profile_name.getText().toString().equals(profileData.get("profileName").toString())) {
											createProfileDialog.dismiss();
										} else {
											String profileName = profile_name.getText().toString();
											profileData.put("profileName", profileName);
											FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/profile.pref"), ListUtil.setHashMapToSharedJSON(profileData));
											ApplicationUtil.toast("Renamed profile sucessfully.", Toast.LENGTH_SHORT);
											tabNavigation.getTabAt(0).select();
											createProfileDialog.dismiss();
											menuDialog.dismiss();
											startActivity(new Intent(ApplicationUtil.getAppContext(), LauncherActivity.class));
											finish();
										}
									} else {
										String profileName = profile_name.getText().toString();
										profileData.put("profileName", profileName);
										FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/profile.pref"), ListUtil.setHashMapToSharedJSON(profileData));
										ApplicationUtil.toast("Renamed profile sucessfully.", Toast.LENGTH_SHORT);
										tabNavigation.getTabAt(0).select();
										createProfileDialog.dismiss();
										menuDialog.dismiss();
										startActivity(new Intent(ApplicationUtil.getAppContext(), LauncherActivity.class));
										finish();
									}
								} else {
									profile_name.setError("Profile name should not be blank.");
								}
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
						if (!settingsData.containsKey("settingsDarkMode")) {
							roundedCorners.setColor(Color.parseColor("#FFFFFF"));
							roundedCorners2.setColor(Color.parseColor("#EEEEEE"));
						} else {
							if (settingsData.get("settingsDarkMode").equals("true")) {
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
						GradientDrawable gradientButton = new GradientDrawable();
						gradientButton.setColor(Color.parseColor("#03A9F4"));
						gradientButton.setCornerRadius(20);
						create.setBackground(gradientButton);
						createProfileDialog.show();
					}
				});
				live_streaming.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!settingsData.containsKey("settingsDarkMode")) {
							RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
							view.setBackground(rippleButton);
						} else {
							if (settingsData.get("settingsDarkMode").equals("true")) {
								RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
								view.setBackground(rippleButton);
							} else {
								RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
								view.setBackground(rippleButton);
							}
						}
						ApplicationUtil.toast("Feature under construction.", Toast.LENGTH_SHORT);
					}
				});
				settings.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!settingsData.containsKey("settingsDarkMode")) {
							RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
							view.setBackground(rippleButton);
						} else {
							if (settingsData.get("settingsDarkMode").equals("true")) {
								RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
								view.setBackground(rippleButton);
							} else {
								RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
								view.setBackground(rippleButton);
							}
						}
						BottomSheetDialog settingsDialog = new BottomSheetDialog(LocalStreamActivity.this);
						View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_settings, null);
						settingsDialog.setContentView(dialogLayout);
						LinearLayout main = dialogLayout.findViewById(R.id.main);
						ImageView back = dialogLayout.findViewById(R.id.back);
						TextView title = dialogLayout.findViewById(R.id.title);
						TextView general_title = dialogLayout.findViewById(R.id.general_title);
						TextView note = dialogLayout.findViewById(R.id.note);
						CheckBox dark_mode = dialogLayout.findViewById(R.id.dark_mode);
						CheckBox disable_ads = dialogLayout.findViewById(R.id.disable_ads);
						title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
						general_title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
						if (settingsData.containsKey("settingsDarkMode")) {
							if (settingsData.get("settingsDarkMode").equals("true")) {
								dark_mode.setChecked(true);
							}
						}
						back.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
								view.setBackground(rippleButton);
								settingsDialog.dismiss();
							}
						});
						dark_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton view, boolean isChecked) {
								if (isChecked) {
									sessionData.put("sessionDarkMode", "true");
									FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
									startActivity(new Intent(ApplicationUtil.getAppContext(), SplashActivity.class));
									finish();
									ApplicationUtil.toast("Dark mode enabled.", Toast.LENGTH_SHORT);
								} else {
									sessionData.put("sessionDarkMode", "false");
									FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
									startActivity(new Intent(ApplicationUtil.getAppContext(), SplashActivity.class));
									finish();
									ApplicationUtil.toast("Dark mode disabled.", Toast.LENGTH_SHORT);
								}
							}
						});
						disable_ads.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton view, boolean isChecked) {
								if (isChecked) {
									sessionData.put("sessionAds", "true");
									ApplicationUtil.toast("Ads enabled.", Toast.LENGTH_SHORT);
								} else {
									sessionData.put("sessionAds", "false");
									ApplicationUtil.toast("Ads disabled.", Toast.LENGTH_SHORT);
								}
							}
						});
						Double TopLeft = 20.0;
						Double TopRight = 20.0;
						Double BottomRight = 0.0;
						Double BottomLeft = 0.0;
						GradientDrawable roundedCorners = new GradientDrawable();
						roundedCorners.setShape(GradientDrawable.RECTANGLE);
						roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
						if (!settingsData.containsKey("settingsDarkMode")) {
							roundedCorners.setColor(Color.parseColor("#FFFFFF"));
						} else {
							if (settingsData.get("settingsDarkMode").equals("true")) {
								roundedCorners.setColor(Color.parseColor("#1A1A1A"));
								dark_mode.setTextColor(Color.parseColor("#FFFFFF"));
								disable_ads.setTextColor(Color.parseColor("#FFFFFF"));
								note.setTextColor(Color.parseColor("#FFFFFF"));
							} else {
								roundedCorners.setColor(Color.parseColor("#FFFFFF"));
							}
						}
						((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
						settingsDialog.show();
					}
				});
				visualizer.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!settingsData.containsKey("settingsDarkMode")) {
							RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
							view.setBackground(rippleButton);
						} else {
							if (settingsData.get("settingsDarkMode").equals("true")) {
								RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
								view.setBackground(rippleButton);
							} else {
								RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
								view.setBackground(rippleButton);
							}
						}
						if (ContextCompat.checkSelfPermission(ApplicationUtil.getAppContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
							ActivityCompat.requestPermissions(LocalStreamActivity.this, new String[]{"android.permission.RECORD_AUDIO"}, 1);
						} else {
							BottomSheetDialog visualizerDialog = new BottomSheetDialog(LocalStreamActivity.this);
							View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_visualizer, null);
							visualizerDialog.setContentView(dialogLayout);
							ImageView back = dialogLayout.findViewById(R.id.back);
							ImageView fullscreen = dialogLayout.findViewById(R.id.fullscreen);
							BarVisualizer visualizer = dialogLayout.findViewById(R.id.visualizer);
							back.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
									view.setBackground(rippleButton);
									if (playbackSrv.mp != null && visualizerDialog != null) {
										visualizer.release();
									}
									visualizerDialog.dismiss();
								}
							});
							fullscreen.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
									view.setBackground(rippleButton);
									if (playbackSrv.mp != null && visualizerDialog != null) {
										visualizer.release();
									}
									startActivity(new Intent(ApplicationUtil.getAppContext(), FullVisualizerActivity.class));
									visualizerDialog.dismiss();
									menuDialog.dismiss();
								}});
							if (playbackSrv.mp != null) {
								if (playbackSrv.mp.getAudioSessionId() != -1) {
									visualizer.setAudioSessionId(playbackSrv.mp.getAudioSessionId());
								}
								if (!playbackSrv.isPlaying()) {
									ApplicationUtil.toast("Visualizer not visible, please resume/play the song.", Toast.LENGTH_LONG);
								}
							}
							Double TopLeft = 20.0;
							Double TopRight = 20.0;
							Double BottomRight = 0.0;
							Double BottomLeft = 0.0;
							GradientDrawable roundedCorners = new GradientDrawable();
							roundedCorners.setShape(GradientDrawable.RECTANGLE);
							roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
							roundedCorners.setColor(Color.parseColor("#000000"));
							((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
							visualizerDialog.setCancelable(false);
							visualizerDialog.show();
						}
					}
				});
				about.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
						view.setBackground(rippleButton);
						BottomSheetDialog about = new BottomSheetDialog(LocalStreamActivity.this);
						View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_about, null);
						about.setContentView(dialogLayout);
						LinearLayout main = dialogLayout.findViewById(R.id.main);
						TextView title = dialogLayout.findViewById(R.id.title);
						TextView author = dialogLayout.findViewById(R.id.author);
						TextView youtube_name = dialogLayout.findViewById(R.id.youtube_name);
						TextView twitter_name = dialogLayout.findViewById(R.id.twitter_name);
						TextView github_name = dialogLayout.findViewById(R.id.github_name);
						TextView discord_name = dialogLayout.findViewById(R.id.discord_name);
						TextView licenses_name = dialogLayout.findViewById(R.id.license_name);
						TextView privacy_name = dialogLayout.findViewById(R.id.privacy_name);
						TextView version = dialogLayout.findViewById(R.id.version);
						LinearLayout youtube = dialogLayout.findViewById(R.id.youtube);
						LinearLayout twitter = dialogLayout.findViewById(R.id.twitter);
						LinearLayout github = dialogLayout.findViewById(R.id.github);
						LinearLayout discord = dialogLayout.findViewById(R.id.discord);
						LinearLayout license = dialogLayout.findViewById(R.id.license);
						LinearLayout privacy = dialogLayout.findViewById(R.id.privacy);
						title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/leixo.ttf"), Typeface.BOLD);
						int randomizer = IntegerUtil.getRandom((int)(0), (int)(9));
						if (randomizer == 0) {
							author.setText(R.string.about_description);
						} else if (randomizer == 1) {
							author.setText(R.string.about_description2);
						} else if (randomizer == 2) {
							author.setText(R.string.about_description3);
						} else if (randomizer == 3) {
							author.setText(R.string.about_description4);
						} else if (randomizer == 4) {
							author.setText(R.string.about_description5);
						} else if (randomizer == 5) {
							author.setText(R.string.about_description6);
						} else if (randomizer == 6) {
							author.setText(R.string.about_description7);
						} else if (randomizer == 7) {
							author.setText(R.string.about_description8);
						} else if (randomizer == 8) {
							author.setText(R.string.about_description9);
						} else if (randomizer == 10) {
							author.setText(R.string.about_description10);
						}
						youtube.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								if (!settingsData.containsKey("settingsDarkMode")) {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
									view.setBackground(rippleButton);
								} else {
									if (settingsData.get("settingsDarkMode").equals("true")) {
										RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
										view.setBackground(rippleButton);
									} else {
										RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
										view.setBackground(rippleButton);
									}
								}
								Intent intent = new Intent();
								intent.putExtra("url", "https://youtube.com/channel/UCndTdCP5Qr-ekaV2Im1VCgg");
								intent.setClass(ApplicationUtil.getAppContext(), ExternalBrowserActivity.class);
								startActivity(intent);
							}
						});
						twitter.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								if (!settingsData.containsKey("settingsDarkMode")) {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
									view.setBackground(rippleButton);
								} else {
									if (settingsData.get("settingsDarkMode").equals("true")) {
										RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
										view.setBackground(rippleButton);
									} else {
										RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
										view.setBackground(rippleButton);
									}
								}
								Intent intent = new Intent();
								intent.putExtra("url", "https://twitter.com/gianxddddd");
								intent.setClass(ApplicationUtil.getAppContext(), ExternalBrowserActivity.class);
								startActivity(intent);
							}
						});
						github.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								if (!settingsData.containsKey("settingsDarkMode")) {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
									view.setBackground(rippleButton);
								} else {
									if (settingsData.get("settingsDarkMode").equals("true")) {
										RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
										view.setBackground(rippleButton);
									} else {
										RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
										view.setBackground(rippleButton);
									}
								}
								Intent intent = new Intent();
								intent.putExtra("url", "https://github.com/gianxddddd/ArchoMusic");
								intent.setClass(ApplicationUtil.getAppContext(), ExternalBrowserActivity.class);
								startActivity(intent);
							}
						});
						discord.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								if (!settingsData.containsKey("settingsDarkMode")) {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
									view.setBackground(rippleButton);
								} else {
									if (settingsData.get("settingsDarkMode").equals("true")) {
										RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
										view.setBackground(rippleButton);
									} else {
										RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
										view.setBackground(rippleButton);
									}
								}
								Intent intent = new Intent();
								intent.putExtra("url", "https://discord.gg/x5t9n9fWCV");
								intent.setClass(ApplicationUtil.getAppContext(), ExternalBrowserActivity.class);
								startActivity(intent);
							}
						});
						license.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								if (!settingsData.containsKey("settingsDarkMode")) {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
									view.setBackground(rippleButton);
								} else {
									if (settingsData.get("settingsDarkMode").equals("true")) {
										RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
										view.setBackground(rippleButton);
									} else {
										RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
										view.setBackground(rippleButton);
									}
								}
								Intent intent = new Intent();
								intent.putExtra("url", "file:///android_asset/LICENSE.html");
								intent.setClass(ApplicationUtil.getAppContext(), ExternalBrowserActivity.class);
								startActivity(intent);
							}
						});
						privacy.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								if (!settingsData.containsKey("settingsDarkMode")) {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
									view.setBackground(rippleButton);
								} else {
									if (settingsData.get("settingsDarkMode").equals("true")) {
										RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
										view.setBackground(rippleButton);
									} else {
										RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
										view.setBackground(rippleButton);
									}
								}
								Intent intent = new Intent();
								intent.putExtra("url", "file:///android_asset/PRIVACY.html");
								intent.setClass(ApplicationUtil.getAppContext(), ExternalBrowserActivity.class);
								startActivity(intent);
							}
						});
						Double TopLeft = 20.0;
						Double TopRight = 20.0;
						Double BottomRight = 0.0;
						Double BottomLeft = 0.0;
						GradientDrawable roundedCorners = new GradientDrawable();
						roundedCorners.setShape(GradientDrawable.RECTANGLE);
						roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
						if (!settingsData.containsKey("settingsDarkMode")) {
							roundedCorners.setColor(Color.parseColor("#FFFFFF"));
						} else {
							if (settingsData.get("settingsDarkMode").equals("true")) {
								roundedCorners.setColor(Color.parseColor("#1A1A1A"));
								author.setTextColor(Color.parseColor("#FFFFFF"));
								youtube_name.setTextColor(Color.parseColor("#FFFFFF"));
								twitter_name.setTextColor(Color.parseColor("#FFFFFF"));
								github_name.setTextColor(Color.parseColor("#FFFFFF"));
								discord_name.setTextColor(Color.parseColor("#FFFFFF"));
								licenses_name.setTextColor(Color.parseColor("#FFFFFF"));
								privacy_name.setTextColor(Color.parseColor("#FFFFFF"));
								version.setTextColor(Color.parseColor("#FFFFFF"));
							} else {
								roundedCorners.setColor(Color.parseColor("#FFFFFF"));
							}
						}
						((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
						about.show();
					}
				});
				Double TopLeft = 20.0;
				Double TopRight = 20.0;
				Double BottomRight = 0.0;
				Double BottomLeft = 0.0;
				GradientDrawable roundedCorners = new GradientDrawable();
				roundedCorners.setShape(GradientDrawable.RECTANGLE);
				roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
				if (!settingsData.containsKey("settingsDarkMode")) {
					roundedCorners.setColor(Color.parseColor("#FFFFFF"));
				} else {
					if (settingsData.get("settingsDarkMode").equals("true")) {
						roundedCorners.setColor(Color.parseColor("#1A1A1A"));
						profile_name.setTextColor(Color.parseColor("#FFFFFF"));
						live_stream_name.setTextColor(Color.parseColor("#FFFFFF"));
						visualizer_name.setTextColor(Color.parseColor("#FFFFFF"));
						settings_name.setTextColor(Color.parseColor("#FFFFFF"));
					} else {
						roundedCorners.setColor(Color.parseColor("#FFFFFF"));
					}
				}
				((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
				menuDialog.show();
			}
		});
		miniplayer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (settingsData.containsKey("settingsDarkMode")) {
					if (!settingsData.get("settingsDarkMode").equals("true")) {
						miniplayer.setBackground(new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null));
					} else {
						miniplayer.setBackground(new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null));
					}
				} else {
					miniplayer.setBackground(new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null));
				}
				tabNavigation.getTabAt(1).select();
			}
		});
		tabNavigation.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				ObjectAnimator fadeAnim = new ObjectAnimator();
				if (Build.VERSION.SDK_INT >= 23) {
					tab.getIcon().setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
				} else {
					tab.getIcon().setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
				}
				if (tab.getPosition() == 0) {
					if (fadeAnim.isRunning()) {
						fadeAnim.cancel();
					}
					sessionData.put("sessionNavigationIndex", "0");
					FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
					player.setVisibility(View.VISIBLE);
					listRefresh.setVisibility(View.GONE);
					miniplayer.setVisibility(View.GONE);
					miniplayerSeekbar.setVisibility(View.GONE);
					fadeAnim.setTarget(player);
					fadeAnim.setPropertyName("alpha");
					fadeAnim.setFloatValues((float)(1.0d), (float)(0.0d));
					fadeAnim.start();
					timerTask = new TimerTask() {
						@Override
						public void run() {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									player.setVisibility(View.GONE);
									listRefresh.setVisibility(View.VISIBLE);
									miniplayer.setVisibility(View.VISIBLE);
									miniplayerSeekbar.setVisibility(View.VISIBLE);
									fadeAnim.setTarget(listRefresh);
									fadeAnim.setPropertyName("alpha");
									fadeAnim.setFloatValues((float)(0.0d), (float)(1.0d));
									fadeAnim.start();
								}
							});
						}
					};
					timer.schedule(timerTask, (int)(250));
				} else if (tab.getPosition() == 1) {
					if (fadeAnim.isRunning()) {
						fadeAnim.cancel();
					}
					sessionData.put("sessionNavigationIndex", "1");
					FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
					player.setVisibility(View.GONE);
					listRefresh.setVisibility(View.VISIBLE);
					miniplayer.setVisibility(View.VISIBLE);
					miniplayerSeekbar.setVisibility(View.VISIBLE);
					fadeAnim.setTarget(listRefresh);
					fadeAnim.setPropertyName("alpha");
					fadeAnim.setFloatValues((float)(1.0d), (float)(0.0d));
					fadeAnim.start();
					timerTask = new TimerTask() {
						@Override
						public void run() {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									player.setVisibility(View.VISIBLE);
									listRefresh.setVisibility(View.GONE);
									miniplayer.setVisibility(View.GONE);
									miniplayerSeekbar.setVisibility(View.GONE);
									fadeAnim.setTarget(player);
									fadeAnim.setPropertyName("alpha");
									fadeAnim.setFloatValues((float)(0.0d), (float)(1.0d));
									fadeAnim.start();
								}
							});
						}
					};
					timer.schedule(timerTask, (int)(250));
				}
			}
			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
				if (Build.VERSION.SDK_INT >= 23) {
					tab.getIcon().setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.SRC_IN);
				}
				else {
					tab.getIcon().setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
				}
			}
			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});
		listRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				listRefresh.setRefreshing(false);
			}
		});
		seekbarDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean idk) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (playbackSrv.mp != null) {
					playbackSrv.seek(seekbarDuration.getProgress());
					seekbarDuration.setProgress((int)seekbarDuration.getProgress());
					miniplayerSeekbar.setProgress((int)seekbarDuration.getProgress());
					currentDuration.setText(String.valueOf((int)((seekbarDuration.getProgress() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((seekbarDuration.getProgress() / 1000) % 60))));
					musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).put("songCurrentDuration", String.valueOf((int)(seekbarDuration.getProgress())));
					FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/song.json"), ListUtil.setArrayListToSharedJSON(musicData));
				}
			}
		});
		repeat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
				view.setBackground(rippleButton);
				if (sessionData.containsKey("sessionRepeatMode")) {
					if (sessionData.get("sessionRepeatMode").equals("0")) {
						if (Build.VERSION.SDK_INT >= 23) {
							repeat.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
						} else {
							repeat.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
						}
						sessionData.put("sessionRepeatMode", "1");
						FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
						if (sessionData.get("sessionShuffleMode").equals("1")) {
							if (Build.VERSION.SDK_INT >= 23) {
								shuffle.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
							} else {
								shuffle.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
							}
							sessionData.put("sessionShuffleMode", "0");
							FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
						}
						playbackSrv.updateOnCompletionListener();
					} else if (sessionData.get("sessionRepeatMode").equals("1")) {
						if (Build.VERSION.SDK_INT >= 23) {
							repeat.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
						} else {
							repeat.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
						}
						sessionData.put("sessionRepeatMode", "0");
						FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
					}
					playbackSrv.updateOnCompletionListener();
				} else {
					if (Build.VERSION.SDK_INT >= 23) {
						repeat.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
					} else {
						repeat.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
					}
					sessionData.put("sessionRepeatMode", "1");
					FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
					playbackSrv.updateOnCompletionListener();
				}
			}
		});
		skipBackward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
				view.setBackground(rippleButton);
				skipPrevious();
			}
		});
		playPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
				view.setBackground(rippleButton);
				playPause();
			}
		});
		skipForward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
				view.setBackground(rippleButton);
				skipNext();
			}
		});
		miniplayerSkipPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
				view.setBackground(rippleButton);
				skipPrevious();
			}
		});
		miniplayerPlayPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
				view.setBackground(rippleButton);
				playPause();
			}
		});
		miniplayerSkipNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
				view.setBackground(rippleButton);
				skipNext();
			}
		});
		shuffle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
				view.setBackground(rippleButton);
				if (sessionData.containsKey("sessionShuffleMode")) {
					if (sessionData.get("sessionShuffleMode").equals("0")) {
						if (Build.VERSION.SDK_INT >= 23) {
							shuffle.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
						} else {
							shuffle.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
						}
						sessionData.put("sessionShuffleMode", "1");
						FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
						if (sessionData.get("sessionRepeatMode").equals("1")) {
							if (Build.VERSION.SDK_INT >= 23) {
								repeat.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
							} else {
								repeat.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
							}
							sessionData.put("sessionRepeatMode", "0");
							FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
						}
						playbackSrv.updateOnCompletionListener();
					} else if (sessionData.get("sessionShuffleMode").equals("1")) {
						if (Build.VERSION.SDK_INT >= 23) {
							shuffle.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
						} else {
							shuffle.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
						}
						sessionData.put("sessionShuffleMode", "0");
                        FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
						playbackSrv.updateOnCompletionListener();
					}
				} else {
					if (Build.VERSION.SDK_INT >= 23) {
						shuffle.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
					} else {
						shuffle.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
					}
					sessionData.put("sessionShuffleMode", "0");
                    FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
					playbackSrv.updateOnCompletionListener();
				}
			}
		});
	}

	private void skipPrevious() {
		if (playbackSrv != null) {
            if (!sessionData.containsKey("sessionRepeatMode") || !profileData.containsKey("sessionShuffleMode")) {
                try {
                    if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) - 1 < musicData.size()) {
						sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) - 1));
                        FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                        playbackSrv.createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
                        playPause.performClick();
                    }
                } catch (Exception exception) {
                    if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) - 1 < musicData.size()) {
						sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) - 1));
                        FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                        playbackSrv.createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
                        playPause.performClick();
                    }
                }
            } else {
                if (sessionData.get("sessionRepeatMode").equals("0") && sessionData.get("sessionShuffleMode").equals("0")) {
                    try {
                        if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) - 1 < musicData.size()) {
							sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) - 1));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                            playbackSrv.createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
                            playPause.performClick();
                        }
                    } catch (Exception exception) {
                        if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) - 1 < musicData.size()) {
							sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) - 1));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                            playbackSrv.createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
                            playPause.performClick();
                        }
                    }
                } else if (sessionData.get("sessionRepeatMode").equals("1")) {
                    playbackSrv.seek(0);
                    if (Build.VERSION.SDK_INT >= 24) {
                        miniplayerSeekbar.setProgress(0, true);
                        seekbarDuration.setProgress(0, true);
                    } else {
                        miniplayerSeekbar.setProgress(0);
                        seekbarDuration.setProgress(0);
                    }
                    currentDuration.setText("0:00");
                    if (playbackSrv.mp != null && !playbackSrv.isPlaying()) {
                    	playPause();
					}
                } else if (sessionData.get("sessionShuffleMode").equals("1")) {
					int randomizer = IntegerUtil.getRandom(Integer.parseInt(sessionData.get("sessionSongPosition").toString()), musicData.size());
					try {
						if (randomizer < musicData.size()) {
							sessionData.put("sessionSongPosition", String.valueOf(randomizer));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
							playbackSrv.createLocalStream(randomizer);
							playPause.performClick();
						}
					} catch (Exception exception) {
						ApplicationUtil.toast("Error loading audio file.", Toast.LENGTH_SHORT);
						if (randomizer < musicData.size()) {
							sessionData.put("sessionSongPosition", String.valueOf(randomizer));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
							playbackSrv.createLocalStream(randomizer);
							playPause.performClick();
						}
					}
                }
            }
		}
	}

	private void playPause() {
		if (playbackSrv.mp != null) {
			if (!playbackSrv.isPlaying()) {
				playbackSrv.play();
				playbackSrv.startAudioFocus();
				playbackSrv.startHeadphoneReceiving();
				setVolumeControlStream(AudioManager.STREAM_MUSIC);
				playPause.setImageResource(R.drawable.ic_media_pause);
				miniplayerPlayPause.setImageResource(R.drawable.ic_media_pause);
				timerTask = new TimerTask() {
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								try {
									seekbarDuration.setProgress((int)playbackSrv.getCurrentPosition());
									miniplayerSeekbar.setProgress((int)playbackSrv.getCurrentPosition());
									currentDuration.setText(String.valueOf((int)((playbackSrv.getCurrentPosition() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((playbackSrv.getCurrentPosition() / 1000) % 60))));
									musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).put("songCurrentDuration", String.valueOf(playbackSrv.getCurrentPosition()));
                                    FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/song.json"), ListUtil.setArrayListToSharedJSON(musicData));
								} catch (Exception exception) {
									Log.e("LocalPlaybackService", "Can't track current duration.");
								}
							}
						});
					}
				};
				timer.scheduleAtFixedRate(timerTask, (int)(0), (int)(1000));
			} else {
				playbackSrv.pause();
				playbackSrv.loseAudioFocus();
				playbackSrv.stopHeadphoneReceiving();
				playPause.setImageResource(R.drawable.ic_media_play);
				miniplayerPlayPause.setImageResource(R.drawable.ic_media_play);
				if (timerTask != null) {
					timerTask.cancel();
				}
			}
		}
	}

	private void skipNext() {
		if (playbackSrv != null) {
			if (!sessionData.containsKey("sessionRepeatMode") || !sessionData.containsKey("sessionShuffleMode")) {
				try {
					if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1 < musicData.size()) {
						sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1));
                        FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
						playbackSrv.createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
						playPause.performClick();
					}
				} catch (Exception exception) {
					ApplicationUtil.toast("Error loading audio file.", Toast.LENGTH_SHORT);
					if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1 < musicData.size()) {
						sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1));
                        FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
						playbackSrv.createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
						playPause.performClick();
					}
				}
			} else {
				if (sessionData.get("sessionRepeatMode").equals("0") && sessionData.get("sessionShuffleMode").equals("0")) {
					try {
						if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1 < musicData.size()) {
							sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
							playbackSrv.createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
							playPause.performClick();
						}
					} catch (Exception exception) {
						ApplicationUtil.toast("Error loading audio file.", Toast.LENGTH_SHORT);
						if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1 < musicData.size()) {
							sessionData.put("profileSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
							playbackSrv.createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
							playPause.performClick();
						}
					}
				} else if (sessionData.get("sessionRepeatMode").equals("1")) {
					playbackSrv.seek(0);
					if (Build.VERSION.SDK_INT >= 24) {
						miniplayerSeekbar.setProgress(0, true);
						seekbarDuration.setProgress(0, true);
					} else {
						miniplayerSeekbar.setProgress(0);
						seekbarDuration.setProgress(0);
					}
					currentDuration.setText("0:00");
				} else if (sessionData.get("sessionShuffleMode").equals("1")) {
					int randomizer = IntegerUtil.getRandom(Integer.parseInt(sessionData.get("sessionSongPosition").toString()), musicData.size());
					try {
						if (randomizer < musicData.size()) {
							sessionData.put("sessionSongPosition", String.valueOf(randomizer));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
							playbackSrv.createLocalStream(randomizer);
							playPause.performClick();
						}
					} catch (Exception exception) {
						ApplicationUtil.toast("Error loading audio file.", Toast.LENGTH_SHORT);
						if (randomizer < musicData.size()) {
							sessionData.put("sessionSongPosition", String.valueOf(randomizer));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
							playbackSrv.createLocalStream(randomizer);
							playPause.performClick();
						}
					}
				}
			}
		}
	}

	private void connectToLocalPlaybackService() {
		musicConnection = new ServiceConnection(){
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				MusicBinder binder = (MusicBinder)service;
				playbackSrv = binder.getService();
				musicBound = true;
				try {
					if (playbackSrv.mp != null && playbackSrv.isPlaying()) {
						playPause.setImageResource(R.drawable.ic_media_pause);
						miniplayerPlayPause.setImageResource(R.drawable.ic_media_pause);
						Glide.with(ApplicationUtil.getAppContext()).asBitmap().load(ImageUtil.getAlbumArt(StringUtil.decodeString(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songData").toString()))).into(albumArt);
						Glide.with(ApplicationUtil.getAppContext()).asBitmap().load(ImageUtil.getAlbumArt(StringUtil.decodeString(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songData").toString()))).into(miniplayerAlbumArt);
						songTitle.setText(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songTitle").toString());
						songArtist.setText(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songArtist").toString());
						miniplayerSongTitle.setText(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songTitle").toString());
						miniplayerSongArtist.setText(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songArtist").toString());
						miniplayerSeekbar.setMax(playbackSrv.getMaxDuration());
						miniplayerSeekbar.setProgress(playbackSrv.getCurrentPosition());
						maxDuration.setText(String.valueOf((int)((playbackSrv.getMaxDuration() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((playbackSrv.getMaxDuration() / 1000) % 60))));
						currentDuration.setText(String.valueOf((int)((playbackSrv.getCurrentPosition() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((playbackSrv.getCurrentPosition() / 1000) % 60))));
						seekbarDuration.setMax(playbackSrv.getMaxDuration());
						seekbarDuration.setProgress(playbackSrv.getCurrentPosition());
					} else {
						if (sessionData.containsKey("sessionSongPosition")) {
							playbackSrv.createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
							if (musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).containsKey("songCurrentDuration")) {
								if (!musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songCurrentDuration").equals(playbackSrv.getCurrentPosition())) {
									playbackSrv.seek(Integer.parseInt(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songCurrentDuration").toString()));
									miniplayerSeekbar.setProgress(Integer.parseInt(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songCurrentDuration").toString()));
									currentDuration.setText(String.valueOf(((Integer.parseInt(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songCurrentDuration").toString()) / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((Integer.parseInt(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songCurrentDuration").toString()) / 1000) % 60))));
									seekbarDuration.setProgress(Integer.parseInt(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songCurrentDuration").toString()));
								}
							}
						} else {
                            if (!musicData.isEmpty()) {
                                if (0 < musicData.size()) {
                                    playbackSrv.createLocalStream(0);
                                    sessionData.put("sessionSongPosition", "0");
                                    FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                                }
                            }
						}
					}
				} catch (Exception e) {
                    if (!musicData.isEmpty()) {
                        if (0 < musicData.size()) {
                            playbackSrv.createLocalStream(0);
                            sessionData.put("sessionSongPosition", "0");
                            FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                        }
                    }
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				musicBound = false;
			}
		};
		if (playIntent == null) {
			playIntent = new Intent(this, LocalPlaybackService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
		} else {
			if (playbackSrv != null) {
				playIntent = new Intent(this, LocalPlaybackService.class);
				unbindService(musicConnection);
				stopService(playIntent);
				// Restart service
				bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
				startService(playIntent);
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
            default:
			break;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
			if (ContextCompat.checkSelfPermission(ApplicationUtil.getAppContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
				// Do nothing here.
			} else {
				ApplicationUtil.toast("Record permission was denied, Visualizer will not run unless you allow it.", Toast.LENGTH_SHORT);
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
		if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
			startActivity(new Intent(ApplicationUtil.getAppContext(), SplashActivity.class));
			finish();
		} else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			startActivity(new Intent(ApplicationUtil.getAppContext(), SplashActivity.class));
			finish();
		}
	}
	
	@Override
	public void onBackPressed() {
		if (playbackSrv.mp != null) {
			if (playbackSrv.mp.isPlaying()) {
				moveTaskToBack(true);
			}
			else {
				if (!playbackSrv.isPlaying()) {
					playIntent = new Intent(this, LocalPlaybackService.class);
					stopService(playIntent);
					finishAffinity();
				}
			}
		}
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
        if (FileUtil.doesExists(FileUtil.getPackageDir().concat("/song.json")) && FileUtil.isFile(FileUtil.getPackageDir().concat("/song.json"))) {
            musicData = ListUtil.getArrayListFromFile(FileUtil.getPackageDir().concat("/song.json"));
            songList.setAdapter(new SongListAdapter(musicData));
            if (!musicData.isEmpty()) {
                listEmptyMsg.setVisibility(View.GONE);
                songList.setVisibility(View.VISIBLE);
            } else {
                listEmptyMsg.setVisibility(View.VISIBLE);
                songList.setVisibility(View.GONE);
            }
        }
        if (FileUtil.doesExists(FileUtil.getPackageDir().concat("/user/profile.pref")) && FileUtil.isFile(FileUtil.getPackageDir().concat("/user/profile.pref"))) {
            profileData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir().concat("/user/profile.pref"));
        } else {
            profileData = new HashMap<>();
        }
        if (FileUtil.doesExists(FileUtil.getPackageDir().concat("/user/session.pref")) && FileUtil.isFile(FileUtil.getPackageDir().concat("/user/session.pref"))) {
            sessionData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir().concat("/user/session.pref"));
        } else {
            sessionData = new HashMap<>();
        }
        if (FileUtil.doesExists(FileUtil.getPackageDir().concat("/user/settings.pref")) && FileUtil.isFile(FileUtil.getPackageDir().concat("/user/settings.pref"))) {
            settingsData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir().concat("/user/settings.pref"));
        } else {
            settingsData = new HashMap<>();
        }
        if (sessionData.containsKey("sessionNavigationIndex")) {
            if (sessionData.get("sessionNavigationIndex").equals("0")) {
                tabNavigation.getTabAt(0).select();
                listRefresh.setVisibility(View.VISIBLE);
                miniplayer.setVisibility(View.VISIBLE);
                player.setVisibility(View.GONE);
                miniplayerSeekbar.setVisibility(View.VISIBLE);
            } else if (sessionData.get("sessionNavigationIndex").equals("1")) {
                tabNavigation.getTabAt(1).select();
                listRefresh.setVisibility(View.GONE);
                player.setVisibility(View.VISIBLE);
                miniplayer.setVisibility(View.GONE);
                miniplayerSeekbar.setVisibility(View.GONE);
            }
        } else {
            profileData.put("sessionNavigationIndex", "0");
            FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
            tabNavigation.getTabAt(0).select();
            listRefresh.setVisibility(View.VISIBLE);
            player.setVisibility(View.GONE);
            miniplayer.setVisibility(View.VISIBLE);
            miniplayerSeekbar.setVisibility(View.VISIBLE);
        }
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (playbackSrv.mp != null) {
			if (!playbackSrv.mp.isPlaying()) {
				playIntent = new Intent(this, LocalPlaybackService.class);
				stopService(playIntent);
			}
		}
	}
	public void startupUI () {
		if (Build.VERSION.SDK_INT >= 23) {
			skipBackward.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
			playPause.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
			skipForward.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
			miniplayerSkipPrev.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
			miniplayerPlayPause.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
			miniplayerSkipNext.setColorFilter(ContextCompat.getColor(ApplicationUtil.getAppContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
			if (settingsData.containsKey("settingsDarkMode")) {
				if (!settingsData.get("settingsDarkMode").equals("true")) {
					setTheme(R.style.Theme_ArchoMusic);
					top.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
					getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
					getWindow().setNavigationBarColor(Color.parseColor("#FFFFFF"));
				} else {
					setTheme(R.style.Theme_ArchoMusic_Dark);
                    top.setBackgroundColor(Color.parseColor("#1A1A1A"));
                    main.setBackgroundColor(Color.parseColor("#1A1A1A"));
                    listRefresh.setProgressBackgroundColorSchemeColor(Color.parseColor("#1A1A1A"));
                    songTitle.setTextColor(Color.parseColor("#FFFFFF"));
                    songArtist.setTextColor(Color.parseColor("#FFFFFF"));
                    currentDuration.setTextColor(Color.parseColor("#FFFFFF"));
                    maxDuration.setTextColor(Color.parseColor("#FFFFFF"));
                    miniplayer.setBackgroundColor(Color.parseColor("#1A1A1A"));
                    miniplayerSeekbar.setBackgroundColor(Color.parseColor("#1A1A1A"));
                    miniplayerSongTitle.setTextColor(Color.parseColor("#FFFFFF"));
                    miniplayerSongArtist.setTextColor(Color.parseColor("#FFFFFF"));
					getWindow().setStatusBarColor(Color.parseColor("#1A1A1A"));
					getWindow().setNavigationBarColor(Color.parseColor("#1A1A1A"));
				}
			} else {
				setTheme(R.style.Theme_ArchoMusic);
				top.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
				getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
				getWindow().setNavigationBarColor(Color.parseColor("#FFFFFF"));
			}
		} else {
			getWindow().setStatusBarColor(Color.parseColor("#000000"));
			getWindow().setNavigationBarColor(Color.parseColor("#000000"));
			miniplayerSongTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/roboto_medium.ttf"), Typeface.NORMAL);
			skipBackward.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
			playPause.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
			skipForward.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
			miniplayerSkipPrev.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
			miniplayerPlayPause.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
			miniplayerSkipNext.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
			repeat.setColorFilter(ApplicationUtil.getAppResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
		}
		miniplayerSongTitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
		logoName.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/leixo.ttf"), Typeface.BOLD);
		songTitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
		listRefresh.setColorSchemeColors(Color.parseColor("#03A9F4"), Color.parseColor("#03A9F4"), Color.parseColor("#03A9F4"));
		songList.setLayoutManager(new LinearLayoutManager(this));
	}

	public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> {

		ArrayList<HashMap<String, Object>> data;

		public SongListAdapter(ArrayList<HashMap<String, Object>> customData) {
			data = customData;
		}
		
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.recyclerview_list, null);
			RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			view.setLayoutParams(layoutParams);
			return new ViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			View view = holder.itemView;
			LinearLayout main = (LinearLayout) view.findViewById(R.id.main);
			ImageView more = (ImageView) view.findViewById(R.id.more);
			ImageView albumArt = (ImageView) view.findViewById(R.id.albumArt);
			TextView songTitle = (TextView) view.findViewById(R.id.songTitle);
			TextView songArtist = (TextView) view.findViewById(R.id.songArtist);
			RecyclerView.LayoutParams recyclerLayoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			view.setLayoutParams(recyclerLayoutParams);
			if (settingsData.containsKey("settingsDarkMode")) {
				if (settingsData.get("settingsDarkMode").equals("true")) {
					main.setBackgroundColor(Color.parseColor("#1A1A1A"));
					songTitle.setTextColor(Color.parseColor("#FFFFFF"));
					songArtist.setTextColor(Color.parseColor("#FFFFFF"));
				}
			}
			Glide.with(ApplicationUtil.getAppContext()).asBitmap().load(ImageUtil.getAlbumArt(StringUtil.decodeString(data.get(position).get("songData").toString()))).into(albumArt);
			songTitle.setText(data.get(position).get("songTitle").toString());
			songArtist.setText(data.get(position).get("songArtist").toString());
			main.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (settingsData.containsKey("settingsDarkMode")) {
						if (!settingsData.get("settingsDarkMode").equals("true")) {
							RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
							main.setBackground(rippleButton);
						} else {
							RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
							main.setBackground(rippleButton);
						}
					} else {
						RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
						main.setBackground(rippleButton);
					}
					if (!(position == Integer.parseInt(sessionData.get("sessionSongPosition").toString()))) {
						if (new java.io.File(StringUtil.decodeString(musicData.get(position).get("songData").toString())).exists()) {
							try {
								playbackSrv.createLocalStream(position);
								sessionData.put("sessionSongPosition", String.valueOf(position));
                                FileUtil.writeStringToFile(FileUtil.getPackageDir().concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
								playPause.performClick();
							} catch (Exception e) {
								ApplicationUtil.toast("Error loading audio file.", Toast.LENGTH_SHORT);
								skipForward.performClick();
							}
						} else {
							ApplicationUtil.toast("Selected song does not exist.", Toast.LENGTH_SHORT);
						}
					} else {
						ApplicationUtil.toast("Selected song is currently playing.", Toast.LENGTH_SHORT);
					}
				}
			});
			more.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
					view.setBackground(rippleButton);
					BottomSheetDialog songOptsDialog = new BottomSheetDialog(LocalStreamActivity.this);
					View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_song_opts, null);
					songOptsDialog.setContentView(dialogLayout);
					LinearLayout main = dialogLayout.findViewById(R.id.main);
					TextView title = dialogLayout.findViewById(R.id.title);
					ImageView albumArt = dialogLayout.findViewById(R.id.albumArt);
					TextView songTitle = dialogLayout.findViewById(R.id.songTitle);
					TextView songArtist = dialogLayout.findViewById(R.id.songArtist);
					TextView rename_title = dialogLayout.findViewById(R.id.rename_title);
					TextView lyrics_title = dialogLayout.findViewById(R.id.lyrics_title);
					TextView share_title = dialogLayout.findViewById(R.id.share_title);
					TextView moreInformation_title = dialogLayout.findViewById(R.id.moreInformation_title);
					TextView remove_title = dialogLayout.findViewById(R.id.remove_title);
					LinearLayout rename = dialogLayout.findViewById(R.id.rename);
					LinearLayout lyrics = dialogLayout.findViewById(R.id.lyrics);
					LinearLayout share = dialogLayout.findViewById(R.id.share);
					LinearLayout moreInformation = dialogLayout.findViewById(R.id.moreInformation);
					LinearLayout remove = dialogLayout.findViewById(R.id.remove);
					title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/roboto_medium.ttf"), Typeface.NORMAL);
					Glide.with(ApplicationUtil.getAppContext()).asBitmap().load(ImageUtil.getAlbumArt(StringUtil.decodeString(data.get(position).get("songData").toString()))).into(albumArt);
					songTitle.setText(musicData.get(position).get("songTitle").toString());
					songArtist.setText(musicData.get(position).get("songArtist").toString());
					rename.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							if (!settingsData.containsKey("settingsDarkMode")) {
								RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
								view.setBackground(rippleButton);
							} else {
								if (settingsData.get("settingsDarkMode").equals("true")) {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
									view.setBackground(rippleButton);
								} else {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
									view.setBackground(rippleButton);
								}
							}
						}
					});
					lyrics.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							if (!settingsData.containsKey("settingsDarkMode")) {
								RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
								view.setBackground(rippleButton);
							} else {
								if (settingsData.get("settingsDarkMode").equals("true")) {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
									view.setBackground(rippleButton);
								} else {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
									view.setBackground(rippleButton);
								}
							}
							BottomSheetDialog lyricsDialog = new BottomSheetDialog(LocalStreamActivity.this);
							View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_lyrics, null);
							lyricsDialog.setContentView(dialogLayout);
							LinearLayout main = dialogLayout.findViewById(R.id.main);
							ImageView back = dialogLayout.findViewById(R.id.back);
							ImageView lyrics_edit = dialogLayout.findViewById(R.id.lyrics_edit);
							TextView title = dialogLayout.findViewById(R.id.title);
							TextView lyrics = dialogLayout.findViewById(R.id.lyrics);
							title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
							if (musicData.get(position).containsKey("songLyrics")) {
								if (musicData.get(position).get("songLyrics").toString().length() == 0) {
									// Lyrics added with 0 letters
								} else {
									lyrics.setText(musicData.get(position).get("songLyrics").toString());
								}
							} else {
								// No Lyrics was found.
							}
							back.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
									view.setBackground(rippleButton);
									lyricsDialog.dismiss();
								}
							});
							lyrics_edit.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
									view.setBackground(rippleButton);
									Intent intent = new Intent(ApplicationUtil.getAppContext(), LyricsEditorActivity.class);
									intent.putExtra("songPosition", String.valueOf((int)(Integer.parseInt(sessionData.get("sessionSongPosition").toString()))));
									startActivity(intent);
									lyricsDialog.dismiss();
									songOptsDialog.dismiss();
								}
							});
							Double TopLeft = 20.0;
							Double TopRight = 20.0;
							Double BottomRight = 0.0;
							Double BottomLeft = 0.0;
							GradientDrawable roundedCorners = new GradientDrawable();
							roundedCorners.setShape(GradientDrawable.RECTANGLE);
							roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
							if (!settingsData.containsKey("settingsDarkMode")) {
								roundedCorners.setColor(Color.parseColor("#FFFFFF"));
							} else {
								if (settingsData.get("settingsDarkMode").equals("true")) {
									roundedCorners.setColor(Color.parseColor("#1A1A1A"));
									lyrics.setTextColor(Color.parseColor("#FFFFFF"));
									lyrics.setHintTextColor(Color.parseColor("#BDBDBD"));
								} else {
									roundedCorners.setColor(Color.parseColor("#FFFFFF"));
								}
							}
							((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
							lyricsDialog.show();
						}
					});
					share.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							if (!settingsData.containsKey("settingsDarkMode")) {
								RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
								view.setBackground(rippleButton);
							} else {
								if (settingsData.get("settingsDarkMode").equals("true")) {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
									view.setBackground(rippleButton);
								} else {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
									view.setBackground(rippleButton);
								}
							}
						}
					});
					moreInformation.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							if (!settingsData.containsKey("settingsDarkMode")) {
								RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
								view.setBackground(rippleButton);
							} else {
								if (settingsData.get("settingsDarkMode").equals("true")) {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
									view.setBackground(rippleButton);
								} else {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
									view.setBackground(rippleButton);
								}
							}
						}
					});
					remove.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							if (!settingsData.containsKey("settingsDarkMode")) {
								RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
								view.setBackground(rippleButton);
							} else {
								if (settingsData.get("settingsDarkMode").equals("true")) {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
									view.setBackground(rippleButton);
								} else {
									RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
									view.setBackground(rippleButton);
								}
							}
						}
					});
					Double TopLeft = 20.0;
					Double TopRight = 20.0;
					Double BottomRight = 0.0;
					Double BottomLeft = 0.0;
					GradientDrawable roundedCorners = new GradientDrawable();
					roundedCorners.setShape(GradientDrawable.RECTANGLE);
					roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
					if (!settingsData.containsKey("settingsDarkMode")) {
						roundedCorners.setColor(Color.parseColor("#FFFFFF"));
					} else {
						if (settingsData.get("settingsDarkMode").equals("true")) {
							roundedCorners.setColor(Color.parseColor("#1A1A1A"));
							songTitle.setTextColor(Color.parseColor("#FFFFFF"));
							songArtist.setTextColor(Color.parseColor("#FFFFFF"));
							rename_title.setTextColor(Color.parseColor("#FFFFFF"));
							lyrics_title.setTextColor(Color.parseColor("#FFFFFF"));
							share_title.setTextColor(Color.parseColor("#FFFFFF"));
							moreInformation_title.setTextColor(Color.parseColor("#FFFFFF"));
						} else {
							roundedCorners.setColor(Color.parseColor("#FFFFFF"));
						}
					}
					((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
					songOptsDialog.show();
				}
			});
			ObjectAnimator itemAnim = new ObjectAnimator();
			itemAnim.setTarget(main);
			itemAnim.setPropertyName("alpha");
			itemAnim.setFloatValues((float)(0.0d), (float)(1.0d));
			itemAnim.start();
		}
		
		@Override
		public int getItemCount() {
			return data.size();
		}
		
		public class ViewHolder extends RecyclerView.ViewHolder{
			public ViewHolder(View v){
				super(v);
			}
		}
		
	}

}
