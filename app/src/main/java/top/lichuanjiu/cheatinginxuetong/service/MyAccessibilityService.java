package top.lichuanjiu.cheatinginxuetong.service;

import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

public class MyAccessibilityService extends android.accessibilityservice.AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

}
