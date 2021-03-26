package com.ly.common.manager;

import android.app.Activity;

import java.util.Stack;

/**
 * activity管理
 */
public class AppManager {

    private static Stack<Activity> activityStack;
    private volatile static AppManager instance;

    private AppManager() {
    }

    /**
     * 单一实例
     */
    public static AppManager get() {
        if (instance == null) {
            synchronized (AppManager.class) {
                if (instance == null) {
                    instance = new AppManager();
                    activityStack = new Stack<>();
                }
            }

        }
        return instance;
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<>();
        }
        if (!activityStack.empty()) {
            Activity topActivity = activityStack.peek();
            if (topActivity != null && activity != null
                    && topActivity.getClass() == activity.getClass()) {//如果栈顶已经存在一样的activity则移除，避免相同的activity出现多次
                finishActivity(topActivity);
            }
        }
        activityStack.add(activity);
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public Activity currentActivity() {
        try {
            return activityStack.lastElement();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取当前Activity的前一个Activity
     */
    public Activity preActivity() {
        int index = activityStack.size() - 2;
        if (index < 0) {
            return null;
        }
        return activityStack.get(index);
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    public void finishActivity() {
        Activity activity = activityStack.lastElement();
        finishActivity(activity);
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * 移除指定的Activity
     */
    public void removeActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity = null;
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls) {
        try {
            for (Activity activity : activityStack) {
                if (activity.getClass() == cls) {
                    finishActivity(activity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }

    /**
     * 返回到指定的activity
     */
    public void returnToActivity(Class<?> cls) {
        while (activityStack.size() != 0)
            if (activityStack.peek().getClass() == cls) {
                break;
            } else {
                finishActivity(activityStack.peek());
            }
    }

    /**
     * 是否已经打开指定的activity
     */
    public boolean isOpenActivity(Class<?> cls) {
        if (activityStack != null) {
            for (int i = 0, size = activityStack.size(); i < size; i++) {
                if (cls == activityStack.get(i).getClass()) {
                    return true;
                }
            }
        }
        return false;
    }

}