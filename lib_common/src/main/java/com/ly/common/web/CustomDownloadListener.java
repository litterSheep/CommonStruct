package com.ly.common.web;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.DownloadListener;

import com.orhanobut.logger.Logger;

/**
 * Created by ly on 2017/9/9 10:15.
 */
public class CustomDownloadListener implements DownloadListener {

    private Activity activity;

    public CustomDownloadListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        if (TextUtils.isEmpty(url) || activity == null) {
            Logger.w("onDownloadStart url/activity is null return...");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(url));
        activity.startActivity(intent);

//        DownloadService.download(activity, url);
    }
}
