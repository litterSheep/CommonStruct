package com.ly.common.web;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;

import com.ly.common.eventBus.EventBusFlag;
import com.ly.common.eventBus.EventObj;
import com.ly.common.frame.BaseActivity;
import com.ly.common.frame.BaseApp;
import com.ly.common.utils.AppUtils;
import com.ly.common.utils.CloseUtils;
import com.ly.common.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;

/**
 * Created by ly on 2017/8/24 14:31.
 */
public class JSBridgeBase {
    public static final String BRIDGE_NAME = "nativeApi";
    private String param;

    public JSBridgeBase() {

    }

    /**
     * 传值构造方法
     * Created by ly on 2017/8/26 10:06
     */
    public JSBridgeBase(String param) {
        this.param = param;
    }

    @JavascriptInterface
    public void toast(String msg) {
        ToastUtil.showShort(msg);
    }

    @JavascriptInterface
    public boolean isLogin() {
        return BaseApp.get().isLogin();
    }

    /**
     * 关于我们获取应用icon
     * by yjx 2019/3/7
     */
    @JavascriptInterface
    public String getAppIcon() {
        ByteArrayOutputStream baos = null;
        try {
            BitmapDrawable bd = (BitmapDrawable) AppUtils.getAppIcon();
            Bitmap bm = bd.getBitmap();
            baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIO(baos);
        }

        return null;
    }

    @JavascriptInterface
    public void goBackActionH5(String action) {
        if ("1".equals(action))
            EventBus.getDefault().post(new EventObj(EventBusFlag.ACTION_FINISH_WEB_PAGE));
    }
}
