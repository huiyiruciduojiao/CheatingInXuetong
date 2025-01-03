package top.lichuanjiu.cheatinginxuetong.tools;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import top.lichuanjiu.cheatinginxuetong.R;
import top.lichuanjiu.cheatinginxuetong.service.NoticeOperationProcessingService;

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
                    "服务通知",
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
        Intent actionIntent2 = new Intent(context, NoticeOperationProcessingService.class);
        Intent actionIntent4 = new Intent(context, NoticeOperationProcessingService.class);
        actionIntent.setAction("ACTION_SCREENSHOT");
        actionIntent2.setAction("ACTION_SEND");
        actionIntent4.setAction("ACTION_SHOW");
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, actionIntent, PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pendingIntent2 = PendingIntent.getService(context, 0, actionIntent2, PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pendingIntent4 = PendingIntent.getService(context, 0, actionIntent4, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher) // 通知图标
                .setContentTitle("学习通AI智能体服务")
                .setContentText("点击下方按钮可以执行操作，请不要清除本通知！")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(R.mipmap.ic_launcher, "截图", pendingIntent)
                .addAction(R.mipmap.ic_launcher,"发送",pendingIntent2)
                .addAction(R.mipmap.ic_launcher,"打开悬浮窗",pendingIntent4)
                .setPriority(NotificationCompat.PRIORITY_HIGH); // 设置优先级
        this.builder = builder;
    }
}
