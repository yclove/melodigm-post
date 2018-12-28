package com.melodigm.post.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.melodigm.post.R;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.InstagramApp;
import com.melodigm.post.util.LogUtil;

public class InstagramDialog extends Dialog {
    static final float[] DIMENSIONS_LANDSCAPE = { 460, 260 };
    static final float[] DIMENSIONS_PORTRAIT = { 280, 420 };
    static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
    static final int MARGIN = 4;
    static final int PADDING = 2;
    private String mUrl;
    private OAuthDialogListener mListener;
    private ProgressDialog mProgressDialog = new ProgressDialog();
    private android.app.ProgressDialog mSpinner;
    private WebView mWebView;
    private LinearLayout mContent;
    private Context mContext;
    private TextView mTitle;
    public InstagramDialog(Context context, String url, OAuthDialogListener listener) {
        super(context, R.style.DialogTheme);
        mUrl = url;
        mListener = listener;
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mProgressDialog != null) {
            if (!mProgressDialog.isShow()) mProgressDialog.showDialog(mContext);
        }
        mContent = new LinearLayout(getContext());
        mContent.setOrientation(LinearLayout.VERTICAL);
        setUpTitle();
        setUpWebView();
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        final float scale = getContext().getResources().getDisplayMetrics().density;
        float[] dimensions = (display.getWidth() < display.getHeight()) ? DIMENSIONS_PORTRAIT : DIMENSIONS_LANDSCAPE;
        //addContentView(mContent, new FrameLayout.LayoutParams( (int) (dimensions[0] * scale + 0.5f), (int) (dimensions[1] * scale + 0.5f)));
        addContentView(mContent, new FrameLayout.LayoutParams(DeviceUtil.getScreenWidthInPXs(mContext), DeviceUtil.getScreenHeightInPXs(mContext)));
        CookieSyncManager.createInstance(getContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    private void setUpTitle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mTitle = new TextView(getContext());
        mTitle.setText("Instagram");
        mTitle.setTextColor(Color.WHITE);
        mTitle.setTypeface(Typeface.DEFAULT_BOLD);
        mTitle.setBackgroundColor(Color.BLACK);
        mTitle.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
        mTitle.setVisibility(View.GONE);
        mContent.addView(mTitle);
    }

    private void setUpWebView() {
        mWebView = new WebView(getContext());
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new OAuthWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(mUrl);
        mWebView.setLayoutParams(FILL);
        mContent.addView(mWebView);
    }

    private class OAuthWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            LogUtil.e("Redirecting URL " + url);
            if (url.startsWith(InstagramApp.mCallbackUrl)) {
                String urls[] = url.split("=");
                mListener.onComplete(urls[1]);
                InstagramDialog.this.dismiss();
                return true;
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            LogUtil.e("Page error: " + description);
            super.onReceivedError(view, errorCode, description, failingUrl);
            mListener.onError(description); InstagramDialog.this.dismiss();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            LogUtil.e("Loading URL: " + url);
            super.onPageStarted(view, url, favicon);
            if(mProgressDialog != null) {
                if (!mProgressDialog.isShow()) mProgressDialog.showDialog(mContext);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String title = mWebView.getTitle();
            if (mTitle != null) {
                if (title != null && title.length() > 0) {
                    mTitle.setText(title);
                }
            }
            LogUtil.e("onPageFinished URL: " + url);
            if(mProgressDialog != null) {
                if (mProgressDialog.isShow()) mProgressDialog.dissDialog();
            }
        }
    }

    public interface OAuthDialogListener {
        public abstract void onComplete(String accessToken);
        public abstract void onError(String error);
    }
}