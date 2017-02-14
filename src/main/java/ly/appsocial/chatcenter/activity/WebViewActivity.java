package ly.appsocial.chatcenter.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import ly.appsocial.chatcenter.R;
import ly.appsocial.chatcenter.constants.ChatCenterConstants;
import ly.appsocial.chatcenter.util.StringUtil;

public class WebViewActivity extends BaseActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");

        mWebView = (WebView) findViewById(R.id.webview);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setSupportZoom(false);
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.setWebViewClient(new MyWebViewClient());

        String url = getIntent().getStringExtra(ChatCenterConstants.Extra.URL);
        String activityTitle = getIntent().getStringExtra(ChatCenterConstants.Extra.ACTIVITY_TITLE);
        if (StringUtil.isBlank(url)) {
            // URL not provided, we don't need to open this.
            finish();
            return;
        }

        if (!StringUtil.isBlank(activityTitle)) {
            getSupportActionBar().setTitle(activityTitle);
        }

        mWebView.loadUrl(url);
    }

    // url = file path or whatever suitable URL you want.
    private static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

	private ProgressBar getProgressBar(){
		return (ProgressBar)findViewById(R.id.progress);
	}

    // //////////////////////////////////////////////////////////
    // Inner class
    // //////////////////////////////////////////////////////////

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
			getProgressBar().setProgress(newProgress);
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if(StringUtil.isBlank(getSupportActionBar().getTitle().toString())) {
                getSupportActionBar().setTitle(title);
            }
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (getMimeType(url) != null && getMimeType(url).equals("application/pdf")) {
                url = "https://docs.google.com/gview?url=" + url;
                view.loadUrl(url);
                return false;
            }
            return super.shouldOverrideUrlLoading(view,url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
			getProgressBar().setProgress(0);
			getProgressBar().setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
			getProgressBar().setProgress(100);
			getProgressBar().setVisibility(View.INVISIBLE);
            super.onPageFinished(view, url);
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
