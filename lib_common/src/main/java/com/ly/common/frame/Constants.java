package com.ly.common.frame;


import com.ly.common.utils.CommonUtils;

public class Constants {

    public static final String INTENT_KEY_URL = "url";
    public final static String INTENT_KEY_PARAM = "param";
    public final static String INTENT_KEY_PARAM1 = "param1";
    public final static String INTENT_KEY_LOGIN_ACTION = "loginAction";

    public static final String CRASH_PATH = CommonUtils.getCacheDir("crash");
    public static final String TEMP_PATH = CommonUtils.getCacheDir("temp");
    public static final String PIC_PATH = CommonUtils.getFilesDir("pic");
}
