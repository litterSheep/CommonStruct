package com.ly.common.manager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 用于倒计时等任务，共用线程池
 *
 * @author ly
 * date 2019/11/1 11:36
 */
public class ExecutorServiceManager {

    private ScheduledExecutorService scheduledExecutorService;
    private static ExecutorServiceManager instance;

    public static ExecutorServiceManager get() {
        if (instance == null) {
            synchronized (ExecutorServiceManager.class) {
                if (instance == null) {
                    instance = new ExecutorServiceManager();
                }
            }
        }
        return instance;
    }

    private ExecutorServiceManager() {
    }

    public ScheduledExecutorService getExecutorService() {
        if (scheduledExecutorService == null || scheduledExecutorService.isShutdown())
            scheduledExecutorService = Executors.newScheduledThreadPool(1);
        return scheduledExecutorService;
    }
}
