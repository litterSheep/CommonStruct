package com.ly.common.frame;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.ly.common.R;
import com.ly.common.annotation.PageTitle;
import com.ly.common.annotation.UseEventBus;
import com.ly.common.annotation.UseLoadSir;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

/**
 * @author ly
 * 2018/3/27 10:12
 */
public abstract class BaseFragment extends Fragment implements ReqStatusListener {
    protected boolean bIsViewCreated;
    protected boolean bIsDataLoaded;
    public ViewGroup rootView;
    protected View contentView;
    public BaseActivity activity;
    private boolean mIsNeedLoadSir;
    //默认不需要标题
    private boolean mIsNeedTitle = false;
    protected LoadService loadService;
    public List<Call> calls;
    private LoadingDialog loadingDialog;
    protected CustomTitleBar topTitleBar;
    protected String title;
    public LayoutInflater mInflater;
    private boolean mUseEventBus = false;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.activity = (BaseActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.mInflater = inflater;
        bIsViewCreated = true;
        initAnnotation();
        if (mUseEventBus)
            EventBus.getDefault().register(this);


        addContent(inflater, container);
        if (mIsNeedTitle)
            initTitle(rootView);
        initViews(rootView);
        return rootView;
    }

    private void initAnnotation() {
        if (getClass().isAnnotationPresent(UseLoadSir.class)) {
            UseLoadSir annotation = getClass().getAnnotation(UseLoadSir.class);
            mIsNeedLoadSir = annotation != null && annotation.useLoadSir();
        }
        if (getClass().isAnnotationPresent(PageTitle.class)) {
            PageTitle annotation = getClass().getAnnotation(PageTitle.class);
            if (annotation != null) {
                mIsNeedTitle = annotation.isNeedTitle();
                title = annotation.titleName();
            }
        }
        if (getClass().isAnnotationPresent(UseEventBus.class)) {
            UseEventBus annotation = getClass().getAnnotation(UseEventBus.class);
            mUseEventBus = annotation != null && annotation.useEventBus();
        }
    }

    private void addContent(LayoutInflater inflater, ViewGroup container) {
        ViewGroup tmp = (ViewGroup) inflater.inflate(mIsNeedTitle ? R.layout.activity_with_title : R.layout.activity_no_title, container, false);

        //此处不能保证外部传入的layout合法，所以try catch
        try {
            //加载子类传入的layout，如果需要标题就把它添加到tmp中，反之直接加载layout
            View view = inflater.inflate(getLayoutResId(), tmp, mIsNeedTitle);

            if (mIsNeedTitle) {
                //此时rootView中包含标题及子类布局两个元素
                rootView = (ViewGroup) view;
                //取出子类传入的layout
                contentView = rootView.getChildAt(1);
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
        if (mIsNeedLoadSir) {
            loadService = LoadSir.getDefault().register(contentView, (Callback.OnReloadListener) v -> {
                if (NetUtils.isConnected(getContext())) {
                    loadData();
                } else {
                    showToast(R.string.network_unavailable);
                }
            });
        }
    }

    public void initTitle(View view) {
        topTitleBar = view.findViewById(R.id.top_title);
        if (topTitleBar != null) {
            topTitleBar.setTitleText(title);
            topTitleBar.showLeftBtn(false);
            topTitleBar.showLeftCloseBtn(false);
            topTitleBar.setOnLeftClickListener(() -> {
                if (activity != null)
                    activity.finish();
            });
        }
    }

    public void setTitle(String title) {
        if (topTitleBar != null)
            topTitleBar.setTitleText(title);
    }

    public void cancelRequests() {
        if (calls != null && !calls.isEmpty())
            for (Call call : calls) {
                if (call != null && !call.isCanceled())
                    call.cancel();
            }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        bIsViewCreated = false;
        bIsDataLoaded = false;

        cancelRequests();

        if (mUseEventBus)
            EventBus.getDefault().unregister(this);

        // 解决ViewPager中的问题
        if (null != rootView) {
            final ViewParent parent = rootView.getParent();
            if (parent != null)
                ((ViewGroup) parent).removeView(rootView);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //数据懒加载
        if (bIsViewCreated && !bIsDataLoaded) {
            loadData();
            bIsDataLoaded = true;
        }
    }

    /**
     * @return 布局资源id
     */
    protected abstract int getLayoutResId();

    /**
     * 初始化View
     */
    protected void initViews(View view) {
    }

    /**
     * 加载数据
     */
    public void loadData(boolean showLoading) {
        if (showLoading && mIsNeedLoadSir && loadService != null)
            loadService.showCallback(LoadingCallback.class);
    }

    public void loadData() {
        loadData(true);
    }

    public void dismissDialog() {
        if (loadingDialog != null && loadingDialog.isShowing())
            loadingDialog.dismiss();
    }

    public void showDialog() {
        if (activity == null || activity.isFinishing())
            return;
        if (loadingDialog == null)
            loadingDialog = new LoadingDialog(activity);
        if (!loadingDialog.isShowing())
            loadingDialog.show();
    }

    public void showToast(String text) {
        ToastUtil.showShort(text);
    }

    public void showToast(int stringId) {
        showToast(getStringById(stringId));
    }

    /**
     * 隐藏软键盘
     */
    public void hideSoftKeyboard() {
        try {
            //如果打开了软键盘，则隐藏
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startActivity(Class cls) {
        startActivity(new Intent(getActivity(), cls));
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

    public <T> void req(@NonNull Call<BaseResponse<T>> call, @NonNull ReqCallback<BaseResponse<T>> callback) {
        req(call, callback, false);
    }

    public <T> void req(@NonNull Call<BaseResponse<T>> call, @NonNull ReqCallback<BaseResponse<T>> callback, boolean showLoading) {
        if (showLoading) {
            callback.reqDialogListener = this;
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
        if (mIsNeedLoadSir && loadService != null)
            loadService.showCallback(EmptyCallback.class);
    }

    @Override
    public void onError() {
        if (mIsNeedLoadSir && loadService != null)
            loadService.showCallback(ErrorCallback.class);
    }

    @Override
    public void onSuccess() {
        if (mIsNeedLoadSir && loadService != null)
            loadService.showSuccess();
    }

    @Override
    public void onDismissDialog() {
        dismissDialog();
    }
}
