package com.ly.common.web;

import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.orhanobut.logger.Logger;

/**
 * Created by ly on 2017/9/1 17:09.
 */
public class CustomWebViewClient extends WebViewClient {

    private final WebView mWebView;

    public CustomWebViewClient(WebView mWebView) {
        this.mWebView = mWebView;
    }

    //return false表示WebView自己处理，return true表示我们自己来处理该url。像“tel:10086”等等类似的url，WebView是不会处理的，需要我们自己来处理。
    //通常情况下，以http、https、file(表示本地网页)开头的url，都给WebView自己来加载，其他类型的url都通过系统应用程序来打开
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if ("about:blank".equals(url)) {
            return false;//不需要处理空白页
        }
        String scheme = Uri.parse(url).getScheme();
        if (scheme != null && (scheme.startsWith("file") || scheme.startsWith("http"))) {
            //如果是本地加载的话，直接用当前浏览器加载
            return false;
        }

        //如果是应用宝的链接，则跳转到系统浏览器
        if (url != null && (url.contains("android.myapp.com") || url.contains("a.app.qq.com"))) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            try {
                intent.setData(Uri.parse(url));
                mWebView.getContext().startActivity(intent);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            //不能识别的，启动系统程序进行加载
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            mWebView.getContext().startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onPageFinished(WebView webView, String s) {
        super.onPageFinished(webView, s);
//        final String phone = BaseApp.get().getUser().getPhone();
//        if (!TextUtils.isEmpty(phone)) {
//            //获取网页中placeholder包含手机号的输入框，并赋值当前登录手机号
//            final String js = "javascript:document.querySelector('input[placeholder*=\"手机号\"]').value = '" + phone + "';";
//            if (mWebView != null)
//                mWebView.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        loadJs(js);
//                    }
//                }, 200);
//        }
    }

    @Override
    public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        super.onReceivedSslError(webView, sslErrorHandler, sslError);
        sslErrorHandler.proceed();//接受所有证书
    }

    /**
     * native调js方法
     * Created by ly on 2019/3/27 10:29
     */
    public void loadJs(final String js) {
        if (mWebView != null && !TextUtils.isEmpty(js)) {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        mWebView.evaluateJavascript(js, new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {
                                Logger.d("onReceiveValue:" + s);
                            }
                        });
                    } else {
                        mWebView.loadUrl(js);
                    }
                }
            });
        }
    }

}
