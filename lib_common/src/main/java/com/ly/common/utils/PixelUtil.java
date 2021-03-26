package com.ly.common.utils;

import androidx.annotation.DimenRes;

import com.ly.common.frame.BaseApp;


/**
 * 像素转换工具
 */
public class PixelUtil {

    /**
     * dp转 px.
     *
     * @param value the value
     * @return the int
     */
    public static int dp2px(float value) {
        final float scale = BaseApp.get().getResources().getDisplayMetrics().densityDpi;
        return (int) (value * (scale / 160) + 0.5f);
    }

    /**
     * px转dp.
     *
     * @param value the value
     * @return the int
     */
    public static int px2dp(float value) {
        final float scale = BaseApp.get().getResources().getDisplayMetrics().densityDpi;
        return (int) ((value * 160) / scale + 0.5f);
    }

    /**
     * sp转px.
     *
     * @param value the value
     * @return the int
     */
    public static int sp2px(float value) {
        float spvalue = value * BaseApp.get().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spvalue + 0.5f);
    }

    /**
     * px转sp.
     *
     * @param value the value
     * @return the int
     */
    public static int px2sp(float value) {
        final float scale = BaseApp.get().getResources().getDisplayMetrics().scaledDensity;
        return (int) (value / scale + 0.5f);
    }

    public static int getDimen(@DimenRes int dimenId) {
        return (int) BaseApp.get().getResources().getDimension(dimenId);
    }

}