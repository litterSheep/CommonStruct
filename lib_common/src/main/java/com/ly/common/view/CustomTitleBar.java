package com.ly.common.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.res.ResourcesCompat;

import com.ly.common.R;
import com.ly.common.utils.PixelUtil;
import com.ly.common.utils.ScreenUtil;
import com.orhanobut.logger.Logger;


/**
 * Android自定义标题栏
 */

public class CustomTitleBar extends RelativeLayout {

    //顶部左右图标的大小
    private int ICON_SIZE;

    private RelativeLayout  rl_title_right;
    private TextView tvLeftBtn, tvClose;
    private TextView tvRightBtn, tvTips;
    private TextView tvTitle;
    private View tvLine;

    //左边按钮相关
    private int leftBtnImgId;
    private int leftCloseBtnImgId;
    private String leftBtnText;
    private int leftBtnTextColor;
    private int leftBtnTextSize;

    //标题相关
    private int titleBackground;
    private String titleText;
    private int titleTextColor;
    private int titleTextSize;

    //右边按钮相关
    private int rightBtnImageId;
    private String rightBtnText;
    private int rightBtnTextColor;
    private int rightBtnTextSize;

    private boolean showLeftBtn;
    private boolean showLeftCloseBtn;
    private boolean showRightBtn;
    private boolean showGrayLine;
    private boolean showTips;

    private OnLeftClickListener onLeftClickListener;
    private OnLeftCloseClickListener onLeftCloseClickListener;
    private OnRightClickListener onRightClickListener;

    public CustomTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.pub_titlebar, this, true);
        rl_title_right = findViewById(R.id.rl_title_right);
        tvLeftBtn = findViewById(R.id.tv_title_left_btn);
        tvRightBtn = findViewById(R.id.tv_title_right_btn);
        tvTips = findViewById(R.id.tv_title_tips);
        tvTips.setVisibility(GONE);
        tvTitle = findViewById(R.id.m_tv_scenic_title);
        tvLine = findViewById(R.id.v_title_line);
        tvClose = findViewById(R.id.tv_title_left_close);

        init();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            //腾出状态栏高度
            setPadding(getPaddingLeft(), ScreenUtil.getStatusBarHeight(), getPaddingRight(), 0);
    }

    private void init() {
        ICON_SIZE = PixelUtil.dp2px(16);

        leftBtnImgId = R.mipmap.ic_title_left;
        titleBackground = getResources().getColor(R.color.top_title);
        titleTextColor = getResources().getColor(R.color.top_title_text);
        rightBtnTextColor = getResources().getColor(R.color.main_color);
        leftBtnTextColor = getResources().getColor(R.color.top_title_text);
        titleTextSize = 18;
        leftBtnTextSize = 16;
        rightBtnTextSize = 14;

        setTitleBackground(titleBackground);
        setTitleText(titleText);
        setTitleTextSize(titleTextSize);
        setTitleTextColor(titleTextColor);

        setLeftBtnTextColor(leftBtnTextColor);
        setLeftBtnTextSize(leftBtnTextSize);
        setRightBtnTextColor(rightBtnTextColor);
        setRightBtnTextSize(rightBtnTextSize);

//        setLeftBtnImgId(leftBtnImgId);
        showLeftBtn(true);
        showLeftCloseBtn(false);
        showRightBtn(false);
        showRightBtnTips(false);
        showGrayLine(false);

        tvLeftBtn.setOnClickListener(v -> {
            if (onLeftClickListener != null)
                onLeftClickListener.onLeftClick();
        });
        tvClose.setOnClickListener(v -> {
            if (onLeftCloseClickListener != null)
                onLeftCloseClickListener.onLeftCloseClick();
        });
        tvRightBtn.setOnClickListener(v -> {
            if (onRightClickListener != null)
                onRightClickListener.onRightClick();
        });
    }

    /**
     * 设置返回按钮的资源图片id
     *
     * @param leftBtnImgId 资源图片id
     */
    public void setLeftBtnImgId(@DrawableRes int leftBtnImgId) {
        setLeftBtnImgId(leftBtnImgId, ICON_SIZE, ICON_SIZE);
    }

    /**
     * 设置返回按钮的资源图片id
     *
     * @param leftBtnImgId 资源图片id
     */
    public void setLeftBtnImgId(@DrawableRes int leftBtnImgId, int imgW, int imgH) {
        this.leftBtnImgId = leftBtnImgId;
        if (leftBtnImgId != 0) {
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), leftBtnImgId, getContext().getTheme());
            if (drawable != null) {
                drawable.setBounds(0, 0, imgW, imgH);
                tvLeftBtn.setPadding(tvLeftBtn.getPaddingLeft(), tvLeftBtn.getPaddingTop(), tvLeftBtn.getPaddingRight(), tvLeftBtn.getPaddingBottom());
                tvLeftBtn.setCompoundDrawables(drawable, null, null, null);
            }
        } else {
            tvLeftBtn.setCompoundDrawables(null, null, null, null);
        }
    }

    public void setLeftCloseBtnImg(int leftCloseBtnImgId) {
        this.leftCloseBtnImgId = leftCloseBtnImgId;
        if (leftCloseBtnImgId != 0) {
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), leftCloseBtnImgId, getContext().getTheme());
            if (drawable != null) {
                drawable.setBounds(0, 0, ICON_SIZE - 8, ICON_SIZE - 8);
                tvClose.setCompoundDrawables(drawable, null, null, null);
            }
        } else {
            tvClose.setCompoundDrawables(null, null, null, null);
        }
    }

    /**
     * 设置右边按钮的资源图片
     */
    public void setRightBtnImageId(@DrawableRes int rightBtnImageId, int imgW, int imgH) {
        this.rightBtnImageId = rightBtnImageId;
        if (rightBtnImageId != 0) {
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), rightBtnImageId, getContext().getTheme());
            if (drawable != null) {
                drawable.setBounds(0, 0, imgW, imgH);
                tvRightBtn.setPadding(tvRightBtn.getPaddingLeft(), tvRightBtn.getPaddingTop(), tvRightBtn.getPaddingRight(), tvRightBtn.getPaddingBottom());
                tvRightBtn.setCompoundDrawables(null, null, drawable, null);
            }
        }
        showRightBtn(true);
    }

    public void setRightBtnImageId(@DrawableRes int rightBtnImageId) {
        setRightBtnImageId(rightBtnImageId, ICON_SIZE, ICON_SIZE);
    }

    public void showRightBtn(boolean showRightBtn) {
        this.showRightBtn = showRightBtn;
        tvRightBtn.setVisibility(showRightBtn ? VISIBLE : INVISIBLE);
    }

    public void showLeftBtn(boolean showLeftBtn) {
        this.showLeftBtn = showLeftBtn;
        tvLeftBtn.setVisibility(showLeftBtn ? VISIBLE : INVISIBLE);
    }

    /**
     * 是否显示左边关闭的按钮（用于web页面）
     * Created by ly on 2018/4/14 15:54
     */
    public void showLeftCloseBtn(boolean showLeftCloseBtn) {
        this.showLeftCloseBtn = showLeftCloseBtn;
        tvClose.setVisibility(showLeftCloseBtn ? VISIBLE : GONE);
    }

    /**
     * 显示右边按钮的消息红点
     * Created by ly on 2017/6/9 16:10
     */
    public void showRightBtnTips(boolean showTips) {
        this.showTips = showTips;
        tvTips.setVisibility(showTips ? VISIBLE : GONE);
    }

    /**
     * 是否显示标题底部灰线
     * Created by ly on 2018/4/14 15:28
     */
    public void showGrayLine(boolean showGrayLine) {
        this.showGrayLine = showGrayLine;
        tvLine.setVisibility(showGrayLine ? VISIBLE : GONE);
    }

    public void setTitleText(String titleText) {
        tvTitle.setText(titleText);
    }

    public void setTitleText(@StringRes int titleTextRes) {
        String title = null;
        if (titleTextRes != 0) {
            try {
                title = getContext().getString(titleTextRes);
            } catch (Exception e) {
                Logger.w(e.getMessage());
            }
        }
        setTitleText(title);
    }

    public void setTitleBackground(@ColorInt int titleBackground) {
        this.titleBackground = titleBackground;
        setBackgroundColor(titleBackground);
//        tvLeftBtn.setBackgroundColor(titleBackground);
//        tvClose.setBackgroundColor(titleBackground);
    }

    public void setTitleBackgroundRes(@ColorRes int titleBackground) {
        this.titleBackground = titleBackground;
        setBackgroundResource(titleBackground);
//        tvLeftBtn.setBackgroundColor(titleBackground);
//        tvClose.setBackgroundColor(titleBackground);
    }

    public void setTitleTextColor(@ColorInt int titleTextColor) {
        this.titleTextColor = titleTextColor;
        tvTitle.setTextColor(titleTextColor);
    }

    public void setTitleTextSize(int titleTextSize) {
        this.titleTextSize = PixelUtil.sp2px(titleTextSize);
        tvTitle.setTextSize(titleTextSize);
    }

    public void setLineColor(@ColorInt int color) {
        tvLine.setBackgroundColor(color);
    }

    public void setLeftBtnText(String leftBtnText) {
        this.leftBtnText = leftBtnText;
        tvLeftBtn.setCompoundDrawables(null, null, null, null);
        tvLeftBtn.setText(leftBtnText);
    }

    public void setLeftBtnTextColor(int leftBtnTextColor) {
        this.leftBtnTextColor = leftBtnTextColor;
        tvLeftBtn.setTextColor(leftBtnTextColor);
    }

    public void setLeftBtnTextSize(int leftBtnTextSize) {
        this.leftBtnTextSize = PixelUtil.sp2px(leftBtnTextSize);
        tvLeftBtn.setTextSize(leftBtnTextSize);
    }

    public void setRightBtnText(String rightBtnText) {
        this.rightBtnText = rightBtnText;
        tvRightBtn.setText(rightBtnText);
        if (!TextUtils.isEmpty(rightBtnText)) {
            showRightBtn(true);
            int beyondLength = rightBtnText.length() - 2;
            if (beyondLength > 0) {//超出2个字后，处理标题文字居中展示
                LayoutParams layoutParams = (LayoutParams) tvTitle.getLayoutParams();
                layoutParams.leftMargin = PixelUtil.dp2px(12) * beyondLength;
                tvTitle.setLayoutParams(layoutParams);
            }
        }
    }

    public void setRightBtnTextColor(int rightBtnTextColor) {
        this.rightBtnTextColor = rightBtnTextColor;
        tvRightBtn.setTextColor(rightBtnTextColor);
    }

    public void setRightBtnTextSize(int rightBtnTextSize) {
        this.rightBtnTextSize = PixelUtil.sp2px(rightBtnTextSize);
        tvRightBtn.setTextSize(rightBtnTextSize);
    }

    public TextView getTvTitle() {
        return tvTitle;
    }

    public TextView getTvLeftBtn() {
        return tvLeftBtn;
    }

    public TextView getTvClose() {
        return tvClose;
    }

    public TextView getTvRightBtn() {
        return tvRightBtn;
    }

    public TextView getTvTips() {
        return tvTips;
    }

    public View getTvLine() {
        return tvLine;
    }

    public int getLeftBtnImgId() {
        return leftBtnImgId;
    }

    public String getLeftBtnText() {
        return leftBtnText;
    }

    public int getLeftBtnTextColor() {
        return leftBtnTextColor;
    }

    public int getLeftBtnTextSize() {
        return leftBtnTextSize;
    }

    public boolean isShowLeftBtn() {
        return showLeftBtn;
    }

    public int getTitleBackground() {
        return titleBackground;
    }

    public String getTitleText() {
        return titleText;
    }

    public int getTitleTextColor() {
        return titleTextColor;
    }

    public int getTitleTextSize() {
        return titleTextSize;
    }

    public boolean isShowRightBtn() {
        return showRightBtn;
    }

    public int getRightBtnImageId() {
        return rightBtnImageId;
    }

    public String getRightBtnText() {
        return rightBtnText;
    }

    public int getRightBtnTextColor() {
        return rightBtnTextColor;
    }

    public int getRightBtnTextSize() {
        return rightBtnTextSize;
    }

    public boolean isShowGrayLine() {
        return showGrayLine;
    }

    public boolean isShowTips() {
        return showTips;
    }

    public boolean isShowLeftCloseBtn() {
        return showLeftCloseBtn;
    }

    public int getLeftCloseBtnImgId() {
        return leftCloseBtnImgId;
    }

    public void adjustStyle4WV() {
        LayoutParams layoutParams = (LayoutParams) tvTitle.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.leftMargin = PixelUtil.dp2px(5);
        layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.tv_title_left_close);
        tvTitle.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        rl_title_right.setVisibility(GONE);
    }

    public void setOnLeftClickListener(OnLeftClickListener onLeftClickListener) {
        this.onLeftClickListener = onLeftClickListener;
    }

    public void setOnLeftCloseClickListener(OnLeftCloseClickListener onLeftCloseClickListener) {
        this.onLeftCloseClickListener = onLeftCloseClickListener;
    }

    public void setOnRightClickListener(OnRightClickListener onRightClickListener) {
        this.onRightClickListener = onRightClickListener;
    }

    public interface OnLeftClickListener {
        void onLeftClick();
    }

    public interface OnLeftCloseClickListener {
        void onLeftCloseClick();
    }

    public interface OnRightClickListener {
        void onRightClick();
    }
}
