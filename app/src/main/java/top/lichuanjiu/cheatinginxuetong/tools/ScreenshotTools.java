package top.lichuanjiu.cheatinginxuetong.tools;

import android.content.Context;

import top.lichuanjiu.cheatinginxuetong.service.FloatWindowService;

public class ScreenshotTools {
    private Context context;

    public ScreenshotTools(Context context) {
        this.context = context;
    }


    public void startScreenshot(ApplyForPermission.PrivilegeLevel mode, int Delayed) {
        if(FloatWindowService.instance != null){
            FloatWindowService.instance.hide();
        }
        if (mode == ApplyForPermission.PrivilegeLevel.ROOT) {
            startScreenshot(Delayed);
        } else {
            startScreenshot();
        }
    }

    public void startScreenshot() {



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

        String path = context.getFilesDir().getAbsolutePath();
        SuCommandTools.asyncSuCommand("screencap -p " + path + "/screenshot.png", Delayed);

    }
}
