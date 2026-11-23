package com.bahlil.onehendrius;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * ONE HENDRIUS CHAT — WebView membungkus aplikasi chat (assets/app/index.html).
 * Aplikasi memakai Firebase JS SDK (Auth + Firestore + Storage) dari CDN,
 * jadi WebView harus mengizinkan akses universal dari file:// agar halaman
 * lokal boleh memanggil https://*.googleapis.com dan *.gstatic.com.
 * Login bertahan antar buka aplikasi lewat DOM storage (Firebase Auth
 * menyimpan sesi di localStorage WebView).
 * Pilih foto lewat <input type=file> butuh onShowFileChooser — tanpa ini
 * tombol foto di halaman tidak melakukan apa-apa di Android.
 */
public class MainActivity extends Activity {

    // Palet sama dengan versi server: hitam pekat + emas menyala + cyan.
    private static final int BG = 0xFF07090D;
    private static final int PANEL = 0xFF0D1117;
    private static final int GOLD = 0xFFF0B93E;
    private static final int CYAN = 0xFF38E1FF;
    private static final int TEXT = 0xFFF5F1E6;
    private static final int MUTED = 0xFF8B93A3;

    private static final int REQ_FILE_CHOOSER = 1001;

    private WebView web;
    private View splash;
    private boolean splashHidden;
    private ValueCallback<Uri[]> filePathCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(BG);
            getWindow().setNavigationBarColor(BG);
        }
        getWindow().setBackgroundDrawable(new ColorDrawable(BG));

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);

        web = new WebView(this);
        web.setBackgroundColor(BG);
        WebSettings ws = web.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setAllowContentAccess(true);
        // Halaman file:// harus boleh memanggil CDN Firebase (https).
        ws.setAllowFileAccessFromFileURLs(true);
        ws.setAllowUniversalAccessFromFileURLs(true);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view,
                                             ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }
                filePathCallback = callback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                try {
                    startActivityForResult(
                            Intent.createChooser(intent, "Pilih foto"), REQ_FILE_CHOOSER);
                } catch (Exception e) {
                    filePathCallback = null;
                    return false;
                }
                return true;
            }
        });
        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                hideSplash();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Semua navigasi tetap di dalam WebView (aplikasi satu halaman).
                return false;
            }
        });
        root.addView(web, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        splash = buildSplash();
        root.addView(splash, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // Pengaman: splash tidak boleh menahan layar bila halaman gagal muat.
        splash.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideSplash();
            }
        }, 12000);

        setContentView(root);
        web.loadUrl("file:///android_asset/app/index.html");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_FILE_CHOOSER) {
            if (filePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            Uri[] result = null;
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                result = new Uri[]{data.getData()};
            }
            filePathCallback.onReceiveValue(result);
            filePathCallback = null;
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) {
            web.goBack();
            return;
        }
        super.onBackPressed();
    }

    /** Layar pembuka: monogram OH emas + nama aplikasi + garis emas berdenyut. */
    private View buildSplash() {
        FrameLayout lay = new FrameLayout(this);
        lay.setBackgroundColor(BG);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        FrameLayout.LayoutParams colLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        colLp.gravity = Gravity.CENTER;
        lay.addView(col, colLp);

        GradientDrawable box = new GradientDrawable();
        box.setColor(PANEL);
        box.setStroke(dp(1), GOLD);
        box.setCornerRadius(dp(20));
        TextView mono = new TextView(this);
        mono.setText("OH");
        mono.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
        mono.setTypeface(Typeface.SERIF, Typeface.BOLD);
        mono.setTextColor(GOLD);
        mono.setGravity(Gravity.CENTER);
        mono.setBackground(box);
        int boxSize = dp(88);
        LinearLayout.LayoutParams monoLp = new LinearLayout.LayoutParams(boxSize, boxSize);
        monoLp.gravity = Gravity.CENTER_HORIZONTAL;
        col.addView(mono, monoLp);

        TextView title = new TextView(this);
        title.setText(R.string.app_name);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(TEXT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            title.setLetterSpacing(0.04f);
        }
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLp.topMargin = dp(18);
        titleLp.gravity = Gravity.CENTER_HORIZONTAL;
        col.addView(title, titleLp);

        GradientDrawable lineBg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.TRANSPARENT, GOLD, Color.TRANSPARENT});
        lineBg.setCornerRadius(dp(1));
        View line = new View(this);
        line.setBackground(lineBg);
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(dp(120), dp(2));
        lineLp.topMargin = dp(16);
        lineLp.gravity = Gravity.CENTER_HORIZONTAL;
        col.addView(line, lineLp);
        ObjectAnimator pulse = ObjectAnimator.ofFloat(line, "alpha", 0.35f, 1f);
        pulse.setDuration(1200);
        pulse.setRepeatMode(ObjectAnimator.REVERSE);
        pulse.setRepeatCount(ObjectAnimator.INFINITE);
        pulse.start();

        TextView loading = new TextView(this);
        loading.setText("Memuat…");
        loading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        loading.setTextColor(MUTED);
        LinearLayout.LayoutParams loadLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadLp.topMargin = dp(14);
        loadLp.gravity = Gravity.CENTER_HORIZONTAL;
        col.addView(loading, loadLp);

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(400);
        col.startAnimation(fadeIn);
        return lay;
    }

    private void hideSplash() {
        if (splashHidden || splash == null) {
            return;
        }
        splashHidden = true;
        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(350);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                splash.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        splash.startAnimation(fadeOut);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
