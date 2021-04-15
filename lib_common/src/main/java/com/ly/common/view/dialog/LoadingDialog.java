package com.ly.common.view.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.ly.common.R;

public class LoadingDialog extends Dialog {
    private long lastClick;
    private static final long MIN_SHOW = 800;
    private static final int DISMISS = 100;
    private long showTime;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == DISMISS) {
                try {
                    LoadingDialog.super.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            super.handleMessage(msg);
        }
    };

    public LoadingDialog(Context context) {
        super(context, R.style.MyLoadDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);

        if (getWindow() != null)
            getWindow().setWindowAnimations(R.style.anim_dialog_common);
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void onBackPressed() {
        if (lastClick > 0 && System.currentTimeMillis() - lastClick <= 1000)
            dismiss();
        lastClick = System.currentTimeMillis();
    }

    public void show() {
        showTime = System.currentTimeMillis();
        try {
            super.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismiss() {
        long cost = System.currentTimeMillis() - showTime;
        handler.sendEmptyMessageDelayed(DISMISS, MIN_SHOW - cost);
    }

}
