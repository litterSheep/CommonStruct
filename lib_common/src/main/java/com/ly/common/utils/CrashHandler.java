package com.ly.common.utils;

import android.os.Build;
import android.os.Debug;
import android.os.Process;

import com.ly.common.frame.BaseApp;
import com.ly.common.frame.Constants;
import com.orhanobut.logger.Logger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Properties;
import com.ly.common.BuildConfig;

import org.jetbrains.annotations.NotNull;


/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,由该类 来接管程序,并记录 发送错误报告.
 */
public class CrashHandler implements UncaughtExceptionHandler {

    private static final boolean CONFIG_CRASH_HANDLER_DEBUG = false;
    /**
     * 错误报告文件的扩展名
     */
    private static final String CRASH_REPORTER_EXTENSION = ".log";
    /**
     * CrashHandler实例
     */
    private static CrashHandler INSTANCE;
    /**
     * 系统默认的UncaughtException处理类
     */
    private UncaughtExceptionHandler mDefaultHandler;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CrashHandler();
        }
        return INSTANCE;
    }

    /**
     * 初始化,注册Context对象, 获取系统默认的UncaughtException处理器, 设置该CrashHandler为程序的默认处理器
     */
    public void init() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(@NotNull Thread thread, @NotNull Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null || CONFIG_CRASH_HANDLER_DEBUG) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            if (mDefaultHandler != null)
                mDefaultHandler.uncaughtException(thread, ex);
        } else {
            // Sleep一会后结束程序
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Logger.e("Error : " + e.toString());
            }
            Process.killProcess(Process.myPid());
            System.exit(10);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
     *
     * @return true:如果处理了该异常信息;否则返回false
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return true;
        }
        uploadOrSave(ex);

        // 如果是OOM异常，手机内存快照
        collectionDumpHprofDataIfOOM(ex);

        return false;
    }

    private void uploadOrSave(Throwable ex) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        String result = info.toString();
        Logger.e(result);
        printWriter.close();

        try {
            String path = Constants.CRASH_PATH;
            File pathFile = new File(path);
            if (!pathFile.exists() || !pathFile.isDirectory()) {
                pathFile.mkdirs();
            }

            String fileName = path + File.separator + "crash-" + DateUtils.getNowDate("yyyy-MM-dd HH-mm-ss") + CRASH_REPORTER_EXTENSION;
            FileOutputStream trace = new FileOutputStream(new File(fileName));
            new Properties().store(trace, "");
            trace.write(result.getBytes());
            trace.flush();
            trace.close();
        } catch (Exception e) {
            Logger.e("an error occured while writing report file..." + e.toString());
        }
        upLoadLog(result);
    }

    private void upLoadLog(String ex) {
        String sb = ex + "\n >\n >\n>>>>>>>>>>>>>DEVICE INFO:\nCURRENT_ACTIVITY: " + CommonUtils.getCurrentActivity() +
                "\nTIME: " + DateUtils.getCurrentTime(DateUtils.FORMAT_DATE_TIME_SECOND) +
                "\nIMEI/MAC: " + DeviceUtil.getDeviceId() +
                "\nMODEL: " + Build.MODEL +
                "\nNETWORK_TYPE: " + NetUtils.getType(BaseApp.get()) +
                "\nSDK_VERSION: " + Build.VERSION.SDK_INT +
                "\nAPP_VERSION: " + BuildConfig.VERSION_NAME +
                "\nCHANNEL: " + AppUtils.getChannelId() ;

        // TODO: 2021/3/23  错误日志上报

    }

    /**
     * 如果是OOM错误，则保存崩溃时的内存快照，供分析使用
     */
    private void collectionDumpHprofDataIfOOM(Throwable ex) {
        // 如果是OOM错误，则保存崩溃时的内存快照，供分析使用
        if (isOOM(ex)) {
            try {
                String path = Constants.CRASH_PATH;
                String fileName = path + "crash-" + DateUtils.getNowDate("yyyy-MM-dd HH-mm-ss") + ".hprof";
                Debug.dumpHprofData(fileName);
            } catch (IOException e) {
                Logger.e("couldn’t dump hprof,  an error occurs while opening or writing files.");
            } catch (UnsupportedOperationException e) {
                Logger.e("couldn’t dump hprof,  the VM was built without HPROF support.");
            }
        }
    }

    /**
     * 检测这个抛出对象是否为OOM Error
     */
    private boolean isOOM(Throwable throwable) {
        if (null != throwable && OutOfMemoryError.class.getName().equals(throwable.getClass().getName())) {
            return true;
        } else {
            Throwable cause = null;
            if (throwable != null) {
                cause = throwable.getCause();
            }
            if (cause != null) {
                return isOOM(cause);
            }
            return false;
        }
    }

}
