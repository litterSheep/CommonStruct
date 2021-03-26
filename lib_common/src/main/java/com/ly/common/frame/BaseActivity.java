package com.ly.common.frame;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;

import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.ly.common.R;
import com.ly.common.annotation.PageTitle;
import com.ly.common.annotation.UseEventBus;
import com.ly.common.annotation.UseLoadSir;
import com.ly.common.manager.AppManager;
import com.ly.common.net.ReqCallback;
import com.ly.common.net.ReqStatusListener;
import com.ly.common.net.respEntity.BaseResponse;
import com.ly.common.utils.NetUtils;
import com.ly.common.utils.ToastUtil;
import com.ly.common.view.CustomTitleBar;
import com.ly.common.view.dialog.LoadingDialog;
import com.ly.common.view.loadSirCallBack.EmptyCallback;
import com.ly.common.view.loadSirCallBack.ErrorCallback;
import com.ly.common.view.loadSirCallBack.LoadingCallback;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

/**
 * 子类如果同时重写了onCreate和initViews/loadData 需要注意这几个方法的执行顺序
 * Created by ly on 2017/2/10 11:02.
 */
public abstract class BaseActivity extends StatusBarActivity implements ReqStatusListener {

    public List<Call> calls;
    public LoadingDialog loadingDialog;
    public LoadService loadService;
    private boolean showLoading;
    protected CustomTitleBar topTitleBar;
    protected MyHandler handler;
    protected View rootView;
    private String activityName;
    private boolean mIsUseLoadSir;
    //默认都需要标题 如果不需要标题，在对应的类上添加@PageTitle(isNeedTitle = false)
    private boolean mIsNeedTitle = true;
    private boolean mUseEventBus = false;
    public boolean showPermissionDenyToast = true;
    private OnRequestPermissionsResult onRequestPermissionsResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置竖屏显示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initAnnotation();

        if (mIsNeedTitle) {
            rootView = View.inflate(this, R.layout.activity_with_title, null);
        } else {
            rootView = View.inflate(this, R.layout.activity_no_title, null);
        }
        addContent();

        setContentView(rootView);

        if (mIsNeedTitle)
            initTitle();
        initViews();

        initData();
        loadData();
    }

    private void initAnnotation() {
        if (getClass().isAnnotationPresent(UseLoadSir.class)) {
            UseLoadSir annotation = getClass().getAnnotation(UseLoadSir.class);
            mIsUseLoadSir = annotation != null && annotation.useLoadSir();
        }
        if (getClass().isAnnotationPresent(PageTitle.class)) {
            PageTitle annotation = getClass().getAnnotation(PageTitle.class);
            mIsNeedTitle = annotation != null && annotation.isNeedTitle();
        }
        if (getClass().isAnnotationPresent(UseEventBus.class)) {
            UseEventBus annotation = getClass().getAnnotation(UseEventBus.class);
            mUseEventBus = annotation != null && annotation.useEventBus();
        }
    }

    private void initData() {
//        if (BaseApp.get().isDebug()) {//严苛模式
//	         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//	                 .detectDiskReads()
//	                 .detectDiskWrites()
//	                 .detectNetwork()   // or .detectAll() for all detectable problems
//	                 .penaltyLog()
//	                 .build());
//	         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//	                 .detectLeakedSqlLiteObjects()
//	                 .detectLeakedClosableObjects()
//	                 .penaltyLog()
//	                 .penaltyDeath()
//	                 .build());
//        }

        if (mUseEventBus)
            EventBus.getDefault().register(this);

        AppManager.get().addActivity(this);

        activityName = getClass().getSimpleName();
    }

    protected abstract @LayoutRes
    int getLayoutResId();

    protected abstract String getPageTitle();

    public void loadData(boolean showLoading) {
        this.showLoading = showLoading;
        if (showLoading && mIsUseLoadSir && loadService != null)
            loadService.showCallback(LoadingCallback.class);
    }

    public void loadData() {
        loadData(true);
    }

    /**
     * 初始化View
     */
    protected abstract void initViews();

    private void addContent() {
        FrameLayout flContent = rootView.findViewById(R.id.fl_content);
        View loadSirContent = null;
        boolean isContentLayoutEmpty = false;
        try {
            loadSirContent = LayoutInflater.from(this).inflate(getLayoutResId(), new RelativeLayout(this), true);
//            loadSirContent = View.inflate(this, getLayoutResId(), null);
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
        if (loadSirContent != null) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            flContent.addView(loadSirContent, params);
        } else {
            loadSirContent = flContent;
            isContentLayoutEmpty = true;
        }
        //保持标题部分显示加载中、加载失败的页面
        if (mIsUseLoadSir) {
            loadService = LoadSir.getDefault().register(loadSirContent, (Callback.OnReloadListener) v -> {
                if (NetUtils.isConnected(getApplicationContext())) {
                    loadData();
                } else {
                    showToast(R.string.network_unavailable);
                }
            });
            if (isContentLayoutEmpty)
                rootView = loadService.getLoadLayout();
        }
    }

    public void initTitle() {
        topTitleBar = findViewById(R.id.top_title);
        if (topTitleBar != null) {
            topTitleBar.setTitleText(getPageTitle());
            topTitleBar.setOnLeftClickListener(this::finish);
        }
    }

    public void setTitle(String title) {
        if (topTitleBar != null)
            topTitleBar.setTitleText(title);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        cancelRequests();
        AppManager.get().removeActivity(this);
        dismissDialog();
        if (mUseEventBus)
            EventBus.getDefault().unregister(this);
    }

    public void cancelRequests() {
        if (calls != null && !calls.isEmpty())
            for (Call call : calls) {
                call.cancel();
            }
    }

    public MyHandler getHandler() {
        if (handler == null)
            handler = new MyHandler(this);
        return handler;
    }

    public void onHandleMessage(Message msg) {
    }

    public void startActivity(Class cls) {
        startActivity(new Intent(this, cls));
    }

    public void startActivityForResult(Class cls, int requestCode) {
        startActivityForResult(new Intent(this, cls), requestCode);
    }

    public void dismissDialog() {
        if (loadingDialog != null && loadingDialog.isShowing())
            loadingDialog.dismiss();
    }

    public void showDialog() {
        getLoadingDialog();
        if (!loadingDialog.isShowing())
            loadingDialog.show();
    }

    public LoadingDialog getLoadingDialog() {
        if (loadingDialog == null)
            loadingDialog = new LoadingDialog(this);
        return loadingDialog;
    }

    public int getColorById(@ColorRes int colorId) {
        int color = 0;
        try {
            color = getResources().getColor(colorId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return color;
    }

    public String getStringById(@StringRes int stringID) {
        String str = "";
        try {
            str = getResources().getString(stringID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public void showToast(String text) {
        ToastUtil.showShort(text);
    }

    public void showToast(@StringRes int stringId) {
        showToast(getStringById(stringId));
    }

    /**
     * 隐藏软键盘
     */
    public void hideSoftKeyboard() {
        try {
            //如果打开了软键盘，则隐藏
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && imm.isActive()) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class MyHandler extends Handler {
        WeakReference<BaseActivity> mActivity;

        private MyHandler(BaseActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NotNull Message msg) {
            super.handleMessage(msg);
            BaseActivity activity = mActivity.get();
            if (activity != null)
                activity.onHandleMessage(msg);
        }
    }

    public void reqPermissions(String[] permissions, int reqCode) {
        ActivityCompat.requestPermissions(this, permissions, reqCode);
    }

    /**
     * 统一处理权限被拒toast提示用户
     * 1.在继承此类的activity 通过ActivityCompat.requestPermissions申请权限 会回调该方法
     * 2.在继承此类的activity中创建的fragment 通过requestPermissions申请权限 同样会回调该函数
     * Created by ly on 2018/11/15 9:38
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (onRequestPermissionsResult != null)
            onRequestPermissionsResult.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            String permission = permissions[i];
            if (showPermissionDenyToast && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                switch (permission) {
                    case Manifest.permission.CAMERA:
                        ToastUtil.showLong("相机启动失败，请到设置-权限管理中允许访问相机权限");
                        break;
                    case Manifest.permission.READ_CONTACTS:
                        ToastUtil.showLong("读取联系人失败，请到设置-权限管理中允许访问电话、通讯录权限");
                        break;
                    case Manifest.permission.ACCESS_COARSE_LOCATION:
                        ToastUtil.showLong("获取位置失败，请到设置-权限管理中允许访问位置权限");

                        break;
                    case Manifest.permission.READ_CALL_LOG:
                        ToastUtil.showLong("获取通话记录失败，请到设置-权限管理中允许访问通话记录权限");

                        break;
                    default:
                        break;
                }
            } else {
                Logger.i("已允许permission：" + permission);
            }
        }
    }

    public interface OnRequestPermissionsResult {
        void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
    }

    public void setOnRequestPermissionsResult(OnRequestPermissionsResult onRequestPermissionsResult) {
        this.onRequestPermissionsResult = onRequestPermissionsResult;
    }

    public <T> void req(@NonNull Call<BaseResponse<T>> call, @NonNull ReqCallback<BaseResponse<T>> callback) {
        call.enqueue(callback);
        if (calls == null)
            calls = new ArrayList<>();
        calls.add(call);
    }

    public <T> void req(@NonNull Call<BaseResponse<T>> call) {
        call.enqueue(null);
    }

    @Override
    public void onEmpty() {
        if (showLoading && mIsUseLoadSir && loadService != null)
            loadService.showCallback(EmptyCallback.class);
    }

    @Override
    public void onError() {
        if (showLoading && mIsUseLoadSir && loadService != null)
            loadService.showCallback(ErrorCallback.class);
    }

    @Override
    public void onSuccess() {
        if (showLoading && mIsUseLoadSir && loadService != null)
            loadService.showSuccess();
    }

    @Override
    public void onDismissDialog() {
        dismissDialog();
    }

    @Override
    public boolean moveTaskToBack(boolean nonRoot) {
        if (!nonRoot) {
            if (!isTaskRoot()) {
                return false;
            }
        }
        return super.moveTaskToBack(nonRoot);
    }
}
