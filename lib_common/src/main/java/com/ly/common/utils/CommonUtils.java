package com.ly.common.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import com.ly.common.frame.BaseApp;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.Environment.MEDIA_MOUNTED;

public class CommonUtils {

    /**
     * 判断邮箱是否合法
     */
    public static boolean isEmail(String email) {
        return (!TextUtils.isEmpty(email)) && email.matches("^\\w+@\\w+\\.(cn|com|net|org)$");
    }

    public static boolean isLoginEmail(String email) {
        return (!TextUtils.isEmpty(email)) && email.matches("^\\w+@\\w+\\.\\w+$");
    }

    public static boolean isPhoneNum(String phoneNum) {
        return (!TextUtils.isEmpty(phoneNum)) && phoneNum.matches("^1\\d{10}$");
    }

    public static boolean isPswRegular(String psw) {
        return (!TextUtils.isEmpty(psw)) && psw.matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,12}$");
    }

    public static boolean isTelephoneNum(String phoneNum) {
        //String reg = "^((010|02[0,1,2,3,4,5,7,8,9])|[0][1-9]\\d{2})$";//简单判断固话区号
//        String reg = "[0-9]{7,8}$";
        String reg = "^0[0-9]{2,3}-[0-9]{7,8}$";
        return (!TextUtils.isEmpty(phoneNum)) && phoneNum.matches(reg);
    }

    /**
     * 检查SD卡是否存在
     */
    public static boolean hasSdCard() {
        if (Environment.getExternalStorageState().equals(MEDIA_MOUNTED)
                || !Environment.isExternalStorageRemovable()) {
            return true;
        } else {
            Logger.e("SD card is not avaiable/writeable right now");
            return false;
        }
    }

    /**
     * 用于存放日志等小文件
     * 获取应用私有path（有sd卡则getExternalCacheDir，否则getCacheDir）
     */
    public static String getCacheDir(String dir) {
        Context context = BaseApp.get();
        String cacheDir = "";
        if (hasSdCard()) {
            if (context.getExternalCacheDir() != null) {
                //mnt/sdcard/Android/data/< package name >/cache/
                cacheDir = context.getExternalCacheDir().getAbsolutePath();
            } else {
                cacheDir = context.getCacheDir().getPath();
            }
        } else {
            //data/data/< package name >/cache/
            cacheDir = context.getCacheDir().getPath();
        }
        cacheDir = cacheDir + File.separator + dir;
        File file = new File(cacheDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return cacheDir;
    }

    /**
     * 用于存放视频、图片等大文件
     */
    public static String getFilesDir(String dir) {
        Context context = BaseApp.get();
        String filesDir;
        if (hasSdCard()) {
            File externalFilesDir = context.getExternalFilesDir(null);
            if (externalFilesDir != null) {
                filesDir = externalFilesDir.getPath();
            } else {
                filesDir = context.getFilesDir().getPath();
            }
        } else {
            filesDir = context.getFilesDir().getPath();
        }
        filesDir = filesDir + File.separator + dir;
        File file = new File(filesDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return filesDir;
    }

    /**
     * 判断应用当前是否运行
     */
    public static boolean isBackground() {
        Context context = BaseApp.get();
        return !getCurrentActivity().contains(context.getPackageName());
    }

    /**
     * 获取手机当前显示的Activity(包括系统的)
     *
     * @author LY    2015-12-10 下午4:58:25
     */
    public static String getCurrentActivity() {
        try {
            Context context = BaseApp.get();
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
            ActivityManager.RunningTaskInfo cinfo = runningTasks.get(0);
            ComponentName component = cinfo.topActivity;
            return component.getClassName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Uri getUriForFile(File file) {
        Context context = BaseApp.get();
        if (file == null) {
            return null;
        }
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                uri = FileProvider.getUriForFile(context.getApplicationContext(), AppUtils.getApplicationId() + ".fileprovider", file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    /**
     * 如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
     *
     * @author LY    2016-1-5 上午11:37:30
     */
    public static boolean isScreenOn() {
        Context context = BaseApp.get();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm != null && pm.isScreenOn();
    }

    /**
     * 判断密码是否合法
     * Created by ly on 2017/3/6 10:53
     */
    public static boolean isPassword(String password) {
        String regex = "(?=.*\\d)(?=.*[a-zA-Z])^.{6,15}$";
        Matcher m = Pattern.compile(regex).matcher(password);
        return m.matches();
    }

    /**
     * 获取sd卡可用空间
     * Created by ly on 2017/5/31 15:28
     */
    public static long getSDFreeSize() {
        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //空闲的数据块的数量
        long freeBlocks = sf.getAvailableBlocks();
        //返回SD卡空闲大小
        return freeBlocks * blockSize;  //单位Byte
        //return (freeBlocks * blockSize)/1024;   //单位KB
        //return (freeBlocks * blockSize)/1024 /1024; //单位MB
    }

    /**
     * 隐藏身份证号中间部分
     * Created by ly on 2017/8/3 10:55
     */
    public static String hintCardNum(String card) {
        if (TextUtils.isEmpty(card) || card.length() < 17)
            return card;
        return card.substring(0, 2) + "**************" + card.substring(16);
    }

    //生成很多个*号
    private static String createAsterisk(int length) {
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuffer.append("*");
        }
        return stringBuffer.toString();
    }

    public static String hidePhoneNum(String phone) {
        try {
            if (TextUtils.isEmpty(phone))
                return "";
            return phone.substring(0, 3) + "****" + phone.substring(7);
        } catch (Exception e) {
            Logger.e(e.toString());
        }
        return "";
    }

    public static String handleSpecialChars(String src) {
        final String[] specialChars = new String[]{"[\ud800\udc00-\udbff\udfff\ud800-\udfff]", "\""};
        if (src != null)
            for (String specialChar : specialChars) {
                src = src.replaceAll(specialChar, "");
            }

        return src;
    }

    /**
     * 除汉字、字母、数字外的字符都按特殊字符处理
     * Created by ly on 2018/1/17 14:11
     */
    public static boolean isContainsSpecialChars(String str) {
        String reg = "[a-zA-Z0-9\\u4E00-\\u9FA5]+";
        return !TextUtils.isEmpty(str) && !str.matches(reg);
    }


    public static boolean isCorrectContact(String contactName) {
        return !TextUtils.isEmpty(contactName) && Pattern.matches("^([\\u3400-\\uA4FF\\uF900-\\uFAFF]|（|）|·)*$", contactName);
    }

    public static boolean isContainsEmoji(String str) {
        String reg = "[\\ud83c\\udc00-\\ud83c\\udfff]|[\\ud83d\\udc00-\\ud83d\\udfff]|[\\u2600-\\u27ff]";
        return !TextUtils.isEmpty(str) && str.matches(reg);
    }

    public static void main(String[] str) {
        String text = "特殊符*&/\\（\\\\号测试ლ(╹◡╹ლ)2333abc■◁★/*+-)(*&^\\\"%$#@!~`<>?:}{[].|;:',_+-=`~!@#%&";
//        System.out.println(isContainsSpecialChars(text));
//        System.out.println(handleSpecialChars(text));
//
//        System.out.println("matches:" + isCorrectContact(""));

        String reg = "[^a-zA-Z0-9\\u4E00-\\u9FA5]+";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(text);
        String trim = m.replaceAll("").trim();

        System.out.println("trim:" + trim);

        String s = "asdasdasd";
        System.out.println(Arrays.asList(s.split("")));

    }

    public static boolean isStr2Int(String str) {
        return Pattern.compile("^[0-9]+$").matcher(str).matches();
    }

}