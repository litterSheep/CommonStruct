package com.ly.common.utils;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Point;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.ly.common.frame.BaseApp;
import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.regex.Pattern;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * 设备基本操作类
 */
public final class DeviceUtil {

    public static TelephonyManager getManager() {
        return (TelephonyManager) BaseApp.get().getSystemService(TELEPHONY_SERVICE);
    }

    public static Context getContext() {
        return BaseApp.get();
    }

    public static String getDeviceId() {
        String device_id = "";
        if (AppUtils.checkPermission(Manifest.permission.READ_PHONE_STATE)) {
            device_id = getManager().getDeviceId();
            if (!TextUtils.isEmpty(device_id) && !"000000000000000".equals(device_id)) {
                Logger.d("有权限，device_id：" + device_id);
                return device_id;
            } else {
                device_id = null;
            }
        }
        Logger.d("无权限，device_id：" + device_id);
        return device_id;
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {

        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {

        return android.os.Build.BRAND;
    }


    /**
     * 获取CPU最大频率（单位KHZ）
     *
     * @return
     */
    public static String getMaxCpuFreq() {
        StringBuilder result = new StringBuilder();
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result.append(new String(re));
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = new StringBuilder("N/A");
        }
        return result.toString().trim() + "Hz";
    }

    /**
     * 获取CPU最小频率（单位KHZ）
     *
     * @return
     */
    public static String getMinCpuFreq() {
        StringBuilder result = new StringBuilder();
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result.append(new String(re));
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = new StringBuilder("N/A");
        }
        return result.toString().trim() + "Hz";
    }

    /**
     * 实时获取CPU当前频率（单位KHZ）
     *
     * @return
     */
    public static String getCurCpuFreq() {
        String result = "N/A";
        FileReader fr = null;
        try {
            fr = new FileReader(
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            result = text.trim() + "Hz";
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fr != null)
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return result;
    }

    /**
     * 获取CPU型号
     *
     * @return
     */
    public static String getCpuName() {
        FileReader fr = null;
        try {
            fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            while (text != null) {
                //例如红米这样的傻逼，会在文件头部添加两行，防止类似的手机系统
                if (text.startsWith("Processor")) {
                    String[] array = text.split(":\\s+", 2);
                    return array[1];
                }
                text = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fr != null)
                    fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取手机核数
     *
     * @return
     */
    public static int getCpuNum() {
        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return Pattern.matches("cpu[0-9]", pathname.getName());
                }
            });
            return files.length;
        } catch (Exception e) {
            Logger.d("CPU Count: Failed.");
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * 获取imsi
     *
     * @return
     */
    public static String getImsiNumber() {
        try {
            return getManager().getSubscriberId();
        } catch (Exception e) {
            Logger.w(e.getMessage());
        }
        return null;
    }

    /**
     * 获取运营商
     *
     * @return
     */
    public static String getSimOperatorName() {
        try {
            return getManager().getSimOperatorName();
        } catch (Exception e) {
            Logger.w(e.getMessage());
        }
        return null;
    }

    /**
     * 获取手机内存大小
     *
     * @return
     */
    public static String getTotalMemory() {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;
        FileReader localFileReader = null;
        try {
            localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                Log.i(str2, num + "\t");
            }

            initial_memory = FormatUtil.strToInt(arrayOfString[1]) * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (localFileReader != null)
                try {
                    localFileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return Formatter.formatFileSize(getContext(), initial_memory);// Byte转换为KB或者MB，内存大小规格化
    }

    /**
     * 获取当前可用内存大小
     *
     * @return
     */
    public static String getAvailMemory() {
        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        if (am != null)
            am.getMemoryInfo(mi);
        return Formatter.formatFileSize(getContext(), mi.availMem);
    }

    /**
     * 获取屏幕的分辨率
     *
     * @return
     */
    public static String getScreenResolution() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        if (windowManager != null)
            windowManager.getDefaultDisplay().getSize(point);
        return point.x + "*" + point.y;
    }

    /**
     * 获取设备的尺寸
     */
    public static double getScreenSize() {
        Point point = new Point();
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null)
            windowManager.getDefaultDisplay().getSize(point);
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        double x = Math.pow(point.x / dm.xdpi, 2);
        double y = Math.pow(point.y / dm.ydpi, 2);
        return Math.sqrt(x + y);
    }

    /**
     * 获取设备mac地址
     * Created by ly on 2018/10/22 14:32
     */
    public static String getMac() {
        String macAddress;
        StringBuilder buf = new StringBuilder();
        NetworkInterface networkInterface;
        try {
            networkInterface = NetworkInterface.getByName("eth1");
            if (networkInterface == null) {
                networkInterface = NetworkInterface.getByName("wlan0");
            }
            if (networkInterface == null) {
                return "02:00:00:00:00:00";
            }
            byte[] addr = networkInterface.getHardwareAddress();
            for (byte b : addr) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            macAddress = buf.toString();
        } catch (SocketException e) {
            e.printStackTrace();
            return "02:00:00:00:00:00";
        }
        return macAddress;
    }

    /**
     * 获取当前连接的wifi mac地址
     * Created by ly on 2018/10/22 14:10
     */
    public static String getWifiMac() {
        String netMac = null;
        WifiManager mWifi = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifi != null && mWifi.isWifiEnabled()) {
            WifiInfo wifiInfo = mWifi.getConnectionInfo();
            netMac = wifiInfo.getBSSID(); //获取被连接网络的mac地址  
//            String netName = wifiInfo.getSSID(); //获取被连接网络的名称  
//            String localMac = wifiInfo.getMacAddress();// 获得本机的MAC地址    
        }
        return netMac;
    }
}
