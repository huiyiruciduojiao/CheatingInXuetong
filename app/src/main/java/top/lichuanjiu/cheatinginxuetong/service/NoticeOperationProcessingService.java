package top.lichuanjiu.cheatinginxuetong.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import top.lichuanjiu.cheatinginxuetong.SettingsActivity;
import top.lichuanjiu.cheatinginxuetong.tools.ApplyForPermission;
import top.lichuanjiu.cheatinginxuetong.tools.ScreenshotTools;
import top.lichuanjiu.cheatinginxuetong.tools.SuCommandTools;

public class NoticeOperationProcessingService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("NoticeOperationProcessingService", "ServiceOnStartCommand");
        if (intent != null) {
            String action = intent.getAction();
            if ("ACTION_SCREENSHOT".equals(action)) {
                // 执行截图操作
                Log.d("NoticeOperationProcessingService", "ACTION_SCREENSHOT");
                //检测root 权限
                if(!ApplyForPermission.isRoot()){
                    Toast.makeText(SettingsActivity.instance,"您的手机没有root权限！无法使用该方式",Toast.LENGTH_LONG).show();
                    return super.onStartCommand(intent, flags, startId);
                }else {
                    //收起通知栏
                    closeNotificationBar(ApplyForPermission.PrivilegeLevel.ROOT);
                }
                ScreenshotTools screenshotTools = new ScreenshotTools(SettingsActivity.instance);
                screenshotTools.startScreenshot(ApplyForPermission.isRootRun(),500);
            } else if ("ACTION_SEND".equals(action)) {
                // 执行发送操作
                Log.d("NoticeOperationProcessingService", "ACTION_SEND");
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    public void closeNotificationBar(ApplyForPermission.PrivilegeLevel level){
        if(level == ApplyForPermission.PrivilegeLevel.ROOT){
            closeNotificationBarRoot();
        }

    }
    private void closeNotificationBarRoot(){
        if(ApplyForPermission.isRoot()){
            Log.d("NoticeOperationProcessingService", "root closeNotificationBar");
            SuCommandTools.asyncSuCommand("service call statusbar 2");
        }else{
            throw new RuntimeException("没有root权限，无法执行root命令");
        }
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
