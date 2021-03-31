package com.gianxd.audiodev.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.RippleDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.gianxd.audiodev.R;
import com.gianxd.audiodev.service.PlaybackService;
import com.gianxd.audiodev.service.PlaybackService.MusicBinder;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class LocalStreamActivity extends  AppCompatActivity  {
	
	private Timer _timer = new Timer();
	private ArrayList<HashMap<String, Object>> musicData = new ArrayList<>();

	private ServiceConnection musicConnection;
	private PlaybackService playbackSrv;
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
	private TimerTask timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_local_streaming);
		initialize(savedInstanceState);
		com.google.firebase.FirebaseApp.initializeApp(this);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
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
		savedData = getSharedPreferences("savedData", Activity.MODE_PRIVATE);
		miniplayer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				miniplayer.setBackground(new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null));
				tabNavigation.getTabAt(1).select();
			}
		});
		tabNavigation.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				if (Build.VERSION.SDK_INT >= 23) {
					tab.getIcon().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
				}
				else {
					tab.getIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
				}
				ObjectAnimator fadeAnim = new ObjectAnimator();
				if (tab.getPosition() == 0) {
					if (fadeAnim.isRunning()) {
						fadeAnim.cancel();
					}
					savedData.edit().putString("savedNavigationID", "0").apply();
					player.setVisibility(View.VISIBLE);
					listRefresh.setVisibility(View.GONE);
					miniplayer.setVisibility(View.GONE);
					miniplayerSeekbar.setVisibility(View.GONE);
					fadeAnim.setTarget(player);
					fadeAnim.setPropertyName("alpha");
					fadeAnim.setFloatValues((float)(1.0d), (float)(0.0d));
					fadeAnim.start();
					timer = new TimerTask() {
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
					_timer.schedule(timer, (int)(250));
				}
				else {
					if (tab.getPosition() == 1) {
						if (fadeAnim.isRunning()) {
							fadeAnim.cancel();
						}
						savedData.edit().putString("savedNavigationID", "1").apply();
						player.setVisibility(View.GONE);
						listRefresh.setVisibility(View.VISIBLE);
						miniplayer.setVisibility(View.VISIBLE);
						miniplayerSeekbar.setVisibility(View.VISIBLE);
						fadeAnim.setTarget(listRefresh);
						fadeAnim.setPropertyName("alpha");
						fadeAnim.setFloatValues((float)(1.0d), (float)(0.0d));
						fadeAnim.start();
						timer = new TimerTask() {
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
						_timer.schedule(timer, (int)(250));
					}
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
		menu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
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
				LinearLayout liveStreaming = dialogLayout.findViewById(R.id.liveStreaming);
				LinearLayout lyrics = dialogLayout.findViewById(R.id.lyrics);
				LinearLayout settings = dialogLayout.findViewById(R.id.settings);
				LinearLayout visualizer = dialogLayout.findViewById(R.id.visualizer);
				title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
				if (savedData.contains("savedProfileData")) {
						HashMap<String, Object> profileData = new Gson().fromJson(savedData.getString("savedProfileData", ""), new TypeToken<HashMap<String, Object>>(){}.getType());
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
								        HashMap<String, Object> profileData = new Gson().fromJson(savedData.getString("savedProfileData", ""), new TypeToken<HashMap<String, Object>>(){}.getType());
								        if (profileData.containsKey("profileName")) {
										        profile_name.setText(profileData.get("profileName").toString());
								            } else {
											    title.setText("Rename profile");
										        create.setText("Finish");
										}
							        }
						        title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
						        profile_icon.setOnClickListener(new View.OnClickListener() {
								        @Override
								        public void onClick(View view) {
										        com.gianxd.musicdev.MusicDevUtil.showMessage(getApplicationContext(), "Profile picture under construction.");
									        }
							        });
						        create.setOnClickListener(new View.OnClickListener() {
								        @Override
								        public void onClick(View view) {
									            HashMap<String, Object> profileData = new Gson().fromJson(savedData.getString("savedProfileData", ""), new TypeToken<HashMap<String, Object>>(){}.getType());
										        if (profile_name.getText().toString().length() > 0) {
														if (profile_name.getText().toString().equals(profileData.get("profileName").toString())) {
															    renameProfile.dismiss();
														} else {
														        HashMap<String, Object> tempProfileData = new HashMap<>();
													            String profileName = profile_name.getText().toString();
													            tempProfileData.put("profileName", profileName);
													            savedData.edit().putString("savedProfileData", new Gson().toJson(tempProfileData)).apply();
														        com.gianxd.musicdev.MusicDevUtil.showMessage(getApplicationContext(), "Renamed profile sucessfully.");
														        tabNavigation.getTabAt(0).select();
													            renameProfile.dismiss();
														        menuDialog.dismiss();
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
				liveStreaming.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
								android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
						        view.setBackground(rippleButton);
								com.gianxd.musicdev.MusicDevUtil.showMessage(getApplicationContext(), "Feature under construction.");
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
								ImageView lyrics_edit = dialogLayout.findViewById(R.id.lyrics_edit);
						        TextView title = dialogLayout.findViewById(R.id.title);
						        TextView lyrics = dialogLayout.findViewById(R.id.lyrics);
						        title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
								if (musicData.get((int)Double.parseDouble(savedData.getString("savedSongPosition", "0"))).containsKey("songLyrics")) {
										if (musicData.get((int)Double.parseDouble(savedData.getString("savedSongPosition", "0"))).get("songLyrics").toString().length() == 0) {
												// lyrics is added but empty cheems.
										} else {
											    lyrics.setText(musicData.get((int)Double.parseDouble(savedData.getString("savedSongPosition", "0"))).get("songLyrics").toString());
										}
								} else {
									    // no lyrics found cheems.
								}
								lyrics_edit.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View view) {
												android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
								                view.setBackground(rippleButton);
												Intent intent = new Intent(getApplicationContext(), LyricsEditorActivity.class);
												intent.putExtra("songPosition", String.valueOf((long)(Double.parseDouble(savedData.getString("savedSongPosition", "0")))));
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
								com.gianxd.musicdev.MusicDevUtil.showMessage(getApplicationContext(), "Feature under construction.");
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
												com.gianxd.musicdev.MusicDevUtil.showMessage(getApplicationContext(), "Visualizer not visible, please resume/play the song.");
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
								LinearLayout youtube = dialogLayout.findViewById(R.id.youtube);
								LinearLayout twitter = dialogLayout.findViewById(R.id.twitter);
						        LinearLayout github = dialogLayout.findViewById(R.id.github);
								LinearLayout discord = dialogLayout.findViewById(R.id.discord);
								LinearLayout licenses = dialogLayout.findViewById(R.id.licenses);
								LinearLayout privacy = dialogLayout.findViewById(R.id.privacy);
						        title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/leixo.ttf"), Typeface.BOLD);
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
					musicData = new Gson().fromJson(savedData.getString("savedMusicData", ""), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
					if (musicData.isEmpty()) {
						{
							HashMap<String, Object> _item = new HashMap<>();
							_item.put("isEmpty", "yes");
							musicData.add(_item);
						}
						
					}
					songList.setAdapter(new SongListAdapter(musicData));
					if (savedData.contains("savedSongPosition")) {
						songList.smoothScrollToPosition((int)Double.parseDouble(savedData.getString("savedSongPosition", "")));
					}
					listRefresh.setRefreshing(false);
				}
				else {
					com.gianxd.musicdev.MusicDevUtil.showMessage(getApplicationContext(), "Library data failed to load. ");
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
			public void onProgressChanged (SeekBar _param1, int _param2, boolean _param3) {
				final int _progressValue = _param2;
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar _param1) {
				
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar _param2) {
				if (playbackSrv.mp != null) {
					playbackSrv.seek(seekbarDuration.getProgress());
					miniplayerSeekbar.setProgress((int)seekbarDuration.getProgress());
					currentDuration.setText(String.valueOf((long)((seekbarDuration.getProgress() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((seekbarDuration.getProgress() / 1000) % 60))));
					savedData.edit().putString("savedSongCurrentPosition", String.valueOf((long)(seekbarDuration.getProgress()))).apply();
				}
			}
		});
		
		skipBackward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (playbackSrv != null) {
					try {
						savedData.edit().putString("savedSongPosition", String.valueOf((long)(Double.parseDouble(savedData.getString("savedSongPosition", "")) - 1))).apply();
						if (Double.parseDouble(savedData.getString("savedSongPosition", "")) < musicData.size()) {
							playbackSrv.createLocalStream((int)Double.parseDouble(savedData.getString("savedSongPosition", "")));

							playPause.performClick();
						}
					} catch (Exception e) {
						savedData.edit().putString("savedSongPosition", String.valueOf((long)(Double.parseDouble(savedData.getString("savedSongPosition", "")) - 1))).apply();
						if (Double.parseDouble(savedData.getString("savedSongPosition", "")) < musicData.size()) {
							playbackSrv.createLocalStream((int)Double.parseDouble(savedData.getString("savedSongPosition", "")));
							playPause.performClick();
						}
					}
				}
			}
		});
		
		playPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (playbackSrv.mp != null) {
					if (!playbackSrv.isPlaying()) {
						playbackSrv.play();
						playPause.setImageResource(R.drawable.ic_media_pause);
						miniplayerPlayPause.setImageResource(R.drawable.ic_media_pause);
						timer = new TimerTask() {
							@Override
							public void run() {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										try {
											seekbarDuration.setProgress((int)playbackSrv.getCurrentPosition());
											miniplayerSeekbar.setProgress((int)playbackSrv.getCurrentPosition());
											currentDuration.setText(String.valueOf((long)((playbackSrv.getCurrentPosition() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((playbackSrv.getCurrentPosition() / 1000) % 60))));
											savedData.edit().putString("savedSongCurrentPosition", String.valueOf((long)(playbackSrv.getCurrentPosition()))).apply();
										} catch (Exception e) {
											// do nothing 
										}
									}
								});
							}
						};
						_timer.scheduleAtFixedRate(timer, (int)(0), (int)(1000));
					}
					else {
						playbackSrv.pause();
						playPause.setImageResource(R.drawable.ic_media_play);
						miniplayerPlayPause.setImageResource(R.drawable.ic_media_play);
						if (timer != null) {
							timer.cancel();
						}
					}
				}
			}
		});
		
		skipForward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (playbackSrv.mp != null) {
					try {
						savedData.edit().putString("savedSongPosition", String.valueOf((long)(Double.parseDouble(savedData.getString("savedSongPosition", "")) + 1))).apply();
						if (Double.parseDouble(savedData.getString("savedSongPosition", "")) < musicData.size()) {
							playbackSrv.createLocalStream((int)Double.parseDouble(savedData.getString("savedSongPosition", "")));
							playPause.performClick();
						}
					} catch (Exception e) {
						savedData.edit().putString("savedSongPosition", String.valueOf((long)(Double.parseDouble(savedData.getString("savedSongPosition", "")) + 1))).apply();
						if (Double.parseDouble(savedData.getString("savedSongPosition", "")) < musicData.size()) {
							playbackSrv.createLocalStream((int)Double.parseDouble(savedData.getString("savedSongPosition", "")));
							playPause.performClick();
						}
					}
				}
			}
		});
		
		miniplayerSkipPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				skipBackward.performClick();
			}
		});
		
		miniplayerPlayPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				playPause.performClick();
			}
		});
		
		miniplayerSkipNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				skipForward.performClick();
			}
		});
	}
	
	private void initializeLogic() {
		_startupUI();
		if (savedData.contains("savedMusicData")) {
			musicData.clear();
			musicData = new Gson().fromJson(savedData.getString("savedMusicData", ""), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			if (musicData.isEmpty()) {
				{
					HashMap<String, Object> _item = new HashMap<>();
					_item.put("isEmpty", "yes");
					musicData.add(_item);
				}
				
			}
			songList.setAdapter(new SongListAdapter(musicData));
		}
		else {
			com.gianxd.musicdev.MusicDevUtil.showMessage(getApplicationContext(), "Library data failed to load. ");
			{
				HashMap<String, Object> _item = new HashMap<>();
				_item.put("isEmpty", "yes");
				musicData.add(_item);
			}
			
			songList.setAdapter(new SongListAdapter(musicData));
		}
		HashMap<String, Object> profileData = new Gson().fromJson(savedData.getString("savedProfileData", ""), new TypeToken<HashMap<String, Object>>(){}.getType());
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
												HashMap<String, Object> profileData = new Gson().fromJson(savedData.getString("savedProfileData", ""), new TypeToken<HashMap<String, Object>>(){}.getType());
												profileData.remove("profileErrorTrace");
												savedData.edit().putString("savedProfileData", new Gson().toJson(profileData)).apply();
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
					} else {
						if (savedData.contains("savedSongPosition")) {
							if (!savedData.getString("savedSongPosition", "").equals("0")) {
								playbackSrv.createLocalStream((int)Double.parseDouble(savedData.getString("savedSongPosition", "0")));
								if (savedData.contains("savedSongCurrentPosition")) {
									playbackSrv.seek(((int)Double.parseDouble(savedData.getString("savedSongCurrentPosition", "0"))));
									miniplayerSeekbar.setMax((int)playbackSrv.getMaxDuration());
									miniplayerSeekbar.setProgress((int)playbackSrv.getCurrentPosition());
									maxDuration.setText(String.valueOf((long)((playbackSrv.getMaxDuration() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((playbackSrv.getMaxDuration() / 1000) % 60))));
									currentDuration.setText(String.valueOf((long)((playbackSrv.getCurrentPosition() / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((playbackSrv.getCurrentPosition() / 1000) % 60))));
									seekbarDuration.setMax((int)playbackSrv.getMaxDuration());
									seekbarDuration.setProgress((int)playbackSrv.getCurrentPosition());
								}
							}
						} else {
							if (!musicData.isEmpty()) {
								savedData.edit().putString("savedSongPosition", "0").apply();
								if (Double.parseDouble(savedData.getString("savedSongPosition", "")) < musicData.size()) {
									playbackSrv.createLocalStream((int)Double.parseDouble(savedData.getString("savedSongPosition", "")));

								}
							}
						}
					}
				} catch (Exception e) {
					if (!musicData.isEmpty()) {
						savedData.edit().putString("savedSongPosition", "0").apply();
						if (Double.parseDouble(savedData.getString("savedSongPosition", "")) < musicData.size()) {
							playbackSrv.createLocalStream((int)Double.parseDouble(savedData.getString("savedSongPosition", "")));

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
			    playIntent = new Intent(this, PlaybackService.class);
			    bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			    startService(playIntent);
		} else {
			    if (playbackSrv != null) {
						playIntent = new Intent(this, PlaybackService.class);
						unbindService(musicConnection);
						stopService(playIntent);
						// restart service
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
					playIntent = new Intent(this, PlaybackService.class);
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
			musicData = new Gson().fromJson(savedData.getString("savedMusicData", ""), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			if (musicData.isEmpty()) {
				{
					HashMap<String, Object> _item = new HashMap<>();
					_item.put("isEmpty", "yes");
					musicData.add(_item);
				}
				
			}
			songList.setAdapter(new SongListAdapter(musicData));
			if (savedData.contains("savedSongPosition")) {
				songList.smoothScrollToPosition((int)Double.parseDouble(savedData.getString("savedSongPosition", "")));
			}
		}
		else {
			com.gianxd.musicdev.MusicDevUtil.showMessage(getApplicationContext(), "Library data failed to load. ");
			{
				HashMap<String, Object> _item = new HashMap<>();
				_item.put("isEmpty", "yes");
				musicData.add(_item);
			}
			
			songList.setAdapter(new SongListAdapter(musicData));
		}
		if (savedData.contains("savedNavigationID")) {
			if (savedData.getString("savedNavigationID", "").equals("0")) {
				tabNavigation.getTabAt(0).select();
				listRefresh.setVisibility(View.VISIBLE);
				miniplayer.setVisibility(View.VISIBLE);
				player.setVisibility(View.GONE);
				miniplayerSeekbar.setVisibility(View.VISIBLE);
			}
			else {
				if (savedData.getString("savedNavigationID", "").equals("1")) {
					tabNavigation.getTabAt(1).select();
					listRefresh.setVisibility(View.GONE);
					player.setVisibility(View.VISIBLE);
					miniplayer.setVisibility(View.GONE);
					miniplayerSeekbar.setVisibility(View.GONE);
				}
			}
		}
		else {
			savedData.edit().putString("savedNavigationID", "0").apply();
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
				playIntent = new Intent(this, PlaybackService.class);
				stopService(playIntent);
			}
		}
	}
	public void _startupUI () {
		logoName.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/leixo.ttf"), Typeface.BOLD);
		songTitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
		miniplayerSongTitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
		skipBackward.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)); 
		playPause.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)); 
		skipForward.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)); 
		miniplayerSkipPrev.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)); 
		miniplayerPlayPause.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)); 
		miniplayerSkipNext.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)); 
		tabNavigation.addTab(tabNavigation.newTab().setIcon(R.drawable.ic_tabnav_library));
		tabNavigation.addTab(tabNavigation.newTab().setIcon(R.drawable.ic_tabnav_nowplaying));
		player.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View p1, MotionEvent p2){
						double f = 0;
						double t = 0;
						switch(p2.getAction()) {
							case MotionEvent.ACTION_DOWN:
								f = p2.getX();
								break;
							case MotionEvent.ACTION_UP:
								t = p2.getX();
								if (((f - t) < -250)) {
									tabNavigation.getTabAt(0).select();
								}
								if (((t - f) < -250)) {
									// do nothing
								}
								break;
						}
						return true;
						}
				});
		listRefresh.setColorSchemeColors(Color.parseColor("#03A9F4"), Color.parseColor("#03A9F4"), Color.parseColor("#03A9F4"));
		songList.setLayoutManager(new LinearLayoutManager(this));
		if (savedData.contains("savedNavigationID")) {
			if (savedData.getString("savedNavigationID", "").equals("0")) {
				tabNavigation.getTabAt(0).select();
				listRefresh.setVisibility(View.VISIBLE);
				miniplayer.setVisibility(View.VISIBLE);
				player.setVisibility(View.GONE);
				miniplayerSeekbar.setVisibility(View.VISIBLE);
			}
			else {
				if (savedData.getString("savedNavigationID", "").equals("1")) {
					tabNavigation.getTabAt(1).select();
					listRefresh.setVisibility(View.GONE);
					player.setVisibility(View.VISIBLE);
					miniplayer.setVisibility(View.GONE);
					miniplayerSeekbar.setVisibility(View.GONE);
				}
			}
		}
		else {
			savedData.edit().putString("savedNavigationID", "0").apply();
			tabNavigation.getTabAt(0).select();
			listRefresh.setVisibility(View.VISIBLE);
			player.setVisibility(View.GONE);
			miniplayer.setVisibility(View.VISIBLE);
			miniplayerSeekbar.setVisibility(View.VISIBLE);
		}
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
	
	
	public void _javaReferences () {
	}
	
	
	public void _xmlReferences () {
		
	}
	
	
	public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> {
		ArrayList<HashMap<String, Object>> data;
		public SongListAdapter(ArrayList<HashMap<String, Object>> _arr) {
			data = _arr;
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
				songTitle.setText(data.get((int)position).get("songTitle").toString());
				songArtist.setText(data.get((int)position).get("songArtist").toString());
				main.setVisibility(View.VISIBLE);
				emptyMsg.setVisibility(View.GONE);
				try {
					MediaMetadataRetriever artRetriever = new MediaMetadataRetriever();
					String decodedData = "";
					if (!data.get((int)position).get("songData").toString().startsWith("/")) {
							try {
									decodedData = new String(android.util.Base64.decode(musicData.get((int)position).get("songData").toString(), android.util.Base64.DEFAULT), "UTF-8");
									artRetriever.setDataSource(decodedData);
							} catch (Exception e) {
									artRetriever.setDataSource(data.get((int)position).get("songData").toString());
							}
					} else {
						    artRetriever.setDataSource(data.get((int)position).get("songData").toString());
					}
					byte[] album_art = artRetriever.getEmbeddedPicture(); 
					if( album_art != null ){ 
						Bitmap bitmapArt = BitmapFactory.decodeByteArray(album_art, 0, album_art.length); 
						Glide.with(getApplicationContext()).asBitmap().load(bitmapArt).into(albumArt);
					} else { 
						Glide.with(getApplicationContext()).asBitmap().load(R.drawable.ic_media_album_art).into(albumArt);
					}
				} catch (Exception e) {
					// applying image art ok die
					Glide.with(getApplicationContext()).asBitmap().load(R.drawable.ic_media_album_art).into(albumArt);
				}
				
				main.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View _view) {
						android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new android.graphics.drawable.ColorDrawable(Color.parseColor("#FFFFFF")), null);
						main.setBackground(rippleButton);
						if (!(position == Double.parseDouble(savedData.getString("savedSongPosition", "")))) {
							String decodedData = "";
							if (!data.get((int)position).get("songData").toString().startsWith("/")) {
									try {
											decodedData = new String(android.util.Base64.decode(musicData.get((int)position).get("songData").toString(), android.util.Base64.DEFAULT), "UTF-8");
									} catch (Exception e) {
											decodedData = data.get((int)position).get("songData").toString();
									}
							} else {
								    decodedData = data.get((int)position).get("songData").toString();
							}
							if (new java.io.File(decodedData).exists()) {
								try {
									playbackSrv.createLocalStream(position);
									playPause.performClick();
								} catch (Exception e) {
									com.gianxd.musicdev.MusicDevUtil.showMessage(getApplicationContext(), "Failed to play selected song. Skipping");
									skipForward.performClick();
								}
							}
							else {
								com.gianxd.musicdev.MusicDevUtil.showMessage(getApplicationContext(), "Selected song does not exist.");
							}
						}
						else {
							com.gianxd.musicdev.MusicDevUtil.showMessage(getApplicationContext(), "Selected song is currently playing.");
						}
					}
				});
				more.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View _view) {
						android.graphics.drawable.RippleDrawable rippleButton = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
						more.setBackground(rippleButton);
						com.gianxd.musicdev.MusicDevUtil.showMessage(getApplicationContext(), "Song options under construction.");
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
