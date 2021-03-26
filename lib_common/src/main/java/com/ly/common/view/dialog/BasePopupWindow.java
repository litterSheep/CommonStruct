package com.ly.common.view.dialog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.PopupWindow;

import com.ly.common.utils.ScreenUtil;


public abstract class BasePopupWindow extends PopupWindow implements ViewTreeObserver.OnGlobalLayoutListener {

    protected final LayoutInflater layoutInflater;
    protected Context mContext;
    private final View contentView;
    private View decorView;
    private int offsetY;
    private int usableHeightNow;
    private View anchor;
    private int usableHeightPrevious;
    private int navigationH;

    public BasePopupWindow(Context context) {
        super(context);
        this.mContext = context;

        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setBackgroundDrawable(new ColorDrawable());
        this.setOutsideTouchable(true);
        this.setFocusable(true);

        layoutInflater = LayoutInflater.from(mContext);
        contentView = layoutInflater.inflate(getLayoutId(), null);

        setContentView(contentView);

        navigationH = ScreenUtil.getNavigationBarHeight(mContext);

        decorView = ((Activity) mContext).getWindow().getDecorView();
        ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(this);
        initViews(contentView);
    }

    public abstract int getLayoutId();

    public abstract void initViews(View contentView);

    @Override
    public void showAsDropDown(View anchor) {
        if (getHeight() != ViewGroup.LayoutParams.MATCH_PARENT) {
            super.showAsDropDown(anchor);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // Android 7.0中,PopupWindow高度为match_parent时,会出现兼容性问题,需要处理兼容性
            int[] mLocation = new int[2];
            this.anchor = anchor;
            this.anchor.getLocationInWindow(mLocation);
            offsetY = mLocation[1] + anchor.getHeight();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) { // Android 7.1中，PopupWindow高度为 match_parent 时，会占据整个屏幕
                setHeight(usableHeightNow - offsetY); // 重新设置 PopupWindow 的高度
            }
            showAtLocation(anchor, Gravity.NO_GRAVITY, 0, offsetY);
        } else {
            super.showAsDropDown(anchor);
        }

    }

    @Override
    public void onGlobalLayout() {
        Rect r = new Rect();
        decorView.getWindowVisibleDisplayFrame(r);
        usableHeightNow = r.bottom;

        //改变的高度
        int changeDis = Math.abs(usableHeightPrevious == 0 ? ScreenUtil.getScreenHeight() - usableHeightNow :
                usableHeightNow - usableHeightPrevious);
        //是否是底部导航栏展示隐藏的变化
        boolean isNavBarChange = changeDis == navigationH && usableHeightNow != usableHeightPrevious;

        if (isNavBarChange) {
            if (anchor != null && isShowing())
                update(-1, usableHeightNow - offsetY);
        }
        usableHeightPrevious = usableHeightNow;

    }

}
