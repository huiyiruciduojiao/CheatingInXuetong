package top.lichuanjiu.cheatinginxuetong.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationManagerCompat;

import java.lang.reflect.Method;

public class ApplyForPermission {
    public ApplyForPermission(Context context,Activity activity){
        //申请悬浮窗权限
        if (!isOverlayPermission(context)) {
            jumpToPermission(context);
        }

        //申请通知栏权限
        if(!isNotificationEnabled(context)){
            openPush(activity);
        }
        //申请通知管理权限
        if(!NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.getPackageName())){
            applyForNoticeManagerPermission(context);
        }

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
    public static void applyForNoticeManagerPermission(Context context){
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            context.startActivity(intent);

    }
    //申请通知栏权限
    public static boolean applyForNotificationPermission(Context context){
        return false;
    }
    //申请辅助功能权限
    public static boolean applyForAccessibilityPermission(Context context){
        return false;
    }
    public static boolean applyForRootPermission(Context context){
        return false;
    }
    public static boolean isRoot(){
        return false;
    }
}