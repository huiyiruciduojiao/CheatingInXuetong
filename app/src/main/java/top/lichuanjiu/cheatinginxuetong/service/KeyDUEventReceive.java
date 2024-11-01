package top.lichuanjiu.cheatinginxuetong.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import top.lichuanjiu.cheatinginxuetong.SettingsActivity;
import top.lichuanjiu.cheatinginxuetong.tools.SuCommandTools;

public class KeyDUEventReceive extends BroadcastReceiver {
    private long lastEventTime;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("top.lichuanjiu.cheatinginxuetong.DUKeyEvent")) {
            Log.i("DUKeyEvent", "DUKeyEvent");
            long eventTime = intent.getLongExtra("eventTime", 0);
            Log.d("eventTime", "" + eventTime);
            Log.d("lastEventTime", "" + lastEventTime);
            if (eventTime -lastEventTime < 300) {
                return;
            }
            Log.i("DUKeyEvent", "DUKeyEvent1");
            lastEventTime = eventTime;
            FloatWindowService.instance.hide();
            SuCommandTools.asyncSuCommand("screencap -p " + SettingsActivity.absPath + "/screenshot.png");
        }

    }

}
