package top.lichuanjiu.cheatinginxuetong;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import top.lichuanjiu.cheatinginxuetong.Service.MyNotificationListenerService;
import top.lichuanjiu.cheatinginxuetong.Service.NoticeForegroundService;
import top.lichuanjiu.cheatinginxuetong.Service.NoticeOperationProcessingService;
import top.lichuanjiu.cheatinginxuetong.tools.ApplyForPermission;

public class SettingsActivity extends AppCompatActivity {
    public Intent foregroundIntent = null;
    public static SettingsActivity instance = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toggleNotificationListenerService();
        instance = this;
        new ApplyForPermission(this, this);

        Intent noticeListenerIntent = new Intent(this, MyNotificationListenerService.class);
        startService(noticeListenerIntent);


        Intent intent = new Intent(this, KeyListenerService.class);
        startService(intent);
        Intent noticeIntent = new Intent(this, NoticeOperationProcessingService.class);
        startService(noticeIntent);



        if(foregroundIntent != null){
            stopService(foregroundIntent);
        }
        foregroundIntent = new Intent(this, NoticeForegroundService.class);
        startService(foregroundIntent);


        //监听事件

    }
    private void toggleNotificationListenerService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, top.lichuanjiu.cheatinginxuetong.Service.MyNotificationListenerService.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(this, top.lichuanjiu.cheatinginxuetong.Service.MyNotificationListenerService.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
    //监听按钮事件
    public void restartForegroundIntent(){
        Log.d("restart", "重启前台服务前"+foregroundIntent);
        if(foregroundIntent == null){
            return;
        }
        Log.d("restart", "重启前台服务");
        stopService(foregroundIntent);

        foregroundIntent = new Intent(this, NoticeForegroundService.class);
        startService(foregroundIntent);

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            // 设置 EditTextPreference 的 SummaryProvider
            EditTextPreference usernamePref = findPreference("username");
            if (usernamePref != null) {
                usernamePref.setSummaryProvider(preference ->
                        usernamePref.getText() == null || usernamePref.getText().isEmpty()
                                ? "未填写用户名"
                                : usernamePref.getText());
            }

            EditTextPreference passwordPref = findPreference("password");
            if (passwordPref != null) {
                passwordPref.setSummaryProvider(preference ->
                        passwordPref.getText() == null || passwordPref.getText().isEmpty()
                                ? "未填写密码"
                                : "已填写密码");
            }

            SwitchPreferenceCompat runfuncPref = findPreference("feature_toggle_run_fun");
            if (runfuncPref != null) {
                runfuncPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    if((boolean)newValue){
                        Toast.makeText(getContext(), "root 权限检查中！", Toast.LENGTH_SHORT).show();
                        Process process = null;
                        Process process1 = null;
                        try {
                            process = Runtime.getRuntime().exec(new String[] { "which", "su" });
                            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                            if(in.readLine() != null){
                                Toast.makeText(getContext(), "root权限检查成功！", Toast.LENGTH_SHORT).show();
                                Log.d("root", "root 权限检查成功！");
                                //申请root 权限  待完善
                                try {
                                    process1 = Runtime.getRuntime().exec("su");
                                    Toast.makeText(getContext(), "root权限申请成功！", Toast.LENGTH_SHORT).show();
                                }catch (IOException e){
                                    Toast.makeText(getContext(), "root权限申请失败！", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(getContext(), "root权限检查失败！", Toast.LENGTH_SHORT).show();
                                runfuncPref.setChecked(false);
                            }
                        }catch (IOException e){
                            Toast.makeText(getContext(), "root权限检查失败！", Toast.LENGTH_SHORT).show();
                            runfuncPref.setChecked(false);
                        }finally {
                            if(process != null){
                                process.destroy();
                            }
                        }
                    }
                    return true;
                });
            }

            // 设置 ListPreference 的 SummaryProvider
            ListPreference optionOnePref = findPreference("option_one");
            if (optionOnePref != null) {
                optionOnePref.setSummaryProvider(preference ->
                        optionOnePref.getValue() == null
                                ? "未选择截屏触发方式"
                                : optionOnePref.getEntry());
            }

            ListPreference optionTwoPref = findPreference("option_two");
            if (optionTwoPref != null) {
                optionTwoPref.setSummaryProvider(preference ->
                        optionTwoPref.getValue() == null
                                ? "未选择请求题解方式"
                                : optionTwoPref.getEntry());
            }

            ListPreference optionThreePref = findPreference("option_three");
            if (optionThreePref != null) {
                optionThreePref.setSummaryProvider(preference ->
                        optionThreePref.getValue() == null
                                ? "未选择显示题解方式"
                                : optionThreePref.getEntry());
            }
            //关于我们点击事件
            Preference aboutUsPref = findPreference("home_url");
            if (aboutUsPref != null) {
                aboutUsPref.setOnPreferenceClickListener(preference -> {
                    //打开网页
                    SettingsActivity.instance.openWeb(getString(R.string.home_url));
                   return true;
                });
            }
            //点击github地址事件
            Preference githubPref = findPreference("github_url");
            if(githubPref != null){
                githubPref.setOnPreferenceClickListener(preference -> {
                    //打开网页
                    SettingsActivity.instance.openWeb(getString(R.string.github_url));
                    return true;
                });
            }
            //点击赞助开发事件
            Preference sponsorPref = findPreference("sponsorship_development");
            if(sponsorPref != null){
                sponsorPref.setOnPreferenceClickListener(preference -> {
                    SettingsActivity.instance.openWeb(getString(R.string.sponsorship_development_url));
                    return true;
                });
            }


        }
    }
    private void openWeb(String url){
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        startActivity(intent);
    }
}