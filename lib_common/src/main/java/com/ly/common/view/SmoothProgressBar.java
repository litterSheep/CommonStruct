package com.ly.common.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;


import com.ly.common.R;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 平滑进度条
 * 只需调用{@link #start()} 开始读条，调用{@link #stop()}结束加载，并不要实时传入实际加载进度值
 * Created by ly on 2017/9/4 15:00.
 */
public class SmoothProgressBar extends ProgressBar {

    /**
     * 进度条的最大值，越大越平滑，建议100-300，太大会导致页面实际上已经加载完，但进度条还未加载完
     */
    private static final int MAX_PROGRESS = 600;
    /**
     * handler 初始delay 越小越平滑
     */
    private static final long DEFAULT_DELAY = 25;//ms

    private boolean isFinish;
    private Handler handler;

    public SmoothProgressBar(Context context) {
        this(context, null, android.R.attr.progressBarStyleHorizontal);
        init();
    }

    public SmoothProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.progressBarStyleHorizontal);
        init();
    }

    public SmoothProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setProgressDrawable(getResources().getDrawable(R.drawable.progressbar_h));
//        setIndeterminate(true);
//        setIndeterminateDrawable(getResources().getDrawable(R.drawable.progressbar_h));
    }

    /**
     * 开始读条
     * Created by ly on 2017/9/5 12:03
     */
    public void start() {
        if (handler == null)
            handler = new Handler(Looper.myLooper());
        setVisibility(VISIBLE);
        setMax(MAX_PROGRESS);

        go(new AtomicLong(DEFAULT_DELAY));
    }

    private void go(@NonNull final AtomicLong atomicLong) {
        if (handler == null)
            return;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int currentProgress = getProgress();

                //Logger.d("currentProgress:" + currentProgress + " isFinish:" + isFinish);
                if (isFinish) {//如果已经命令结束，则继续加快循环，结束进度
                    if (currentProgress >= MAX_PROGRESS) {
                        release();//跳出循环
                        return;
                    } else {
                        atomicLong.decrementAndGet();
                    }
                    setProgress(currentProgress + MAX_PROGRESS * 10 / (MAX_PROGRESS - currentProgress));

                } else {//还没有命令结束，开始加速循环，达到指定值后放慢循环，等待结束的命令（始终不会让进度条读满）
                    if (currentProgress >= MAX_PROGRESS * 0.9) {
                        setProgress(currentProgress);
                    } else if (currentProgress >= MAX_PROGRESS * 0.75) {
                        atomicLong.set(atomicLong.get() + DEFAULT_DELAY);
                        setProgress(currentProgress + 1);

                    } else if (currentProgress >= MAX_PROGRESS * 0.7) {
                        if (atomicLong.get() < DEFAULT_DELAY)
                            atomicLong.set(DEFAULT_DELAY);
                        atomicLong.incrementAndGet();
                        setProgress(currentProgress + 1);

                    } else if (currentProgress >= MAX_PROGRESS * 0.5) {
                        if (atomicLong.get() <= 0)
                            atomicLong.set(0);
                        atomicLong.incrementAndGet();
                        setProgress(currentProgress + 1);

                    } else {
                        atomicLong.decrementAndGet();
                        setProgress(currentProgress + 2);
                    }
                }

                go(atomicLong);
            }
        }, atomicLong.get() <= 0 ? 0 : atomicLong.get());
    }

    /**
     * 发出动作完成的命令，之后会直到进度条读满后消失（不能保证立即停止）
     * Created by ly on 2017/9/5 12:01
     */
    public void stop() {
        isFinish = true;
        if (handler == null)//在这之前没有start
            setVisibility(GONE);
    }

    /**
     * 释放资源，activity结束时务必要调用
     * Created by ly on 2017/9/5 12:03
     */
    public void release() {
        setVisibility(GONE);
        setIndeterminate(false);
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        handler = null;
        isFinish = false;
    }

}
