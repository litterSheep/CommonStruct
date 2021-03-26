package com.ly.common.web;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.ly.common.frame.Constants;
import com.ly.common.utils.CommonUtils;
import com.orhanobut.logger.Logger;

import java.io.File;

/**
 * Created by ly on 2017/9/1 16:53.
 */
public class CustomWebChromeClient extends WebChromeClient {
    public static final int FILECHOOSER_TAKEPICTURE = 1;
    public static final int FILECHOOSER_CHOOSERFILE = 2;
    public static final int REQUEST_PERMISSION_CAMERA = 20;
    /**
     * 用于展示在web端<input type=text>的标签被选择之后，文件选择器的制作和生成
     */
    private ValueCallback<Uri> uploadFile;
    private ValueCallback<Uri[]> uploadFiles;
    private Activity activity;

    private File takePicFile;//拍照保存的图片

    public CustomWebChromeClient(@NonNull Activity activity) {
        this.activity = activity;
    }

    /**
     * 当网页里a标签target="_blank"，打开新窗口时，这里会调用
     */
    @Override
    public boolean onCreateWindow(WebView webView, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        CommonWebView newWebView = new CommonWebView(activity);
        CommonWebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        newWebView.setWebChromeClient(new CustomWebChromeClient(activity));
        newWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (activity != null) {
                    //拦截url，跳转新窗口=，=
                    Intent intent = new Intent(activity, CommWebActivity.class);
                    intent.putExtra(Constants.INTENT_KEY_URL, url);
                    activity.startActivity(intent);
                }
                //防止触发现有界面的WebChromeClient的相关回调
                return true;
            }
        });
        transport.setWebView(newWebView);
        resultMsg.sendToTarget();

        return true;
//        return super.onCreateWindow(webView, isDialog, isUserGesture, message);
    }

    /**
     * 控制台消息输出
     */
    @Override
    public boolean onConsoleMessage(ConsoleMessage cm) {
        if (cm.messageLevel() == ConsoleMessage.MessageLevel.DEBUG) {
            Logger.d(String.format("%s -- From line %s of %s", cm.message(), cm.lineNumber(), cm.sourceId()));
        } else if (cm.messageLevel() == ConsoleMessage.MessageLevel.LOG
                || cm.messageLevel() == ConsoleMessage.MessageLevel.TIP) {
            Logger.i(String.format("%s -- From line %s of %s", cm.message(), cm.lineNumber(), cm.sourceId()));
        } else if (cm.messageLevel() == ConsoleMessage.MessageLevel.WARNING) {
            Logger.w(String.format("%s -- From line %s of %s", cm.message(), cm.lineNumber(), cm.sourceId()));
        } else if (cm.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
            Logger.e(String.format("%s -- From line %s of %s", cm.message(), cm.lineNumber(), cm.sourceId()));
        }
        return true;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        if (activity != null) {
            new AlertDialog.Builder(activity)
                    .setMessage(message)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm();
                        }
                    })
                    .setCancelable(true)
                    .create()
                    .show();
            return true;
        }
        // 返回true表示自已处理，返回false表示由系统处理
        return false;
    }

    /**
     * 页面加载进度
     */
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
//        if (pb_web != null) {
//            pb_web.setVisibility(View.VISIBLE);
//            pb_web.setMax(100);
//            pb_web.setProgress(newProgress);
//            if (pb_web.getProgress() == 100) {
//                pb_web.setVisibility(View.GONE);
//            }
//        }
        super.onProgressChanged(view, newProgress);
    }

    /**
     * 针对网页里的<input type="file"  accept="image/*" capture="camera" >, WebView默认是不会弹出选择文件对话框的
     * 需要重写该方法，自己来弹出选择文件对话框。
     * <p>
     * 注意SDK不同的版本会有不同的方法，需要统一处理
     */
    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType) {
        openFileChooseProcess(uploadFile, acceptType);
    }

    // For Android < 3.0
    public void openFileChooser(ValueCallback<Uri> uploadFile) {
        openFileChooseProcess(uploadFile, null);
    }

    // For Android  >= 5.0
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onShowFileChooser(WebView webView,
                                     ValueCallback<Uri[]> filePathCallback,
                                     FileChooserParams fileChooserParams) {
        super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        this.uploadFiles = filePathCallback;
        if (fileChooserParams.isCaptureEnabled() && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            takePicture();
        } else {
            String[] acceptTypes = fileChooserParams.getAcceptTypes();
            String acceptType = null;
            if (acceptTypes != null && acceptTypes.length > 0)
                acceptType = acceptTypes[0];
            showMenu4InputFile(acceptType);
        }
        return true;//返回true，filePathCallback被调用
    }

    private void openFileChooseProcess(ValueCallback<Uri> uploadFile, String acceptType) {
        this.uploadFile = uploadFile;
        showMenu4InputFile(acceptType);
    }

    private void showMenu4InputFile(final String acceptType) {
        try {
            String[] menus = {"拍照", "相册"};
            Dialog dialog = new AlertDialog.Builder(activity).setItems(menus, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            takePicture();
                            break;
                        case 1:
                        default:
                            chooseFile(acceptType);
                            break;
                    }
                }
            }).create();

            //这里很重要，如果弹出对话框，用户选择一个图片或者进行拍照，但是进行到一半的时候，用户cancel了，这个时候再去点击“选择文件”按钮时，网页会失去响应。
            //原因是：点击“选择文件”按钮时，网页会缓存一个ValueCallback对象，必须触发了该对象的onReceiveValue()方法，WebView才会释放，进而才能再一次的选择文件。
            //当弹层被取消时，返回未选择文件
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (uploadFiles != null)
                        uploadFiles.onReceiveValue(null);
                    if (uploadFile != null)
                        uploadFile.onReceiveValue(null);
                    uploadFile = null;
                    uploadFiles = null;
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chooseFile(String acceptType) {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);     //能够返回一个Uri结果
            if (TextUtils.isEmpty(acceptType)) {//接收类型
                acceptType = "*/*";     //选择的文件类型，例如：image/*表示图片
            }
            intent.setType(acceptType);
            activity.startActivityForResult(Intent.createChooser(intent, "File Chooser"), FILECHOOSER_CHOOSERFILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void takePicture() {
        try {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PermissionChecker.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
            }
            //这种方式拍照后onActivityResult data=null
            String pictureName = "webPic-" + System.currentTimeMillis() + ".jpg";

            File file = new File(Constants.TEMP_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }
            takePicFile = new File(file, pictureName);

            Uri imageUri = CommonUtils.getUriForFile(takePicFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.putExtra("return-data", false);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("noFaceDetection", true);

            ComponentName componentName = intent.resolveActivity(activity.getPackageManager());
            if (componentName != null) {
                activity.startActivityForResult(intent, FILECHOOSER_TAKEPICTURE);
            }

//            方式二
//            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            activity.startActivityForResult(takePictureIntent, FILECHOOSER_TAKEPICTURE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ValueCallback<Uri> getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(ValueCallback<Uri> uploadFile) {
        this.uploadFile = uploadFile;
    }

    public ValueCallback<Uri[]> getUploadFiles() {
        return uploadFiles;
    }

    public void setUploadFiles(ValueCallback<Uri[]> uploadFiles) {
        this.uploadFiles = uploadFiles;
    }

    public File getTakePicFile() {
        return takePicFile;
    }

    public void setTakePicFile(File takePicFile) {
        this.takePicFile = takePicFile;
    }
}
