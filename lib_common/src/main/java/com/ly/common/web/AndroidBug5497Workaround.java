package com.ly.common.web;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.ly.common.utils.ScreenUtil;

/**
 * 解决webView中键盘弹出时遮住输入框的bug
 * Created by ly on 2017/9/4 14:18.
 */
public class AndroidBug5497Workaround {
    // For more information, see https://code.google.com/p/android/issues/detail?id=5497
    // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;

    private AndroidBug5497Workaround(Activity activity) {
        /**
         * android.R.id.content所指的View，是Android所有Activity界面上开发者所能控制的区域的根View
         *
         * 如果Activity是全屏模式，那么android.R.id.content就是占满全部屏幕区域的。
         * 如果Activity是普通的非全屏模式，那么android.R.id.content就是占满除状态栏之外的所有区域。
         * 其他情况，如Activity是弹窗、或者7.0以后的分屏样式等，android.R.id.content也是弹窗的范围或者分屏所在的半个屏幕——这些情况较少，就暂且不考虑了。
         */
        FrameLayout content = activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                possiblyResizeChildOfContent();
            }
        });
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }

    public static void assistActivity(Activity activity) {
        new AndroidBug5497Workaround(activity);
    }

    private void possiblyResizeChildOfContent() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        int usableHeightNow = r.bottom;
        if (usableHeightNow != usableHeightPrevious) {
            int screenHeight = ScreenUtil.getScreenHeight();
            int heightDifference = screenHeight - usableHeightNow;
            if (heightDifference > (screenHeight / 4)) {
                // keyboard probably just became visible
                frameLayoutParams.height = screenHeight - heightDifference;
            } else {
                // keyboard probably just became hidden
                frameLayoutParams.height = usableHeightNow;
            }
            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

}
