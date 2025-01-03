package top.lichuanjiu.cheatinginxuetong.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import top.lichuanjiu.cheatinginxuetong.App;
import top.lichuanjiu.cheatinginxuetong.R;
import top.lichuanjiu.cheatinginxuetong.SettingsActivity;
import top.lichuanjiu.cheatinginxuetong.tools.ApplyForPermission;
import top.lichuanjiu.cheatinginxuetong.tools.ScreenshotTools;
import top.lichuanjiu.cheatinginxuetong.tools.mediaprojection.WindowHelper;

public class MediaProjectionService extends Service {
    private static final MediaProjectionManager MEDIA_PROJECTION_MANAGER = (MediaProjectionManager) App.getApp().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    private static final String CHANNEL_ID = "ScreenCaptureServiceChannel";
    @Nullable
    public static MediaProjection mMediaProjection;
    @Nullable
    private static VirtualDisplay mVirtualDisplayImageReader;
    private static boolean mImageAvailable = false;
    @Nullable
    private static ImageReader mImageReader;
    private static final MediaProjection.Callback MEDIA_PROJECTION_CALLBACK = new MediaProjection.Callback() {
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForegroundService();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Screen Capture Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(); //创建通知栏，你正在录屏
            Bundle bundle = intent.getExtras();
//            MediaProject = mediaProjectionManager.getMediaProjection( bundle.getInt("code",-1), Objects.requireNonNull(intent.getParcelableExtra("data")));
//            ApplyForPermission.mediaProjection = ApplyForPermission.mediaProjectionManager.getMediaProjection(bundle.getInt("code", -1), Objects.requireNonNull(intent.getParcelableExtra("data")));
            mMediaProjection = MEDIA_PROJECTION_MANAGER.getMediaProjection(bundle.getInt("code", -1), Objects.requireNonNull(intent.getParcelableExtra("data")));

            createImageReaderVirtualDisplay();
        }
        return START_STICKY;
    }

    private void startForegroundService() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("屏幕捕获")
                .setContentText("正在进行屏幕捕获...")
                .setSmallIcon(R.drawable.ic_home)  // 替换为实际的图标资源
                .build();

        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);

    }

    @SuppressLint("WrongConstant")
    private static void createImageReaderVirtualDisplay() {
        if (mMediaProjection != null) {
            DisplayMetrics dm = WindowHelper.getRealMetrics();
            mImageReader = ImageReader.newInstance(dm.widthPixels, dm.heightPixels, PixelFormat.RGBA_8888, 1);
            mImageReader.setOnImageAvailableListener(reader -> {
                mImageAvailable = true;
            }, null);
            mMediaProjection.registerCallback(MEDIA_PROJECTION_CALLBACK, null);
            mVirtualDisplayImageReader = mMediaProjection.createVirtualDisplay("ImageReader", dm.widthPixels, dm.heightPixels, dm.densityDpi, Display.FLAG_ROUND, mImageReader.getSurface(), null, null);
        }
    }

    public static void screenshot() {
        if (!mImageAvailable) {
            return;
        }
        if (mImageReader == null) {
            return;
        }
        try {
            Image image = mImageReader.acquireLatestImage();

            // 获取数据
            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane plane = image.getPlanes()[0];
            final ByteBuffer buffer = plane.getBuffer();

            // 重新计算Bitmap宽度，防止Bitmap显示错位
            int pixelStride = plane.getPixelStride();
            int rowStride = plane.getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            int bitmapWidth = width + rowPadding / pixelStride;

            // 创建Bitmap
            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);

            // 释放资源
            image.close();

            // 裁剪Bitmap，因为重新计算宽度原因，会导致Bitmap宽度偏大
            Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            bitmap.recycle();

            File screenshotDir = new File(SettingsActivity.absPath);
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }
            String fileName = "screenshot_no_root.png";
            File file = new File(screenshotDir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            result.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.close();
            result.recycle();
        } catch (IOException e) {
            e.printStackTrace();
            FloatWindowService.instance.setText("截图失败："+e.getMessage());
        }finally {
            FloatWindowService.instance.show();
            ScreenshotTools.isComplete = true;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);

        if (mVirtualDisplayImageReader != null) {
            mVirtualDisplayImageReader.release();
            mVirtualDisplayImageReader = null;
        }
        if(mImageReader != null){
            mImageReader.close();
            mImageReader = null;
        }
        mImageAvailable = false;
        if(mMediaProjection != null){
            mMediaProjection.unregisterCallback(MEDIA_PROJECTION_CALLBACK);
            mMediaProjection.stop();
            mMediaProjection = null;
        }


    }
}
