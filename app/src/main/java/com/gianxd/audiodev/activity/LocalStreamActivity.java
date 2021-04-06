package com.gianxd.audiodev.activity;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.RippleDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
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
import com.gianxd.audiodev.util.ImageUtil;
import com.gianxd.audiodev.util.ListUtil;
import com.gianxd.audiodev.util.StringUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static com.gianxd.audiodev.AudioDev.applicationContext;

public class LocalStreamActivity extends  AppCompatActivity  {
	
	private Timer timer = new Timer();
	private ArrayList<HashMap<String, Object>> musicData;
	private HashMap<String, Object> profileData;

	private ServiceConnection musicConnection;
	private LocalPlaybackService playbackSrv;
	private Intent playIntent;
	private boolean musicBound = false;

	private LinearLayout top;
	public static ProgressBar miniplayerSeekbar;
	private LinearLayout miniplayer;
	private TextView logoName;
	private TabLayout tabNavigation;
	private ImageView menu;
	private SwipeRefreshLayout listRefresh;
	private LinearLayout player;
	private RecyclerView songList;
    public static ImageView albumArt;
    public static TextView songTitle;
    public static TextView songArtist;
    public static TextView currentDuration;
    public static SeekBar seekbarDuration;
    public static TextView maxDuration;
    public static ImageView skipBackward;
    public static ImageView playPause;
    public static ImageView skipForward;
	private ImageView miniplayerSkipPrev;
    public static ImageView miniplayerPlayPause;
	private ImageView miniplayerSkipNext;
	public static ImageView miniplayerAlbumArt;
    public static TextView miniplayerSongTitle;
    public static TextView miniplayerSongArtist;
	
	private SharedPreferences savedData;
	private TimerTask timerTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_local_streaming);
		initialize(savedInstanceState);
		com.google.firebase.FirebaseApp.initializeApp(this);
		initializeLogic();
	}
	
	private void initialize(Bundle savedInstanceState) {
		top = (LinearLayout) findViewById(R.id.up);
		miniplayerSeekbar = (ProgressBar) findViewById(R.id.miniplayerSeekbar);
		miniplayer = (LinearLayout) findViewById(R.id.miniplayer);
		logoName = (TextView) findViewById(R.id.logoName);
		tabNavigation = (TabLayout) findViewById(R.id.tabNavigation);
		menu = (ImageView) findViewById(R.id.menu);
		listRefresh = (SwipeRefreshLayout) findViewById(R.id.listRefresh);
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
		savedData = applicationContext.getSharedPreferences("savedData", Context.MODE_PRIVATE);
		tabNavigation.addTab(tabNavigation.newTab().setIcon(R.drawable.ic_tabnav_library));
		tabNavigation.addTab(tabNavigation.newTab().setIcon(R.drawable.ic_tabnav_nowplaying));
		if (musicData != null) {
			if (savedData.contains("savedMusicData")) {
			    musicData = ListUtil.getArrayListFromSharedJSON(savedData, "savedMusicData");
			     if (musicData.isEmpty()) {
				    {
					     HashMap<String, Object> _item = new HashMap<>();
					     _item.put("isEmpty", "yes");
					     musicData.add(_item);
				    }
			     }
			     songList.setAdapter(new SongListAdapter(musicData));
			} else {
				 ApplicationUtil.toast(getApplicationContext(), "Library data failed to load.", Toast.LENGTH_LONG);
			     {
			     	HashMap<String, Object> _item = new HashMap<>();
			     	_item.put("isEmpty", "yes");
				    musicData.add(_item);
			     }
			     songList.setAdapter(new SongListAdapter(musicData));
		    }
		}
		if (profileData != null) {
			if (savedData.contains("savedProfileData")) {
				profileData = ListUtil.getHashMapFromSharedJSON(savedData, "savedProfileData");
		    } else {
				ApplicationUtil.toast(getApplicationContext(), "Profile settings failed to load.", Toast.LENGTH_LONG);
		    }
		}
		miniplayer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				miniplayer.setBackground(new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null));
				tabNavigation.getTabAt(1).select();
			}
		});
		tabNavigation.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				ObjectAnimator fadeAnim = new ObjectAnimator();
				if (Build.VERSION.SDK_INT >= 23) {
					tab.getIcon().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
				} else {
					tab.getIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
				}
				if (tab.getPosition() == 0) {
					if (fadeAnim.isRunning()) {
						fadeAnim.cancel();
					}
					profileData.put("profileNavigationIndex", "0");
					savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).apply();
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
					profileData.put("profileNavigationIndex", "1");
					savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).apply();
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
					tab.getIcon().setColorFilter(getResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
				}
			}
			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});
		if (profileData.containsKey("profileNavigationIndex")) {
			if (profileData.get("profileNavigationIndex").equals("0")) {
				tabNavigation.getTabAt(0).select();
				listRefresh.setVisibility(View.VISIBLE);
				miniplayer.setVisibility(View.VISIBLE);
				player.setVisibility(View.GONE);
				miniplayerSeekbar.setVisibility(View.VISIBLE);
			} else if (profileData.get("profileNavigationIndex").equals("1")) {
				tabNavigation.getTabAt(1).select();
				listRefresh.setVisibility(View.GONE);
				player.setVisibility(View.VISIBLE);
				miniplayer.setVisibility(View.GONE);
				miniplayerSeekbar.setVisibility(View.GONE);
			}
		} else {
			profileData.put("profileNavigationIndex", "0");
			savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).apply();
			tabNavigation.getTabAt(0).select();
			listRefresh.setVisibility(View.VISIBLE);
			player.setVisibility(View.GONE);
			miniplayer.setVisibility(View.VISIBLE);
			miniplayerSeekbar.setVisibility(View.VISIBLE);
		}
		menu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
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
				LinearLayout live_streaming = dialogLayout.findViewById(R.id.live_streaming);
				LinearLayout lyrics = dialogLayout.findViewById(R.id.lyrics);
				LinearLayout settings = dialogLayout.findViewById(R.id.settings);
				LinearLayout visualizer = dialogLayout.findViewById(R.id.visualizer);
				title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
				if (savedData.contains("savedProfileData")) {
						if (profileData.containsKey("profileName")) {
								profile_name.setText(profileData.get("profileName").toString());
						}
				}
				profile.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
								android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
						        view.setBackground(rippleButton);
								BottomSheetDialog renameProfile = new BottomSheetDialog(LocalStreamActivity.this);
						        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_create_a_profile, null);
						        renameProfile.setContentView(dialogLayout);
						        LinearLayout main = dialogLayout.findViewById(R.id.main);
						        TextView title = dialogLayout.findViewById(R.id.title);
						        ImageView profile_icon = dialogLayout.findViewById(R.id.profile_icon);
						        EditText profile_name = dialogLayout.findViewById(R.id.profile_name);
						        Button create = dialogLayout.findViewById(R.id.create);
								if (savedData.contains("savedProfileData")) {
								        if (profileData.containsKey("profileName")) {
											    title.setText("Rename profile");
											    create.setText("Finish");
										        profile_name.setText(profileData.get("profileName").toString());
										}
							        }
						        title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
						        profile_icon.setOnClickListener(new View.OnClickListener() {
								        @Override
								        public void onClick(View view) {
										        ApplicationUtil.toast(getApplicationContext(), "Profile picture under construction.", Toast.LENGTH_SHORT);
									        }
							        });
						        create.setOnClickListener(new View.OnClickListener() {
								        @Override
								        public void onClick(View view) {
										        if (profile_name.getText().toString().length() > 0) {
														if (profile_name.getText().toString().equals(profileData.get("profileName").toString())) {
															    renameProfile.dismiss();
														} else {
													            String profileName = profile_name.getText().toString();
													            profileData.put("profileName", profileName);
													            savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).apply();
														        ApplicationUtil.toast(getApplicationContext(), "Renamed profile sucessfully.", Toast.LENGTH_SHORT);
														        tabNavigation.getTabAt(0).select();
													            renameProfile.dismiss();
														        menuDialog.dismiss();
														        startActivity(new Intent(getApplicationContext(), LauncherActivity.class));
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
						        android.graphics.drawable.GradientDrawable roundedCorners = new android.graphics.drawable.GradientDrawable();
						        roundedCorners.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
						        roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
						        roundedCorners.setColor(Color.parseColor("#FFFFFF"));
						        ((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
								android.graphics.drawable.GradientDrawable roundedCorners2 = new android.graphics.drawable.GradientDrawable();
						        roundedCorners2.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
						        roundedCorners2.setCornerRadius(20);
						        roundedCorners2.setColor(Color.parseColor("#EEEEEE"));
						        profile_name.setBackground(roundedCorners2);
						        android.graphics.drawable.GradientDrawable gradientButton = new android.graphics.drawable.GradientDrawable();
						        gradientButton.setColor(Color.parseColor("#03A9F4"));
						        gradientButton.setCornerRadius(20);
						        create.setBackground(gradientButton);
						        renameProfile.show();
						}
				});
				live_streaming.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
								android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
						        view.setBackground(rippleButton);
								ApplicationUtil.toast(getApplicationContext(), "Feature under construction.", Toast.LENGTH_SHORT);
						}
				});
				lyrics.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
								android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
						        view.setBackground(rippleButton);
								BottomSheetDialog lyricsDialog = new BottomSheetDialog(LocalStreamActivity.this);
						        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_lyrics, null);
						        lyricsDialog.setContentView(dialogLayout);
						        LinearLayout main = dialogLayout.findViewById(R.id.main);
						        ImageView back = dialogLayout.findViewById(R.id.back);
								ImageView lyrics_edit = dialogLayout.findViewById(R.id.lyrics_edit);
						        TextView title = dialogLayout.findViewById(R.id.title);
						        TextView lyrics = dialogLayout.findViewById(R.id.lyrics);
						        title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
								if (musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).containsKey("songLyrics")) {
										if (musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).get("songLyrics").toString().length() == 0) {
												// Lyrics added with 0 letters
										} else {
											    lyrics.setText(musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).get("songLyrics").toString());
										}
								} else {
									    // No Lyrics was found.
								}
								back.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
										view.setBackground(rippleButton);
										lyricsDialog.dismiss();
									}
								});
								lyrics_edit.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View view) {
												android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
								                view.setBackground(rippleButton);
												Intent intent = new Intent(getApplicationContext(), LyricsEditorActivity.class);
												intent.putExtra("songPosition", String.valueOf((int)(Integer.parseInt(profileData.get("profileSongPosition").toString()))));
												startActivity(intent);
												lyricsDialog.dismiss();
												menuDialog.dismiss();
										}
								});
						        Double TopLeft = 20.0;
						        Double TopRight = 20.0;
						        Double BottomRight = 0.0;
						        Double BottomLeft = 0.0;
						        android.graphics.drawable.GradientDrawable roundedCorners = new android.graphics.drawable.GradientDrawable();
						        roundedCorners.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
						        roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
						        roundedCorners.setColor(Color.parseColor("#FFFFFF"));
						        ((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
						        lyricsDialog.show();
						}
				});
				settings.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
								android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
						        view.setBackground(rippleButton);
								BottomSheetDialog settingsDialog = new BottomSheetDialog(LocalStreamActivity.this);
								View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_settings, null);
								settingsDialog.setContentView(dialogLayout);
								LinearLayout main = dialogLayout.findViewById(R.id.main);
								ImageView back = dialogLayout.findViewById(R.id.back);
								TextView title = dialogLayout.findViewById(R.id.title);
								TextView general_title = dialogLayout.findViewById(R.id.general_title);
							    CheckBox dark_mode = dialogLayout.findViewById(R.id.dark_mode);
							    CheckBox disable_ads = dialogLayout.findViewById(R.id.disable_ads);
							    title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
							    general_title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
							    back.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
										view.setBackground(rippleButton);
										settingsDialog.dismiss();
									}
								});
							    dark_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
									@Override
									public void onCheckedChanged(CompoundButton view, boolean isChecked) {
										if (isChecked) {
											profileData.put("profileDarkMode", "enabled");
											ApplicationUtil.toast(getApplicationContext(), "Dark mode enabled.", Toast.LENGTH_SHORT);
										} else {
											profileData.put("profileDarkMode", "disabled");
											ApplicationUtil.toast(getApplicationContext(), "Dark mode disabled.", Toast.LENGTH_SHORT);
										}
									}
								});
							    disable_ads.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
									@Override
									public void onCheckedChanged(CompoundButton view, boolean isChecked) {
										if (isChecked) {
											profileData.put("profileAds", "enabled");
											ApplicationUtil.toast(getApplicationContext(), "Ads enabled.", Toast.LENGTH_SHORT);
										} else {
											profileData.put("profileAds", "disabled");
											ApplicationUtil.toast(getApplicationContext(), "Ads disabled.", Toast.LENGTH_SHORT);
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
							    roundedCorners.setColor(Color.parseColor("#000000"));
							    ((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
							    settingsDialog.show();
						}
				});
				visualizer.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
								android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
						        view.setBackground(rippleButton);
								BottomSheetDialog visualizerDialog = new BottomSheetDialog(LocalStreamActivity.this);
								View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_visualizer, null);
								visualizerDialog.setContentView(dialogLayout);
								ImageView back = dialogLayout.findViewById(R.id.back);
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
								if (playbackSrv.mp != null) {
										if (playbackSrv.mp.getAudioSessionId() != -1) {
												visualizer.setAudioSessionId(playbackSrv.mp.getAudioSessionId());
										}
										if (!playbackSrv.isPlaying()) {
												ApplicationUtil.toast(getApplicationContext(), "Visualizer not visible, please resume/play the song.", Toast.LENGTH_LONG);
										}
								}
								Double TopLeft = 20.0;
						        Double TopRight = 20.0;
						        Double BottomRight = 0.0;
						        Double BottomLeft = 0.0;
						        android.graphics.drawable.GradientDrawable roundedCorners = new android.graphics.drawable.GradientDrawable();
						        roundedCorners.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
						        roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
						        roundedCorners.setColor(Color.parseColor("#000000"));
						        ((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
								visualizerDialog.setCancelable(false);
								visualizerDialog.show();
						}
				});
				about.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
								android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
						        view.setBackground(rippleButton);
								BottomSheetDialog about = new BottomSheetDialog(LocalStreamActivity.this);
						        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_about, null);
						        about.setContentView(dialogLayout);
						        LinearLayout main = dialogLayout.findViewById(R.id.main);
						        TextView title = dialogLayout.findViewById(R.id.title);
						        TextView author = dialogLayout.findViewById(R.id.author);
								LinearLayout youtube = dialogLayout.findViewById(R.id.youtube);
								LinearLayout twitter = dialogLayout.findViewById(R.id.twitter);
						        LinearLayout github = dialogLayout.findViewById(R.id.github);
								LinearLayout discord = dialogLayout.findViewById(R.id.discord);
								LinearLayout licenses = dialogLayout.findViewById(R.id.licenses);
								LinearLayout privacy = dialogLayout.findViewById(R.id.privacy);
						        title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/leixo.ttf"), Typeface.BOLD);
						        int randomizer = com.gianxd.audiodev.util.MusicDevUtil.getRandom((int)(0), (int)(9));
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
												android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
								                view.setBackground(rippleButton);
												Intent intent = new Intent();
												intent.putExtra("url", "https://youtube.com/channel/UCndTdCP5Qr-ekaV2Im1VCgg");
												intent.setClass(getApplicationContext(), ExternalBrowserActivity.class);
												startActivity(intent);
										}
								});
								twitter.setOnClickListener(new View.OnClickListener() {
										@Override 
										public void onClick(View view) {
												android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
								                view.setBackground(rippleButton);
												Intent intent = new Intent();
												intent.putExtra("url", "https://twitter.com/gianxddddd");
												intent.setClass(getApplicationContext(), ExternalBrowserActivity.class);
												startActivity(intent);
										}
								});
								github.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View view) {
												android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
								                view.setBackground(rippleButton);
												Intent intent = new Intent();
												intent.putExtra("url", "https://github.com/gianxddddd/ArchoMusic");
								                intent.setClass(getApplicationContext(), ExternalBrowserActivity.class);
								                startActivity(intent);
										}
								});
								discord.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View view) {
												android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
								                view.setBackground(rippleButton);
												Intent intent = new Intent();
												intent.putExtra("url", "https://discord.gg/x5t9n9fWCV");
												intent.setClass(getApplicationContext(), ExternalBrowserActivity.class);
												startActivity(intent);
										}
								});
								licenses.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View view) {
												android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
								                view.setBackground(rippleButton);
												Intent intent = new Intent();
												intent.putExtra("url", "file:///android_asset/LICENSES.html");
												intent.setClass(getApplicationContext(), ExternalBrowserActivity.class);
												startActivity(intent);
										}
								});
								privacy.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View view) {
												android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
								                view.setBackground(rippleButton);
												Intent intent = new Intent();
												intent.putExtra("url", "file:///android_asset/PRIVACY.html");
												intent.setClass(getApplicationContext(), ExternalBrowserActivity.class);
												startActivity(intent);
										}
								});
						        Double TopLeft = 20.0;
						        Double TopRight = 20.0;
						        Double BottomRight = 0.0;
						        Double BottomLeft = 0.0;
						        android.graphics.drawable.GradientDrawable roundedCorners = new android.graphics.drawable.GradientDrawable();
						        roundedCorners.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
						        roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
						        roundedCorners.setColor(Color.parseColor("#FFFFFF"));
						        ((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
						        about.show();
						}
				});
				Double TopLeft = 20.0;
				Double TopRight = 20.0;
				Double BottomRight = 0.0;
				Double BottomLeft = 0.0;
				android.graphics.drawable.GradientDrawable roundedCorners = new android.graphics.drawable.GradientDrawable();
				roundedCorners.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
				roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
				roundedCorners.setColor(Color.parseColor("#FFFFFF"));
				((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
				menuDialog.show();
			}
		});
		
		listRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override 
			public void onRefresh() {
				if (savedData.contains("savedMusicData")) {
					musicData.clear();
					musicData = ListUtil.getArrayListFromSharedJSON(savedData, "savedMusicData");
					if (musicData.isEmpty()) {
						{
							HashMap<String, Object> _item = new HashMap<>();
							_item.put("isEmpty", "yes");
							musicData.add(_item);
						}
						
					}
					songList.setAdapter(new SongListAdapter(musicData));
					if (savedData.contains("savedSongPosition")) {
						songList.scrollToPosition(Integer.parseInt(profileData.get("profileSongPosition").toString()));
					}
					listRefresh.setRefreshing(false);
				}
				else {
					ApplicationUtil.toast(getApplicationContext(), "Library data failed to load.", Toast.LENGTH_LONG);
					{
						HashMap<String, Object> _item = new HashMap<>();
						_item.put("isEmpty", "yes");
						musicData.add(_item);
					}
					listRefresh.setRefreshing(false);
				}
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
					musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).put("songCurrentDuration", String.valueOf((int)(seekbarDuration.getProgress())));
					savedData.edit().putString("savedMusicData", ListUtil.setArrayListToSharedJSON(musicData)).apply();
				}
			}
		});
		
		skipBackward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (playbackSrv != null) {
					try {
						profileData.put("profileSongPosition", String.valueOf((Integer.parseInt(profileData.get("profileSongPosition").toString()) - 1)));
						savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).apply();
						if (Integer.parseInt(profileData.get("lastSongItemPosition").toString()) < musicData.size()) {
							playbackSrv.createLocalStream(Integer.parseInt(profileData.get("profileSongPosition").toString()));
							playPause.performClick();
						}
					} catch (Exception e) {
						profileData.put("profileSongPosition", String.valueOf((Integer.parseInt(profileData.get("profileSongPosition").toString()) - 1)));
						savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).apply();
						if (Integer.parseInt(profileData.get("profileSongPosition").toString()) < musicData.size()) {
							playbackSrv.createLocalStream(Integer.parseInt(profileData.get("profileSongPosition").toString()));
							playPause.performClick();
						}
					}
				}
			}
		});
		
		playPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (playbackSrv.mp != null) {
					if (!playbackSrv.isPlaying()) {
						playbackSrv.play();
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
											musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).put("songCurrentDuration", String.valueOf(playbackSrv.getCurrentPosition()));
											savedData.edit().putString("savedMusicData", ListUtil.setArrayListToSharedJSON(musicData)).apply();
										} catch (Exception e) {
											// do nothing 
										}
									}
								});
							}
						};
						timer.scheduleAtFixedRate(timerTask, (int)(0), (int)(1000));
					} else {
						playbackSrv.pause();
						playPause.setImageResource(R.drawable.ic_media_play);
						miniplayerPlayPause.setImageResource(R.drawable.ic_media_play);
						if (timerTask != null) {
							timerTask.cancel();
						}
					}
				}
			}
		});
		
		skipForward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (playbackSrv.mp != null) {
					try {
						profileData.put("profileSongPosition", String.valueOf((Integer.parseInt(profileData.get("profileSongPosition").toString()) + 1)));
						savedData.edit().putString("profileSongPosition", ListUtil.setHashMapToSharedJSON(profileData)).apply();
						if (Integer.parseInt(profileData.get("profileSongPosition").toString()) < musicData.size()) {
							playbackSrv.createLocalStream(Integer.parseInt(profileData.get("profileSongPosition").toString()));
							playPause.performClick();
						}
					} catch (Exception e) {
						profileData.put("profileSongPosition", String.valueOf((Integer.parseInt(profileData.get("profileSongPosition").toString()) + 1)));
						savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).apply();
						if (Integer.parseInt(profileData.get("profileSongPosition").toString()) < musicData.size()) {
							playbackSrv.createLocalStream(Integer.parseInt(profileData.get("profileSongPosition").toString()));
							playPause.performClick();
						}
					}
				}
			}
		});
		
		miniplayerSkipPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				skipBackward.performClick();
			}
		});
		
		miniplayerPlayPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				playPause.performClick();
			}
		});
		
		miniplayerSkipNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				skipForward.performClick();
			}
		});
	}
	
	private void initializeLogic() {
		startupUI();
		if (profileData.containsKey("profileErrorTrace")) {
			Snackbar.make(miniplayer, "An error occurred.", Snackbar.LENGTH_SHORT).setAction("Show", new View.OnClickListener(){
				@Override
				public void onClick(View view) {
					BottomSheetDialog errorDialog = new BottomSheetDialog(LocalStreamActivity.this);
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
							android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#03A9F4")), null);
							view.setBackground(rippleButton);
							HashMap<String, Object> profileData = ListUtil.getHashMapFromSharedJSON(savedData, "savedProfileData");
							profileData.remove("profileErrorTrace");
							savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).apply();
							errorDialog.dismiss();
						}
					});
					Double TopLeft = 20.0;
					Double TopRight = 20.0;
					Double BottomRight = 0.0;
					Double BottomLeft = 0.0;
					android.graphics.drawable.GradientDrawable roundedCorners = new android.graphics.drawable.GradientDrawable();
					roundedCorners.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
					roundedCorners.setCornerRadii(new float[] {TopLeft.floatValue(),TopLeft.floatValue(), TopRight.floatValue(),TopRight.floatValue(), BottomRight.floatValue(),BottomRight.floatValue(), BottomLeft.floatValue(),BottomLeft.floatValue()});
					roundedCorners.setColor(Color.parseColor("#FFFFFF"));
					((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
					android.graphics.drawable.GradientDrawable roundedCorners2 = new android.graphics.drawable.GradientDrawable();
					roundedCorners2.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
					roundedCorners2.setCornerRadius(20);
					roundedCorners2.setColor(Color.parseColor("#EEEEEE"));
					log.setBackground(roundedCorners2);
					android.graphics.drawable.GradientDrawable gradientButton = new android.graphics.drawable.GradientDrawable();
					gradientButton.setColor(Color.parseColor("#03A9F4"));
					gradientButton.setCornerRadius(20);
					close.setBackground(gradientButton);
					errorDialog.show();
				}
			}).show();
		}
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
						Glide.with(getApplicationContext()).asBitmap().load(ImageUtil.getAlbumArt(musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).get("songData").toString(), getResources())).into(albumArt);
						Glide.with(getApplicationContext()).asBitmap().load(ImageUtil.getAlbumArt(musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).get("songData").toString(), getResources())).into(miniplayerAlbumArt);
						songTitle.setText(musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).get("songTitle").toString());
						songArtist.setText(musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).get("songArtist").toString());
						miniplayerSongTitle.setText(musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).get("songTitle").toString());
						miniplayerSongArtist.setText(musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).get("songArtist").toString());
						miniplayerSeekbar.setMax(playbackSrv.getMaxDuration());
						miniplayerSeekbar.setProgress(playbackSrv.getCurrentPosition());
						maxDuration.setText(String.valueOf((int) ((playbackSrv.getMaxDuration() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((playbackSrv.getMaxDuration() / 1000) % 60))));
						currentDuration.setText(String.valueOf((int)((playbackSrv.getCurrentPosition() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((playbackSrv.getCurrentPosition() / 1000) % 60))));
						seekbarDuration.setMax(playbackSrv.getMaxDuration());
						seekbarDuration.setProgress(playbackSrv.getCurrentPosition());
					} else {
						if (profileData.containsKey("profileSongPosition")) {
							if (!profileData.get("profileSongPosition").equals("0")) {
								playbackSrv.createLocalStream(Integer.parseInt(profileData.get("profileSongPosition").toString()));
								if (musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).containsKey("songCurrentDuration")) {
									if (!musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).get("songCurrentDuration").equals(playbackSrv.getCurrentPosition())) {
										playbackSrv.seek(Integer.parseInt(musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).get("songCurrentDuration").toString()));
										miniplayerSeekbar.setProgress(Integer.parseInt(musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).get("songCurrentDuration").toString()));
										currentDuration.setText(String.valueOf(((Integer.parseInt(musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).get("songCurrentDuration").toString()) / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((Integer.parseInt(musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).get("songCurrentDuration").toString()) / 1000) % 60))));
										seekbarDuration.setProgress(Integer.parseInt(musicData.get(Integer.parseInt(profileData.get("profileSongPosition").toString())).get("songCurrentDuration").toString()));
								    }
								}
							}
						} else {
							if (!musicData.isEmpty()) {
								if (0 < musicData.size()) {
									playbackSrv.createLocalStream(0);
									profileData.put("profileSongPosition", "0");
									savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).apply();
								}
							}
						}
					}
				} catch (Exception e) {
					ApplicationUtil.toast(getApplicationContext(), e.toString(), Toast.LENGTH_LONG);
					if (!musicData.isEmpty()) {
						if (0 < musicData.size()) {
							playbackSrv.createLocalStream(0);
							profileData.put("profileSongPosition", "0");
							savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).apply();
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
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
		switch (_requestCode) {
			default:
			break;
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
		if (savedData.contains("savedMusicData")) {
			musicData.clear();
			musicData = ListUtil.getArrayListFromSharedJSON(savedData, "savedMusicData");
			if (musicData.isEmpty()) {
				{
					HashMap<String, Object> _item = new HashMap<>();
					_item.put("isEmpty", "yes");
					musicData.add(_item);
				}
				
			}
			songList.setAdapter(new SongListAdapter(musicData));
			if (profileData.containsKey("lastSongItemPosition")) {
				songList.scrollToPosition(Integer.parseInt(profileData.get("profileSongPosition").toString()));
			}
		} else {
			ApplicationUtil.toast(getApplicationContext(), "Library data failed to load.", Toast.LENGTH_LONG);
			{
				HashMap<String, Object> _item = new HashMap<>();
				_item.put("isEmpty", "yes");
				musicData.add(_item);
			}
			songList.setAdapter(new SongListAdapter(musicData));
		}
		if (savedData.contains("savedProfileData")) {
			profileData = ListUtil.getHashMapFromSharedJSON(savedData, "savedProfileData");
		} else {
			ApplicationUtil.toast(getApplicationContext(), "Profile settings failed to load.", Toast.LENGTH_LONG);
		}
		if (profileData.containsKey("profileNavigationIndex")) {
			if (profileData.get("profileNavigationIndex").equals("0")) {
				tabNavigation.getTabAt(0).select();
				listRefresh.setVisibility(View.VISIBLE);
				miniplayer.setVisibility(View.VISIBLE);
				player.setVisibility(View.GONE);
				miniplayerSeekbar.setVisibility(View.VISIBLE);
			} else {
				if (profileData.get("profileNavigationIndex").equals("1")) {
					tabNavigation.getTabAt(1).select();
					listRefresh.setVisibility(View.GONE);
					player.setVisibility(View.VISIBLE);
					miniplayer.setVisibility(View.GONE);
					miniplayerSeekbar.setVisibility(View.GONE);
				}
			}
		} else {
			profileData.put("profileNavigationIndex", "0");
			savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).apply();
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
		logoName.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/leixo.ttf"), Typeface.BOLD);
		songTitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
		miniplayerSongTitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
		skipBackward.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)); 
		playPause.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)); 
		skipForward.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)); 
		miniplayerSkipPrev.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)); 
		miniplayerPlayPause.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)); 
		miniplayerSkipNext.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
		listRefresh.setColorSchemeColors(Color.parseColor("#03A9F4"), Color.parseColor("#03A9F4"), Color.parseColor("#03A9F4"));
		songList.setLayoutManager(new LinearLayoutManager(this));
		if (Build.VERSION.SDK_INT >= 23) {
			top.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
			getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
			getWindow().setNavigationBarColor(Color.parseColor("#FFFFFF"));
		}
		else {
			getWindow().setStatusBarColor(Color.parseColor("#000000"));
			getWindow().setNavigationBarColor(Color.parseColor("#000000"));
		}
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
			TextView emptyMsg = (TextView) view.findViewById(R.id.emptyMsg);
			ImageView more = (ImageView) view.findViewById(R.id.more);
			ImageView albumArt = (ImageView) view.findViewById(R.id.albumArt);
			TextView songTitle = (TextView) view.findViewById(R.id.songTitle);
			TextView songArtist = (TextView) view.findViewById(R.id.songArtist);
			RecyclerView.LayoutParams recyclerLayoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			view.setLayoutParams(recyclerLayoutParams);
			if (!data.get((int)position).containsKey("isEmpty")) {
				Glide.with(getApplicationContext()).asBitmap().load(ImageUtil.getAlbumArt(StringUtil.decodeString(data.get(position).get("songData").toString()), getResources())).into(albumArt);
				songTitle.setText(data.get((int)position).get("songTitle").toString());
				songArtist.setText(data.get((int)position).get("songArtist").toString());
				main.setVisibility(View.VISIBLE);
				emptyMsg.setVisibility(View.GONE);
				main.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
						main.setBackground(rippleButton);
						if (!(position == Integer.parseInt(profileData.get("lastSongItemPosition").toString()))) {
							if (new java.io.File(StringUtil.decodeString(musicData.get(position).get("songData").toString())).exists()) {
								try {
									playbackSrv.createLocalStream(position);
									profileData.put("profileSongPosition", String.valueOf(position));
									savedData.edit().putString("savedProfileData", ListUtil.setHashMapToSharedJSON(profileData)).apply();
									playPause.performClick();
								} catch (Exception e) {
									ApplicationUtil.toast(getApplicationContext(), "Error loading audio file.", Toast.LENGTH_SHORT);
									skipForward.performClick();
								}
							} else {
								ApplicationUtil.toast(getApplicationContext(), "Selected song does not exist.", Toast.LENGTH_SHORT);
							}
						} else {
							ApplicationUtil.toast(getApplicationContext(), "Selected song is currently playing.", Toast.LENGTH_SHORT);
						}
					}
				});
				more.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
						more.setBackground(rippleButton);
						ApplicationUtil.toast(getApplicationContext(), "Song options under construction.", Toast.LENGTH_SHORT);
					}
				});
			}
			else {
				if (data.get((int)position).get("isEmpty").toString().equals("yes")) {
					main.setVisibility(View.GONE);
					emptyMsg.setVisibility(View.VISIBLE);
				}
			}
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
