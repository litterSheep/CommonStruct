package com.ly.common.net;

import com.ly.common.net.respEntity.BaseResponse;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 简单的网络回调，可以自主选择实现 onResult onFailure
 * 无错误信息toast
 *
 * @author ly
 * date 2019/8/7 16:33
 */
public abstract class ReqCallbackSim<T extends BaseResponse> implements Callback<T> {

    @Override
    public void onResponse(@NotNull Call<T> call, @NotNull Response<T> response) {
        if (response.isSuccessful()) {
            T body = response.body();
            if (body != null && body.success) {
                onResult(body);
            } else {
                onFailure();
            }
        } else {
            onFailure();
        }
    }

    @Override
    public void onFailure(@NotNull Call<T> call, @NotNull Throwable t) {
        onFailure();
    }

    public void onResult(@NotNull T t) {
    }

    public void onFailure() {

    }
}
