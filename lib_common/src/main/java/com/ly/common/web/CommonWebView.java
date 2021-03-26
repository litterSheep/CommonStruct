package com.ly.common.web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by ly on 2017/9/8 20:16
 */
public class CommonWebView extends WebView {

    public CommonWebView(Context context) {
        super(context);
        init();
    }

    public CommonWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        init();
    }

    public CommonWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        // WebStorage webStorage = WebStorage.get();
        this.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        setClickable(true);

        initWebViewSettings();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebViewSettings() {
        WebSettings webSetting = this.getSettings();
//        webSetting.setNavDump(true);
        webSetting.setJavaScriptEnabled(true);
        //支持通过JS打开新窗口
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        //要打开新窗口时onCreateWindow会调用
        webSetting.setSupportMultipleWindows(true);
        //把所有内容放在webview等宽的一列中，从而实现适应屏幕（可能会出现页面中链接失效）
        //webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        //是否显示缩放按钮
        webSetting.setBuiltInZoomControls(false);
        //设置字体缩放按100%展示，避免调整体统字体大小导致页面变形
        webSetting.setTextZoom(100);
        //设置默认的字体大小，默认为16，有效值区间在1-72之间
        webSetting.setDefaultFontSize(18);
        //设置自适应屏幕
        webSetting.setUseWideViewPort(true);
        // 缩放至屏幕的大小
        webSetting.setLoadWithOverviewMode(true);
        //展示图片
        webSetting.setBlockNetworkImage(false);
        //支持自动加载图片
        webSetting.setLoadsImagesAutomatically(true);

        //手动设置5.0以上的系统在https链接中也能加载http资源
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webSetting.setDatabaseEnabled(true);
        webSetting.setAppCacheEnabled(true);
        // webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
        //关闭webView组件的保存密码功能
        webSetting.setSavePassword(false);
        webSetting.setSaveFormData(false);
        //禁止webview上面控件获取焦点(黄色边框)
        webSetting.setNeedInitialFocus(false);
        //设置可以访问文件
        webSetting.setAllowFileAccess(true);
        //设置编码格式
        webSetting.setDefaultTextEncodingName("utf-8");
    }

    public void setWebViewClient(WebViewClient webViewClient) {
        super.setWebViewClient(webViewClient);
    }

    public void setWebChromeClient(WebChromeClient webChromeClient) {
        super.setWebChromeClient(webChromeClient);
    }

    public void setDownloadListener(DownloadListener downloadListener) {
        super.setDownloadListener(downloadListener);
    }

}
