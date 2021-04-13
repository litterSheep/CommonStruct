package com.ly.common.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;

import com.ly.common.R;
import com.ly.common.utils.PixelUtil;
import com.ly.common.utils.ScreenUtil;
import com.orhanobut.logger.Logger;

/**
 * Created by ly on 2021/4/13 10:34
 */
public class NavigationBar extends FrameLayout {

    private final String[] tabs = new String[]{"首页", "我的"};
    public static final int TAB1 = 0;
    public static final int TAB2 = 1;
    public static final int DEFAULT_TAB = TAB1;

    private static final int ICON_SIZE = 19;
    private static final int TEXT_SELECTOR = R.color.navigation_tab_text_selector;
    private static final int rbtId = 23;

    private OnTabSelectedListener onTabSelectedListener;
    private final int[] icons = new int[]{R.drawable.navigation_tab1_selector
            , R.drawable.navigation_tab1_selector};
    private int curSelectedPosition = DEFAULT_TAB;
    private RadioGroup radioGroup;
    private TextView specialV;
    private LinearLayout llTabContainer;
    private View topGrayLine;

    public NavigationBar(Context context) {
        super(context);
        init();
    }

    public NavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        if (tabs.length != icons.length)
            throw new RuntimeException("the tabs size must equals icons size!");

        setBackgroundResource(R.color.transparent);

        llTabContainer = new LinearLayout(getContext());
        llTabContainer.setBackgroundResource(R.color.transparent);
        llTabContainer.setOrientation(LinearLayout.VERTICAL);
        //tab顶部灰线
        topGrayLine = new View(getContext());
        topGrayLine.setBackgroundResource(R.drawable.gray_gradient_line);
        LayoutParams lpLine = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PixelUtil.dp2px(5));
        topGrayLine.setLayoutParams(lpLine);
        llTabContainer.addView(topGrayLine);

        radioGroup = new RadioGroup(getContext());
        int iconSize = PixelUtil.dp2px(ICON_SIZE);
        for (int i = 0; i < tabs.length; i++) {
            RadioButton radioButton = new RadioButton(getContext());

            LinearLayout.LayoutParams lpRbt = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lpRbt.width = ScreenUtil.getScreenWidth() / tabs.length;

            Drawable drawable = ResourcesCompat.getDrawable(getResources(), icons[i], getContext().getTheme());
            if (drawable != null) {
                drawable.setBounds(0, 0, iconSize, iconSize);
                radioButton.setCompoundDrawablePadding(PixelUtil.dp2px(5));
                radioButton.setCompoundDrawables(null, drawable, null, null);
            }

            radioButton.setId(i);
            radioButton.setText(tabs[i]);
            radioButton.setTextSize(11);
            radioButton.setTextColor(AppCompatResources.getColorStateList(getContext(), TEXT_SELECTOR));
            radioButton.setGravity(Gravity.CENTER);
            radioButton.setButtonDrawable(new ColorDrawable(Color.TRANSPARENT));
            radioButton.setBackgroundResource(R.color.transparent);
            radioButton.setLayoutParams(lpRbt);
            radioButton.setOnClickListener(v -> selectTab(v.getId()));
            radioGroup.addView(radioButton);
        }
        radioGroup.setPadding(0, PixelUtil.dp2px(7), 0, PixelUtil.dp2px(3));
        radioGroup.setId(rbtId);
        radioGroup.setBackgroundColor(getResources().getColor(R.color.white));
        radioGroup.setOrientation(RadioGroup.HORIZONTAL);
        radioGroup.setGravity(Gravity.CENTER);
        radioGroup.check(curSelectedPosition);

        //让llTabContainer置底
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.BOTTOM;
        llTabContainer.setLayoutParams(lp);
        llTabContainer.addView(radioGroup);

        addView(llTabContainer);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Logger.w("》》》onMeasure:" + getMeasuredHeight());
    }

    public void selectTab(int newPosition) {
        if (newPosition >= 0 && newPosition < tabs.length) {
            if (onTabSelectedListener != null) {
                if (newPosition != curSelectedPosition) {
                    onTabSelectedListener.selected(newPosition);
                } else {
                    onTabSelectedListener.reSelected(newPosition);
                }
            }

            this.curSelectedPosition = newPosition;
            radioGroup.check(curSelectedPosition);
        } else {
            Logger.w("selectTab 非法position... ");
        }
    }

    public void setOnTabSelectedListener(OnTabSelectedListener onTabSelectedListener) {
        this.onTabSelectedListener = onTabSelectedListener;
    }

    public interface OnTabSelectedListener {
        void selected(int position);

        void reSelected(int position);
    }

    public int getTabContainerHeight() {
        return llTabContainer.getMeasuredHeight() - topGrayLine.getMeasuredHeight();
    }
}

