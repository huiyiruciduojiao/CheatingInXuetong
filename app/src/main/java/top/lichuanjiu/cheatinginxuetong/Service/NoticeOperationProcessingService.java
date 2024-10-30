package top.lichuanjiu.cheatinginxuetong.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class NoticeOperationProcessingService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("NoticeOperationProcessingService", "ServiceOnStartCommand");
        return super.onStartCommand(intent, flags, startId);

    }

    public void onCreate() {
        super.onCreate();
        Log.d("NoticeOperationProcessingService", "ServiceOnCreate");
    }
    public void onDestroy() {
        super.onDestroy();
        Log.d("NoticeOperationProcessingService", "ServiceOnDestroy");
    }
}
