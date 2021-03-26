package com.ly.common.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.ly.common.frame.BaseApp;
import com.orhanobut.logger.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ly on 2017/3/8 12:50.
 */

public class ApkSignUtil {

    /**
     * 验证指定apk是否和当前签名一致(用于app更新)
     * Created by ly on 2017/3/8 13:50
     */
    public static boolean isCorrectSign(String apkFile) {
        String selfSign = getSelfSign();
        if (TextUtils.isEmpty(selfSign))
            return false;
        return getSelfSign().equals(getSHA1(getPackageArchiveInfo(apkFile)));
    }

    /**
     * 获取签名SHA1信息
     * Created by ly on 2017/4/12 17:05
     */
    public static String getSelfSign() {
        return getSHA1(getSign());
    }

    private static String getSHA1(byte[] signature) {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(signature));

            //加密算法的类，这里的参数可以使 MD4,MD5 等加密算法
            MessageDigest md = MessageDigest.getInstance("SHA1");
            //获得公钥
            byte[] publicKey = md.digest(cert.getEncoded());

            //字节到十六进制的格式转换
            String sha1 = byte2HexFormatted(publicKey);

            Logger.w("sha1:" + sha1);

            return sha1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] getSign() {
        PackageManager pm = BaseApp.get().getPackageManager();
        List<PackageInfo> apps = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
        Iterator<PackageInfo> iter = apps.iterator();
        while (iter.hasNext()) {
            PackageInfo info = iter.next();
            String packageName = info.packageName;
            //按包名读取签名
            if (packageName.equals(AppUtils.getApplicationId())) { //根据你自己的包名替换
                return info.signatures[0].toByteArray();
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static byte[] getPackageArchiveInfo(String apkFile) {

        //这个是与显示有关的, 里面涉及到一些像素显示等等, 我们使用默认的情况
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.setToDefaults();
        Object pkgParserPkg = null;
        Class[] typeArgs = null;
        Object[] valueArgs = null;
        try {
            Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
            Constructor<?> packageParserConstructor = null;
            Object packageParser = null;
            //由于SDK版本问题，这里需做适配，来生成不同的构造函数
            if (Build.VERSION.SDK_INT > 20) {
                //无参数 constructor
                packageParserConstructor = packageParserClass.getDeclaredConstructor();
                packageParser = packageParserConstructor.newInstance();
                packageParserConstructor.setAccessible(true);//允许访问

                typeArgs = new Class[2];
                typeArgs[0] = File.class;
                typeArgs[1] = int.class;
                Method pkgParser_parsePackageMtd = packageParserClass.getDeclaredMethod("parsePackage", typeArgs);
                pkgParser_parsePackageMtd.setAccessible(true);

                valueArgs = new Object[2];
                valueArgs[0] = new File(apkFile);
                valueArgs[1] = PackageManager.GET_SIGNATURES;
                pkgParserPkg = pkgParser_parsePackageMtd.invoke(packageParser, valueArgs);
            } else {
                //低版本有参数 constructor
                packageParserConstructor = packageParserClass.getDeclaredConstructor(String.class);
                Object[] fileArgs = {apkFile};
                packageParser = packageParserConstructor.newInstance(fileArgs);
                packageParserConstructor.setAccessible(true);//允许访问

                typeArgs = new Class[4];
                typeArgs[0] = File.class;
                typeArgs[1] = String.class;
                typeArgs[2] = DisplayMetrics.class;
                typeArgs[3] = int.class;

                Method pkgParser_parsePackageMtd = packageParserClass.getDeclaredMethod("parsePackage", typeArgs);
                pkgParser_parsePackageMtd.setAccessible(true);

                valueArgs = new Object[4];
                valueArgs[0] = new File(apkFile);
                valueArgs[1] = apkFile;
                valueArgs[2] = metrics;
                valueArgs[3] = PackageManager.GET_SIGNATURES;
                pkgParserPkg = pkgParser_parsePackageMtd.invoke(packageParser, valueArgs);
            }

            typeArgs = new Class[2];
            typeArgs[0] = pkgParserPkg.getClass();
            typeArgs[1] = int.class;
            Method pkgParser_collectCertificatesMtd = packageParserClass.getDeclaredMethod("collectCertificates", typeArgs);
            valueArgs = new Object[2];
            valueArgs[0] = pkgParserPkg;
            valueArgs[1] = PackageManager.GET_SIGNATURES;
            pkgParser_collectCertificatesMtd.invoke(packageParser, valueArgs);
            // 应用程序信息包, 这个公开的, 不过有些函数变量没公开
            Field packageInfoFld = pkgParserPkg.getClass().getDeclaredField("mSignatures");
            Signature[] info = (Signature[]) packageInfoFld.get(pkgParserPkg);
            return info[0].toByteArray();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //这里是将获取到得编码进行16 进制转换
    private static String byte2HexFormatted(byte[] arr) {

        StringBuilder str = new StringBuilder(arr.length * 2);

        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();
            if (l == 1)
                h = "0" + h;
            if (l > 2)
                h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
            if (i < (arr.length - 1))
                str.append(':');
        }

        return str.toString().trim();
    }

}
