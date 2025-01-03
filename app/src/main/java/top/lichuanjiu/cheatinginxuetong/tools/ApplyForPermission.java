package top.lichuanjiu.cheatinginxuetong.tools;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;

import top.lichuanjiu.cheatinginxuetong.SettingsActivity;
import top.lichuanjiu.cheatinginxuetong.service.MediaProjectionService;
import top.lichuanjiu.cheatinginxuetong.service.MyAccessibilityService;

public class ApplyForPermission {
    //ROOT 权限申请编码
    public static final int REQUEST_CODE_ROOT = 100;
    //申请截屏权限
    public static final int REQUEST_CODE_SCREENSHOT = 101;
    //无障碍权限申请编码
    public static final int REQUEST_CODE_ACCESSIBILITY = 102;
    //通知栏权限申请编码
    public static final int REQUEST_CODE_NOTICE = 103;
    //通知管理权限申请编码
    public static final int REQUEST_CODE_NOTICE_MANAGER = 103;
    public static MediaProjectionManager mediaProjectionManager = null;

    public static MediaProjection mediaProjection = null;


    public ApplyForPermission(Context context, Activity activity) {
        //申请悬浮窗权限
        if (!isOverlayPermission(context)) {
            //弹窗提示用户是否授权
            showAuthorizationDialog(context, "请授予悬浮窗权限？拒绝授权将退出程序！", () -> {
                jumpToPermission(context);
            }, ApplyForPermission::onUserRefused);

        }

        //申请通知栏权限
        if (!isNotificationEnabled(context)) {
            showAuthorizationDialog(context, "请授予通知栏权限？拒绝授权将退出程序！", () -> {
                openPush(activity);
            }, ApplyForPermission::onUserRefused);
        }
        //申请通知管理权限
        if (!NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.getPackageName())) {
            showAuthorizationDialog(context, "请授予通知管理权限？拒绝授权将退出程序！", () -> {
                applyForNoticeManagerPermission(context);
            }, ApplyForPermission::onUserRefused);
        }
        //请求用户授权屏幕捕获
        showApplyForScreenshotPermission(context, activity);
        //申请辅助功能权限
        //申请root权限


    }

    public static boolean isOverlayPermission(Context context) {
        // 判断是否有悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        } else {
            try {
                Class<Settings> clazz = Settings.class;
                Method method = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                return (Boolean) method.invoke(null, context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // 跳转系统设置-悬浮窗页面
    public static void jumpToPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 是否打开通知按钮
     *
     * @param context
     * @return
     */
    public static boolean isNotificationEnabled(Context context) {
        return NotificationManagerCompat.from(context.getApplicationContext()).areNotificationsEnabled();
    }

    public static void openPush(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, activity.getApplicationInfo().uid);
            activity.startActivity(intent);
        } else {
            toPermissionSetting(activity);
        }
    }

    /**
     * 跳转到权限设置
     *
     * @param activity
     */
    public static void toPermissionSetting(Activity activity) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            toSystemConfig(activity);
        } else {
            try {
                toApplicationInfo(activity);
            } catch (Exception e) {
                e.printStackTrace();
                toSystemConfig(activity);
            }
        }
    }

    /**
     * 应用信息界面
     *
     * @param activity
     */
    public static void toApplicationInfo(Activity activity) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        localIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(localIntent);
    }

    /**
     * 系统设置界面
     *
     * @param activity
     */
    public static void toSystemConfig(Activity activity) {
        try {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void applyForNoticeManagerPermission(Context context) {
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        context.startActivity(intent);

    }

    //申请辅助功能权限
    public static void applyForAccessibilityPermission(Context context) {
        if (isAccessibilityServiceEnabled(context, MyAccessibilityService.class.getName())) {
            return;
        }
        showAuthorizationDialog(context, "请授予辅助功能权限？拒绝授权将退出程序！", () -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            context.startActivity(intent);

        }, ApplyForPermission::onUserRefused);
    }

    public static boolean isAccessibilityServiceEnabled(Context context, String className) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo>
                runningServices = activityManager.getRunningServices(100);
        if (runningServices.size() < 0) {
            return false;
        }
        for (int i = 0; i < runningServices.size(); i++) {
            ComponentName service = runningServices.get(i).service;
            if (service.getClassName().contains(className)) {
                return true;
            }
        }
        return false;
    }

    //    public static boolean is
    public static void showApplyForScreenshotPermission(Context context, Activity activity) {
        if (MediaProjectionService.mMediaProjection == null && SettingsActivity.instance != null && SettingsActivity.instance.isUse()  && isRootRun() == PrivilegeLevel.USER ) {
            showAuthorizationDialog(context, "请授予截屏权限？拒绝授权将退出程序！", () -> {
                applyForScreenshotPermission(activity);
            }, ApplyForPermission::onUserRefused);
        }
    }


    private static void applyForScreenshotPermission(Activity activity) {
        mediaProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        // 请求用户授权屏幕捕获
        Intent intent = mediaProjectionManager.createScreenCaptureIntent();
        activity.startActivityForResult(intent, REQUEST_CODE_SCREENSHOT);
    }


    public static boolean applyForRootPermission(Context context) {
        return false;
    }

    public static boolean isRoot() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) {
                Log.d("root", "root 权限检查成功！");
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    public static ApplyForPermission.PrivilegeLevel isRootRun() {
        if (!isRoot()) {
            return PrivilegeLevel.USER;
        }
        return SettingsActivity.instance.checkRunMode();
//        return true;
    }

    public static void showAuthorizationDialog(Context context, String msg, Runnable onUserAgreed, Runnable onUserRefused) {
        new AlertDialog.Builder(context)
                .setTitle("授权请求")
                .setMessage(msg)
                .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户点击了“同意”
                        onUserAgreed.run();
                    }
                })
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户点击了“拒绝”
                        onUserRefused.run();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        onUserRefused();
                    }
                })
                .show();
    }

    public static void onUserRefused() {
        System.exit(-1);
    }

    public enum PrivilegeLevel {
        USER,
        ROOT
    }
}
