package com.patternjkh.ui.webcams;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.patternjkh.R;

public class WebviewCamActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_cam);

        String link = getIntent().getExtras().getString("url");

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new MyWebViewClient());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);

        webView.loadUrl(link);
    }

    private class MyWebViewClient extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            showDialog(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            showDialog(false);
        }
    }

    private void showDialog(boolean toShow) {
        if (!isFinishing() && !isDestroyed()) {
            if (toShow) {
                dialog = new ProgressDialog(this);
                dialog.setMessage("Загрузка страницы...");
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.show();
            } else {
                if (!isFinishing() && !isDestroyed()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }
        }
    }
}
