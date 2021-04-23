package tk.gianxddddd.audiodev.activity;

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

import tk.gianxddddd.audiodev.R;
import tk.gianxddddd.audiodev.util.ApplicationUtil;
import tk.gianxddddd.audiodev.util.FileUtil;
import tk.gianxddddd.audiodev.util.ListUtil;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.HashMap;

public class LyricsEditorActivity extends  AppCompatActivity  {

    ArrayList<HashMap<String, Object>> musicData;
    HashMap<String, Object> settingsData;

    LinearLayout toolbar;
    EditText lyrics;
    ImageView back;
    TextView bruh;
    ImageView autoedit;
    ImageView save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics_editor);

        initialize();
        FirebaseApp.initializeApp(this);
        initializeLogic();
    }

    private void initialize() {
        toolbar = findViewById(R.id.toolbar);
        lyrics = findViewById(R.id.lyrics);
        back = findViewById(R.id.back);
        bruh = findViewById(R.id.bruh);
        autoedit = findViewById(R.id.autoedit);
        save = findViewById(R.id.save);

        if (FileUtil.doesExists(FileUtil.getPackageDir(this).concat("/song.json")) && FileUtil.isFile(FileUtil.getPackageDir(this).concat("/song.json"))) {
            musicData = ListUtil.getArrayListFromFile(FileUtil.getPackageDir(this).concat("/song.json"));
        } else {
            musicData = new ArrayList<>();
        }

        if (FileUtil.doesExists(FileUtil.getPackageDir(this).concat("/user/settings.pref")) && FileUtil.isFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"))) {
            settingsData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"));
        } else {
            settingsData = new HashMap<>();
        }

        lyrics.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) { }

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
            public void afterTextChanged(Editable editable) { }
        });

        back.setOnClickListener(view -> {
            RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
            back.setBackground(rippleButton);
            finish();
        });

        autoedit.setOnClickListener(view -> {
            RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
            autoedit.setBackground(rippleButton);
        });

        save.setOnClickListener(view -> {
            RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor("#BDBDBD") }), null, null);
            save.setBackground(rippleButton);
            try {
                musicData.get(Integer.parseInt(getIntent().getStringExtra("songPosition"))).put("songLyrics", lyrics.getText().toString());
                FileUtil.writeStringToFile(FileUtil.getPackageDir(this).concat("/song.json"), ListUtil.setArrayListToSharedJSON(musicData));
                ApplicationUtil.toast(this, "Lyrics saved successfully.", Toast.LENGTH_SHORT);
                finish();
            } catch (Exception e) {
                ApplicationUtil.toast(this, "Error saving lyrics.", Toast.LENGTH_SHORT);
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
    public void onBackPressed() {
        finish();
    }
}
