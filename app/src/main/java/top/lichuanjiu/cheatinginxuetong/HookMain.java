package top.lichuanjiu.cheatinginxuetong;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.WindowManager;

import androidx.preference.PreferenceManager;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMain implements IXposedHookLoadPackage {
    private final Intent intent;
    private Context context;

    private long lastDownTime = 0;

    public HookMain() {
        intent = new Intent("top.lichuanjiu.cheatinginxuetong.DUKeyEvent");
        intent.setPackage("top.lichuanjiu.cheatinginxuetong");
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("android")) {
            // 获取 Context 对象
            XposedHelpers.findAndHookMethod("android.app.ActivityThread", lpparam.classLoader, "currentApplication", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    context = (Context) param.getResult();
                    hook(lpparam);
                }
            });

            Class<?> windowStateAnimatorClass = XposedHelpers.findClass("com.android.server.wm.WindowStateAnimator", lpparam.classLoader);
            XposedHelpers.findAndHookConstructor("com.android.server.wm.WindowSurfaceController",
                    lpparam.classLoader, String.class, int.class, int.class,
                    windowStateAnimatorClass, int.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            int windowType = (int)param.args[4];
                            if (windowType == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) {
                                int flags = (int) param.args[2];
                                flags |= 0x00000040;
                                param.args[2] = flags;
                            }
                        }
                    });
        }
    }

    private void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        long[] downTime = new long[2];

        XposedHelpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "interceptKeyBeforeDispatching",
                IBinder.class, KeyEvent.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        KeyEvent keyEvent = (KeyEvent) param.args[1];
                        if ((keyEvent.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN || keyEvent.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {

                            downTime[keyEvent.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP ? 0 : 1] = keyEvent.getEventTime();
                            if (isDUKeyEvent(downTime)) {
                                intent.putExtra("key", keyEvent.getKeyCode());
                                intent.putExtra("downTime", downTime);
                                intent.putExtra("eventTime", lastDownTime);
                                context.sendBroadcast(intent);
                            }
                        }

                    }
                }
        );
    }

    private boolean isDUKeyEvent(long[] downTime) {
        if (downTime[0] == 0 || downTime[1] == 0) {
            return false;
        }
        if (downTime[0] - lastDownTime < 300 || downTime[1] - lastDownTime < 300) {
            return false;
        }
        if (Math.abs(downTime[0] - downTime[1]) > 40) {
            return false;
        }
        lastDownTime = Math.max(downTime[0], downTime[1]);
        //将数组初始化
        downTime[0] = 0;
        downTime[1] = 0;
        return true;


    }
}
