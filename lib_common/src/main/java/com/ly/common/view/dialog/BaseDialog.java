package com.ly.common.view.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.ly.common.R;
import com.ly.common.utils.ScreenUtil;

/**
 * Created by ly on 2021/4/15 14:49
 */
public abstract class BaseDialog extends DialogFragment {

    public final AppCompatActivity mContext;
    public DialogInterface.OnDismissListener onDismissListener;

    public BaseDialog(@NonNull AppCompatActivity mContext) {
        this.mContext = mContext;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResId(), container);
        initViews(view);
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //去掉dialog的标题，需要在setContentView()之前
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //点击外部可取消
        dialog.setCanceledOnTouchOutside(true);

        Window window = dialog.getWindow();
        //去掉dialog默认的padding
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = ScreenUtil.getScreenWidth() * 7 / 8;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        lp.windowAnimations = R.style.anim_dialog_common;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable());

        return dialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null)
            onDismissListener.onDismiss(dialog);
    }

    public abstract int getLayoutResId();

    public abstract void initViews(View view);

    public void show() {
        try {
            super.show(mContext.getSupportFragmentManager(), getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismiss() {
        try {
            super.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DialogInterface.OnDismissListener getOnDismissListener() {
        return onDismissListener;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

}
