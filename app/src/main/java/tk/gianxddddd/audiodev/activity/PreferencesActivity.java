package tk.gianxddddd.audiodev.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

import tk.gianxddddd.audiodev.R;
import tk.gianxddddd.audiodev.util.ApplicationUtil;
import tk.gianxddddd.audiodev.util.FileUtil;
import tk.gianxddddd.audiodev.util.ListUtil;

public class PreferencesActivity extends AppCompatActivity {
    
    private HashMap<String, Object> settingsData;

    private LinearLayout main;
    private ImageView back;
    private TextView title;
    private TextView general_title;
    private TextView appearance_title;
    private TextView audio_title;
    private TextView other_title;
    private CheckBox disable_ads;
    private CheckBox dark_mode;
    private CheckBox disable_anim;
    private CheckBox background_play;
    private CheckBox capture_error;
    private Button clear_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        initialize(savedInstanceState);
        com.google.firebase.FirebaseApp.initializeApp(this);
    }

    private void initialize(Bundle savedInstanceState) {
        main = (LinearLayout) findViewById(R.id.main);
        back = (ImageView) findViewById(R.id.back);
        title = (TextView) findViewById(R.id.title);
        general_title = (TextView) findViewById(R.id.general_title);
        appearance_title = (TextView) findViewById(R.id.appearance_title);
        audio_title = (TextView) findViewById(R.id.audio_title);
        other_title = (TextView) findViewById(R.id.other_title);
        disable_ads = (CheckBox) findViewById(R.id.disable_ads);
        dark_mode = (CheckBox) findViewById(R.id.dark_mode);
        disable_anim = (CheckBox) findViewById(R.id.disable_anim);
        background_play = (CheckBox) findViewById(R.id.background_play);
        capture_error = (CheckBox) findViewById(R.id.capture_error);
        clear_data = (Button) findViewById(R.id.clear_data);
        title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
        general_title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
        appearance_title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
        audio_title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);
        other_title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/roboto_medium.ttf"), Typeface.NORMAL);

        if (FileUtil.doesExists(FileUtil.getPackageDir(this).concat("/user/settings.pref")) && FileUtil.isFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"))) {
            settingsData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"));
        } else {
            settingsData = new HashMap<>();
        }
        if (settingsData.containsKey("settingsAds")) {
            if (settingsData.get("settingsAds").equals("false")) {
                disable_ads.setChecked(true);
            }
        }
        if (settingsData.containsKey("settingsDarkMode")) {
            if (settingsData.get("settingsDarkMode").equals("true")) {
                dark_mode.setChecked(true);
            }
        }
        if (settingsData.containsKey("settingsAnimation")) {
            if (settingsData.get("settingsAnimation").equals("false")) {
                disable_anim.setChecked(true);
            }
        }
        if (settingsData.containsKey("settingsBackgroundAudio")) {
            if (settingsData.get("settingsBackgroundAudio").equals("true")) {
                background_play.setChecked(true);
            }
        }
        if (settingsData.containsKey("settingsCaptureError")) {
            if (settingsData.get("settingsCaptureError").equals("true")) {
                capture_error.setChecked(true);
            }
        }

        back.setOnClickListener(view -> {
            RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
            view.setBackground(rippleButton);

            startActivity(new Intent(PreferencesActivity.this, SplashActivity.class));
            finish();
        });
        disable_ads.setOnCheckedChangeListener((view, isChecked) -> {
            ApplicationUtil.toast(PreferencesActivity.this, "Ads have been removed, use the donation button.", Toast.LENGTH_SHORT);
        });
        dark_mode.setOnCheckedChangeListener((view, isChecked) -> {
            if (isChecked) {
                settingsData.put("settingsDarkMode", "true");
                FileUtil.writeStringToFile(FileUtil.getPackageDir(PreferencesActivity.this).concat("/user/settings.pref"), ListUtil.setHashMapToSharedJSON(settingsData));
                initializeLogic();
                ApplicationUtil.toast(PreferencesActivity.this, "Dark mode is enabled.", Toast.LENGTH_SHORT);
            } else {
                settingsData.put("settingsDarkMode", "false");
                FileUtil.writeStringToFile(FileUtil.getPackageDir(PreferencesActivity.this).concat("/user/settings.pref"), ListUtil.setHashMapToSharedJSON(settingsData));
                initializeLogic();
                ApplicationUtil.toast(PreferencesActivity.this,"Dark mode is disabled.", Toast.LENGTH_SHORT);
            }
        });
        disable_anim.setOnCheckedChangeListener((view, isChecked) -> {
            if (isChecked) {
                settingsData.put("settingsAnimation", "false");
                FileUtil.writeStringToFile(FileUtil.getPackageDir(PreferencesActivity.this).concat("/user/settings.pref"), ListUtil.setHashMapToSharedJSON(settingsData));
                ApplicationUtil.toast(PreferencesActivity.this, "Animations are disabled.", Toast.LENGTH_SHORT);
            } else {
                settingsData.put("settingsAnimation", "true");
                FileUtil.writeStringToFile(FileUtil.getPackageDir(PreferencesActivity.this).concat("/user/settings.pref"), ListUtil.setHashMapToSharedJSON(settingsData));
                ApplicationUtil.toast(PreferencesActivity.this, "Animations are enabled.", Toast.LENGTH_SHORT);
            }
        });
        background_play.setOnCheckedChangeListener((view, isChecked) -> {
            if (isChecked) {
                settingsData.put("settingsBackgroundAudio", "true");
                FileUtil.writeStringToFile(FileUtil.getPackageDir(PreferencesActivity.this).concat("/user/settings.pref"), ListUtil.setHashMapToSharedJSON(settingsData));
                ApplicationUtil.toast(PreferencesActivity.this, "Audio will be played while in background.", Toast.LENGTH_SHORT);
            } else {
                settingsData.put("settingsBackgroundAudio", "false");
                FileUtil.writeStringToFile(FileUtil.getPackageDir(PreferencesActivity.this).concat("/user/settings.pref"), ListUtil.setHashMapToSharedJSON(settingsData));
                ApplicationUtil.toast(PreferencesActivity.this, "Audio will NOT be played while in background.", Toast.LENGTH_SHORT);
            }
        });
        capture_error.setOnCheckedChangeListener((view, isChecked) -> {
            if (isChecked) {
                settingsData.put("settingsCaptureError", "true");
                FileUtil.writeStringToFile(FileUtil.getPackageDir(PreferencesActivity.this).concat("/user/settings.pref"), ListUtil.setHashMapToSharedJSON(settingsData));
                ApplicationUtil.toast(PreferencesActivity.this, "Capturing errors are enabled.", Toast.LENGTH_SHORT);
            } else {
                settingsData.put("settingsCaptureError", "false");
                FileUtil.writeStringToFile(FileUtil.getPackageDir(PreferencesActivity.this).concat("/user/settings.pref"), ListUtil.setHashMapToSharedJSON(settingsData));
                ApplicationUtil.toast(PreferencesActivity.this, "Capturing errors are disabled.", Toast.LENGTH_SHORT);
            }
        });

        initializeLogic();
    }

    private void initializeLogic() {
        if (!settingsData.containsKey("settingsDarkMode")) {
            main.setBackgroundColor(Color.parseColor("#FFFFFF"));
            main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
            getWindow().setNavigationBarColor(Color.parseColor("#FFFFFF"));
            disable_ads.setTextColor(Color.parseColor("#000000"));
            dark_mode.setTextColor(Color.parseColor("#000000"));
            disable_anim.setTextColor(Color.parseColor("#000000"));
            background_play.setTextColor(Color.parseColor("#000000"));
            capture_error.setTextColor(Color.parseColor("#000000"));
        } else {
            if (settingsData.get("settingsDarkMode").equals("true")) {
                if (main.getSystemUiVisibility() == View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) {
                    main.setSystemUiVisibility(0);
                }
                getWindow().setStatusBarColor(Color.parseColor("#1A1A1A"));
                getWindow().setNavigationBarColor(Color.parseColor("#1A1A1A"));
                main.setBackgroundColor(Color.parseColor("#1A1A1A"));
                disable_ads.setTextColor(Color.parseColor("#FFFFFF"));
                dark_mode.setTextColor(Color.parseColor("#FFFFFF"));
                disable_anim.setTextColor(Color.parseColor("#FFFFFF"));
                background_play.setTextColor(Color.parseColor("#FFFFFF"));
                capture_error.setTextColor(Color.parseColor("#FFFFFF"));
            } else {
                main.setBackgroundColor(Color.parseColor("#FFFFFF"));
                main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
                getWindow().setNavigationBarColor(Color.parseColor("#FFFFFF"));
                disable_ads.setTextColor(Color.parseColor("#000000"));
                dark_mode.setTextColor(Color.parseColor("#000000"));
                disable_anim.setTextColor(Color.parseColor("#000000"));
                background_play.setTextColor(Color.parseColor("#000000"));
                capture_error.setTextColor(Color.parseColor("#000000"));
            }
        }
        GradientDrawable gradientButton = new GradientDrawable();
        gradientButton.setColor(Color.parseColor("#03A9F4"));
        gradientButton.setCornerRadius(10);
        clear_data.setBackground(gradientButton);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(PreferencesActivity.this, SplashActivity.class));
        finish();
    }

}
