package com.ly.module_main;

import com.ly.common.frame.BaseActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected String getPageTitle() {
        return null;
    }

    @Override
    protected void initViews() {
    }

    @Override
    public void onBackPressed() {
        //按返回键不结束activity
        moveTaskToBack(true);
    }

}