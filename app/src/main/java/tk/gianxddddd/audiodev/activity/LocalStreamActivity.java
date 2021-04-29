package tk.gianxddddd.audiodev.activity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import tk.gianxddddd.audiodev.R;
import tk.gianxddddd.audiodev.service.LocalPlaybackService;
import tk.gianxddddd.audiodev.util.ApplicationUtil;
import tk.gianxddddd.audiodev.util.Base64Util;
import tk.gianxddddd.audiodev.util.FileUtil;
import tk.gianxddddd.audiodev.util.ImageUtil;
import tk.gianxddddd.audiodev.util.IntegerUtil;
import tk.gianxddddd.audiodev.util.ListUtil;

public class LocalStreamActivity extends  AppCompatActivity  {

    private ArrayList<HashMap<String, Object>> musicData;
    private HashMap<String, Object> profileData;
    private HashMap<String, Object> sessionData;
    private HashMap<String, Object> settingsData;

    private LocalPlaybackService playbackSrv;
    private Intent playIntent;
    private boolean isHandlerRunning = false;
    private boolean isMusicBounded = false;

    private LinearLayout top;
    private LinearLayout main;
    public static ProgressBar miniplayerSeekbar;
    private LinearLayout miniplayer;
    private TextView logoName;
    private TabLayout tabNavigation;
    private ImageView menu;
    private SwipeRefreshLayout listRefresh;
    private TextView listEmptyMsg;
    private LinearLayout player;
    private Runnable playerDurationUpdater;
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
    public static ImageView repeat;
    public static ImageView shuffle;
    public static ImageView miniplayerAlbumArt;
    public static TextView miniplayerSongTitle;
    public static TextView miniplayerSongArtist;

    private Timer timer;
    private TimerTask timerTask;
    private Handler runnableHandler;
    private DatabaseReference liveStreamDatabase;
    private LinearLayoutManager songListLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_streaming);
        initialize();
        com.google.firebase.FirebaseApp.initializeApp(this);
    }

    private void initialize() {
        top = findViewById(R.id.up);
        main = findViewById(R.id.main);
        miniplayerSeekbar = findViewById(R.id.miniplayerSeekbar);
        miniplayer = findViewById(R.id.miniplayer);
        logoName = findViewById(R.id.logoName);
        tabNavigation = findViewById(R.id.tabNavigation);
        menu = (CircleImageView) findViewById(R.id.menu);
        listRefresh = findViewById(R.id.listRefresh);
        ProgressBar listLoadBar = findViewById(R.id.listLoadBar);
        listEmptyMsg = findViewById(R.id.listEmptyMsg);
        songList = findViewById(R.id.songList);
        player = findViewById(R.id.player);
        albumArt = findViewById(R.id.albumArt);
        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
        currentDuration = findViewById(R.id.currentDuration);
        seekbarDuration = findViewById(R.id.seekbarDuration);
        maxDuration = findViewById(R.id.maxDuration);
        skipBackward = findViewById(R.id.skipBackward);
        playPause = findViewById(R.id.playPause);
        skipForward = findViewById(R.id.skipForward);
        miniplayerSkipPrev = findViewById(R.id.miniplayerSkipPrev);
        miniplayerPlayPause = findViewById(R.id.miniplayerPlayPause);
        miniplayerSkipNext = findViewById(R.id.miniplayerSkipNext);
        miniplayerAlbumArt = findViewById(R.id.miniplayerAlbumArt);
        miniplayerSongTitle = findViewById(R.id.miniplayerSongTitle);
        miniplayerSongArtist = findViewById(R.id.miniplayerSongArtist);
        repeat = findViewById(R.id.repeat);
        shuffle = findViewById(R.id.shuffle);
        timer = new Timer();
        runnableHandler = new Handler();
        songListLayoutManager = new LinearLayoutManager(this);
        liveStreamDatabase = FirebaseDatabase.getInstance().getReference("webstreams/");
        tabNavigation.addTab(tabNavigation.newTab().setIcon(R.drawable.ic_tabnav_library));
        tabNavigation.addTab(tabNavigation.newTab().setIcon(R.drawable.ic_tabnav_nowplaying));

        MobileAds.initialize(this);
        listLoadBar.setVisibility(View.GONE);

        if (FileUtil.doesExists(FileUtil.getPackageDir(this).concat("/song.json")) && FileUtil.isFile(FileUtil.getPackageDir(this).concat("/song.json"))) {
            musicData = ListUtil.getArrayListFromFile(FileUtil.getPackageDir(this).concat("/song.json"));
            if (musicData != null && !musicData.isEmpty()) {
                songList.setAdapter(new SongListAdapter(musicData));
                listEmptyMsg.setVisibility(View.GONE);
                songList.setVisibility(View.VISIBLE);
            } else {
                listEmptyMsg.setVisibility(View.VISIBLE);
                songList.setVisibility(View.GONE);
            }
            connectToLocalPlaybackService();
        } else {
            listEmptyMsg.setVisibility(View.VISIBLE);
            songList.setVisibility(View.GONE);
        }
        if (FileUtil.doesExists(FileUtil.getPackageDir(this).concat("/user/profile.pref")) && FileUtil.isFile(FileUtil.getPackageDir(this).concat("/user/profile.pref"))) {
            profileData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir(this).concat("/user/profile.pref"));
        } else {
            profileData = new HashMap<>();
        }
        if (FileUtil.doesExists(FileUtil.getPackageDir(this).concat("/user/session.pref")) && FileUtil.isFile(FileUtil.getPackageDir(this).concat("/user/session.pref"))) {
            sessionData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir(this).concat("/user/session.pref"));
        } else {
            sessionData = new HashMap<>();
        }
        if (FileUtil.doesExists(FileUtil.getPackageDir(this).concat("/user/settings.pref")) && FileUtil.isFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"))) {
            settingsData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"));
        } else {
            settingsData = new HashMap<>();
        }
        if (settingsData.containsKey("settingsAnimation")) {
            if (settingsData.get("settingsAnimation").equals("true")) {
                logoName.setTransitionName("fade");
            }
        } else {
            logoName.setTransitionName("fade");
            settingsData.put("settingsAnimation", "true");
            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"), ListUtil.setHashMapToSharedJSON(settingsData));
        }
        if (!settingsData.containsKey("settingsBackgroundAudio")) {
            settingsData.put("settingsBackgroundAudio", "true");
            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"), ListUtil.setHashMapToSharedJSON(settingsData));
        }
        if (!settingsData.containsKey("settingsCaptureError")) {
            settingsData.put("settingsCaptureError", "true");
            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"), ListUtil.setHashMapToSharedJSON(settingsData));
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
                    FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                    introDialog.dismiss();
                }
            });
            float TopLeft = 10.0f;
            float TopRight = 10.0f;
            float BottomRight = 0.0f;
            float BottomLeft = 0.0f;
            GradientDrawable roundedCorners = new GradientDrawable();
            roundedCorners.setShape(GradientDrawable.RECTANGLE);
            roundedCorners.setCornerRadii(new float[] {TopLeft, TopLeft, TopRight, TopRight, BottomRight, BottomRight, BottomLeft, BottomLeft});
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
            ((ViewGroup)dialogLayout.getParent()).setBackground(roundedCorners);
            GradientDrawable gradientButton = new GradientDrawable();
            gradientButton.setColor(Color.parseColor("#03A9F4"));
            gradientButton.setCornerRadius(10);
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
            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
            tabNavigation.getTabAt(0).select();
            listRefresh.setVisibility(View.VISIBLE);
            player.setVisibility(View.GONE);
            miniplayer.setVisibility(View.VISIBLE);
            miniplayerSeekbar.setVisibility(View.VISIBLE);
        }
        if (profileData.containsKey("profilePicture")) {
            if (!profileData.get("profilePicture").toString().equals("")) {
                Glide.with(this).load(profileData.get("profilePicture").toString()).into(menu);
            } else {
                Glide.with(this).load(R.drawable.ic_profile_icon).into(menu);
            }
        }
        if (sessionData.containsKey("sessionRepeatMode")) {
            if (sessionData.get("sessionRepeatMode").equals("0")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    repeat.setColorFilter(ContextCompat.getColor(this, R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
                } else {
                    repeat.setColorFilter(getResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
                }
            } else if (sessionData.get("sessionRepeatMode").equals("1")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    repeat.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                } else {
                    repeat.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                repeat.setColorFilter(ContextCompat.getColor(this, R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
            } else {
                repeat.setColorFilter(getResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
            }
            sessionData.put("sessionRepeatMode", "0");
            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
        }
        if (sessionData.containsKey("sessionShuffleMode")) {
            if (sessionData.get("sessionShuffleMode").equals("0")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    shuffle.setColorFilter(ContextCompat.getColor(this, R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
                } else {
                    shuffle.setColorFilter(getResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
                }
            } else if (sessionData.get("sessionShuffleMode").equals("1")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    shuffle.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                } else {
                    shuffle.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                shuffle.setColorFilter(ContextCompat.getColor(this, R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
            } else {
                shuffle.setColorFilter(getResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
            }
            sessionData.put("sessionShuffleMode", "0");
            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
        }
        registerListeners();
        startupUI();
    }

    public void startupUI () {
        if (Build.VERSION.SDK_INT >= 23) {
            skipBackward.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            playPause.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            skipForward.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            miniplayerSkipPrev.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            miniplayerPlayPause.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            miniplayerSkipNext.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
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
                settingsData.put("settingsDarkMode", "false");
                FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"), ListUtil.setHashMapToSharedJSON(settingsData));
                setTheme(R.style.Theme_ArchoMusic);
                top.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
                getWindow().setNavigationBarColor(Color.parseColor("#FFFFFF"));
            }
        } else {
            getWindow().setStatusBarColor(Color.parseColor("#000000"));
            getWindow().setNavigationBarColor(Color.parseColor("#000000"));
            miniplayerSongTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/roboto_medium.ttf"), Typeface.NORMAL);
            skipBackward.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            playPause.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            skipForward.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            miniplayerSkipPrev.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            miniplayerPlayPause.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            miniplayerSkipNext.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            repeat.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        }
        miniplayerSongTitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
        logoName.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/leixo.ttf"), Typeface.BOLD);
        songTitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
        listRefresh.setColorSchemeColors(Color.parseColor("#03A9F4"), Color.parseColor("#03A9F4"), Color.parseColor("#03A9F4"));
        songList.setLayoutManager(songListLayoutManager);
    }

    private void registerListeners() {
        menu.setOnClickListener(view -> {
            /* Iyxan23 was here */
            RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
            menu.setBackground(rippleButton);

            BottomSheetDialog menuDialog = new BottomSheetDialog(LocalStreamActivity.this);
            View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_menu, null);
            menuDialog.setContentView(dialogLayout);

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
                    Glide.with(LocalStreamActivity.this).load(profileData.get("profilePicture").toString()).into(profile_icon);
                } else {
                    Glide.with(LocalStreamActivity.this).load(R.drawable.ic_profile_icon).into(profile_icon);
                }
            }
            profile.setOnClickListener(view15 -> {
                if (!settingsData.containsKey("settingsDarkMode")) {
                    RippleDrawable rippleButton15 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                    view15.setBackground(rippleButton15);
                } else {
                    if (settingsData.get("settingsDarkMode").equals("true")) {
                        RippleDrawable rippleButton15 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                        view15.setBackground(rippleButton15);
                    } else {
                        RippleDrawable rippleButton15 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                        view15.setBackground(rippleButton15);
                    }
                }

                BottomSheetDialog createProfileDialog = new BottomSheetDialog(LocalStreamActivity.this);
                View dialogLayout1 = getLayoutInflater().inflate(R.layout.dialog_create_a_profile, null);
                createProfileDialog.setContentView(dialogLayout1);
                TextView title1 = dialogLayout1.findViewById(R.id.title);
                ImageView profile_icon1 = dialogLayout1.findViewById(R.id.profile_icon);
                EditText profile_name1 = dialogLayout1.findViewById(R.id.profile_name);
                Button create = dialogLayout1.findViewById(R.id.create);
                if (profileData.containsKey("profileName")) {
                    profile_name1.setText(profileData.get("profileName").toString());
                }
                if (profileData.containsKey("profilePicture")) {
                    if (!profileData.get("profilePicture").toString().equals("")) {
                        Glide.with(LocalStreamActivity.this).load(profileData.get("profilePicture").toString()).into(profile_icon1);
                    } else {
                        Glide.with(LocalStreamActivity.this).load(R.drawable.ic_profile_icon).into(profile_icon1);
                    }
                }
                title1.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
                profile_icon1.setOnClickListener(view151 -> {
                    RippleDrawable rippleButton15 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor("#BDBDBD")}), null, null);
                    view151.setBackground(rippleButton15);
                    BottomSheetDialog pfpDialog = new BottomSheetDialog(LocalStreamActivity.this);
                    View dialogLayout11 = getLayoutInflater().inflate(R.layout.dialog_create_a_profile_icon, null);
                    pfpDialog.setContentView(dialogLayout11);
                    TextView title11 = dialogLayout11.findViewById(R.id.title);
                    ImageView profile_picture = dialogLayout11.findViewById(R.id.profile_icon);
                    EditText url = dialogLayout11.findViewById(R.id.url);
                    Button finish = dialogLayout11.findViewById(R.id.finish);
                    Button cancel = dialogLayout11.findViewById(R.id.cancel);
                    if (profileData.containsKey("profilePicture")) {
                        Glide.with(LocalStreamActivity.this).load(profileData.get("profilePicture").toString()).into(profile_picture);
                        url.setText(profileData.get("profilePicture").toString());
                    }
                    title11.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
                    url.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                            // DO NOTHING
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                            if (!(url.getText().toString().length() == 0)) {
                                Glide.with(LocalStreamActivity.this).load(url.getText().toString()).into(profile_picture);
                            } else {
                                Glide.with(LocalStreamActivity.this).load(R.drawable.ic_profile_icon).into(profile_picture);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            // DO NOTHING
                        }
                    });
                    finish.setOnClickListener(view1511 -> {
                        if (profileData.containsKey("profilePicture")) {
                            if (url.getText().toString().equals(profileData.get("profileName").toString())) {
                                pfpDialog.dismiss();
                            } else {
                                String pfpUrl = url.getText().toString();
                                profileData.put("profilePicture", pfpUrl);
                                FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/profile.pref"), ListUtil.setHashMapToSharedJSON(profileData));
                                if (!pfpUrl.equals("")) {
                                    Glide.with(LocalStreamActivity.this).load(pfpUrl).into(profile_icon1);
                                    Glide.with(LocalStreamActivity.this).load(pfpUrl).into(menu);
                                } else {
                                    Glide.with(LocalStreamActivity.this).load(R.drawable.ic_profile_icon).into(profile_icon1);
                                    Glide.with(LocalStreamActivity.this).load(R.drawable.ic_profile_icon).into(menu);
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
                                FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/profile.pref"), ListUtil.setHashMapToSharedJSON(profileData));
                                if (!pfpUrl.equals("")) {
                                    Glide.with(LocalStreamActivity.this).load(pfpUrl).into(profile_icon1);
                                    Glide.with(LocalStreamActivity.this).load(pfpUrl).into(menu);
                                } else {
                                    Glide.with(LocalStreamActivity.this).load(R.drawable.ic_profile_icon).into(profile_icon1);
                                    Glide.with(LocalStreamActivity.this).load(R.drawable.ic_profile_icon).into(menu);
                                }
                                tabNavigation.getTabAt(0).select();
                                menuDialog.dismiss();
                                pfpDialog.dismiss();
                            }
                        }
                    });
                    cancel.setOnClickListener(view1 -> {
                        if (!settingsData.containsKey("settingsDarkMode")) {
                            RippleDrawable rippleButton1 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                            view1.setBackground(rippleButton1);
                        } else {
                            if (settingsData.get("settingsDarkMode").equals("true")) {
                                RippleDrawable rippleButton1 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                                view1.setBackground(rippleButton1);
                            } else {
                                RippleDrawable rippleButton1 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                                view1.setBackground(rippleButton1);
                            }
                        }
                        pfpDialog.dismiss();
                    });
                    float TopLeft = 10.0f;
                    float TopRight = 10.0f;
                    float BottomRight = 0.0f;
                    float BottomLeft = 0.0f;
                    GradientDrawable roundedCorners = new GradientDrawable();
                    roundedCorners.setShape(GradientDrawable.RECTANGLE);
                    roundedCorners.setCornerRadii(new float[] {TopLeft, TopLeft, TopRight, TopRight, BottomRight, BottomRight, BottomLeft, BottomLeft});
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
                    ((ViewGroup) dialogLayout11.getParent()).setBackground(roundedCorners);
                    url.setBackground(roundedCorners2);
                    GradientDrawable gradientButton = new GradientDrawable();
                    gradientButton.setColor(Color.parseColor("#03A9F4"));
                    gradientButton.setCornerRadius(10);
                    finish.setBackground(gradientButton);
                    cancel.setBackground(gradientButton);
                    pfpDialog.show();
                });
                create.setOnClickListener(view1512 -> {
                    if (profile_name1.getText().toString().length() > 0) {
                        if (profileData.containsKey("profileName")) {
                            if (profile_name1.getText().toString().equals(profileData.get("profileName").toString())) {
                                createProfileDialog.dismiss();
                            } else {
                                String profileName = profile_name1.getText().toString();
                                profileData.put("profileName", profileName);
                                FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/profile.pref"), ListUtil.setHashMapToSharedJSON(profileData));
                                ApplicationUtil.toast(LocalStreamActivity.this, "Renamed profile sucessfully.", Toast.LENGTH_SHORT);
                                tabNavigation.getTabAt(0).select();
                                createProfileDialog.dismiss();
                                menuDialog.dismiss();
                                startActivity(new Intent(LocalStreamActivity.this, SplashActivity.class));
                                finish();
                            }
                        } else {
                            String profileName = profile_name1.getText().toString();
                            profileData.put("profileName", profileName);
                            FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/profile.pref"), ListUtil.setHashMapToSharedJSON(profileData));
                            ApplicationUtil.toast(LocalStreamActivity.this, "Renamed profile sucessfully.", Toast.LENGTH_SHORT);
                            tabNavigation.getTabAt(0).select();
                            createProfileDialog.dismiss();
                            menuDialog.dismiss();
                            startActivity(new Intent(LocalStreamActivity.this, SplashActivity.class));
                            finish();
                        }
                    } else {
                        profile_name1.setError("Profile name should not be blank.");
                    }
                });
                float TopLeft = 10.0f;
                float TopRight = 10.0f;
                float BottomRight = 0.0f;
                float BottomLeft = 0.0f;
                GradientDrawable roundedCorners = new GradientDrawable();
                roundedCorners.setShape(GradientDrawable.RECTANGLE);
                roundedCorners.setCornerRadii(new float[] {TopLeft, TopLeft, TopRight, TopRight, BottomRight, BottomRight, BottomLeft, BottomLeft});
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
                        profile_name1.setTextColor(Color.parseColor("#FFFFFF"));
                        profile_name1.setHintTextColor(Color.parseColor("#BDBDBD"));
                    } else {
                        roundedCorners2.setColor(Color.parseColor("#EEEEEE"));
                        roundedCorners.setColor(Color.parseColor("#FFFFFF"));
                    }
                }
                ((ViewGroup) dialogLayout1.getParent()).setBackground(roundedCorners);
                profile_name1.setBackground(roundedCorners2);
                GradientDrawable gradientButton = new GradientDrawable();
                gradientButton.setColor(Color.parseColor("#03A9F4"));
                gradientButton.setCornerRadius(10);
                create.setBackground(gradientButton);
                createProfileDialog.show();
            });
            live_streaming.setOnClickListener(view12 -> {
                if (!settingsData.containsKey("settingsDarkMode")) {
                    RippleDrawable rippleButton12 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                    view12.setBackground(rippleButton12);
                } else {
                    if (settingsData.get("settingsDarkMode").equals("true")) {
                        RippleDrawable rippleButton12 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                        view12.setBackground(rippleButton12);
                    } else {
                        RippleDrawable rippleButton12 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                        view12.setBackground(rippleButton12);
                    }
                }
            });
            settings.setOnClickListener(view16 -> {
                if (!settingsData.containsKey("settingsDarkMode")) {
                    RippleDrawable rippleButton16 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                    view16.setBackground(rippleButton16);
                } else {
                    if (settingsData.get("settingsDarkMode").equals("true")) {
                        RippleDrawable rippleButton16 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                        view16.setBackground(rippleButton16);
                    } else {
                        RippleDrawable rippleButton16 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                        view16.setBackground(rippleButton16);
                    }
                }

                startActivity(new Intent(LocalStreamActivity.this, PreferencesActivity.class));
                menuDialog.dismiss();
                finish();
            });
            visualizer.setOnClickListener(view17 -> {
                if (!settingsData.containsKey("settingsDarkMode")) {
                    RippleDrawable rippleButton17 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                    view17.setBackground(rippleButton17);
                } else {
                    if (settingsData.get("settingsDarkMode").equals("true")) {
                        RippleDrawable rippleButton17 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                        view17.setBackground(rippleButton17);
                    } else {
                        RippleDrawable rippleButton17 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                        view17.setBackground(rippleButton17);
                    }
                }
                if (ContextCompat.checkSelfPermission(LocalStreamActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(LocalStreamActivity.this, new String[]{"android.permission.RECORD_AUDIO"}, 1);
                } else {
                    startActivity(new Intent(LocalStreamActivity.this, FullVisualizerActivity.class));
                }
            });
            about.setOnClickListener(view18 -> {
                RippleDrawable rippleButton18 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
                view18.setBackground(rippleButton18);
                BottomSheetDialog about1 = new BottomSheetDialog(LocalStreamActivity.this);
                View dialogLayout13 = getLayoutInflater().inflate(R.layout.dialog_about, null);
                about1.setContentView(dialogLayout13);
                LinearLayout main = dialogLayout13.findViewById(R.id.main);
                TextView title12 = dialogLayout13.findViewById(R.id.title);
                TextView author = dialogLayout13.findViewById(R.id.author);
                TextView youtube_name = dialogLayout13.findViewById(R.id.youtube_name);
                TextView twitter_name = dialogLayout13.findViewById(R.id.twitter_name);
                TextView github_name = dialogLayout13.findViewById(R.id.github_name);
                TextView discord_name = dialogLayout13.findViewById(R.id.discord_name);
                TextView licenses_name = dialogLayout13.findViewById(R.id.license_name);
                TextView privacy_name = dialogLayout13.findViewById(R.id.privacy_name);
                TextView version = dialogLayout13.findViewById(R.id.version);
                LinearLayout youtube = dialogLayout13.findViewById(R.id.youtube);
                LinearLayout twitter = dialogLayout13.findViewById(R.id.twitter);
                LinearLayout github = dialogLayout13.findViewById(R.id.github);
                LinearLayout discord = dialogLayout13.findViewById(R.id.discord);
                LinearLayout license = dialogLayout13.findViewById(R.id.license);
                LinearLayout privacy = dialogLayout13.findViewById(R.id.privacy);
                title12.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/leixo.ttf"), Typeface.BOLD);
                int randomizer = IntegerUtil.getRandom(0, 9);
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
                youtube.setOnClickListener(view13 -> {
                    if (!settingsData.containsKey("settingsDarkMode")) {
                        RippleDrawable rippleButton13 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                        view13.setBackground(rippleButton13);
                    } else {
                        if (settingsData.get("settingsDarkMode").equals("true")) {
                            RippleDrawable rippleButton13 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                            view13.setBackground(rippleButton13);
                        } else {
                            RippleDrawable rippleButton13 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                            view13.setBackground(rippleButton13);
                        }
                    }
                    Intent intent = new Intent();
                    intent.putExtra("url", "https://youtube.com/channel/UCndTdCP5Qr-ekaV2Im1VCgg");
                    intent.setClass(LocalStreamActivity.this, ExternalBrowserActivity.class);
                    startActivity(intent);
                });
                twitter.setOnClickListener(view14 -> {
                    if (!settingsData.containsKey("settingsDarkMode")) {
                        RippleDrawable rippleButton14 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                        view14.setBackground(rippleButton14);
                    } else {
                        if (settingsData.get("settingsDarkMode").equals("true")) {
                            RippleDrawable rippleButton14 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                            view14.setBackground(rippleButton14);
                        } else {
                            RippleDrawable rippleButton14 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                            view14.setBackground(rippleButton14);
                        }
                    }
                    Intent intent = new Intent();
                    intent.putExtra("url", "https://twitter.com/gianxddddd");
                    intent.setClass(LocalStreamActivity.this, ExternalBrowserActivity.class);
                    startActivity(intent);
                });
                github.setOnClickListener(view181 -> {
                    if (!settingsData.containsKey("settingsDarkMode")) {
                        RippleDrawable rippleButton181 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                        view181.setBackground(rippleButton181);
                    } else {
                        if (settingsData.get("settingsDarkMode").equals("true")) {
                            RippleDrawable rippleButton181 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                            view181.setBackground(rippleButton181);
                        } else {
                            RippleDrawable rippleButton181 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                            view181.setBackground(rippleButton181);
                        }
                    }
                    Intent intent = new Intent();
                    intent.putExtra("url", "https://github.com/gianxddddd/ArchoMusic");
                    intent.setClass(LocalStreamActivity.this, ExternalBrowserActivity.class);
                    startActivity(intent);
                });
                discord.setOnClickListener(view1812 -> {
                    if (!settingsData.containsKey("settingsDarkMode")) {
                        RippleDrawable rippleButton1812 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                        view1812.setBackground(rippleButton1812);
                    } else {
                        if (settingsData.get("settingsDarkMode").equals("true")) {
                            RippleDrawable rippleButton1812 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                            view1812.setBackground(rippleButton1812);
                        } else {
                            RippleDrawable rippleButton1812 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                            view1812.setBackground(rippleButton1812);
                        }
                    }
                    Intent intent = new Intent();
                    intent.putExtra("url", "https://discord.gg/x5t9n9fWCV");
                    intent.setClass(LocalStreamActivity.this, ExternalBrowserActivity.class);
                    startActivity(intent);
                });
                license.setOnClickListener(view1813 -> {
                    if (!settingsData.containsKey("settingsDarkMode")) {
                        RippleDrawable rippleButton1813 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                        view1813.setBackground(rippleButton1813);
                    } else {
                        if (settingsData.get("settingsDarkMode").equals("true")) {
                            RippleDrawable rippleButton1813 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                            view1813.setBackground(rippleButton1813);
                        } else {
                            RippleDrawable rippleButton1813 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                            view1813.setBackground(rippleButton1813);
                        }
                    }
                    Intent intent = new Intent();
                    intent.putExtra("url", "file:///android_asset/LICENSE.html");
                    intent.setClass(LocalStreamActivity.this, ExternalBrowserActivity.class);
                    startActivity(intent);
                });
                privacy.setOnClickListener(view1814 -> {
                    if (!settingsData.containsKey("settingsDarkMode")) {
                        RippleDrawable rippleButton1814 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                        view1814.setBackground(rippleButton1814);
                    } else {
                        if (settingsData.get("settingsDarkMode").equals("true")) {
                            RippleDrawable rippleButton1814 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                            view1814.setBackground(rippleButton1814);
                        } else {
                            RippleDrawable rippleButton1814 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                            view1814.setBackground(rippleButton1814);
                        }
                    }
                    Intent intent = new Intent();
                    intent.putExtra("url", "file:///android_asset/PRIVACY.html");
                    intent.setClass(LocalStreamActivity.this, ExternalBrowserActivity.class);
                    startActivity(intent);
                });
                float TopLeft = 10.0f;
                float TopRight = 10.0f;
                float BottomRight = 0.0f;
                float BottomLeft = 0.0f;
                GradientDrawable roundedCorners = new GradientDrawable();
                roundedCorners.setShape(GradientDrawable.RECTANGLE);
                roundedCorners.setCornerRadii(new float[] {TopLeft, TopLeft, TopRight, TopRight, BottomRight, BottomRight, BottomLeft, BottomLeft});
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
                ((ViewGroup) dialogLayout13.getParent()).setBackground(roundedCorners);
                about1.show();
            });
            float TopLeft = 10.0f;
            float TopRight = 10.0f;
            float BottomRight = 0.0f;
            float BottomLeft = 0.0f;
            GradientDrawable roundedCorners = new GradientDrawable();
            roundedCorners.setShape(GradientDrawable.RECTANGLE);
            roundedCorners.setCornerRadii(new float[] {TopLeft, TopLeft, TopRight, TopRight, BottomRight, BottomRight, BottomLeft, BottomLeft});
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
        });

        miniplayer.setOnClickListener(view -> {
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
        });

        tabNavigation.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ObjectAnimator fadeAnim = new ObjectAnimator();
                if (Build.VERSION.SDK_INT >= 23) {
                    tab.getIcon().setColorFilter(ContextCompat.getColor(LocalStreamActivity.this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                } else {
                    tab.getIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                }
                if (tab.getPosition() == 0) {
                    sessionData.put("sessionNavigationIndex", "0");
                    FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                    if (settingsData.containsKey("settingsAnimation")) {
                        if (settingsData.get("settingsAnimation").equals("true")) {
                            if (fadeAnim.isRunning()) {
                                fadeAnim.cancel();
                            }
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
                                    runOnUiThread(() -> {
                                        player.setVisibility(View.GONE);
                                        listRefresh.setVisibility(View.VISIBLE);
                                        miniplayer.setVisibility(View.VISIBLE);
                                        miniplayerSeekbar.setVisibility(View.VISIBLE);
                                        fadeAnim.setTarget(listRefresh);
                                        fadeAnim.setPropertyName("alpha");
                                        fadeAnim.setFloatValues((float)(0.0d), (float)(1.0d));
                                        fadeAnim.start();
                                    });
                                }
                            };
                            timer.schedule(timerTask, 250);
                        } else {
                            player.setVisibility(View.GONE);
                            listRefresh.setVisibility(View.VISIBLE);
                            miniplayer.setVisibility(View.VISIBLE);
                            miniplayerSeekbar.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (fadeAnim.isRunning()) {
                            fadeAnim.cancel();
                        }
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
                                runOnUiThread(() -> {
                                    player.setVisibility(View.GONE);
                                    listRefresh.setVisibility(View.VISIBLE);
                                    miniplayer.setVisibility(View.VISIBLE);
                                    miniplayerSeekbar.setVisibility(View.VISIBLE);
                                    fadeAnim.setTarget(listRefresh);
                                    fadeAnim.setPropertyName("alpha");
                                    fadeAnim.setFloatValues((float)(0.0d), (float)(1.0d));
                                    fadeAnim.start();
                                });
                            }
                        };
                        timer.schedule(timerTask, 250);
                    }
                } else if (tab.getPosition() == 1) {
                    sessionData.put("sessionNavigationIndex", "1");
                    FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                    if (settingsData.containsKey("settingsAnimation")) {
                        if (settingsData.get("settingsAnimation").equals("true")) {
                            if (fadeAnim.isRunning()) {
                                fadeAnim.cancel();
                            }
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
                                    runOnUiThread(() -> {
                                        player.setVisibility(View.VISIBLE);
                                        listRefresh.setVisibility(View.GONE);
                                        miniplayer.setVisibility(View.GONE);
                                        miniplayerSeekbar.setVisibility(View.GONE);
                                        fadeAnim.setTarget(player);
                                        fadeAnim.setPropertyName("alpha");
                                        fadeAnim.setFloatValues((float)(0.0d), (float)(1.0d));
                                        fadeAnim.start();
                                    });
                                }
                            };
                            timer.schedule(timerTask, 250);
                        } else {
                            player.setVisibility(View.VISIBLE);
                            listRefresh.setVisibility(View.GONE);
                            miniplayer.setVisibility(View.GONE);
                            miniplayerSeekbar.setVisibility(View.GONE);
                        }
                    } else {
                        if (fadeAnim.isRunning()) {
                            fadeAnim.cancel();
                        }
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
                                runOnUiThread(() -> {
                                    player.setVisibility(View.VISIBLE);
                                    listRefresh.setVisibility(View.GONE);
                                    miniplayer.setVisibility(View.GONE);
                                    miniplayerSeekbar.setVisibility(View.GONE);
                                    fadeAnim.setTarget(player);
                                    fadeAnim.setPropertyName("alpha");
                                    fadeAnim.setFloatValues((float)(0.0d), (float)(1.0d));
                                    fadeAnim.start();
                                });
                            }
                        };
                        timer.schedule(timerTask, 250);
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
        listRefresh.setOnRefreshListener(() -> {
            if (FileUtil.doesExists(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/song.json")) && FileUtil.isFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/song.json"))) {
                musicData = ListUtil.getArrayListFromFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/song.json"));
                if (musicData != null && !musicData.isEmpty()) {
                    songList.setAdapter(new SongListAdapter(musicData));
                    listEmptyMsg.setVisibility(View.GONE);
                    songList.setVisibility(View.VISIBLE);
                } else {
                    listEmptyMsg.setVisibility(View.VISIBLE);
                    songList.setVisibility(View.GONE);
                }
            } else {
                listEmptyMsg.setVisibility(View.VISIBLE);
                songList.setVisibility(View.GONE);
            }
            listRefresh.setRefreshing(false);
        });
        songList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
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
                    seekbarDuration.setProgress(seekbarDuration.getProgress());
                    miniplayerSeekbar.setProgress(seekbarDuration.getProgress());
                    currentDuration.setText(String.valueOf((seekbarDuration.getProgress() / 1000) / 60).concat(":".concat(new DecimalFormat("00").format((seekbarDuration.getProgress() / 1000) % 60))));
                    musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).put("songCurrentDuration", String.valueOf(seekbarDuration.getProgress()));
                    FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/song.json"), ListUtil.setArrayListToSharedJSON(musicData));
                }
            }
        });
        repeat.setOnClickListener(view -> {
            RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
            view.setBackground(rippleButton);
            if (sessionData.containsKey("sessionRepeatMode")) {
                if (sessionData.get("sessionRepeatMode").equals("0")) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        repeat.setColorFilter(ContextCompat.getColor(LocalStreamActivity.this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                    } else {
                        repeat.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                    }
                    sessionData.put("sessionRepeatMode", "1");
                    FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                    if (sessionData.get("sessionShuffleMode").equals("1")) {
                        if (Build.VERSION.SDK_INT >= 23) {
                            shuffle.setColorFilter(ContextCompat.getColor(LocalStreamActivity.this, R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
                        } else {
                            shuffle.setColorFilter(getResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
                        }
                        sessionData.put("sessionShuffleMode", "0");
                        FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                    }
                    playbackSrv.updateOnCompletionListener();
                } else if (sessionData.get("sessionRepeatMode").equals("1")) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        repeat.setColorFilter(ContextCompat.getColor(LocalStreamActivity.this, R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
                    } else {
                        repeat.setColorFilter(getResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
                    }
                    sessionData.put("sessionRepeatMode", "0");
                    FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                }
                playbackSrv.updateOnCompletionListener();
            } else {
                if (Build.VERSION.SDK_INT >= 23) {
                    repeat.setColorFilter(ContextCompat.getColor(LocalStreamActivity.this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                } else {
                    repeat.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                }
                sessionData.put("sessionRepeatMode", "1");
                FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                playbackSrv.updateOnCompletionListener();
            }
        });

        skipBackward.setOnClickListener(view -> {
            RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
            view.setBackground(rippleButton);
            skipPrevious();
        });

        playPause.setOnClickListener(view -> {
            RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
            view.setBackground(rippleButton);
            playPause();
        });

        skipForward.setOnClickListener(view -> {
            RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
            view.setBackground(rippleButton);
            skipNext();
        });

        miniplayerSkipPrev.setOnClickListener(view -> {
            RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
            view.setBackground(rippleButton);
            skipPrevious();
        });

        miniplayerPlayPause.setOnClickListener(view -> {
            RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
            view.setBackground(rippleButton);
            playPause();
        });

        miniplayerSkipNext.setOnClickListener(view -> {
            RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
            view.setBackground(rippleButton);
            skipNext();
        });

        shuffle.setOnClickListener(view -> {
            RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
            view.setBackground(rippleButton);
            if (sessionData.containsKey("sessionShuffleMode")) {
                if (sessionData.get("sessionShuffleMode").equals("0")) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        shuffle.setColorFilter(ContextCompat.getColor(LocalStreamActivity.this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                    } else {
                        shuffle.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                    }
                    sessionData.put("sessionShuffleMode", "1");
                    FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                    if (sessionData.get("sessionRepeatMode").equals("1")) {
                        if (Build.VERSION.SDK_INT >= 23) {
                            repeat.setColorFilter(ContextCompat.getColor(LocalStreamActivity.this, R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
                        } else {
                            repeat.setColorFilter(getResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
                        }
                        sessionData.put("sessionRepeatMode", "0");
                        FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                    }
                    playbackSrv.updateOnCompletionListener();
                } else if (sessionData.get("sessionShuffleMode").equals("1")) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        shuffle.setColorFilter(ContextCompat.getColor(LocalStreamActivity.this, R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
                    } else {
                        shuffle.setColorFilter(getResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
                    }
                    sessionData.put("sessionShuffleMode", "0");
                    FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                    playbackSrv.updateOnCompletionListener();
                }
            } else {
                if (Build.VERSION.SDK_INT >= 23) {
                    shuffle.setColorFilter(ContextCompat.getColor(LocalStreamActivity.this, R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
                } else {
                    shuffle.setColorFilter(getResources().getColor(R.color.colorControlHighlight), PorterDuff.Mode.SRC_IN);
                }
                sessionData.put("sessionShuffleMode", "0");
                FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                playbackSrv.updateOnCompletionListener();
            }
        });
    }

    private void skipPrevious() {
        if (playbackSrv != null && playbackSrv.mp != null) {
            if (!sessionData.containsKey("sessionRepeatMode") || !profileData.containsKey("sessionShuffleMode")) {
                try {
                    if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) - 1 < musicData.size()) {
                        sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) - 1));
                        FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                        playbackSrv.createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
                        playPause.performClick();
                    }
                } catch (Exception exception) {
                    if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) - 1 < musicData.size()) {
                        sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) - 1));
                        FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                        playbackSrv.createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
                        playPause.performClick();
                    }
                }
            } else {
                if (sessionData.get("sessionRepeatMode").equals("0") && sessionData.get("sessionShuffleMode").equals("0")) {
                    try {
                        if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) - 1 < musicData.size()) {
                            sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) - 1));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                            playbackSrv.createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
                            playPause.performClick();
                        }
                    } catch (Exception exception) {
                        if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) - 1 < musicData.size()) {
                            sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) - 1));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
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
                            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                            playbackSrv.createLocalStream(randomizer);
                            playPause.performClick();
                        }
                    } catch (Exception exception) {
                        ApplicationUtil.toast(this, "Error loading audio file.", Toast.LENGTH_SHORT);
                        if (randomizer < musicData.size()) {
                            sessionData.put("sessionSongPosition", String.valueOf(randomizer));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                            playbackSrv.createLocalStream(randomizer);
                            playPause.performClick();
                        }
                    }
                }
            }
        }
    }

    private void playPause() {
        if (playbackSrv != null && playbackSrv.mp != null) {
            if (!playbackSrv.isPlaying()) {
                playbackSrv.play();
                playbackSrv.startAudioFocus();
                playbackSrv.startHeadphoneReceiving();
                setVolumeControlStream(AudioManager.STREAM_MUSIC);
                playPause.setImageResource(R.drawable.ic_media_pause);
                miniplayerPlayPause.setImageResource(R.drawable.ic_media_pause);
                playerDurationUpdater = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            seekbarDuration.setProgress(playbackSrv.getCurrentPosition());
                            miniplayerSeekbar.setProgress(playbackSrv.getCurrentPosition());
                            currentDuration.setText(String.valueOf((playbackSrv.getCurrentPosition() / 1000) / 60).concat(":".concat(new DecimalFormat("00").format((playbackSrv.getCurrentPosition() / 1000) % 60))));
                            musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).put("songCurrentDuration", String.valueOf(playbackSrv.getCurrentPosition()));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/song.json"), ListUtil.setArrayListToSharedJSON(musicData));
                        } catch (Exception exception) {
                            Log.e("LocalPlaybackService", "Can't track current duration.");
                        }
                        runnableHandler.postDelayed(this, 500);
                    }
                };
                runnableHandler.postDelayed(playerDurationUpdater, 500);
                isHandlerRunning = true;
            } else {
                playbackSrv.pause();
                playbackSrv.loseAudioFocus();
                playbackSrv.stopHeadphoneReceiving();
                playPause.setImageResource(R.drawable.ic_media_play);
                miniplayerPlayPause.setImageResource(R.drawable.ic_media_play);
                if (isHandlerRunning) {
                    runnableHandler.removeCallbacks(playerDurationUpdater);
                    isHandlerRunning = false;
                }
            }
        }
    }

    private void skipNext() {
        if (playbackSrv != null && playbackSrv.mp != null) {
            if (!sessionData.containsKey("sessionRepeatMode") || !sessionData.containsKey("sessionShuffleMode")) {
                try {
                    if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1 < musicData.size()) {
                        sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1));
                        FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                        playbackSrv.createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
                        playPause.performClick();
                    }
                } catch (Exception exception) {
                    ApplicationUtil.toast(this, "Error loading audio file.", Toast.LENGTH_SHORT);
                    if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1 < musicData.size()) {
                        sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1));
                        FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                        playbackSrv.createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
                        playPause.performClick();
                    }
                }
            } else {
                if (sessionData.get("sessionRepeatMode").equals("0") && sessionData.get("sessionShuffleMode").equals("0")) {
                    try {
                        if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1 < musicData.size()) {
                            sessionData.put("sessionSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                            playbackSrv.createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
                            playPause.performClick();
                        }
                    } catch (Exception exception) {
                        ApplicationUtil.toast(this, "Error loading audio file.", Toast.LENGTH_SHORT);
                        if (Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1 < musicData.size()) {
                            sessionData.put("profileSongPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString()) + 1));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
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
                            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                            playbackSrv.createLocalStream(randomizer);
                            playPause.performClick();
                        }
                    } catch (Exception exception) {
                        ApplicationUtil.toast(this, "Error loading audio file.", Toast.LENGTH_SHORT);
                        if (randomizer < musicData.size()) {
                            sessionData.put("sessionSongPosition", String.valueOf(randomizer));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                            playbackSrv.createLocalStream(randomizer);
                            playPause.performClick();
                        }
                    }
                }
            }
        }
    }

    private void connectToLocalPlaybackService() {
        ServiceConnection musicConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LocalPlaybackService.MusicBinder binder = (LocalPlaybackService.MusicBinder) service;
                playbackSrv = binder.getService();
                isMusicBounded = true;
                try {
                    if (playbackSrv.mp != null && playbackSrv.isPlaying()) {
                        playPause.setImageResource(R.drawable.ic_media_pause);
                        miniplayerPlayPause.setImageResource(R.drawable.ic_media_pause);
                        Glide.with(LocalStreamActivity.this).asBitmap().load(ImageUtil.getAlbumArt(Base64Util.decode(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songData").toString()), getResources(), getTheme())).into(albumArt);
                        Glide.with(LocalStreamActivity.this).asBitmap().load(ImageUtil.getAlbumArt(Base64Util.decode(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songData").toString()), getResources(), getTheme())).into(miniplayerAlbumArt);
                        songTitle.setText(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songTitle").toString());
                        songArtist.setText(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songArtist").toString());
                        miniplayerSongTitle.setText(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songTitle").toString());
                        miniplayerSongArtist.setText(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songArtist").toString());
                        seekbarDuration.setMax(playbackSrv.getMaxDuration());
                        seekbarDuration.setProgress(playbackSrv.getCurrentPosition());
                        miniplayerSeekbar.setMax(playbackSrv.getMaxDuration());
                        miniplayerSeekbar.setProgress(playbackSrv.getCurrentPosition());
                        maxDuration.setText(String.valueOf((playbackSrv.getMaxDuration() / 1000) / 60).concat(":".concat(new DecimalFormat("00").format((playbackSrv.getMaxDuration() / 1000) % 60))));
                        currentDuration.setText(String.valueOf((playbackSrv.getCurrentPosition() / 1000) / 60).concat(":".concat(new DecimalFormat("00").format((playbackSrv.getCurrentPosition() / 1000) % 60))));
                    } else {
                        if (sessionData.containsKey("sessionSongPosition")) {
                            if (FileUtil.doesExists(Base64Util.decode(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songData").toString()))
                                  && FileUtil.isFile(Base64Util.decode(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songData").toString()))) {
                                playbackSrv.createLocalStream(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
                                if (musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).containsKey("songCurrentDuration")) {
                                    if (!musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songCurrentDuration").equals(playbackSrv.getCurrentPosition())) {
                                        playbackSrv.seek(Integer.parseInt(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songCurrentDuration").toString()));
                                        miniplayerSeekbar.setProgress(Integer.parseInt(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songCurrentDuration").toString()));
                                        currentDuration.setText(String.valueOf(((Integer.parseInt(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songCurrentDuration").toString()) / 1000) / 60)).concat(":".concat(new DecimalFormat("00").format((Integer.parseInt(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songCurrentDuration").toString()) / 1000) % 60))));
                                        seekbarDuration.setProgress(Integer.parseInt(musicData.get(Integer.parseInt(sessionData.get("sessionSongPosition").toString())).get("songCurrentDuration").toString()));
                                    }
                                }
                            }
                        } else {
                            if (!musicData.isEmpty()) {
                                if (0 < musicData.size()) {
                                    playbackSrv.createLocalStream(0);
                                    sessionData.put("sessionSongPosition", "0");
                                    FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                                }
                            }
                        }
                    }
                } catch (Exception exception) {
                    if (!musicData.isEmpty()) {
                        if (0 < musicData.size()) {
                            playbackSrv.createLocalStream(0);
                            sessionData.put("sessionSongPosition", "0");
                            FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                        }
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isMusicBounded = false;
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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
               startActivity(new Intent(LocalStreamActivity.this, FullVisualizerActivity.class));
            } else {
                ApplicationUtil.toast(this, "Record permission was denied, Visualizer cannot unless you allow the permission again.", Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        } else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (settingsData.containsKey("settingsBackgroundAudio")) {
            if (!settingsData.get("settingsBackgroundAudio").equals("true")) {
                if (playbackSrv != null && playbackSrv.mp != null
                       && playbackSrv.isPlaying()) {
                    playPause();
                }
            }
        }
        if (playbackSrv != null && playbackSrv.mp != null) {
            if (playbackSrv.isPlaying()) {
                moveTaskToBack(true);
            } else {
                if (!playbackSrv.isPlaying()) {
                    playIntent = new Intent(this, LocalPlaybackService.class);
                    stopService(playIntent);
                    finishAffinity();
                }
            }
        } else {
            finishAffinity();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (settingsData.containsKey("settingsBackgroundAudio")) {
            if (settingsData.get("settingsBackgroundAudio").equals("false")) {
                if (playbackSrv != null) {
                    if (playbackSrv.mp != null && playbackSrv.isPlaying()) {
                        playPause();
                    }
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FileUtil.doesExists(FileUtil.getPackageDir(this).concat("/song.json")) && FileUtil.isFile(FileUtil.getPackageDir(this).concat("/song.json"))) {
            musicData = ListUtil.getArrayListFromFile(FileUtil.getPackageDir(this).concat("/song.json"));
            if (musicData != null && !musicData.isEmpty()) {
                songList.setAdapter(new SongListAdapter(musicData));
                listEmptyMsg.setVisibility(View.GONE);
                songList.setVisibility(View.VISIBLE);
            } else {
                listEmptyMsg.setVisibility(View.VISIBLE);
                songList.setVisibility(View.GONE);
            }
        } else {
            listEmptyMsg.setVisibility(View.VISIBLE);
            songList.setVisibility(View.GONE);
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
            FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
            tabNavigation.getTabAt(0).select();
            listRefresh.setVisibility(View.VISIBLE);
            player.setVisibility(View.GONE);
            miniplayer.setVisibility(View.VISIBLE);
            miniplayerSeekbar.setVisibility(View.VISIBLE);
        }
        if (sessionData.containsKey("sessionSongPosition")) {
            songList.scrollToPosition(Integer.parseInt(sessionData.get("sessionSongPosition").toString()));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (playbackSrv != null) {
            if (playbackSrv.mp != null) {
                if (!playbackSrv.mp.isPlaying()) {
                    playIntent = new Intent(this, LocalPlaybackService.class);
                    stopService(playIntent);
                }
            }
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
            LinearLayout main = view.findViewById(R.id.main);
            ImageView more = view.findViewById(R.id.more);
            TextView songTitle = view.findViewById(R.id.songTitle);
            TextView songArtist = view.findViewById(R.id.songArtist);
            ImageView albumArt = view.findViewById(R.id.albumArt);
            if (settingsData.containsKey("settingsDarkMode")) {
                if (settingsData.get("settingsDarkMode").equals("true")) {
                    main.setBackgroundColor(Color.parseColor("#1A1A1A"));
                    songTitle.setTextColor(Color.parseColor("#FFFFFF"));
                    songArtist.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }
            Glide.with(LocalStreamActivity.this).asBitmap().load(ImageUtil.getAlbumArt(Base64Util.decode(data.get(position).get("songData").toString()), getResources(), getTheme())).into(albumArt);
            songTitle.setText(data.get(position).get("songTitle").toString());
            songArtist.setText(data.get(position).get("songArtist").toString());
            main.setOnClickListener(view17 -> {
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
                    if (FileUtil.doesExists(Base64Util.decode(data.get(position).get("songData").toString()))
                           && FileUtil.isFile(Base64Util.decode(data.get(position).get("songData").toString()))) {
                        try {
                            playbackSrv.createLocalStream(position);
                            sessionData.put("sessionSongPosition", String.valueOf(position));
                            FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/user/session.pref"), ListUtil.setHashMapToSharedJSON(sessionData));
                            playPause.performClick();
                        } catch (Exception exception) {
                            ApplicationUtil.toast(LocalStreamActivity.this, "Error loading audio file.", Toast.LENGTH_SHORT);
                            skipForward.performClick();
                        }
                    } else {
                        ApplicationUtil.toast(LocalStreamActivity.this, "The file path of this song does not exist.", Toast.LENGTH_SHORT);
                    }
                } else {
                    ApplicationUtil.toast(LocalStreamActivity.this, "This song is currently played.", Toast.LENGTH_SHORT);
                }
            });
            more.setOnClickListener(view16 -> {
                RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
                view16.setBackground(rippleButton);
                BottomSheetDialog songOptsDialog = new BottomSheetDialog(LocalStreamActivity.this);
                View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_song_opts, null);
                songOptsDialog.setContentView(dialogLayout);
                TextView title = dialogLayout.findViewById(R.id.title);
                ImageView albumArt1 = dialogLayout.findViewById(R.id.albumArt);
                TextView songTitle1 = dialogLayout.findViewById(R.id.songTitle);
                TextView songArtist1 = dialogLayout.findViewById(R.id.songArtist);
                TextView rename_title = dialogLayout.findViewById(R.id.rename_title);
                TextView lyrics_title = dialogLayout.findViewById(R.id.lyrics_title);
                TextView share_title = dialogLayout.findViewById(R.id.share_title);
                TextView moreInformation_title = dialogLayout.findViewById(R.id.moreInformation_title);
                LinearLayout rename = dialogLayout.findViewById(R.id.rename);
                LinearLayout lyrics = dialogLayout.findViewById(R.id.lyrics);
                LinearLayout share = dialogLayout.findViewById(R.id.share);
                LinearLayout moreInformation = dialogLayout.findViewById(R.id.moreInformation);
                LinearLayout remove = dialogLayout.findViewById(R.id.remove);
                title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/roboto_medium.ttf"), Typeface.NORMAL);
                songTitle1.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/roboto_medium.ttf"), Typeface.NORMAL);
                Glide.with(LocalStreamActivity.this).asBitmap().load(ImageUtil.getAlbumArt(Base64Util.decode(data.get(position).get("songData").toString()), getResources(), getTheme())).into(albumArt1);
                songTitle1.setText(musicData.get(position).get("songTitle").toString());
                songArtist1.setText(musicData.get(position).get("songArtist").toString());
                rename.setOnClickListener(view15 -> {
                    if (!settingsData.containsKey("settingsDarkMode")) {
                        RippleDrawable rippleButton15 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                        view15.setBackground(rippleButton15);
                    } else {
                        if (settingsData.get("settingsDarkMode").equals("true")) {
                            RippleDrawable rippleButton15 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                            view15.setBackground(rippleButton15);
                        } else {
                            RippleDrawable rippleButton15 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                            view15.setBackground(rippleButton15);
                        }
                    }

                    BottomSheetDialog renameDialog = new BottomSheetDialog(LocalStreamActivity.this);
                    View dialogLayout1 = getLayoutInflater().inflate(R.layout.dialog_song_opts_rename, null);
                    renameDialog.setContentView(dialogLayout1);
                    LinearLayout main1 = dialogLayout1.findViewById(R.id.main);
                    ImageView back = dialogLayout1.findViewById(R.id.back);
                    TextView title1 = dialogLayout1.findViewById(R.id.title);
                    TextView song_name_title = dialogLayout1.findViewById(R.id.song_name_title);
                    TextView artist_name_title = dialogLayout1.findViewById(R.id.artist_name_title);
                    TextView filename_title = dialogLayout1.findViewById(R.id.filename_title);
                    EditText song_name = dialogLayout1.findViewById(R.id.song_name);
                    EditText artist_name = dialogLayout1.findViewById(R.id.artist_name);
                    EditText filename = dialogLayout1.findViewById(R.id.filename);
                    Button confirm = dialogLayout1.findViewById(R.id.confirm);
                    Button cancel = dialogLayout1.findViewById(R.id.cancel);
                    filename.setEnabled(false);
                    filename.setAlpha(0.5f);
                    title1.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
                    song_name_title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
                    artist_name_title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
                    filename_title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
                    song_name.setText(musicData.get(position).get("songTitle").toString());
                    artist_name.setText(musicData.get(position).get("songArtist").toString());
                    filename.setText(Uri.parse(Base64Util.decode(musicData.get(position).get("songData").toString())).getLastPathSegment());
                    back.setOnClickListener(view18 -> {
                        RippleDrawable rippleButton17 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
                        view18.setBackground(rippleButton17);
                        renameDialog.dismiss();
                    });
                    song_name.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                            filename.setText(song_name.getText().toString().concat(".".concat(FileUtil.getFileExtension(Base64Util.decode(musicData.get(position).get("songData").toString())))));
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });
                    confirm.setOnClickListener(view19 -> {
                        if (!settingsData.containsKey("settingsDarkMode")) {
                            RippleDrawable rippleButton18 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                            view19.setBackground(rippleButton18);
                        } else {
                            if (settingsData.get("settingsDarkMode").equals("true")) {
                                RippleDrawable rippleButton18 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                                view19.setBackground(rippleButton18);
                            } else {
                                RippleDrawable rippleButton18 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                                view19.setBackground(rippleButton18);
                            }
                        }

                        if (song_name.getText().toString().length() > 0) {
                            String oldDecodedData = Base64Util.decode(data.get(position).get("songData").toString());
                            data.get(position).put("songTitle", song_name.getText().toString());
                            data.get(position).put("songArtist", artist_name.getText().toString());
                            data.get(position).put("songData", Base64Util.decode(new File(Base64Util.decode(data.get(position).get("songData").toString())).getParent().concat("/".concat(filename.getText().toString()))));

                            if (!songTitle.getText().toString().equals(song_name.getText().toString())
                                    && !miniplayerSongTitle.getText().toString().equals(song_name.getText().toString())) {
                                songTitle.setText(song_name.getText().toString());
                                miniplayerSongTitle.setText(song_name.getText().toString());
                            }
                            if (!songArtist.getText().toString().equals(artist_name.getText().toString())
                                    && miniplayerSongArtist.getText().toString().equals(artist_name.getText().toString())) {
                                songArtist.setText(artist_name.getText().toString());
                                miniplayerSongArtist.setText(artist_name.getText().toString());
                            }
                            if (FileUtil.doesExists(oldDecodedData)
                                && FileUtil.isFile(oldDecodedData)) {
                                FileUtil.renameFile(oldDecodedData, new File(oldDecodedData).getParent().concat("/".concat(filename.getText().toString())));
                            }
                            ApplicationUtil.toast(LocalStreamActivity.this, "Song renamed successfully.", Toast.LENGTH_SHORT);
                            FileUtil.writeStringToFile(FileUtil.getPackageDir(LocalStreamActivity.this).concat("/song.json"), ListUtil.setArrayListToSharedJSON(data));
                            songList.setAdapter(new SongListAdapter(data));
                            songList.scrollToPosition(position);
                            songOptsDialog.dismiss();
                            renameDialog.dismiss();
                        } else {
                            song_name.setError("Song name should not be blank.");
                        }
                    });
                    float TopLeft = 10.0f;
                    float TopRight = 10.0f;
                    float BottomRight = 0.0f;
                    float BottomLeft = 0.0f;
                    GradientDrawable roundedCorners = new GradientDrawable();
                    roundedCorners.setShape(GradientDrawable.RECTANGLE);
                    roundedCorners.setCornerRadii(new float[] {TopLeft, TopLeft, TopRight, TopRight, BottomRight,BottomRight, BottomLeft, BottomLeft});
                    GradientDrawable roundedCorners2 = new GradientDrawable();
                    roundedCorners2.setShape(GradientDrawable.RECTANGLE);
                    roundedCorners2.setCornerRadius(10);
                    if (!settingsData.containsKey("settingsDarkMode")) {
                        roundedCorners.setColor(Color.parseColor("#FFFFFF"));
                        roundedCorners2.setColor(Color.parseColor("#EEEEEE"));
                    } else {
                        if (settingsData.get("settingsDarkMode").equals("true")) {
                            roundedCorners.setColor(Color.parseColor("#1A1A1A"));
                            roundedCorners2.setColor(Color.parseColor("#212121"));
                            song_name.setTextColor(Color.parseColor("#FFFFFF"));
                            song_name.setHintTextColor(Color.parseColor("#BDBDBD"));
                            artist_name.setTextColor(Color.parseColor("#FFFFFF"));
                            artist_name.setHintTextColor(Color.parseColor("#BDBDBD"));
                            filename.setTextColor(Color.parseColor("#FFFFFF"));
                            filename.setHintTextColor(Color.parseColor("#BDBDBD"));
                        } else {
                            roundedCorners.setColor(Color.parseColor("#FFFFFF"));
                            roundedCorners2.setColor(Color.parseColor("#EEEEEE"));
                        }
                    }
                    ((ViewGroup) dialogLayout1.getParent()).setBackground(roundedCorners);
                    song_name.setBackground(roundedCorners2);
                    artist_name.setBackground(roundedCorners2);
                    filename.setBackground(roundedCorners2);
                    GradientDrawable gradientButton = new GradientDrawable();
                    gradientButton.setColor(Color.parseColor("#03A9F4"));
                    gradientButton.setCornerRadius(10);
                    confirm.setBackground(gradientButton);
                    cancel.setBackground(gradientButton);
                    renameDialog.show();
                });
                lyrics.setOnClickListener(view161 -> {
                    if (!settingsData.containsKey("settingsDarkMode")) {
                        RippleDrawable rippleButton16 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                        view161.setBackground(rippleButton16);
                    } else {
                        if (settingsData.get("settingsDarkMode").equals("true")) {
                            RippleDrawable rippleButton16 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                            view161.setBackground(rippleButton16);
                        } else {
                            RippleDrawable rippleButton16 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                            view161.setBackground(rippleButton16);
                        }
                    }

                    BottomSheetDialog lyricsDialog = new BottomSheetDialog(LocalStreamActivity.this);
                    View dialogLayout2 = getLayoutInflater().inflate(R.layout.dialog_lyrics, null);
                    lyricsDialog.setContentView(dialogLayout2);
                    LinearLayout main2 = dialogLayout2.findViewById(R.id.main);
                    ImageView back1 = dialogLayout2.findViewById(R.id.back);
                    ImageView lyrics_edit = dialogLayout2.findViewById(R.id.lyrics_edit);
                    TextView title2 = dialogLayout2.findViewById(R.id.title);
                    TextView lyrics1 = dialogLayout2.findViewById(R.id.lyrics);
                    title2.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
                    if (data.get(position).containsKey("songLyrics")) {
                        if (data.get(position).get("songLyrics").toString().length() == 0) {
                            // Lyrics added with 0 letters
                        } else {
                            lyrics1.setText(data.get(position).get("songLyrics").toString());
                        }
                    } else {
                        // No Lyrics was found.
                    }
                    back1.setOnClickListener(view14 -> {
                        RippleDrawable rippleButton14 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
                        view14.setBackground(rippleButton14);
                        lyricsDialog.dismiss();
                    });
                    lyrics_edit.setOnClickListener(view1611 -> {
                        RippleDrawable rippleButton16 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
                        view1611.setBackground(rippleButton16);
                        Intent intent = new Intent(LocalStreamActivity.this, LyricsEditorActivity.class);
                        intent.putExtra("songPosition", String.valueOf(Integer.parseInt(sessionData.get("sessionSongPosition").toString())));
                        startActivity(intent);
                        lyricsDialog.dismiss();
                        songOptsDialog.dismiss();
                    });
                    float TopLeft = 10.0f;
                    float TopRight = 10.0f;
                    float BottomRight = 0.0f;
                    float BottomLeft = 0.0f;
                    GradientDrawable roundedCorners = new GradientDrawable();
                    roundedCorners.setShape(GradientDrawable.RECTANGLE);
                    roundedCorners.setCornerRadii(new float[] {TopLeft, TopLeft, TopRight, TopRight, BottomRight, BottomRight, BottomLeft, BottomLeft});
                    if (!settingsData.containsKey("settingsDarkMode")) {
                        roundedCorners.setColor(Color.parseColor("#FFFFFF"));
                    } else {
                        if (settingsData.get("settingsDarkMode").equals("true")) {
                            roundedCorners.setColor(Color.parseColor("#1A1A1A"));
                            lyrics1.setTextColor(Color.parseColor("#FFFFFF"));
                            lyrics1.setHintTextColor(Color.parseColor("#BDBDBD"));
                        } else {
                            roundedCorners.setColor(Color.parseColor("#FFFFFF"));
                        }
                    }
                    ((ViewGroup) dialogLayout2.getParent()).setBackground(roundedCorners);
                    lyricsDialog.show();
                });
                share.setOnClickListener(view13 -> {
                    if (!settingsData.containsKey("settingsDarkMode")) {
                        RippleDrawable rippleButton13 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                        view13.setBackground(rippleButton13);
                    } else {
                        if (settingsData.get("settingsDarkMode").equals("true")) {
                            RippleDrawable rippleButton13 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                            view13.setBackground(rippleButton13);
                        } else {
                            RippleDrawable rippleButton13 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                            view13.setBackground(rippleButton13);
                        }
                    }

                    Uri decodedUriData = Uri.parse(Base64Util.decode(data.get(position).get("songData").toString()));
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("audio/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, decodedUriData);
                    startActivity(Intent.createChooser(shareIntent, "Share this song to:"));
                });
                moreInformation.setOnClickListener(view1 -> {
                    if (!settingsData.containsKey("settingsDarkMode")) {
                        RippleDrawable rippleButton1 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                        view1.setBackground(rippleButton1);
                    } else {
                        if (settingsData.get("settingsDarkMode").equals("true")) {
                            RippleDrawable rippleButton1 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                            view1.setBackground(rippleButton1);
                        } else {
                            RippleDrawable rippleButton1 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                            view1.setBackground(rippleButton1);
                        }
                    }
                });
                remove.setOnClickListener(view12 -> {
                    if (!settingsData.containsKey("settingsDarkMode")) {
                        RippleDrawable rippleButton12 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                        view12.setBackground(rippleButton12);
                    } else {
                        if (settingsData.get("settingsDarkMode").equals("true")) {
                            RippleDrawable rippleButton12 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                            view12.setBackground(rippleButton12);
                        } else {
                            RippleDrawable rippleButton12 = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                            view12.setBackground(rippleButton12);
                        }
                    }

                    BottomSheetDialog removeDialog = new BottomSheetDialog(LocalStreamActivity.this);
                    View dialogLayout3 = getLayoutInflater().inflate(R.layout.dialog_song_opts_remove, null);
                    removeDialog.setContentView(dialogLayout3);
                });
                float TopLeft = 10.0f;
                float TopRight = 10.0f;
                float BottomRight = 0.0f;
                float BottomLeft = 0.0f;
                GradientDrawable roundedCorners = new GradientDrawable();
                roundedCorners.setShape(GradientDrawable.RECTANGLE);
                roundedCorners.setCornerRadii(new float[] {TopLeft, TopLeft, TopRight, TopRight, BottomRight, BottomRight, BottomLeft, BottomLeft});
                if (!settingsData.containsKey("settingsDarkMode")) {
                    roundedCorners.setColor(Color.parseColor("#FFFFFF"));
                } else {
                    if (settingsData.get("settingsDarkMode").equals("true")) {
                        roundedCorners.setColor(Color.parseColor("#1A1A1A"));
                        songTitle1.setTextColor(Color.parseColor("#FFFFFF"));
                        songArtist1.setTextColor(Color.parseColor("#FFFFFF"));
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
            });
            ObjectAnimator itemAnim = new ObjectAnimator();
            itemAnim.setTarget(main);
            itemAnim.setPropertyName("alpha");
            itemAnim.setFloatValues(0.0f, 1.0f);

            if (settingsData.containsKey("settingsAnimation")) {
                if (settingsData.get("settingsAnimation").equals("true")) {
                    itemAnim.start();
                }
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            public ViewHolder(View view){
                super(view);
            }
        }

    }

}
