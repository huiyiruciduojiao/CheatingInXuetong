package top.lichuanjiu.cheatinginxuetong.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import top.lichuanjiu.cheatinginxuetong.SettingsActivity;
import top.lichuanjiu.cheatinginxuetong.tools.ApplyForPermission;
import top.lichuanjiu.cheatinginxuetong.tools.EncryptionTools;
import top.lichuanjiu.cheatinginxuetong.tools.ScreenshotTools;
import top.lichuanjiu.cheatinginxuetong.tools.SuCommandTools;
import top.lichuanjiu.cheatinginxuetong.tools.network.Connected;
import top.lichuanjiu.cheatinginxuetong.tools.network.OptionsType;

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
                if (ApplyForPermission.isRoot()) {
                    //收起通知栏
                    closeNotificationBar(ApplyForPermission.PrivilegeLevel.ROOT);
                }
                ScreenshotTools screenshotTools = new ScreenshotTools(SettingsActivity.instance);
                screenshotTools.startScreenshot(ApplyForPermission.isRootRun(), 500);
            } else if ("ACTION_SEND".equals(action)) {
                // 执行发送操作
                Log.d("NoticeOperationProcessingService", "ACTION_SEND");

                Connected.SendSolve();

//                //获取用户名密码
//                String[] userAndPwd = SettingsActivity.instance.getUserAndPwd();
//                if(userAndPwd[0] == null || userAndPwd[1] == null || userAndPwd[0].isEmpty() || userAndPwd[1].isEmpty()){
//                    FloatWindowService.instance.setText("用户名或密码为空，请先设置用户名密码");
//                    return super.onStartCommand(intent,flags,startId);
//                }
//                FloatWindowService.instance.setText("正在识别答案...");
//
//                new Connected().execute(OptionsType.SOLVE, SettingsActivity.absPath + "/screenshot.png", userAndPwd[0], EncryptionTools.md5(userAndPwd[1]));

            }else if("ACTION_SHOW".equals(action)){
                //显示悬浮窗
                FloatWindowService.instance.show();
            }

        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void closeNotificationBar(ApplyForPermission.PrivilegeLevel level) {
        if (level == ApplyForPermission.PrivilegeLevel.ROOT) {
            closeNotificationBarRoot();
        }

    }

    private void closeNotificationBarRoot() {
        if (ApplyForPermission.isRoot()) {
            Log.d("NoticeOperationProcessingService", "root closeNotificationBar");
            SuCommandTools.asyncSuCommand("service call statusbar 2");
        } else {
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
