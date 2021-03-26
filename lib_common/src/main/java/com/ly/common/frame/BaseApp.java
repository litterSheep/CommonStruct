package com.ly.common.frame;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Looper;

import androidx.multidex.MultiDex;

import com.alibaba.android.arouter.launcher.ARouter;
import com.facebook.stetho.Stetho;
import com.kingja.loadsir.core.LoadSir;
import com.ly.common.R;
import com.ly.common.glide.GlideApp;
import com.ly.common.utils.AppUtils;
import com.ly.common.utils.CrashHandler;
import com.ly.common.view.loadSirCallBack.EmptyCallback;
import com.ly.common.view.loadSirCallBack.ErrorCallback;
import com.ly.common.view.loadSirCallBack.LoadingCallback;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import io.objectbox.android.AndroidObjectBrowser;

/**
 * @author ly
 * date 2019/7/26 17:00
 */
public class BaseApp extends Application {

    private static BaseApp instance;
    public static BaseApp get() {
        return instance;
    }

    static {
        ClassicsHeader.REFRESH_HEADER_PULLING = "";
        ClassicsHeader.REFRESH_HEADER_REFRESHING = "";
        ClassicsHeader.REFRESH_HEADER_LOADING = "";
        ClassicsHeader.REFRESH_HEADER_RELEASE = "";
        ClassicsHeader.REFRESH_HEADER_FINISH = "刷新完成";
        ClassicsHeader.REFRESH_HEADER_FAILED = "刷新失败";
        ClassicsHeader.REFRESH_HEADER_SECONDARY = "";

        ClassicsFooter.REFRESH_FOOTER_PULLING = "上拉加载更多";
        ClassicsFooter.REFRESH_FOOTER_RELEASE = "";
        ClassicsFooter.REFRESH_FOOTER_REFRESHING = "正在刷新...";
        ClassicsFooter.REFRESH_FOOTER_LOADING = "加载中...";
        ClassicsFooter.REFRESH_FOOTER_FINISH = "加载完成";
        ClassicsFooter.REFRESH_FOOTER_FAILED = "加载失败";
        ClassicsFooter.REFRESH_FOOTER_NOTHING = "没有更多了";

        //设置全局默认配置（优先级最低，会被其他设置覆盖）
        SmartRefreshLayout.setDefaultRefreshInitializer((context, layout) -> {
            //开始设置全局的基本参数（可以被下面的DefaultRefreshHeaderCreator覆盖）
            layout.setReboundDuration(400);
            layout.setEnableAutoLoadMore(true);
            layout.setEnableLoadMoreWhenContentNotFull(false);
            layout.setDragRate(0.6f);
            layout.setEnableOverScrollBounce(true);
//                layout.setReboundInterpolator(new DropBounceInterpolator());
            layout.setDisableContentWhenLoading(false);
            layout.setDisableContentWhenRefresh(false);
            layout.setEnableScrollContentWhenRefreshed(true);
            layout.setPrimaryColorsId(R.color.white, R.color.white);
            layout.setHeaderInsetStart(8);
        });

//        //设置全局的Header构建器
//        SmartRefreshLayout.setDefaultRefreshHeaderCreator((context, layout) -> new RefreshHeaderView1(context));
//        //设置全局的Footer构建器
//        SmartRefreshLayout.setDefaultRefreshFooterCreator((context, layout) -> new RefreshFooterView(context));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // dex突破65535的限制
        MultiDex.install(this);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        CrashHandler.getInstance().init();

        PrettyFormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .tag("MyLog")
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return isDebug();
            }
        });

        if (isDebug()) {
            Stetho.initializeWithDefaults(this);
            //开启InstantRun之后，一定要在ARouter.init之前调用openDebug
            ARouter.openDebug();
            ARouter.openLog();
        }
        ARouter.init(this);

        String processName = AppUtils.getProcessName(android.os.Process.myPid());
        if (processName != null && processName.equals(getPackageName())) {
            initLoadSir();
            //延迟初始化组件
            new Handler(Looper.myLooper()).postDelayed(() -> {
                //这部分只在主进程初始化
                initData();
            }, 2500);

        }
    }

    private void initData() {
        if (isDebug()) {
            boolean started = new AndroidObjectBrowser(ObjectBox.get()).start(this);
        }
    }

    private void initLoadSir() {
        LoadSir.beginBuilder()
                .addCallback(new ErrorCallback())
                .addCallback(new EmptyCallback())
                .addCallback(new LoadingCallback())
                .commit();
    }

    public boolean isDebug() {
        try {
            ApplicationInfo info = getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        GlideApp.get(this).clearMemory();
    }

    public boolean isLogin() {
        // TODO: 2021/3/23
        return false;
    }
}
