package com.ly.common.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

/**
 * 跟网络相关的工具类
 */
public class NetUtils {
    private NetUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 判断网络是否连接
     */
    public static boolean isConnected(Context context) {
        if (context == null)
            return false;
        NetworkInfo info = getNetworkInfo(context);
        if (null != info && info.isConnected()) {
            return info.getState() == NetworkInfo.State.CONNECTED;
        }

        return false;
    }

    /**
     * 判断是否是wifi连接
     */
    public static boolean isWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null
                && info.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    public static String getType(Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        if (networkInfo != null) {
            return networkInfo.getTypeName() + networkInfo.getSubtypeName();
        }
        return "";
    }

    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm == null ? null : cm.getActiveNetworkInfo();
    }

    /**
     * 打开网络设置界面
     */
    public static void openSetting(Activity activity) {
        Intent intent = new Intent("/");
        ComponentName cm = new ComponentName("com.android.settings",
                "com.android.settings.WirelessSettings");
        intent.setComponent(cm);
        intent.setAction("android.intent.action.VIEW");
        activity.startActivityForResult(intent, 0);
    }

}  