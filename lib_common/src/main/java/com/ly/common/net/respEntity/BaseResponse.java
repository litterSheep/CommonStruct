package com.ly.common.net.respEntity;

public class BaseResponse<T> {

    public int code;//1：成功；0：失败
    public boolean success;
    public String msg;
    public T data;

    public BaseResponse(int code) {
        this.code = code;
    }

    public boolean isSuccessWithNoData() {
        return data == null;
    }
}
