package com.ly.common.frame;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ly.common.R;
import com.ly.common.utils.ScreenUtil;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 透明状态栏、导航栏处理基类
 *
 * @author ly
 * 2018/4/17 17:01
 */
public class StatusBarActivity extends AppCompatActivity implements ViewTreeObserver.OnGlobalLayoutListener {
    private int result;
    private View contentView;
    //上次的可用高度
    private int usableHeightPrevious;
    private ViewGroup.LayoutParams frameLayoutParams;
    private ViewTreeObserver viewTreeObserver;
    //底部虚拟导航栏高度
    private int navigationH;


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        init();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        init();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        init();
    }

    private void init() {
        //默认设置亮色statusBar,以适用白色主题
        setStatusBarModel(true);

        //在有虚拟导航栏的手机添加ViewTreeObserver，动态更改视图高度以适应虚拟键盘
        if (ScreenUtil.checkDeviceHasNavigationBar(getApplicationContext())) {
            navigationH = ScreenUtil.getNavigationBarHeight(getApplicationContext());
            contentView = findViewById(android.R.id.content);
            if (contentView != null) {
                viewTreeObserver = contentView.getViewTreeObserver();
                viewTreeObserver.addOnGlobalLayoutListener(this);
                frameLayoutParams = contentView.getLayoutParams();
            }
        }

        //如果手机有底部导航栏，则腾出导航栏同高度的padding，避免导航栏遮挡布局内容，这个方法不适用于动态隐藏、显示导航栏
//        if (ScreenUtil.checkDeviceHasNavigationBar(getApplicationContext())) {
//            getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0, 0, 0, ScreenUtil.getNavigationBarHeight(getApplicationContext()));
//        } else {
//            getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0, 0, 0, 0);
//        }
    }

    public void setTopPadding(@NonNull View view) {
        int top = 0;
        //腾出状态栏高度
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            top = ScreenUtil.getStatusBarHeight();
        view.setPadding(0, top, 0, 0);
    }

    private void setTransBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0及以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                getWindow().getDecorView().setSystemUiVisibility(option);
            } else {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                //注意要清除 FLAG_TRANSLUCENT_STATUS flag
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4到5.0
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);

            if (!"SplashActivity".equals(getClass().getSimpleName())
                    && !"GuideActivity".equals(getClass().getSimpleName())) {//启动页、引导页等需要全屏的页面不添加半透明遮罩
                SystemBarTintManager tintManager = new SystemBarTintManager(this);
                tintManager.setStatusBarTintEnabled(true);
                tintManager.setNavigationBarTintEnabled(false);
                tintManager.setStatusBarTintResource(R.color.status_half_transparent);//为状态栏添加半透明遮罩，避免里面的文字被覆盖
            }
        }
    }

    /**
     * 设置状态栏文字及图标颜色
     *
     * @param isDarkText true状态栏文字及图标颜色设置为深色,false:白色
     *                   Created by ly on 2018/4/17 17:12
     */
    public void setStatusBarModel(boolean isDarkText) {
        setTransBar();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            //这里只做大概判断及少量兼容，无法穷举所有手机
//            String brand = android.os.Build.BRAND.toUpperCase();
//            switch (brand) {
//                case "XIAOMI":
//                    MIUISetStatusBarMode(isDarkText);
//                    break;
//                case "MEIZU":
//                    FlymeSetStatusBarMode(isDarkText);
//                    break;
//                default:
//                    break;
//            }
//        }
        if (isDarkText) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //android6.0以后对状态栏文字颜色和图标改为暗色
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            } else {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
    }

    /**
     * 设置状态栏图标为深色和魅族特定的文字风格
     * 可以用来判断是否为Flyme用户
     * <p>
     * 详情：https://www.jianshu.com/p/7f5a9969be53
     *
     * @param dark 是否把状态栏文字及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    private void FlymeSetStatusBarMode(boolean dark) {
        try {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class
                    .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class
                    .getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (dark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            getWindow().setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 需要MIUIV6以上
     * <p>
     * 详情：https://www.jianshu.com/p/7f5a9969be53
     *
     * @param dark 是否把状态栏文字及图标颜色设置为深色
     */
    private void MIUISetStatusBarMode(boolean dark) {
        try {
            Class clazz = getWindow().getClass();
            int darkModeFlag = 0;
            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            if (dark) {
                extraFlagField.invoke(getWindow(), darkModeFlag, darkModeFlag);//状态栏透明且黑色字体
            } else {
                extraFlagField.invoke(getWindow(), 0, darkModeFlag);//清除黑色字体
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGlobalLayout() {
        if (contentView == null)
            return;
        Rect r = new Rect();
        contentView.getWindowVisibleDisplayFrame(r);
        int usableHeightNow = r.bottom;
        //改变的高度
        int change = usableHeightPrevious == 0 ? ScreenUtil.getScreenHeight() - usableHeightNow : usableHeightNow - usableHeightPrevious;
        int changeDis = Math.abs(change);
        //是否是底部导航栏展示隐藏的变化
        boolean isNavBarChange = changeDis == navigationH && usableHeightNow != usableHeightPrevious;
//        Logger.i("onGlobalLayout usableHeightNow:" + usableHeightNow + " isNavBarChange:" + isNavBarChange + " changeDis=" + changeDis + " navigationH" + navigationH);

        //当前可用高度和上次的不相等并且是导航栏展开/隐藏 则调整视图高度
        if (isNavBarChange) {
            frameLayoutParams.height = usableHeightNow;
            contentView.requestLayout();
        }

        //键盘弹出、隐藏
        if (changeDis > 150 && usableHeightNow != usableHeightPrevious) {
            onKeyboardChange(change > 0);
        }
        usableHeightPrevious = usableHeightNow;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewTreeObserver != null) {
            if (viewTreeObserver.isAlive())
                viewTreeObserver.removeOnGlobalLayoutListener(this);
            viewTreeObserver = null;
        }
        contentView = null;
        frameLayoutParams = null;
    }

    public void onKeyboardChange(boolean isHide) {

    }

}
