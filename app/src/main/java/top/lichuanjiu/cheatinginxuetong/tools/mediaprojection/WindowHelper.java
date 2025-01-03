package top.lichuanjiu.cheatinginxuetong.tools.mediaprojection;


import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import top.lichuanjiu.cheatinginxuetong.App;


public class WindowHelper {
    private static final WindowManager WINDOW_MANAGER = (WindowManager) App.getApp().getSystemService(Context.WINDOW_SERVICE);

    public static DisplayMetrics getRealMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WINDOW_MANAGER.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }
}
