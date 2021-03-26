package com.ly.common.web;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.ly.common.R;
import com.ly.common.annotation.UseEventBus;
import com.ly.common.entity.LoginAction;
import com.ly.common.eventBus.EventObj;
import com.ly.common.frame.BaseApp;
import com.ly.common.frame.Constants;
import com.ly.common.manager.RouterManager;
import com.ly.common.utils.ToastUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author ly
 * date 2019/7/27 11:19
 */
@UseEventBus(useEventBus = true)
public class CommWebActivity extends BaseWebActivity {

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMsg(EventObj eventObj) {
        if (eventObj != null) {
            switch (eventObj.getEventBusFlag()) {
                case ACTION_LOGIN_SUCCESS:
                    reLoad();
                    break;
            }
        }
    }

    /**
     * @param params 0：url,1：网页内需要用到的参数，2：...
     * @author ly on 2019/5/20 11:25
     */
    public static void launch(Context context, @NonNull String... params) {
        if (params.length > 0 && !TextUtils.isEmpty(params[0])) {
            Intent i = new Intent(context, CommWebActivity.class);
            i.putExtra(Constants.INTENT_KEY_URL, params[0]);
            if (params.length > 1)
                i.putExtra(Constants.INTENT_KEY_PARAM, params[1]);

            if (context != null) {
                context.startActivity(i);
            } else {
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                BaseApp.get().getApplicationContext().startActivity(i);
            }
        } else {
            ToastUtil.showShort(R.string.toast_loading_fail);
        }
    }

    /**
     * @param checkLogin true：检验登录状态，如果没登录则跳转登录，登录成功后自动打开之前的url
     * @param params     0：url,1：网页内需要用到的参数，2：网页标题
     * @author ly on 2019/5/20 11:20
     */
    public static void launch(Activity activity, boolean checkLogin, @NonNull String... params) {
        if (checkLogin) {
            if (BaseApp.get().isLogin()) {
                launch(activity, params);
            } else {
                RouterManager.toLoginActivityForResult(activity, new LoginAction(LoginAction.ACTION_TO_WEB, params));
            }
        } else {
            launch(activity, params);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_common_web;
    }

    @SuppressLint("AddJavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWebView().addJavascriptInterface(new JSBridgeBase(getIntent().getStringExtra(Constants.INTENT_KEY_PARAM)), JSBridgeBase.BRIDGE_NAME);
    }

}
