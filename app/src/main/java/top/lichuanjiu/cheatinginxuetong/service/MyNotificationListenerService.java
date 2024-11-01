package top.lichuanjiu.cheatinginxuetong.service;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import top.lichuanjiu.cheatinginxuetong.SettingsActivity;

public class MyNotificationListenerService extends android.service.notification.NotificationListenerService {

    private static final String TAG = "MyNotificationListener";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // 处理通知发布事件
        Log.d(TAG, "Notification posted: " + sbn.getPackageName() + " - " + sbn.getNotification().tickerText);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // 处理通知移除事件
        Log.d(TAG, "Notification removed: " + sbn.getPackageName() + " - " + sbn.getNotification().tickerText);

        // 检查是否是你自己的通知
        if ("top.lichuanjiu.cheatinginxuetong".equals(sbn.getPackageName())) {
            // 通知被关闭，执行相应操作
            handleNotificationClosed(sbn);
        }
    }

    private void handleNotificationClosed(StatusBarNotification sbn) {
        // 在这里处理通知被关闭的逻辑
        Log.d(TAG, "Your notification was closed: " + sbn.getNotification().tickerText);
        SettingsActivity.instance.restartForegroundIntent();
    }
}
