package com.ly.common.web;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.ly.common.R;
import com.ly.common.frame.BaseFragment;
import com.ly.common.utils.CommonUtils;
import com.ly.common.view.SmoothProgressBar;

/**
 * web界面基类
 * Created by ly on 2017/8/24 10:09.
 */
public abstract class BaseWebFragment extends BaseFragment {

    private CommonWebView wv;
    private CustomWebChromeClient webChromeClient;
    private CustomDownloadListener customDownloadListener;
    private SmoothProgressBar pb_web;
    private ViewGroup root_view;
    private View error_view;
    private boolean hasReceivedError;

    @Override
    protected void initViews(View view) {
        initWebView(view);
        initViews(view);
    }

    private void initWebView(View view) {
        root_view = view.findViewById(R.id.ll_web);
        wv = view.findViewById(R.id.web_view);
        pb_web = view.findViewById(R.id.pb_web);
        pb_web.setVisibility(View.GONE);
        webChromeClient = new CustomWebChromeClient(activity) {
            /**
             * 解析网页标题后的回调
             * Created by ly on 2017/9/26 9:54
             */
            @Override
            public void onReceivedTitle(WebView webView, String s) {
                super.onReceivedTitle(webView, s);
            }
        };
        customDownloadListener = new CustomDownloadListener(activity);
        wv.setWebChromeClient(webChromeClient);
        wv.setWebViewClient(new CustomWebViewClient(wv) {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                hasReceivedError = false;
                startLoading();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
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
                onPageFinish();
                hasReceivedError = true;
                showErrorPage();
            }
        });
        wv.setDownloadListener(customDownloadListener);
        wv.getSettings().setLoadsImagesAutomatically(true);

        AndroidBug5497Workaround.assistActivity(activity);
    }

    public void startLoading() {
        if (pb_web != null)
            pb_web.start();
    }

    public void stopLoading() {
        if (pb_web != null)
            pb_web.stop();
    }

    public WebView getWebView() {
        return wv;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    public void onKeyDown(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (wv != null && wv.canGoBack()) {
                wv.goBack();
            } else {
                if (activity != null)
                    activity.finish();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        wv.onPause();//暂停部分可安全处理的操作，如动画，定位，视频播放等
        wv.pauseTimers();//暂停所有WebView的页面布局、解析以及JavaScript的定时器操作
    }

    @Override
    public void onResume() {
        super.onResume();
        wv.onResume();
        wv.resumeTimers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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

    public void onPageFinish() {

    }

    public void loadUrl(String url) {
        if (wv != null)
            wv.loadUrl(url);
    }

    public void showErrorPage() {
        if (error_view == null) {
            error_view = LayoutInflater.from(getContext()).inflate(R.layout.layout_web_load_fail, null);
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
}
