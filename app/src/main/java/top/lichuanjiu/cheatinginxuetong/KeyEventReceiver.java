package top.lichuanjiu.cheatinginxuetong;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class KeyEventReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int key = intent.getIntExtra("key", -1);
        Log.d("key", "keycode: " + key);

    }
}
