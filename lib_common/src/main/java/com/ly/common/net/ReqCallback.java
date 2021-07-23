package com.ly.common.net;

import androidx.annotation.NonNull;


import com.ly.common.R;
import com.ly.common.frame.BaseApp;
import com.ly.common.net.respEntity.BaseResponse;
import com.ly.common.utils.NetUtils;
import com.ly.common.utils.ToastUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 通用callback,回调onResult时，保证了data!=null
 * 处理了错误信息toast，同时可选择重写onFailure进行错误处理
 *
 * @author ly
 * date 2019/8/7 16:33
 */
public abstract class ReqCallback<T extends BaseResponse> implements Callback<T> {

    public ReqStatusListener reqLoadSirListener, reqDialogListener;
    private boolean showToast = true;

    public ReqCallback() {
    }

    public ReqCallback(ReqStatusListener reqLoadSirListener, ReqStatusListener reqDialogListener) {
        this.reqLoadSirListener = reqLoadSirListener;
        this.reqDialogListener = reqDialogListener;
    }

    public ReqCallback(ReqStatusListener reqDialogListener) {
        this.reqDialogListener = reqDialogListener;
    }

    public ReqCallback(boolean showToast) {
        this.showToast = showToast;
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        dismissDialog();
        T body = response.body();
        if (response.isSuccessful()) {
            if (body != null) {
                if (body.success) {
                    if (body.data != null) {
                        if (reqLoadSirListener != null)
                            reqLoadSirListener.onSuccess();
                        onResult(body);
                    } else {
                        if (reqLoadSirListener != null)
                            reqLoadSirListener.onEmpty();
                        onResult();
                    }
                    return;
                } else {
                    if (showToast)
                        ToastUtil.showShort(body.msg);
                }
            }
        }
        onFailure(body != null ? body : new BaseResponse(RespCode.ERROR));
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        dismissDialog();
        if (showToast)
            if (NetUtils.isConnected(BaseApp.get().getApplicationContext())) {
                ToastUtil.showShort(BaseApp.get().getApplicationContext().getString(R.string.t_network_bad));
            } else {
                ToastUtil.showShort(BaseApp.get().getApplicationContext().getString(R.string.connect_server_fail));
            }
        onFailure(new BaseResponse(RespCode.ERROR));
    }

    public void onResult() {
    }

    public void onResult(@NonNull T t) {
    }

    /**
     * 网络请求失败，或者后台返回的data为空时调用
     * 需要处理失败的情况则重写该方法
     * 如果子类有重写onFailure，并弹toast，则以子类的为准
     *
     * @param error 具体错误信息
     * @author ly on 2020/6/9 16:16
     */
    public void onFailure(@NonNull BaseResponse error) {
        if (reqLoadSirListener != null) {
            reqLoadSirListener.onDismissDialog();
            if (error.isSuccessWithNoData()) {
                reqLoadSirListener.onEmpty();
            } else {
                reqLoadSirListener.onError();
            }
        }
    }

    private void dismissDialog() {
        if (reqDialogListener != null)
            reqDialogListener.onDismissDialog();
    }
}
