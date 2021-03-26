package com.ly.common.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ly.common.BuildConfig;
import com.ly.common.R;
import com.ly.common.utils.ApkSignUtil;
import com.ly.common.utils.AppUtils;
import com.ly.common.utils.ToastUtil;
import com.orhanobut.logger.Logger;
import java.io.File;
import java.util.HashMap;

/**
 * 调用系统下载器下载文件
 *
 * @author ly
 * date 2019/6/6 11:33
 */
public class DownloadService extends Service {
    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_UPDATE = 1;
    private static final String URL = "url";
    private static final String TYPE = "type";
    private DownloadCompleteReceiver downloadCompleteReceiver;
    private DownloadManager downloadManager;
    private HashMap<String, Integer> tasks;

    private class DownloadCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                switch (intent.getAction()) {
                    case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                        if (isDownloadSuccess(downloadId)) {
                            ToastUtil.showShort("下载完成");
                            openFile(downloadId);
                        }
                        if (tasks.isEmpty())
                            stopSelf();

                        break;
                    case DownloadManager.ACTION_NOTIFICATION_CLICKED:
                        Intent intent1 = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent1);

                        //跳转系统文件管理器
//                        File file = new File(getUrl());
//                        File parentFile = new File(file.getParent());
//                        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//                        i.setDataAndType(Uri.fromFile(parentFile), "*/*");
//                        i.addCategory(Intent.CATEGORY_OPENABLE);
//                        startActivity(i);

                        break;
                    default:

                        break;
                }
            }
        }
    }

    public static void download(@NonNull Context context, String url) {
        download(context, url, TYPE_DEFAULT);
    }

    public static void download(@NonNull Context context, String url, int type) {
        if (!TextUtils.isEmpty(url)) {
            Intent intent = new Intent(context, DownloadService.class);
            intent.putExtra(URL, url);
            intent.putExtra(TYPE, type);
            context.startService(intent);
        }
    }

    @SuppressLint("UseSparseArrays")
    @Override
    public void onCreate() {
        super.onCreate();
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        tasks = new HashMap<>();
        Logger.i("DownLoadService onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (downloadCompleteReceiver != null)
            unregisterReceiver(downloadCompleteReceiver);
        Logger.w("DownLoadService onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String url = intent.getStringExtra(URL);
            tasks.put(url, intent.getIntExtra(TYPE, TYPE_DEFAULT));
            downloadBySystem(url);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void downloadBySystem(String url) {
        if (isDownloading(url) || isDownloadSuccess(url))
            return;

        if (downloadCompleteReceiver == null) {
            downloadCompleteReceiver = new DownloadCompleteReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
            registerReceiver(downloadCompleteReceiver, intentFilter);
        }

        // 指定下载地址
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
        request.allowScanningByMediaScanner();
        // 设置通知的显示类型，下载进行时和完成后显示通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // 允许在计费流量下下载
        request.setAllowedOverMetered(true);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);//不设置该项，某些机型不展示通知栏
        // 允许该记录在下载管理界面可见
        request.setVisibleInDownloadsUi(true);
        // 允许漫游时下载
        request.setAllowedOverRoaming(true);

        //通过setMimeType方法重写从服务器返回的mime type，下载管理Ui中点击某个已下载完成文件及下载完成点击通知栏提示都会根据mimeType去打开文件
        String mimeType = getMimeTypeFromUrl(url);
        request.setMimeType(mimeType);
//        request.setMimeType("application/vnd.android.package-archive");
        // 设置下载文件保存的路径和文件名
        String fileName = URLUtil.guessFileName(url, null, mimeType);
//        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
//        另外可选一下方法，自定义下载路径
        request.setDestinationInExternalFilesDir(getApplicationContext(), "/temp", fileName);
        // 添加一个下载任务
        long downloadId = downloadManager.enqueue(request);

        ToastUtil.showShort("开始下载" + fileName);
        Logger.i("download task create success downloadId:" + downloadId + " fileName:" + fileName);
    }

    /**
     * 根据downloadId打开下载的文件
     *
     * @author ly on 2019/6/6 16:10
     */
    private void openFile(long downloadId) {
        String url = getUrlById(downloadId);
        Integer downloadType = tasks.get(url);

        //执行完安装动作，表明这个任务已经完成，从task中移除
        tasks.remove(url);
        //如果是版本升级，则校验apk签名，校验通过则安装
        if (downloadType != null && downloadType == TYPE_UPDATE) {
            if (!isLegalApk(downloadId))
                return;
        }
        String mimeType = downloadManager.getMimeTypeForDownloadedFile(downloadId);
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = "*/*";
        }
        Uri uri = downloadManager.getUriForDownloadedFile(downloadId);
        if (uri != null) {
            Intent handlerIntent = new Intent(Intent.ACTION_VIEW);
            handlerIntent.setDataAndType(uri, mimeType);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                handlerIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//这一步很重要。给目标应用一个临时的授权。
            }
            handlerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(handlerIntent);
        }
    }

    private boolean isDownloading(String url) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PENDING);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            if (c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI)).equals(url)) {
                ToastUtil.showShort("任务已经存在");
                c.close();
                return true;
            }
        }
        c.close();
        return false;
    }

    /**
     * 任务是否下载成功，成功则查询出downloadId自动打开文件
     *
     * @author ly on 2019/6/6 16:10
     */
    private boolean isDownloadSuccess(String url) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            if (c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI)).equals(url)) {
                String uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                String filePath = Uri.parse(uri).getPath();
                if (TextUtils.isEmpty(filePath))
                    return false;
                File file = new File(filePath);
                if (file.exists()) {//检查文件是否存在
                    long downloadId = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
                    openFile(downloadId);
                    c.close();
                    return true;
                }
            }
        }
        c.close();
        return false;
    }

    private boolean isDownloadSuccess(long id) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            if (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                c.close();
                return true;
            }
        }
        c.close();
        return false;
    }

    private String getUrlById(long id) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            String url = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
            c.close();
            return url;
        }
        c.close();
        return "";
    }

    private File getFileById(long id) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            String uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            String filePath = Uri.parse(uri).getPath();
            c.close();
            if (TextUtils.isEmpty(filePath))
                return null;
            return new File(filePath);
        }
        c.close();
        return null;
    }

    private String getMimeTypeFromUrl(String url) {
        String type = "application/vnd.android.package-archive";//默认.apk
        //使用系统API，获取URL路径中文件的后缀名（扩展名）
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (!TextUtils.isEmpty(extension)) {
            //使用系统API，获取MimeTypeMap的单例实例，然后调用其内部方法获取文件后缀名（扩展名）所对应的MIME类型
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return type;
    }

    private boolean isLegalApk(long downloadId) {
        boolean isLegal = false;
        File file = getFileById(downloadId);
        if (file != null) {
            if (file.exists() && file.isFile()) {
                String apkPath = file.getAbsolutePath();
                if (AppUtils.getVersionCodeByApkFile(apkPath) > BuildConfig.VERSION_CODE) {
                    if (ApkSignUtil.isCorrectSign(apkPath)) {
                        isLegal = true;
                    } else {
                        ToastUtil.showShort(R.string.t_sign_incorrect);
                        file.delete();
                    }
                } else {
                    ToastUtil.showShort(R.string.t_install_version_error);
                    file.delete();
                }
            } else {
                ToastUtil.showShort(R.string.t_apk_file_non_exist);
            }
        } else {
            ToastUtil.showShort(R.string.t_install_error);
        }
        return isLegal;
    }

}
