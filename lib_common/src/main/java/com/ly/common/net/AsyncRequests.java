package com.ly.common.net;

import com.ly.common.net.respEntity.BaseResponse;

/**
 * 不需要请求结果的request用这个类
 *
 * @author ly
 * date 2019/8/8 15:15
 */
public class AsyncRequests {

    private static AsyncRequests instance;

    private AsyncRequests() {
    }

    public static AsyncRequests get() {
        if (instance == null) {
            synchronized (AsyncRequests.class) {
                if (instance == null)
                    instance = new AsyncRequests();
            }
        }
        return instance;
    }

    public void uploadOpenLog() {
        ReqManager.get().uploadOpenLog().enqueue(new ReqCallbackSim<BaseResponse>() {
        });
    }

}
