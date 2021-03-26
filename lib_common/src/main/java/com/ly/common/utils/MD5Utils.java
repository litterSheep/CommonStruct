package com.ly.common.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Created by ly on 2017/7/25 10:48.
 */
public class MD5Utils {

    private static final String[] strDigits = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
    private static final String authSalt = "zzkjcardAuth";

    private static String byteToArrayString(byte bByte) {
        int iRet = bByte;
        if (bByte < 0) {
            iRet = bByte + 256;
        }

        int iD1 = iRet / 16;
        int iD2 = iRet % 16;
        return strDigits[iD1] + strDigits[iD2];
    }

    private static String byteToNum(byte bByte) {
        int iRet = bByte;
        if (bByte < 0) {
            iRet = bByte + 256;
        }

        return String.valueOf(iRet);
    }

    private static String byteToString(byte[] bByte) {
        StringBuffer sBuffer = new StringBuffer();

        for (int i = 0; i < bByte.length; ++i) {
            sBuffer.append(byteToArrayString(bByte[i]));
        }

        return sBuffer.toString();
    }

    public static String md5(String strObj) {
        String resultString = null;

        try {
            MessageDigest e = MessageDigest.getInstance("MD5");
            resultString = byteToString(e.digest(strObj.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException var3) {
            var3.printStackTrace();
        } catch (UnsupportedEncodingException var4) {
            var4.printStackTrace();
        }

        return resultString;
    }

    public static String md5(byte[] bs) {
        String resultString = null;

        try {
            MessageDigest ex = MessageDigest.getInstance("MD5");
            resultString = byteToString(ex.digest(bs));
        } catch (NoSuchAlgorithmException var3) {
            var3.printStackTrace();
        }

        return resultString;
    }

    public static String md5AddSalt(String content) {
        return md5(content + authSalt);
    }

    public static void main(String[] arg) {

        String str = "e10adc3949ba59asdgsdfgxcvdfd3436784786796780dfgdsh56785485696706763452353674568abbe56e057f20f883e";
        System.out.print(System.currentTimeMillis() + "str>>>>" + str);

        String a = md5(str);
        System.out.print("\n" + System.currentTimeMillis() + "a>>>>" + a);

        Random rand = new Random();
        int randNum = rand.nextInt(3);

        System.out.print("\nrandNum:" + randNum);

    }

}
