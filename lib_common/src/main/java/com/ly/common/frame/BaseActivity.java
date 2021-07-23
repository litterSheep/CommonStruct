package com.ly.common.frame;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

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
import com.ly.common.manager.ActivityManager;
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
    protected ViewGroup rootView;
    protected View contentView;
    private String activityName;
    private boolean mIsUseLoadSir;
    //默认都需要标题 如果不需要标题，在对应的类上添加@PageTitle(isNeedTitle = false)
    private boolean mIsNeedTitle = true;
    private boolean mUseEventBus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置竖屏显示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initAnnotation();

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

        ActivityManager.get().addActivity(this);

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
        ViewGroup tmp = (ViewGroup) View.inflate(this, mIsNeedTitle ? R.layout.activity_with_title : R.layout.activity_no_title, null);

        //此处不能保证外部传入的layout合法，所以try catch
        try {
            //加载子类传入的layout，如果需要标题就把它添加到tmp中，反之直接加载layout
            View view = LayoutInflater.from(this).inflate(getLayoutResId(), tmp, mIsNeedTitle);

            if (mIsNeedTitle) {
                //此时rootView中包含标题及子类布局两个元素
                rootView = (ViewGroup) view;
                //取出子类传入的layout
                contentView = (ViewGroup) rootView.getChildAt(1);
            } else {
                //不确定子类传入的layout是否为viewGroup容器
                //为了保证rootView为viewGroup容器，将子类传入的view放入activity_no_title
                tmp.addView(view);
                contentView = rootView = tmp;
            }
        } catch (Exception e) {
            Logger.e(String.valueOf(e.getMessage()));
            //这种情况就是外部未传入合法的layout，我们给他一个空容器即可
            contentView = rootView = tmp;
        }
        //保持标题部分显示加载中、加载失败的页面
        if (mIsUseLoadSir) {
            loadService = LoadSir.getDefault().register(contentView, (Callback.OnReloadListener) v -> {
                if (NetUtils.isConnected(getApplicationContext())) {
                    loadData();
                } else {
                    showToast(R.string.network_unavailable);
                }
            });
        }
    }

    protected View addViewToContent(@LayoutRes int layout) {
        contentView.setVisibility(View.GONE);
        View view = LayoutInflater.from(this).inflate(layout, null, false);
        rootView.addView(view);
        return view;
    }

    protected void removeAllContent() {
        rootView.removeView(contentView);
    }

    protected void removeViewFromContent(View view) {
        rootView.removeView(view);
        contentView.setVisibility(View.VISIBLE);
    }

    public void initTitle() {
        topTitleBar = findViewById(R.id.top_title);
        if (topTitleBar != null) {
            topTitleBar.setTitleText(getPageTitle());
            topTitleBar.setOnLeftClickListener(this::finish);
        }
    }

    public void setTitle(@StringRes int str) {
        if (topTitleBar != null)
            topTitleBar.setTitleText(getStringById(str));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        cancelRequests();
        ActivityManager.get().removeActivity(this);
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

    public void showKeyboard(@NonNull EditText et) {
        et.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.showSoftInput(et, InputMethodManager.SHOW_FORCED);
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

    public void reqNoData(@NonNull Call<BaseResponse> call, @NonNull ReqCallback<BaseResponse> callback) {
        reqNoData(call, callback, false);
    }

    public void reqNoData(@NonNull Call<BaseResponse> call, @NonNull ReqCallback<BaseResponse> callback, boolean retainWhenDestroy) {
        if (callback.reqDialogListener != null) {
            showDialog();
        }
        call.enqueue(callback);
        if (!retainWhenDestroy) {
            if (calls == null)
                calls = new ArrayList<>();
            calls.add(call);
        }
    }

    public <T> void req(@NonNull Call<BaseResponse<T>> call, @NonNull ReqCallback<BaseResponse<T>> callback) {
        if (callback.reqDialogListener != null) {
            showDialog();
        }
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
