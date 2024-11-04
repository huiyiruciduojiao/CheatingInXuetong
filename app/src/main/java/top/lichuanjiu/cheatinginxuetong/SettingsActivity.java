package top.lichuanjiu.cheatinginxuetong;

import static top.lichuanjiu.cheatinginxuetong.tools.ApplyForPermission.jumpToPermission;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import top.lichuanjiu.cheatinginxuetong.service.FloatWindowService;
import top.lichuanjiu.cheatinginxuetong.service.MyNotificationListenerService;
import top.lichuanjiu.cheatinginxuetong.service.NoticeForegroundService;
import top.lichuanjiu.cheatinginxuetong.service.NoticeOperationProcessingService;
import top.lichuanjiu.cheatinginxuetong.tools.ApplyForPermission;
import top.lichuanjiu.cheatinginxuetong.tools.ConnectedService;
import top.lichuanjiu.cheatinginxuetong.tools.EncryptionTools;
import top.lichuanjiu.cheatinginxuetong.tools.network.Connected;
import top.lichuanjiu.cheatinginxuetong.tools.network.HttpRequestUtil;
import top.lichuanjiu.cheatinginxuetong.tools.network.OptionsType;
import top.lichuanjiu.cheatinginxuetong.tools.network.UrlUtil;

public class SettingsActivity extends AppCompatActivity {
    public static SettingsActivity instance = null;
    public static SettingsFragment settingsFragment = null;
    public static String absPath = null;
    public Intent foregroundIntent = null;
    public Intent floatWindowService = null;

    public ConnectedService cService = null;

    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            settingsFragment = new SettingsFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, settingsFragment)
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //初始化加载动画
        initLoading();

        absPath = getFilesDir().getAbsolutePath();
        floatWindowService = new Intent(this, FloatWindowService.class);

        toggleNotificationListenerService();
        instance = this;
        // new ApplyForPermission(this, this);

        Intent noticeListenerIntent = new Intent(this, MyNotificationListenerService.class);
        startService(noticeListenerIntent);

        Intent noticeIntent = new Intent(this, NoticeOperationProcessingService.class);
        startService(noticeIntent);


        if (foregroundIntent != null) {
            stopService(foregroundIntent);
        }
        foregroundIntent = new Intent(this, NoticeForegroundService.class);
        startService(foregroundIntent);

        startFloatingWindow();

        cService = new ConnectedService();


    }

    /**
     * 初始化加载动画
     */
    private void initLoading() {
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_view);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        loadingDialog.setCancelable(false);
    }

    public ApplyForPermission.PrivilegeLevel checkRunMode() {
        SwitchPreferenceCompat runModePref = settingsFragment.findPreference("feature_toggle_run_fun");
        if (runModePref == null) {
            return ApplyForPermission.PrivilegeLevel.USER;
        }
        if (ApplyForPermission.isRoot() && runModePref.isChecked()) {
            return ApplyForPermission.PrivilegeLevel.ROOT;
        }
        return ApplyForPermission.PrivilegeLevel.USER;

    }

    private boolean isUse() {
        SwitchPreferenceCompat runModePref = settingsFragment.findPreference("feature_toggle");
        if (runModePref == null) {
            return false;
        }
        return runModePref.isChecked();
    }

    /**
     * 获取settingsFragment 中的指定 ListPreference的value
     * @param key key  内容
     * @return 选中的值
     */
    public String getListPreferenceValue(String key){
        if(settingsFragment == null){
            return null;
        }
        ListPreference listPreference = settingsFragment.findPreference(key);
        if(listPreference == null){
            return null;
        }
        return listPreference.getValue();

    }
    /***
     * 启动悬浮窗服务
     */
    public void startFloatingWindow() {
        //检测悬浮窗权限
        if (ApplyForPermission.isOverlayPermission(this)) {
            if (floatWindowService == null) {
                floatWindowService = new Intent(this, FloatWindowService.class);
            }

            startService(floatWindowService);
        } else {
            ApplyForPermission.showAuthorizationDialog(this, "请授予悬浮窗权限？拒绝授权将退出程序！", () -> {
                jumpToPermission(this);
            }, ApplyForPermission::onUserRefused);
        }
    }

    //关闭悬浮窗
    public void closeFloatingWindow() {
        stopService(floatWindowService);
    }

    //暂时隐藏悬浮窗
    public void hideFloatingWindow() {
        FloatWindowService.instance.hide();
    }

    //显示悬浮窗
    public void showFloatWindow() {
        FloatWindowService.instance.show();
    }

    /**
     * 显示加载动画
     */
    public void showLoad() {
        if (loadingDialog != null && !loadingDialog.isShowing()) {

            ImageView imageView = loadingDialog.findViewById(R.id.loading_image);
            imageView.setVisibility(View.VISIBLE);
            Animation rotateAnimation = AnimationUtils.loadAnimation(SettingsActivity.instance, R.anim.rotate);
            rotateAnimation.setFillAfter(true);
            imageView.startAnimation(rotateAnimation);
            loadingDialog.show();
        }
    }

    /**
     * 隐藏加载动画
     */
    public void stopLoad() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            ImageView imageView = loadingDialog.findViewById(R.id.loading_image);
            imageView.setVisibility(View.GONE);
            imageView.clearAnimation();
            loadingDialog.dismiss();
        }
    }

    private void toggleNotificationListenerService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, top.lichuanjiu.cheatinginxuetong.service.MyNotificationListenerService.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(this, top.lichuanjiu.cheatinginxuetong.service.MyNotificationListenerService.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    //监听按钮事件
    public void restartForegroundIntent() {
        Log.d("restart", "重启前台服务前" + foregroundIntent);
        if (foregroundIntent == null) {
            return;
        }
        Log.d("restart", "重启前台服务");
        stopService(foregroundIntent);

        foregroundIntent = new Intent(this, NoticeForegroundService.class);
        startService(foregroundIntent);

    }

    public String[] getUserAndPwd() {
        EditTextPreference usernamePref = settingsFragment.findPreference("username");
        EditTextPreference passwordPref = settingsFragment.findPreference("password");
        if (usernamePref == null || passwordPref == null) {
            return null;
        }

        String[] data = null;
        if (usernamePref.getText() != null && !usernamePref.getText().isEmpty() && passwordPref.getText() != null && !passwordPref.getText().isEmpty()) {
            data = new String[]{usernamePref.getText(), passwordPref.getText()};
        }
        return data;
    }

    private void openWeb(String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        startActivity(intent);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Log.d("SettingsFragment", "onCreatePreferences");
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

            EditTextPreference serverBaseUrlPref = findPreference("server_base_url_input");
            if (serverBaseUrlPref != null) {
                serverBaseUrlPref.setSummaryProvider(preference ->
                        serverBaseUrlPref.getText() == null || serverBaseUrlPref.getText().isEmpty()
                                ? "未填写，将使用默认服务器地址"
                                : serverBaseUrlPref.getText());
                serverBaseUrlPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    String newUrl = (String) newValue;
                    if (UrlUtil.isUrl(newUrl)) {
                        Connected.setSetUrl(newUrl);
                    }else{
                        FloatWindowService.instance.setText("输入的地址无效"+newUrl);
                        Toast.makeText(SettingsActivity.instance, "输入的地址无效"+newUrl, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    return true;
                });
                if(UrlUtil.isUrl(serverBaseUrlPref.getText())){
                    Connected.setSetUrl(serverBaseUrlPref.getText());
                }
            }

            //验证按钮 点击事件
            Preference authenticationPref = findPreference("authenticationBtn");
            if (authenticationPref != null) {
                authenticationPref.setOnPreferenceClickListener(preference -> {
                    //获取输入框的值
                    String[] userAndPwd = instance.getUserAndPwd();
                    if (SettingsActivity.instance.getUserAndPwd() == null) {
                        Toast.makeText(getContext(), R.string.user_or_pdw_is_null, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    SettingsActivity.instance.showLoad();
                    //创建json 数据
                    Map<String, Object> mapData = new HashMap<>();
                    mapData.put("username", userAndPwd[0]);
                    mapData.put("password", EncryptionTools.md5(userAndPwd[1]));
                    String formData = HttpRequestUtil.convertMapToFormData(mapData);
                    new Connected().execute(OptionsType.AUTHENTICATION, formData);

                    return true;
                });
            }
            //

            SwitchPreferenceCompat runFunPref = findPreference("feature_toggle_run_fun");
            if (runFunPref != null) {
                runFunPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    if ((boolean) newValue) {
                        Toast.makeText(getContext(), "root 权限检查中！", Toast.LENGTH_SHORT).show();
                        Process process = null;
                        Process process1 = null;
                        try {
                            process = Runtime.getRuntime().exec(new String[]{"which", "su"});
                            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                            if (in.readLine() != null) {
                                Toast.makeText(getContext(), "root权限检查成功！", Toast.LENGTH_SHORT).show();
                                Log.d("root", "root 权限检查成功！");
                                //申请root 权限  待完善
                                try {
                                    process1 = Runtime.getRuntime().exec("su");
                                    Toast.makeText(getContext(), "root权限申请成功！", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    Toast.makeText(getContext(), "root权限申请失败！", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "root权限检查失败！", Toast.LENGTH_SHORT).show();
                                runFunPref.setChecked(false);
                            }
                        } catch (IOException e) {
                            Toast.makeText(getContext(), "root权限检查失败！", Toast.LENGTH_SHORT).show();
                            runFunPref.setChecked(false);
                        } finally {
                            if (process != null) {
                                process.destroy();
                            }
                        }
                    } else {
                        ApplyForPermission.applyForAccessibilityPermission(SettingsActivity.instance);
                    }
                    return true;
                });
            }
            SwitchPreferenceCompat feature_toggle = findPreference("feature_toggle");
            if (feature_toggle != null) {
                feature_toggle.setOnPreferenceChangeListener((preference, newValue) -> {
                    if ((boolean) newValue && SettingsActivity.instance.checkRunMode() == ApplyForPermission.PrivilegeLevel.USER) {
                        //提示用户授权截取屏幕、无障碍服务
//                        ApplyForPermission.
                        ApplyForPermission.applyForAccessibilityPermission(SettingsActivity.instance);
                    }
                    return true;
                });
            }

            // 设置 ListPreference 的 SummaryProvider
            ListPreference triggerMethod = findPreference("trigger_method");
            if (triggerMethod != null) {
                triggerMethod.setSummaryProvider(preference ->
                        triggerMethod.getValue() == null
                                ? "未选择截屏触发方式"
                                : triggerMethod.getEntry());
            }

            ListPreference requestSolutionMethod = findPreference("request_solution_method");
            if (requestSolutionMethod != null) {
                requestSolutionMethod.setSummaryProvider(preference ->
                        requestSolutionMethod.getValue() == null
                                ? "未选择请求题解方式"
                                : requestSolutionMethod.getEntry());
            }

            ListPreference showMethod = findPreference("show_method");
            if (showMethod != null) {
                showMethod.setSummaryProvider(preference ->
                        showMethod.getValue() == null
                                ? "未选择显示题解方式"
                                : showMethod.getEntry());
                showMethod.setOnPreferenceChangeListener((preference, newValue) -> {
                    // 获取当前选择的选项值
                    String selectedOption = newValue.toString();
                    Log.i("option_three", "Selected Option: " + selectedOption);
                    return true;
                });
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
            if (githubPref != null) {
                githubPref.setOnPreferenceClickListener(preference -> {
                    //打开网页
                    SettingsActivity.instance.openWeb(getString(R.string.github_url));
                    return true;
                });
            }
            //点击赞助开发事件
            Preference sponsorPref = findPreference("sponsorship_development");
            if (sponsorPref != null) {
                sponsorPref.setOnPreferenceClickListener(preference -> {
                    SettingsActivity.instance.openWeb(getString(R.string.sponsorship_development_url));
                    return true;
                });
            }
            //监听事件
            if (SettingsActivity.instance.checkRunMode() == ApplyForPermission.PrivilegeLevel.USER && SettingsActivity.instance.isUse()) {
                ApplyForPermission.applyForAccessibilityPermission(SettingsActivity.instance);
            }

        }
    }
}