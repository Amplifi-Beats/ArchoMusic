package tk.gianxddddd.audiodev.activity;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.RippleDrawable;
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

import com.google.firebase.FirebaseApp;

import java.util.HashMap;

import tk.gianxddddd.audiodev.R;
import tk.gianxddddd.audiodev.util.FileUtil;
import tk.gianxddddd.audiodev.util.ListUtil;

public class ExternalBrowserActivity extends AppCompatActivity {

    private HashMap<String, Object> settingsData;

    private LinearLayout toolbar;
    private ProgressBar loadbar;
    private WebView web;
    private ImageView back;
    private TextView webtitle;
    private TextView weburl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_browser);

        initialize();
        FirebaseApp.initializeApp(this);
        initializeLogic();
    }

    private void initialize() {
        toolbar = findViewById(R.id.toolbar);
        loadbar = findViewById(R.id.loadbar);
        back = findViewById(R.id.back);
        web = findViewById(R.id.web);

        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setSupportZoom(true);

        webtitle = findViewById(R.id.webtitle);
        weburl = findViewById(R.id.weburl);

        if (FileUtil.doesExists(FileUtil.getPackageDir(this).concat("/user/settings.pref")) && FileUtil.isFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"))) {
            settingsData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"));
        } else {
            settingsData = new HashMap<>();
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

                } else {
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

        back.setOnClickListener(view -> {
            RippleDrawable rippleButton = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor("#BDBDBD")}), null, null);
            back.setBackground(rippleButton);

            if (web != null) {
                web.destroy();
            }

            finish();
        });
    }

    private void initializeLogic() {
        webtitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/roboto_medium.ttf"), Typeface.NORMAL);

        loadbar.setBackgroundColor(Color.parseColor("#03A9F4"));
        loadbar.setVisibility(View.GONE);
        loadbar.setElevation(10f);
        toolbar.setElevation(10f);

        if (Build.VERSION.SDK_INT >= 23) {
            if (settingsData.containsKey("settingsDarkMode")) {
                if (settingsData.get("settingsDarkMode").equals("true")) {
                    setTheme(R.style.Theme_ArchoMusic_Dark);

                    toolbar.setBackgroundColor(Color.parseColor("#1A1A1A"));
                    weburl.setTextColor(Color.parseColor("#BDBDBD"));

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

        if (getIntent().getStringExtra("url") != null) {
            web.loadUrl(getIntent().getStringExtra("url"));
        } else {
            web.loadUrl("https://www.google.com");
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
