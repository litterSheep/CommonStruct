package com.ly.common.net;

import com.ly.common.net.reqEntity.CommParams;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author ly
 * date 2020/3/7 18:07
 */
public class CommonParamsInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        CommParams c = CommParams.get();
        Request.Builder builder = request.newBuilder()
                .header("ApkVersion", c.getApkVersion())
                .header("Imei", c.getImei())
                .header("ApkChannel", c.getApkChannel())
                .header("ApkPkgName", c.getApkPkgName())
                .header("Os", c.getOs())
                .header("AppId", c.getAppId())
                .header("OsVersion", c.getOsVersion())
                .header("ApkName", c.getApkName())
                .header("Authorization", "c.getAuthorization()");
        return chain.proceed(builder.build());
    }
}
