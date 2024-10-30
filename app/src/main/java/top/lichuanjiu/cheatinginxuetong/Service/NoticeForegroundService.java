package top.lichuanjiu.cheatinginxuetong.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import top.lichuanjiu.cheatinginxuetong.tools.NoticeTools;

public class NoticeForegroundService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("NoticeForegroundService", "ServiceOnCreate");
        NoticeTools noticeTools = new NoticeTools(this);
        startForeground(1000,noticeTools.builder.build());
        noticeTools.sendNotice("服务已开启", 0, this);

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("NoticeForegroundService", "ServiceOnDestroy");
    }
}
