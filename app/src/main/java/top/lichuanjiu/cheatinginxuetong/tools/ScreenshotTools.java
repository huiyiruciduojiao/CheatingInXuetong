package top.lichuanjiu.cheatinginxuetong.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import top.lichuanjiu.cheatinginxuetong.App;
import top.lichuanjiu.cheatinginxuetong.SettingsActivity;
import top.lichuanjiu.cheatinginxuetong.service.FloatWindowService;
import top.lichuanjiu.cheatinginxuetong.service.MediaProjectionService;

public class ScreenshotTools {
    private Context context;
    public static boolean isComplete = false;

    public ScreenshotTools(Context context) {
        this.context = context;
    }


    public void startScreenshot(ApplyForPermission.PrivilegeLevel mode, int Delayed) {
        if (FloatWindowService.instance != null) {
            FloatWindowService.instance.hide();
        }
        if (mode == ApplyForPermission.PrivilegeLevel.ROOT) {
            startScreenshot(Delayed);
        } else {
            startScreenshot(MediaProjectionService.mMediaProjection,Delayed);
        }
    }

    public void startScreenshot(MediaProjection mediaProjection,int Delayed) {
        ApplyForPermission.showApplyForScreenshotPermission(context, SettingsActivity.instance);
        if (mediaProjection == null) {
            FloatWindowService.instance.setText("获取截屏权限失败！");
            return;
        }
        isComplete = false;
        new Thread(() -> {
            try {
                Thread.sleep(Delayed);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(MediaProjectionService::screenshot);
        }).start();
//        DisplayMetrics metrics = new DisplayMetrics();
//
//        WindowManager windowManager = (WindowManager) App.getApp().getSystemService(Context.WINDOW_SERVICE);
//        windowManager.getDefaultDisplay().getRealMetrics(metrics);
//
//        int screenDensity = metrics.densityDpi;
//
//        // 设置屏幕的分辨率和密度
//        @SuppressLint("WrongConstant") ImageReader mImageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 1);
//        VirtualDisplay mVirtualDisplay = mediaProjection.createVirtualDisplay(
//                "ScreenCapture",
//                metrics.widthPixels,
//                metrics.heightPixels,
//                screenDensity,
//                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
//                mImageReader.getSurface(),
//                null,
//                null
//        );
//
//
//        Image image = mImageReader.acquireLatestImage();
//        // 获取数据
//        int width = image.getWidth();
//        int height = image.getHeight();
//        final Image.Plane plane = image.getPlanes()[0];
//        final ByteBuffer buffer = plane.getBuffer();
//
//        // 重新计算Bitmap宽度，防止Bitmap显示错位
//        int pixelStride = plane.getPixelStride();
//        int rowStride = plane.getRowStride();
//        int rowPadding = rowStride - pixelStride * width;
//        int bitmapWidth = width + rowPadding / pixelStride;
//
//        // 创建Bitmap
//        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, height, Bitmap.Config.ARGB_8888);
//        bitmap.copyPixelsFromBuffer(buffer);
//
//        // 释放资源
//        image.close();
//
//        // 裁剪Bitmap，因为重新计算宽度原因，会导致Bitmap宽度偏大
//        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, width, height);
//
//        File screenshotDir = new File(SettingsActivity.absPath);
//        if (!screenshotDir.exists()) {
//            screenshotDir.mkdirs();
//        }
//        String fileName = "screenshot_" + System.currentTimeMillis() + ".png";
//        File file = new File(screenshotDir, fileName);
//
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream(file);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        BufferedOutputStream bos = new BufferedOutputStream(fos);
//        result.compress(Bitmap.CompressFormat.PNG, 100, bos);
//        try {
//            bos.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        result.recycle();


    }

    public void saveScreenshot(Image image) {
        // 保存截图到文件
        File screenshotDir = new File(SettingsActivity.absPath);
        if (!screenshotDir.exists()) {
            screenshotDir.mkdirs();
        }
        String fileName = "screenshot_" + System.currentTimeMillis() + ".jpeg";
        File file = new File(screenshotDir, fileName);
        // 获取 Image 中的 ByteBuffer
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(bytes);  // 将字节数据写入文件
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private Bitmap convertYuvToRgba(Image image, int width, int height) {
//        Image.Plane[] planes = image.getPlanes();
//        ByteBuffer yBuffer = planes[0].getBuffer();
//        ByteBuffer uBuffer = planes[1].getBuffer();
//        ByteBuffer vBuffer = planes[2].getBuffer();
//
//        int ySize = yBuffer.remaining();
//        int uSize = uBuffer.remaining();
//        int vSize = vBuffer.remaining();
//
//        byte[] nv21 = new byte[ySize + uSize + vSize];
//        yBuffer.get(nv21, 0, ySize);
//        vBuffer.get(nv21, ySize, vSize);
//        uBuffer.get(nv21, ySize + vSize, uSize);
//
//        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, out);
//        byte[] imageBytes = out.toByteArray();
//        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//    }

    private void saveScreenshot(Bitmap bitmap) {
        Log.d("ScreenshotTools", "saveScreenshot");
        // 保存截图到文件
        File screenshotDir = new File(SettingsActivity.absPath);
        if (!screenshotDir.exists()) {
            screenshotDir.mkdirs();
        }
        String fileName = "screenshot_" + System.currentTimeMillis() + ".png";
        File file = new File(screenshotDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 调用该方法以ROOT su 命令截屏
     *
     * @param Delayed 等待执行事件，因为收起通知栏需要时间
     */
    public void startScreenshot(int Delayed) {

        if (context == null) {
            throw new NullPointerException("context is null");
        }
        isComplete = false;
        String path = context.getFilesDir().getAbsolutePath();
        SuCommandTools.asyncSuCommand("screencap -p " + path + "/screenshot.png", Delayed);

    }

}
