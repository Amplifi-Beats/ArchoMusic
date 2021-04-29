package tk.gianxddddd.audiodev.activity;

import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

import java.util.HashMap;
import java.util.Map;

import tk.gianxddddd.audiodev.R;
import tk.gianxddddd.audiodev.util.FileUtil;
import tk.gianxddddd.audiodev.util.ListUtil;

public class ExternalBrowserActivity extends AppCompatActivity {

    private HashMap<String, Object> settingsData;

    private WebViewClient webViewClient;
    private WebChromeClient webChromeClient;
    private WebSettings webSettings;

    private LinearLayout toolbar;
    private ProgressBar loadbar;
    private WebView web;
    private ImageView back;
    private ImageView options;
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
        toolbar = (LinearLayout) findViewById(R.id.toolbar);
        loadbar = (ProgressBar) findViewById(R.id.loadbar);
        back = (ImageView) findViewById(R.id.back);
        options = (ImageView) findViewById(R.id.options);
        web = (WebView) findViewById(R.id.web);
        webSettings = web.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setSupportZoom(true);
        CookieManager.getInstance().setAcceptCookie(true);

        webtitle = findViewById(R.id.webtitle);
        weburl = findViewById(R.id.weburl);

        if (FileUtil.doesExists(FileUtil.getPackageDir(this).concat("/user/settings.pref")) && FileUtil.isFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"))) {
            settingsData = ListUtil.getHashMapFromFile(FileUtil.getPackageDir(this).concat("/user/settings.pref"));
        } else {
            settingsData = new HashMap<>();
        }

        webViewClient = new WebViewClient() {

            @Override
            public void onPageStarted(WebView webView, String url, Bitmap favicon) {
                super.onPageStarted(webView, url, favicon);

                loadbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView webView, String url) {
                super.onPageFinished(webView, url);

                loadbar.setVisibility(View.GONE);
            }
        };

        webChromeClient = new WebChromeClient() {
            private View customView;
            private WebChromeClient.CustomViewCallback customViewCallback;

            private int originalOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            private int originalSystemUiVisibility;

            @Override
            public Bitmap getDefaultVideoPoster() {
                if (ExternalBrowserActivity.this == null) {
                    return null;
                }
                return BitmapFactory.decodeResource(ExternalBrowserActivity.this.getApplicationContext().getResources(), 2130837573);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                if (Build.VERSION.SDK_INT >= 24) {
                    loadbar.setProgress(newProgress, true);
                } else {
                    loadbar.setProgress(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);

                webtitle.setText(title);
                weburl.setText(view.getUrl());
            }

            @Override
            public void onShowCustomView(View view, WebChromeClient.CustomViewCallback viewCallback) {
                super.onShowCustomView(view, viewCallback);

                if (customView != null) {
                    onHideCustomView();
                    return;
                }

                customView = view;
                originalSystemUiVisibility = ExternalBrowserActivity.this.getWindow().getDecorView().getSystemUiVisibility();
                ExternalBrowserActivity.this.setRequestedOrientation(originalOrientation);
                originalOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                customViewCallback = viewCallback;
                ((FrameLayout) ExternalBrowserActivity.this.getWindow().getDecorView()).addView(customView, new FrameLayout.LayoutParams(-1, -1));
                ExternalBrowserActivity.this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }

            @Override
            public void onHideCustomView() {
                super.onHideCustomView();

                ((FrameLayout) ExternalBrowserActivity.this.getWindow().getDecorView()).removeView(customView);
                customView = null;
                ExternalBrowserActivity.this.getWindow().getDecorView().setSystemUiVisibility(originalSystemUiVisibility);
                ExternalBrowserActivity.this.setRequestedOrientation(originalOrientation);
                originalOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                customViewCallback.onCustomViewHidden();
                customViewCallback = null;
            }

        };

        web.setWebViewClient(webViewClient);
        web.setWebChromeClient(webChromeClient);

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
        loadbar.setIndeterminate(false);
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
