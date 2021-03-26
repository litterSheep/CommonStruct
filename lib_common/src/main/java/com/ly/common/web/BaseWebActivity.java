package com.ly.common.web;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.ly.common.R;
import com.ly.common.frame.BaseActivity;
import com.ly.common.frame.Constants;
import com.ly.common.utils.CommonUtils;
import com.ly.common.view.SmoothProgressBar;
import com.orhanobut.logger.Logger;

/**
 * web界面基类
 * Created by ly on 2017/8/24 10:09.
 */
public abstract class BaseWebActivity extends BaseActivity {

    private CommonWebView wv;
    private CustomWebChromeClient webChromeClient;
    private CustomDownloadListener customDownloadListener;
    private SmoothProgressBar pb_web;
    protected String url, title;
    private ViewGroup root_view;
    private View error_view;
    private boolean hasReceivedError;
    private CustomWebViewClient customWebViewClient;

    @Override
    protected String getPageTitle() {
        return null;
    }

    @Override
    public void initViews() {
        Intent intent = getIntent();
        if (intent.hasExtra(Constants.INTENT_KEY_URL))
            url = intent.getStringExtra(Constants.INTENT_KEY_URL);
        Logger.i(">>>>>>open url:" + url);
//        if (TextUtils.isEmpty(url)) {
//            finish();
//            return;
//        }

        //url = "https://www-demo.dianrong.com/mkt/bor_mojie_sign/index.html?callbackUrl=http://192.168.1.16:8080/loanApi/signCallback&token=eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJDSEFOTkVMX0FVVEhFTlRJQ0FUSU9OIiwiQVBQX0lEIjoiNDI3MTgwMjc0MCIsImlzcyI6IkFVVEhfU0VSVkVSIiwiZXhwIjoxNTEwNzEwNzM5LCJpYXQiOjE1MTA2MjQzMzksIkNIQU5ORUxfQVBQX05BTUUiOiLoi4_lt57otYTml5fnvZHnu5znp5HmioDmnInpmZDlhazlj7giLCJqdGkiOiI2NDM1NjNmZi01ODAzLTRiZDYtYTZlZC1iM2ZhM2UxYzU2NzIifQ.N9jF9-YIcvWTs96hbdQ6sY8aG64Y2LyM-jsxGVshQLMlcn-Zw7oBPAa4N2F4BPIg2G7ClkYXXHpjRROBXD_fZSFvikQOfw3RPBI6mPwGztT1K7ug3Sej8BvXiQjAujdFZCYrshaJIM-e8nfY8YMqSOyBYSi2q1shRLKujwF-UIw&appId=4271802740&borrowerId=15960970#/?_k=xmrnfb";
        //测试文件选择
//        url = "file:///android_asset/test.html";
//        url = "https://h.sinaif.com/html/activity/promotion/middlePage/ABTLoading-dw.html?code=O4CVX";

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        //解决网页中的视频，上屏幕的时候，可能出现闪烁的情况
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initTopTitle();
        initWebView();
    }

    private void initWebView() {
        root_view = findViewById(R.id.ll_web);
        wv = findViewById(R.id.web_view);
        pb_web = findViewById(R.id.pb_web);
        if (TextUtils.isEmpty(url))
            pb_web.setVisibility(View.GONE);
        webChromeClient = new CustomWebChromeClient(this) {
            /**
             * 解析网页标题后的回调
             * Created by ly on 2017/9/26 9:54
             */
            @Override
            public void onReceivedTitle(WebView webView, String s) {
                super.onReceivedTitle(webView, s);
                setTopTitle(s);
            }
        };
        customWebViewClient = new CustomWebViewClient(wv) {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                hasReceivedError = false;
                startLoading();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setLeftCloseBtn();
                stopLoading();
                if (!hasReceivedError) {
                    if (error_view != null)
                        error_view.setVisibility(View.GONE);
                    if (wv != null)
                        wv.setVisibility(View.VISIBLE);
                }
                onPageFinish();
            }

            @Override
            public void onReceivedError(WebView webView, int i, String s, String s1) {
                super.onReceivedError(webView, i, s, s1);
                hasReceivedError = true;
                showErrorPage();
            }
        };
        customDownloadListener = new CustomDownloadListener(this);
        wv.setWebChromeClient(webChromeClient);
        wv.setWebViewClient(customWebViewClient);
        wv.setDownloadListener(customDownloadListener);
        wv.getSettings().setLoadsImagesAutomatically(true);

        wv.loadUrl(url);

        AndroidBug5497Workaround.assistActivity(this);
    }

    public void startLoading() {
        if (pb_web != null)
            pb_web.start();
    }

    public void stopLoading() {
        if (pb_web != null)
            pb_web.stop();
    }

    private void initTopTitle() {
        if (topTitleBar == null)
            return;
        topTitleBar.setTitleText(title);
        topTitleBar.setLeftCloseBtnImg(R.mipmap.title_close);
        topTitleBar.adjustStyle4WV();
        topTitleBar.setOnLeftClickListener(() -> {
            if (wv != null && wv.canGoBack()) {
                wv.goBack();
            } else {
                finish();
            }
        });
        topTitleBar.setOnLeftCloseClickListener(this::finish);
    }

    public WebView getWebView() {
        return wv;
    }

    /**
     * 设置页面标题
     *
     * @param title 网页中解析的标题
     *              Created by ly on 2017/9/26 9:51
     */
    public void setTopTitle(String title) {
        if (topTitleBar != null) {
            setLeftCloseBtn();
            if (TextUtils.isEmpty(title)) {
                topTitleBar.setTitleText(this.title);
            } else {
                topTitleBar.setTitleText(title);
            }
        }
    }

    private void setLeftCloseBtn() {
        if (wv != null) {
            if (wv.canGoBack()) {//可以返回则添加关闭按钮
                if (!topTitleBar.isShowLeftCloseBtn())
                    topTitleBar.showLeftCloseBtn(true);
            } else {
                if (topTitleBar.isShowLeftCloseBtn())
                    topTitleBar.showLeftCloseBtn(false);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //从web页选择文件/拍照的处理
        if (requestCode == CustomWebChromeClient.FILECHOOSER_CHOOSERFILE
                || requestCode == CustomWebChromeClient.FILECHOOSER_TAKEPICTURE) {
            Uri result;
            if (requestCode == CustomWebChromeClient.FILECHOOSER_CHOOSERFILE) {
                result = data == null ? null : data.getData();
            } else {
                result = CommonUtils.getUriForFile(webChromeClient.getTakePicFile());
            }

            ValueCallback<Uri> uploadFile = webChromeClient.getUploadFile();
            ValueCallback<Uri[]> uploadFiles = webChromeClient.getUploadFiles();
            if (null != uploadFile) {
                uploadFile.onReceiveValue(result);
                webChromeClient.setUploadFile(null);
            }
            if (null != uploadFiles) {
                uploadFiles.onReceiveValue(result == null ? null : new Uri[]{result});
                webChromeClient.setUploadFiles(null);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (wv != null && wv.canGoBack()) {
                wv.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause() {
        super.onPause();
        wv.onPause();//暂停部分可安全处理的操作，如动画，定位，视频播放等
        wv.pauseTimers();//暂停所有WebView的页面布局、解析以及JavaScript的定时器操作
    }

    @Override
    protected void onResume() {
        super.onResume();
        wv.onResume();
        wv.resumeTimers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wv != null) {
            wv.setWebViewClient(null);
            wv.setWebChromeClient(null);
            wv.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            wv.clearHistory();
            if (root_view != null)
                root_view.removeView(wv);
            wv.removeAllViews();
            wv.destroy();
            wv = null;
        }
        if (pb_web != null)
            pb_web.release();
        if (webChromeClient != null) {
            webChromeClient.setUploadFile(null);
            webChromeClient.setUploadFiles(null);
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void onPageFinish() {

    }

    public void load(String url) {
        if (wv != null)
            wv.loadUrl(url);
    }

    public void reLoad() {
        if (wv != null)
            wv.reload();
    }

    public void showErrorPage() {
        if (error_view == null) {
            error_view = LayoutInflater.from(this).inflate(R.layout.layout_web_load_fail, null);
            if (root_view != null) {
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                root_view.addView(error_view, lp);
            }
        } else {//证明之前添加过该页面  直接展示即可
            error_view.setVisibility(View.VISIBLE);
        }
        if (wv != null)
            wv.setVisibility(View.GONE);
        if (pb_web != null)
            pb_web.setVisibility(View.GONE);
    }

    public CustomWebChromeClient getWebChromeClient() {
        return webChromeClient;
    }

    public CustomWebViewClient getCustomWebViewClient() {
        return customWebViewClient;
    }
}
