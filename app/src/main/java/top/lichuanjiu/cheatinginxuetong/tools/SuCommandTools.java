package top.lichuanjiu.cheatinginxuetong.tools;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

import top.lichuanjiu.cheatinginxuetong.service.FloatWindowService;

public class SuCommandTools {
    public static void suCommand(String command) {
        Process process = null;
        Log.d("SuCommandTools", command);
        try {
            Log.d("SuCommandTools", "su -c " + command);
            process = Runtime.getRuntime().exec("su -c " + command);
            //发送退出命令
            DataOutputStream dos = new DataOutputStream(process.getOutputStream());
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                process.destroy();
                if (FloatWindowService.instance != null) {
                    FloatWindowService.instance.show();
                }
            }
        }
    }

    /**
     * 异步执行命令
     *
     * @param command 命令内容，不需要su
     */
    public static void asyncSuCommand(String command) {
        new Thread(() -> suCommand(command)).start();
    }

    /***
     * 异步执行命令，并且延迟一定时间执行
     * @param command 命令内容，不需要su
     * @param Delayed 延时多久（毫秒）
     */
    public static void asyncSuCommand(String command, int Delayed) {
        new Thread(() -> {
            try {
                Thread.sleep(Delayed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            suCommand(command);

        }).start();
    }
}
