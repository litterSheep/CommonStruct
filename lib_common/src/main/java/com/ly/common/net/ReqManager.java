package com.ly.common.net;

import android.text.TextUtils;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.ly.common.BuildConfig;
import com.ly.common.frame.BaseApp;
import com.ly.common.net.converter.ErrorCodeConverterFactory;
import com.orhanobut.logger.Logger;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class ReqManager {

    private static final long CONNECTION_TIMEOUT = 15;
    private static final long READ_TIMEOUT = 20;
    private static final long WRITE_TIMEOUT = 15;
    private static volatile ReqManager instance;
    private String host;
    private static IRequests iRequests;

    public static ReqManager getInstance() {
        if (instance == null) {
            synchronized (ReqManager.class) {
                if (instance == null) {
                    instance = new ReqManager();
                }
            }
        }
        return instance;
    }

    public static IRequests get() {
        if (iRequests == null) {
            synchronized (ReqManager.class) {
                if (iRequests == null) {
                    iRequests = ReqManager.getInstance().getRetrofit();
                }
            }
        }
        return iRequests;
    }

    private ReqManager() {
    }

    private IRequests getRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(getHost())
                .client(getBuilderAndAddInterceptor().build())
                //这里可以设置很多的转换器如 ScalarsConverterFactory  默认 GsonConverterFactory.create()
                .addConverterFactory(ErrorCodeConverterFactory.create())
                .build().create(IRequests.class);
    }

    public OkHttpClient.Builder getOkHttpBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //设置超时
        builder.connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        //错误重连
        builder.retryOnConnectionFailure(true);
        return builder;
    }

    private OkHttpClient.Builder getBuilderAndAddInterceptor(){
        OkHttpClient.Builder builder = getOkHttpBuilder();
        builder.addInterceptor(new CommonParamsInterceptor());
        if (BaseApp.get().isDebug()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
            builder.addNetworkInterceptor(new StethoInterceptor());
        }
        return builder;
    }

    public OkHttpClient getOkHttpClient4File() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //设置超时
        builder.connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS);
        //错误重连
        builder.retryOnConnectionFailure(true);
        if (BaseApp.get().isDebug()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            builder.addInterceptor(loggingInterceptor);
        }
        return builder.build();
    }

    /**
     * 上传大文件不能使用拦截器（否则oom），同时超时时长要设置更长
     * 因此文件单独用一套
     * <p>
     * 由于没有公共参数的拦截器，需在接口位置传入headers
     *
     * @author ly on 2020/3/7 19:54
     */
    public IRequests getRetrofit4File() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //设置超时
        builder.connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS);
        //错误重连
        builder.retryOnConnectionFailure(true);
        if (BaseApp.get().isDebug()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            builder.addInterceptor(loggingInterceptor);
        }
        return new Retrofit.Builder()
                .baseUrl(getHost())
                .client(getOkHttpClient4File())
                //这里可以设置很多的转换器如 ScalarsConverterFactory  默认 GsonConverterFactory.create()
                .addConverterFactory(ErrorCodeConverterFactory.create())
                .build().create(IRequests.class);
    }

    public void reset() {
        iRequests = null;
    }


    public String getHost() {
        if (TextUtils.isEmpty(host))
            host = BuildConfig.HOST;
        return host;
    }

    public void setHost(String host) {
        this.host = host;
        reset();
    }

    public void setHostAndReset(String host) {
        if (this.host != null && this.host.equals(host))//要更改的host与之前的一样就不处理
            return;
        Logger.w("host地址变更:" + this.host + ">>>>>>" + host);
        setHost(host);
    }
}
