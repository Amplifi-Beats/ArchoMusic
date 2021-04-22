package tk.gianxddddd.audiodev.activity;

import android.app.ActivityOptions;
import android.content.Intent;
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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import tk.gianxddddd.audiodev.R;
import tk.gianxddddd.audiodev.util.FileUtil;
import tk.gianxddddd.audiodev.util.ListUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.FirebaseApp;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    final Timer timer = new Timer();

    HashMap<String, Object> settingsData;

    LinearLayout mainLayout;
    TextView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initialize();

        FirebaseApp.initializeApp(this);
        initializeLogic();
    }

    private void initialize() {
        mainLayout = findViewById(R.id.mainLayout);
        logo = findViewById(R.id.logo);
    }

    private void initializeLogic() {
        logo.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/leixo.ttf"), Typeface.BOLD);

        if (FileUtil.doesExists(FileUtil.getPackageDir(this).concat("/user/settings.pref")) && FileUtil.isFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"))) {
            settingsData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"));
        } else {
            settingsData = new HashMap<>();
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (settingsData.containsKey("settingsDarkMode")) {
                if (!settingsData.get("settingsDarkMode").equals("true")) {
                    mainLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
                    getWindow().setNavigationBarColor(Color.parseColor("#FFFFFF"));

                } else {
                    setTheme(R.style.Theme_ArchoMusic_Dark);

                    mainLayout.setBackgroundColor(Color.parseColor("#1A1A1A"));
                    logo.setTextColor(Color.parseColor("#03A9F4"));

                    getWindow().setStatusBarColor(Color.parseColor("#1A1A1A"));
                    getWindow().setNavigationBarColor(Color.parseColor("#1A1A1A"));
                }

            } else {
                mainLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
                getWindow().setNavigationBarColor(Color.parseColor("#FFFFFF"));
            }

        } else {
            getWindow().setStatusBarColor(Color.parseColor("#000000"));
            getWindow().setNavigationBarColor(Color.parseColor("#000000"));
        }

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (FileUtil.doesExists(FileUtil.getPackageDir(SplashActivity.this).concat("/user/crash.log")) && FileUtil.isFile(FileUtil.getPackageDir(SplashActivity.this).concat("/user/crash.log"))) {
                        BottomSheetDialog errorDialog = new BottomSheetDialog(SplashActivity.this);
                        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_debug, null);
                        errorDialog.setContentView(dialogLayout);

                        TextView title = dialogLayout.findViewById(R.id.title);
                        TextView log = dialogLayout.findViewById(R.id.log);
                        Button close = dialogLayout.findViewById(R.id.close);

                        title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/roboto_medium.ttf"), Typeface.NORMAL);
                        log.setText(FileUtil.readFile(FileUtil.getPackageDir(SplashActivity.this).concat("/user/crash.log")));

                        close.setOnClickListener(view -> {
                            if (!settingsData.containsKey("settingsDarkMode")) {
                                RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor("#BDBDBD")}), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                                view.setBackground(rippleButton);
                            } else {
                                if (settingsData.get("settingsDarkMode").equals("true")) {
                                    RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor("#BDBDBD")}), new ColorDrawable(Color.parseColor("#1A1A1A")), null);
                                    view.setBackground(rippleButton);
                                } else {
                                    RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor("#BDBDBD")}), new ColorDrawable(Color.parseColor("#FFFFFF")), null);
                                    view.setBackground(rippleButton);
                                }
                            }

                            FileUtil.deleteFile(FileUtil.getPackageDir(SplashActivity.this).concat("/user/crash.log"));

                            errorDialog.dismiss();

                            if (settingsData.containsKey("settingsAnimation")) {
                                if (settingsData.get("settingsAnimation").equals("true")) {
                                    Intent intent = new Intent();
                                    intent.setClass(SplashActivity.this, LocalStreamActivity.class);

                                    logo.setTransitionName("fade");
                                    ActivityOptions optionsCompat = ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this, logo, "fade");

                                    startActivity(intent, optionsCompat.toBundle());
                                } else {
                                    startActivity(new Intent(SplashActivity.this, LocalStreamActivity.class));
                                }

                            } else {
                                Intent intent = new Intent();
                                intent.setClass(SplashActivity.this, LocalStreamActivity.class);

                                logo.setTransitionName("fade");
                                ActivityOptions optionsCompat = ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this, logo, "fade");

                                startActivity(intent, optionsCompat.toBundle());
                            }
                        });

                        float TopLeft = 20.0f;
                        float TopRight = 20.0f;
                        float BottomRight = 0.0f;
                        float BottomLeft = 0.0f;

                        GradientDrawable roundedCorners = new GradientDrawable();
                        roundedCorners.setShape(GradientDrawable.RECTANGLE);
                        roundedCorners.setCornerRadii(new float[]{TopLeft, TopLeft, TopRight, TopRight, BottomRight, BottomRight, BottomLeft, BottomLeft});

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

                                log.setTextColor(Color.parseColor("#FFFFFF"));
                                log.setHintTextColor(Color.parseColor("#BDBDBD"));

                            } else {
                                roundedCorners.setColor(Color.parseColor("#FFFFFF"));
                                roundedCorners2.setColor(Color.parseColor("#EEEEEE"));
                            }
                        }

                        ((ViewGroup) dialogLayout.getParent()).setBackground(roundedCorners);

                        log.setBackground(roundedCorners2);

                        GradientDrawable gradientButton = new GradientDrawable();
                        gradientButton.setColor(Color.parseColor("#03A9F4"));
                        gradientButton.setCornerRadius(20);

                        close.setBackground(gradientButton);

                        errorDialog.setCancelable(false);
                        errorDialog.show();

                    } else {
                        if (settingsData.containsKey("settingsAnimation")) {
                            if (settingsData.get("settingsAnimation").equals("true")) {
                                Intent intent = new Intent();
                                intent.setClass(SplashActivity.this, LocalStreamActivity.class);

                                logo.setTransitionName("fade");
                                ActivityOptions optionsCompat = ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this, logo, "fade");
                                startActivity(intent, optionsCompat.toBundle());

                            } else {
                                startActivity(new Intent(SplashActivity.this, LocalStreamActivity.class));
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.setClass(SplashActivity.this, LocalStreamActivity.class);
                            logo.setTransitionName("fade");
                            ActivityOptions optionsCompat = ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this, logo, "fade");
                            startActivity(intent, optionsCompat.toBundle());
                        }
                    }
                });
            }
        };

        timer.schedule(timerTask, 1500);
    }
}
