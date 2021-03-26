package com.ly.common.web;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.ly.common.R;
import com.ly.common.annotation.UseEventBus;
import com.ly.common.eventBus.EventBusFlag;
import com.ly.common.eventBus.EventObj;
import com.ly.common.frame.BaseApp;
import com.ly.common.frame.Constants;
import com.ly.common.utils.ToastUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 处理返回键的web Activity
 *
 * @author ly
 * date 2020/4/26 20:08
 */
@UseEventBus(useEventBus = true)
public class BackWebActivity extends CommWebActivity {

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMsg(EventObj eventObj) {
        if (eventObj != null) {
            if (eventObj.getEventBusFlag() == EventBusFlag.ACTION_FINISH_WEB_PAGE) {
                finish();
            }
        }
    }

    public static void launch(Context context, @NonNull String... params) {
        if (params.length > 0 && !TextUtils.isEmpty(params[0])) {
            Intent i = new Intent(context, BackWebActivity.class);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        topTitleBar.setOnLeftClickListener(this::back);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * h5返回处理
     * <p>
     * <p>
     * Created by ly on 2019/3/30 10:19
     */
    private void back() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {//<19不能调用evaluateJavascript，所以直接finish
            finish();
            return;
        }
        final WebView webView = getWebView();
        if (webView != null)
            webView.evaluateJavascript("javascript:window.handleBack()", new ValueCallback<String>() {
                /**
                 * @param action 2不做操作 1关闭页面
                 * Created by ly on 2019/3/30 10:44
                 */
                @Override
                public void onReceiveValue(String action) {
                    switch (action) {
                        case "2":

                            break;
                        case "1":
                            finish();

                            break;
                        default:
                            if (webView.canGoBack()) {
                                webView.goBack();
                            } else {
                                finish();
                            }

                            break;
                    }
                }
            });
    }
}
