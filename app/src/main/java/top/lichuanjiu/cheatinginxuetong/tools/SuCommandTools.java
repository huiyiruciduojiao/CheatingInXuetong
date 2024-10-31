package top.lichuanjiu.cheatinginxuetong.tools;

import java.io.IOException;

public class SuCommandTools {
    public static void suCommand(String command){
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su -c "+command);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(process != null){
                process.destroy();
            }
        }
    }

    /**
     * 异步执行命令
     * @param command 命令内容，不需要su
     */
    public static void asyncSuCommand(String command){
        new Thread(() -> suCommand(command)).start();
    }

    /***
     * 异步执行命令，并且延迟一定时间执行
     * @param command 命令内容，不需要su
     * @param Delayed 延时多久（毫秒）
     */
    public static  void asyncSuCommand(String command,int Delayed){
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
