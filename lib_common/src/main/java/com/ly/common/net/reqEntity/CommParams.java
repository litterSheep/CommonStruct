package com.ly.common.net.reqEntity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

/**
 * @author ly
 * date 2019/8/16 10:58
 */
public class CommParams {

    private String ApkVersion;
    private String Imei;
    private String ApkChannel;
    private String ApkPkgName;
    private String Os;
    private String AppId;
    private String OsVersion;
    private String ApkName;
    private String Authorization;

    private static CommParams instance;

    public static CommParams get() {
        if (instance == null)
            synchronized (CommParams.class) {
                if (instance == null)
                    instance = new CommParams();
            }
        return instance;
    }

    public String getJson() {
        return new Gson().toJson(this);
    }

    public Map<String, String> getMap() {
        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(this), new TypeToken<Map<String, String>>() {
        }.getType());
    }

    private CommParams() {
    }

    public String getApkVersion() {
        return ApkVersion;
    }

    public String getImei() {
        return Imei;
    }

    public String getApkChannel() {
        return ApkChannel;
    }

    public String getApkPkgName() {
        return ApkPkgName;
    }

    public String getOs() {
        return Os;
    }

    public String getAppId() {
        return AppId;
    }

    public String getOsVersion() {
        return OsVersion;
    }

    public String getApkName() {
        return ApkName;
    }
}
