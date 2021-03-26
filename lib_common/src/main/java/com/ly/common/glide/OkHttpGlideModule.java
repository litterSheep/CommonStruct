package com.ly.common.glide;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.ly.common.net.ReqManager;

import java.io.InputStream;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * Created by ly on 2021/3/24 13:37
 */
@GlideModule
public class OkHttpGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);
        OkHttpClient.Builder builder = ReqManager.getInstance().getOkHttpBuilder();
//        信任所有证书，使能加载所有https的图片
//        TrustManager trustManager = new TrustManager();
//        builder.sslSocketFactory(getSslFactory(trustManager),trustManager);

        OkHttpClient okHttpClient = builder.build();
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(okHttpClient));
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    private SSLSocketFactory getSslFactory(TrustManager trustManager) {
        try {
            SSLContext ssl = SSLContext.getInstance("SSL");
            ssl.init(null, new X509TrustManager[]{trustManager}, new SecureRandom());
            return ssl.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private class TrustManager implements X509TrustManager {

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {

        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {

        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }
    }
}
