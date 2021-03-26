package com.ly.common.manager;

import android.app.Activity;
import android.content.Context;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ly.common.entity.LoginAction;
import com.ly.common.frame.BaseApp;
import com.ly.common.frame.Constants;

import javax.annotation.Nullable;

/**
 * 页面路由管理类（主要用于module间跳转页面）
 * 所有需要页面跳转的地方都在这里调用
 * 注意不同的module 要使用不同的group
 * eg. moduleA中 path = "/A/**",moduleB中 path = "/B/**"
 * <p>
 * path至少需要有两级，/xx/xx
 * <p>
 * 具体使用方法--> https://github.com/alibaba/ARouter
 *
 * @author ly on 2017/10/18 13:50
 */
public class RouterManager {

    //module_main
    public static final String ROUTE_MAIN = "/main/MainActivity";
    public static final String ROUTE_LOGIN = "/main/LoginActivity";

    public static void commonNavigation(String path) {
        commonNavigation(BaseApp.get(), path);
    }

    public static void commonNavigation(@Nullable Context context, String path) {
        ARouter.getInstance()
                .build(path)
                .navigation(context);
    }

    public static void commonNavigation(String path, boolean param) {
        ARouter.getInstance()
                .build(path)
                .withBoolean(Constants.INTENT_KEY_PARAM, param)
                .navigation();
    }

    public static void commonNavigation(String path, String param) {
        commonNavigation(null, path, param);
    }

    public static void commonNavigation(Context context, String path, String param) {
        ARouter.getInstance()
                .build(path)
                .withString(Constants.INTENT_KEY_PARAM, param)
                .navigation(context);
    }

    public static void toLoginActivityForResult(Activity activity, LoginAction loginAction) {
        if (loginAction != null) {
            ARouter.getInstance()
                    .build(ROUTE_LOGIN)
                    .withParcelable(Constants.INTENT_KEY_LOGIN_ACTION, loginAction)
                    .navigation(activity, loginAction.getAction());
        } else {
            toLoginActivity(activity);
        }
    }

    public static void toLoginActivity(Context context) {
        ARouter.getInstance()
                .build(ROUTE_LOGIN)
                .navigation(context);
    }

}
