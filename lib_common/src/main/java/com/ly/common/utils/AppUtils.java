package com.ly.common.utils;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.core.content.ContextCompat;

import com.ly.common.R;
import com.ly.common.frame.BaseApp;
import com.orhanobut.logger.Logger;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by ly on 2017/2/16 16:28.
 */
public class AppUtils {

    private static Context getContext() {
        return BaseApp.get().getApplicationContext();
    }

    public static boolean checkPermission(String permission) {
        Context context = getContext();
        if (TextUtils.isEmpty(permission))
            return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context.getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            try {
                PackageInfo pack = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
                String[] permissionStrings = pack.requestedPermissions;
                if (permissionStrings != null)
                    for (String permissionString : permissionStrings) {
                        if (permission.equals(permissionString)) {
                            if (Manifest.permission.READ_CONTACTS.equals(permission)) {
                                Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null, null, null, null);
                                boolean hasPermission = false;
                                if (cursor != null) {
                                    hasPermission = cursor.moveToNext();
                                    cursor.close();
                                }
                                return hasPermission;
                            } else {
                                PackageManager pm = context.getPackageManager();
                                return (pm != null && pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED);
                            }
                        }
                    }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }
    }

    /**
     * 通过apk文件获取版本号
     * Created by ly on 2017/5/26 16:49
     */
    public static int getVersionCodeByApkFile(String apkPath) {
        if (TextUtils.isEmpty(apkPath))
            return -1;
        try {
            PackageManager packageManager = BaseApp.get().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getPackageNameByApkFile(String apkPath) {
        if (TextUtils.isEmpty(apkPath))
            return "";
        try {
            PackageManager packageManager = BaseApp.get().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getProcessName(int pid) {
        Context context = getContext();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = null;
        if (am != null) {
            runningApps = am.getRunningAppProcesses();
        }
        if (runningApps != null)
            for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
                if (procInfo.pid == pid) {
                    return procInfo.processName;
                }
            }
        return null;
    }

    /**
     * 获取应用详情页面intent
     */
    public static Intent getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        localIntent.setData(Uri.fromParts("package", BaseApp.get().getPackageName(), null));
        return localIntent;
    }

    /**
     * 获取设备安装的非系统应用
     */
    public static String getInstalledApps() {
        StringBuilder builder = new StringBuilder();
        try {
            PackageManager packageManager = getContext().getPackageManager();
            // Return a List of all packages that are installed on the device.
            List<PackageInfo> packages = packageManager.getInstalledPackages(0);
            for (PackageInfo packageInfo : packages) {
                // 判断系统/非系统应用
                if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) { // 非系统应用
                    String name = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                    builder.append(name).append(",");
//                    info.pkgName = packageInfo.packageName;
//                    info.appIcon = packageInfo.applicationInfo.loadIcon(packageManager);
                    // 获取该应用安装包的Intent，用于启动该应用
//                    info.appIntent = packageManager.getLaunchIntentForPackage(packageInfo.packageName);
                }
            }
        } catch (Exception e) {
            Logger.w("获取已安装应用列表发生异常" + e.getMessage());
        }
        return builder.toString();
    }

    /**
     * 获取androidManifest中label(即app名)
     * Created by ly on 2017/8/26 14:05
     */
    public static String getAppName() {
        String appName = null;
        ApplicationInfo applicationInfo = getAppInfo();
        if (applicationInfo != null) {
            if (applicationInfo.nonLocalizedLabel != null)
                appName = applicationInfo.nonLocalizedLabel.toString();
        }
        if (TextUtils.isEmpty(appName))
            appName = BaseApp.get().getString(R.string.app_name);
        return appName;
    }

    /**
     * 获取应用图标（由于采用了多渠道不同图标配置，所以直接获取资源文件的icon是不准确的）
     * Created by ly on 2017/10/18 10:02
     */
    public static Drawable getAppIcon() {
        Context context = getContext();
        Drawable iconDrawable = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    iconDrawable = applicationInfo.loadIcon(packageManager);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return iconDrawable;
    }


    /**
     * 获取应用ID(包名)
     * Created by ly on 2017/10/18 10:01
     */
    public static String getApplicationId() {
        return BaseApp.get().getPackageName();
    }

    public static String getAppId() {
        return String.valueOf(getMetaDataInt("APP_ID"));
    }

    /**
     * 获取APP渠道号
     * Created by ly on 2017/10/18 10:00
     */
    public static String getChannelId() {
        String channel = getMetaDataStr("CHANNEL_ID");
        if (TextUtils.isEmpty(channel))//全是数字的渠道，比如360渠道
            channel = String.valueOf(getMetaDataInt("CHANNEL_ID"));
        return channel;
    }

    public static String getUMKey() {
        return getMetaDataStr("UM_API_KEY");
    }

    public static String getUMPushKey() {
        return getMetaDataStr("UM_PUSH_KEY");
    }

    public static String getWXKey() {
        return getMetaDataStr("WX_KEY");
    }

    public static String getWXSecret() {
        return getMetaDataStr("WX_SECRET");
    }

    public static String getQQKey() {
        return String.valueOf(getMetaDataInt("QQ_KEY"));
    }

    public static String getQQSecret() {
        return getMetaDataStr("QQ_SECRET");
    }

    public static int getGDTAppId() {
        return getMetaDataInt("GDT_APP_ID");
    }

    public static int getGDTAccountId() {
        return getMetaDataInt("GDT_ACCOUNT_ID");
    }

    public static String getGDTAppKey() {
        return getMetaDataStr("GDT_APP_KEY");
    }

    public static String getCompanyName() {
        return getMetaDataStr("COMPANY_NAME");
    }

    public static String getCompanyNameSimple() {
        return getMetaDataStr("COMPANY_NAME_SIMPLE");
    }

    public static String getH5IconCode() {
        return getMetaDataStr("H5_APP_ICON_CODE");
    }

    public static String getMetaDataStr(String key) {
        String resultData = "";
        if (!TextUtils.isEmpty(key)) {
            Bundle appInfoBundle = getAppInfoBundle();
            if (appInfoBundle != null)
                resultData = appInfoBundle.getString(key);
        }
        return resultData;
    }

    public static int getMetaDataInt(String key) {
        int resultData = 0;
        if (!TextUtils.isEmpty(key)) {
            Bundle appInfoBundle = getAppInfoBundle();
            if (appInfoBundle != null)
                resultData = appInfoBundle.getInt(key);
        }
        return resultData;
    }

    public static boolean getMetaDataBool(String key) {
        boolean resultData = false;
        if (!TextUtils.isEmpty(key)) {
            Bundle appInfoBundle = getAppInfoBundle();
            if (appInfoBundle != null)
                resultData = appInfoBundle.getBoolean(key);
        }
        return resultData;
    }

    /**
     * 获取application中指定的meta-data
     *
     * @return 如果没有获取成功(没有对应值 ， 或者异常)，则返回值为空
     */
    private static Bundle getAppInfoBundle() {
        ApplicationInfo applicationInfo = getAppInfo();
        if (applicationInfo != null) {
            return applicationInfo.metaData;
        }
        return null;
    }

    private static ApplicationInfo getAppInfo() {
        PackageManager packageManager = getContext().getPackageManager();
        ApplicationInfo applicationInfo = null;
        if (packageManager != null) {
            try {
                applicationInfo = packageManager.getApplicationInfo(getContext().getPackageName(), PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return applicationInfo;
    }

    /**
     * 通过反射获取app（application module） buildConfig中定义的值
     *
     * @param fieldName key  如 DEBUG
     *                  Created by ly on 2017/10/18 9:16
     */
    public static Object getBuildConfigValue(String fieldName) {
        try {
            Class<?> clazz = Class.forName(BaseApp.get().getPackageName() + ".BuildConfig");
            Field field = clazz.getField(fieldName);
            return field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
