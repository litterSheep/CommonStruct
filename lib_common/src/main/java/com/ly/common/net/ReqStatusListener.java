package com.ly.common.net;

/**
 * @author ly
 * date 2019/8/14 11:39
 */
public interface ReqStatusListener {

    void onError();

    void onSuccess();

    void onEmpty();

    void onDismissDialog();
}
