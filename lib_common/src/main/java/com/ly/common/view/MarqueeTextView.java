package com.ly.common.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * 让textview一直获得焦点，实现跑马灯效果
 * Created by ly on 2017/7/25 9:56.
 */
public class MarqueeTextView extends AppCompatTextView {

    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        //告诉父类 一直是获得焦点状态，从而解决键盘等弹出导致textView失去焦点而停止滚动
        super.onFocusChanged(true, direction, previouslyFocusedRect);
    }
}
