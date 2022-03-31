package com.grapecandy.view.webview;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.grapecandy.R;

public class InAppBrowser extends AppCompatActivity {

    private static final String EXTRA_URL = "extra.url";
    private static final String LOG_TAG = "InAppBrowser";

    private Animation startAnim;
    private String url;
    private WebView webView;
    private Toolbar toolbar;
    private CoordinatorLayout inappbrowserlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_browser);

        // The act of intelligently selecting the HTML part to be drawn in WebView to reduce memory space and increase performance.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw();
        }

        toolbar = findViewById(R.id.toolbar);
        webView = findViewById(R.id.webview);
        inappbrowserlayout = findViewById(R.id.inappbrowserlayout);

        startAnim = AnimationUtils.loadAnimation(this, R.anim.flow_up);     // Animation that comes up from the bottom.
        url = getIntent().getStringExtra(EXTRA_URL);                               // Receive url delivered from Intent.

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //clearCookies(this);
        webView.clearCache(true);
        webView.clearHistory();
        webView.setWebViewClient(new InAppBrowserWebViewClient());                 // Configure WebViewClient.
        webView.setNetworkAvailable(true);

        // Using hardware/software acceleration because WebView loads too slowly.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        // NOTE: Software layers should be avoided when the affected view tree updates often.
        // Every update will require to re-render the software layer, which can potentially be slow
//        else {
//            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        WebSettings webSettings = webView.getSettings();
        // android webview "TypeError: Cannot read properties of null (reading 'getItem')", source:
        webSettings.setDomStorageEnabled(true);                                     // Sets whether the DOM storage API is enabled.
        webSettings.setJavaScriptEnabled(true);                                     // Allow JavaScript.
        webSettings.setSupportZoom(true);                                           // Allow zoom.
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);                        // Cash is not allowed.
        webSettings.setUseWideViewPort(true);                                       // WebView supports html viewport meta-tag.
        webSettings.setBuiltInZoomControls(true);                                   // A built-in expansion/reduction mechanism is available.
        webSettings.setGeolocationEnabled(true);                                    // Allow geographic location activation.
        webSettings.setDatabaseEnabled(true);                                       // Allow database storage APIs.
        webSettings.setLoadWithOverviewMode(true);                                  // Allow customized screen size when content is larger than WebView.

        inappbrowserlayout.startAnimation(startAnim);                              // Anime starts from the bottom up.
        webView.loadUrl(url);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        // NOTE: clears RAM cache, if you pass true, it will also clear the disk cache.
        // Probably not a great idea to pass true if you have other WebViews still alive.
        clearCookies(this);
        webView.clearCache(true);

        //webView.loadUrl("about:blank");
        webView.clearFormData();
        webView.clearHistory();
        webView.removeAllViews();
        webView.destroyDrawingCache();
        webView.destroy();

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public class InAppBrowserWebViewClient extends android.webkit.WebViewClient
    {
        // At the beginning of loading
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        // Multiple calls while loading resources.
        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        // When updating the history of your visit.
        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            super.doUpdateVisitedHistory(view, url, isReload);
        }

        // Called once when loading is complete.
        @Override
        public void onPageFinished(WebView view, String url) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("result_url", url);
            setResult(RESULT_OK, resultIntent);
            super.onPageFinished(view, url);
        }

        // If there is an error, the error cannot be multiple.
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            String errorString;
            switch (errorCode) {
                case ERROR_AUTHENTICATION:// User authentication failed on the server.
                    errorString = "ERROR_AUTHENTICATION";
                    break;
                case ERROR_BAD_URL:// Invalid URL.
                    errorString = "ERROR_BAD_URL";
                    break;
                case ERROR_CONNECT:// Connection to server failed.
                    errorString = "ERROR_CONNECT";
                    break;
                case ERROR_FAILED_SSL_HANDSHAKE:// SSL handshake execution failed.
                    errorString = "ERROR_FAILED_SSL_HANDSHAKE";
                    break;
                case ERROR_FILE:// General file error.
                    errorString = "ERROR_FILE";
                    break;
                case ERROR_FILE_NOT_FOUND:// Can't find the file.
                    errorString = "ERROR_FILE_NOT_FOUND";
                    break;
                case ERROR_HOST_LOOKUP:// Server or proxy host name lookup failed.
                    errorString = "ERROR_HOST_LOOKUP";
                    break;
                case ERROR_IO:// Failed to read from server or write to server.
                    errorString = "ERROR_IO";
                    break;
                case ERROR_PROXY_AUTHENTICATION:// Proxy user authentication failed.
                    errorString = "ERROR_PROXY_AUTHENTICATION";
                    break;
                case ERROR_REDIRECT_LOOP:// Too many redirects.
                    errorString = "ERROR_REDIRECT_LOOP";
                    break;
                case ERROR_TIMEOUT:// Connection time out.
                    errorString = "ERROR_TIMEOUT";
                    break;
                case ERROR_TOO_MANY_REQUESTS:// Too many requests occurred while loading the page.
                    errorString = "ERROR_TOO_MANY_REQUESTS";
                    break;
                case ERROR_UNKNOWN:// General error
                    errorString = "ERROR_UNKNOWN";
                    break;
                case ERROR_UNSUPPORTED_AUTH_SCHEME:// Unsupported authentication system.
                    errorString = "ERROR_UNSUPPORTED_AUTH_SCHEME";
                    break;
                case ERROR_UNSUPPORTED_SCHEME:// Unsupported URI.
                    errorString = "ERROR_UNSUPPORTED_SCHEME";
                    break;
                default:
                    errorString = "Unknown Error..";
                    break;
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("errorCode", errorCode);
            resultIntent.putExtra("failingUrl", failingUrl);
            resultIntent.putExtra("errorString", errorString);
            resultIntent.putExtra("description", description);
            setResult(RESULT_CANCELED, resultIntent);
        }

        // If there is an http authentication request, the existing operation cancels the request.
        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
        }

        // If there is a change in size or enlargement.
        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
        }

        // If there is an incorrect key input.
        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }

        // If a new URL tries to load into the webview, it gives you a chance to replace the control.
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return false;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading (WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return false;
        }
    }

    // Resolved the phenomenon that the previous screen remains the same even after WebView ends.
    public static void clearCookies(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();

            // Callback
            /*CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookies(new ValueCallback() {
                @Override
                public void onReceiveValue(Boolean value) {
                    Log.d("onReceiveValue", value.toString());
                }
            });
            cookieManager.getInstance().flush();*/
        }
        else {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
            cookieSyncManager.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncManager.stopSync();
            cookieSyncManager.sync();
        }
    }
}