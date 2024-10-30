package top.lichuanjiu.cheatinginxuetong.tools;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import top.lichuanjiu.cheatinginxuetong.R;
import top.lichuanjiu.cheatinginxuetong.Service.NoticeForegroundService;
import top.lichuanjiu.cheatinginxuetong.Service.NoticeOperationProcessingService;

public class NoticeTools {
    public NotificationCompat.Builder builder = null;
    String channelId = "测试渠道";

    public NoticeTools(Context context) {
        buildNotification(context);
    }

    public void sendNotice(String msg, int type, Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel Name",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1000, builder.build());


    }

    private void buildNotification(Context context) {
        //创建Intent对象，用于打开执行自定义操作
        Intent actionIntent = new Intent(context, NoticeOperationProcessingService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, actionIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_background) // 通知图标
                .setContentTitle("通知标题")
                .setContentText("这是通知内容")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(R.drawable.ic_launcher_background, "截图", pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH); // 设置优先级
        this.builder = builder;
    }
}
