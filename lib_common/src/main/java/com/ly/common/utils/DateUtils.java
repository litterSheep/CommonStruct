package com.ly.common.utils;

import android.annotation.SuppressLint;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 */
@SuppressLint("SimpleDateFormat")
public class DateUtils {
    public final static String FORMAT_YEAR = "yyyy";
    public final static String FORMAT_MONTH_DAY = "MM月dd日";

    public final static String FORMAT_DATE = "yyyy-MM-dd";
    public final static String FORMAT_TIME = "HH:mm";
    public final static String FORMAT_MONTH_DAY_TIME = "MM月dd日  hh:mm";

    public final static String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm";
    public final static String FORMAT_DATE1_TIME = "yyyy/MM/dd HH:mm";
    public final static String FORMAT_DATE_TIME_SECOND = "yyyy/MM/dd HH:mm:ss";

    public static final String DATE_FORMAT = "yyyyMMdd";
    public static final String NUMBER_FORMAT = "#,###";
    public static final String NUMBER_FORMAT_DIAN = "0.00";
    public static final String NUMBER_FORMAT_QIAN_DIAN = "#,###.00";
    public static final String NUMBER_LILV = "0.####";

    /**
     * String类型的日期时间转化为毫秒（1970-）类型.
     *
     * @param strDate String形式的日期时间
     * @param format  格式化字符串，如："yyyy-MM-dd HH:mm:ss"
     * @author LY 2015-9-16 上午11:40:26
     */
    public static long getMillisecondByFormat(String strDate, String format) {
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = mSimpleDateFormat.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) return 0;
        return date.getTime();
    }

    /**
     * 毫秒----> String 日期
     *
     * @author LY 2015-9-18 下午3:41:32
     */
    public static String getDateByMillisecond(long milliseconds, String format) {
        Date d = new Date(milliseconds);
        SimpleDateFormat f = new SimpleDateFormat(format);

        return f.format(d);
    }

    /**
     * 获取当前日期的指定格式的字符串
     *
     * @param format 指定的日期时间格式，若为null或""则使用指定的格式"yyyy-MM-dd HH:MM"
     */
    public static String getCurrentTime(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        if (format == null || format.trim().equals("")) {
            sdf.applyPattern(FORMAT_DATE_TIME);
        } else {
            sdf.applyPattern(format);
        }
        return sdf.format(new Date());
    }

    /**
     * 获得日期  格式：X月Y日
     *
     * @return String 格式：X月Y日
     */
    public static String getFormatByString(String todayStr, String dateFormat) {
        Date today = getDateByStr(todayStr, dateFormat);
        return getCurMonth(today) + "月" + getCurDay(today) + "日";
    }

    /**
     * 根据dateformat获得格式日期字符串
     *
     * @return 格式日期字符串
     */
    public static String getFormatDate(Date date, String dateFormat) {
        return new SimpleDateFormat(dateFormat).format(date);
    }

    /**
     * 将yyyyMMdd格式的日期字符串  转换成yyyy-MM-dd形式输出
     *
     * @return 日期 以yyyy-MM-dd形式输出
     */
    public static String getStringByDateStr(String dayStr) {
        return DateUtils.getFormatDate(DateUtils.getDateByStr(dayStr, DateUtils.DATE_FORMAT), DateUtils.FORMAT_DATE);
    }


    /**
     * 根据Date 显示 X月Y日
     * @return 显示 X月Y日
     */
    public static String getStringByDate(Date date) {
        return getCurYear(date) + "年" + getCurMonth(date) + "月" + getCurDay(date) + "日";
    }

    /**
     * 获得间隔天数
     *
     * @param nextZDR 下一个日期
     * @param today   今天
     * @return int 间隔天数
     */
    public static int getBetweenDay(Date nextZDR, Date today) {
        long betweenDays = 0;
        long DAY = 24L * 60L * 60L * 1000L;
        try {
            betweenDays = (nextZDR.getTime() - today.getTime()) / DAY + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (int) betweenDays;
    }


    public static int getDaysBetween(Date startDate, Date endDate) {
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(startDate);
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(endDate);
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);

        return (int) (toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24);
    }

    /**
     * 获得间隔天数
     *
     * @param nextZDR   下一个日期
     * @param today     今天
     */
    public static int getBetweenDay(String nextZDR, String today, String formatStr) {
        long betweenDays = 0;
        long DAY = 24L * 60L * 60L * 1000L;

        try {
            betweenDays = (getDateByStr(nextZDR, formatStr).getTime() - getDateByStr(today, formatStr).getTime()) / DAY + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (int) betweenDays;
    }

    /**
     * 根据年月日获得date
     *
     * @return 根据年月日获得date
     */
    public static Date getDateByYMD(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal.getTime();
    }

    /**
     * 根据日期字符串获得Date
     *
     * @return Date类型 日期字符串获得Date
     */
    public static Date getDateByStr(String dateStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 根据日期Date类型 获得年
     */
    public static int getCurYear(Date date) {
        int year = 1;
        Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
            year = cal.get(Calendar.YEAR);
        }
        return year;
    }

    /**
     * 根据日期Date类型 获得月
     */
    public static int getCurMonth(Date date) {
        int month = 0;
        Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
            month = cal.get(Calendar.MONTH);
        }
        return month + 1;
    }

    /**
     * 根据日期Date类型 获得日
     */
    public static int getCurDay(Date date) {
        int day = 0;
        Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
            day = cal.get(Calendar.DAY_OF_MONTH);
        }
        return day;
    }

    /**
     * 获得日期中的当月最后一天
     */
    private static int getLastDayByDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static String getNowDate(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }


}
