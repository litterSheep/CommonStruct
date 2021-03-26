package com.ly.common.frame;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

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
    public View rootView;
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
        if (null == rootView)
            if (mIsNeedTitle) {
                rootView = inflater.inflate(R.layout.activity_with_title, container, false);
            } else {
                rootView = inflater.inflate(R.layout.activity_no_title, container, false);
            }
        addContent();
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
    }

    private void addContent() {
        FrameLayout flContent = rootView.findViewById(R.id.fl_content);
        View loadSirContent = null;
        boolean isContentLayoutEmpty = false;
        try {
            loadSirContent = View.inflate(getContext(), getLayoutResId(), null);
        } catch (Exception e) {
            e.printStackTrace();
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
        if (mIsNeedLoadSir) {
            loadService = LoadSir.getDefault().register(loadSirContent, (Callback.OnReloadListener) v -> {
                if (NetUtils.isConnected(getContext())) {
                    loadData();
                } else {
                    showToast(R.string.network_unavailable);
                }
            });
            if (isContentLayoutEmpty)
                rootView = loadService.getLoadLayout();
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
        if(calls!=null&&!calls.isEmpty())
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
