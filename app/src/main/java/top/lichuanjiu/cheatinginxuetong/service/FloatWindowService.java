package top.lichuanjiu.cheatinginxuetong.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;

import top.lichuanjiu.cheatinginxuetong.R;

public class FloatWindowService extends Service {
    public static FloatWindowService instance = null;
    public WindowManager.LayoutParams params;
    Handler handler = new Handler(Looper.getMainLooper());
    ;
    private WindowManager windowManager;
    private View floatView;
    private boolean isFloatViewAdded = false;

    @Override
    public void onCreate() {
        Log.d("FloatWindowService", "ServiceOnCreate");
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatView = LayoutInflater.from(this).inflate(R.layout.float_window_layout, null);

        // 设置悬浮窗的参数
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Android 8.0 及以上
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // 设置悬浮窗的位置
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        // 添加悬浮窗到窗口管理器
        windowManager.addView(floatView, params);
        isFloatViewAdded = true;

        // 处理触摸事件以移动悬浮窗
        floatView.setOnTouchListener(new View.OnTouchListener() {
            private int lastAction;
            private float initialX;
            private float initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        lastAction = MotionEvent.ACTION_DOWN;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = (int) (initialX + (event.getRawX() - initialTouchX));
                        params.y = (int) (initialY + (event.getRawY() - initialTouchY));
                        windowManager.updateViewLayout(floatView, params);
                        lastAction = MotionEvent.ACTION_MOVE;
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (lastAction == MotionEvent.ACTION_MOVE) {
                            // 悬浮窗移动后的操作
                        }
                        return true;
                }
                return false;
            }
        });
        instance = this;

    }

    public void hide() {
        Log.d("FloatWindowService", "ServiceOnDestroy");
        if (floatView != null && isFloatViewAdded) {
            handler.post(() -> windowManager.removeView(floatView));
            isFloatViewAdded = false;
        }
    }

    public void show() {
        if (floatView != null && !isFloatViewAdded) {
            handler.post(() -> windowManager.addView(floatView, params));
            isFloatViewAdded = true;
        }
    }

    public void setText(String text) {
        handler.post(() -> {
            TextView floatWindowText = floatView.findViewById(R.id.float_window_text);
            floatWindowText.setText(text);
        });

    }

    public void addText(String text) {
        handler.post(() -> {
            TextView floatWindowText = floatView.findViewById(R.id.float_window_text);
            floatWindowText.append(text);
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
