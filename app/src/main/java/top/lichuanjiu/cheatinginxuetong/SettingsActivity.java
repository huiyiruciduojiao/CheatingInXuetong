package top.lichuanjiu.cheatinginxuetong;

import static top.lichuanjiu.cheatinginxuetong.tools.ApplyForPermission.jumpToPermission;
import static top.lichuanjiu.cheatinginxuetong.tools.ApplyForPermission.showApplyForScreenshotPermission;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import top.lichuanjiu.cheatinginxuetong.service.FloatWindowService;
import top.lichuanjiu.cheatinginxuetong.service.MediaProjectionService;
import top.lichuanjiu.cheatinginxuetong.service.MyNotificationListenerService;
import top.lichuanjiu.cheatinginxuetong.service.NoticeForegroundService;
import top.lichuanjiu.cheatinginxuetong.service.NoticeOperationProcessingService;
import top.lichuanjiu.cheatinginxuetong.sso.AuthManager;
import top.lichuanjiu.cheatinginxuetong.sso.LoginStates;
import top.lichuanjiu.cheatinginxuetong.sso.TokenManager;
import top.lichuanjiu.cheatinginxuetong.sso.TokenSet;
import top.lichuanjiu.cheatinginxuetong.sso.UserInfoSet;
import top.lichuanjiu.cheatinginxuetong.tools.ApplyForPermission;
import top.lichuanjiu.cheatinginxuetong.tools.ConnectedService;
import top.lichuanjiu.cheatinginxuetong.tools.ExecuteOperate;
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
    public AuthManager authManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);


        //测试代码
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);


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
        new ApplyForPermission(this, this);

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
        SettingsActivity.instance.authManager = new AuthManager(SettingsActivity.instance);

        reLogin();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ApplyForPermission.REQUEST_CODE_SCREENSHOT:
                if (resultCode == RESULT_OK && data != null) {
                    Intent intent = new Intent(this, MediaProjectionService.class);
                    intent.putExtra("code", resultCode);
                    intent.putExtra("data", data);

                    startForegroundService(intent);
                } else {
                    ApplyForPermission.onUserRefused();
                }
                break;
            case ApplyForPermission.REQUEST_CODE_NOTICE:
                break;
            case 1000:
                //登录回调
                authManager.handleAuthorizationResponse(data).thenAccept(this::reLogin);
                break;
            default:
                stopLoad();
        }
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

    public void reLogin() {
        // 加载Token
        TokenSet tokenSet = TokenManager.getToken(this);
        reLogin(tokenSet);
    }

    public void reLogin(TokenSet tokenSet) {
        showLoad();

        if (tokenSet.refreshToken != null) {
            authManager.refreshToken(tokenSet.refreshToken).thenAccept(this::onRefreshSuccess)
                    .exceptionally(e -> {
                        Log.e("token_load", "refresh_token_failed");
                        LoginStates.loginState = LoginStates.LoginStateEnum.LoginExpired;
                        e.printStackTrace();
                        onLoginExpired();
                        stopLoad();
                        // 可以选择跳转登录页或提示
                        return null;
                    });
        } else {
            stopLoad();
            Log.i("token_load", "refresh_token is null");
            LoginStates.loginState = LoginStates.LoginStateEnum.NoLogin;
            // 可以考虑在这里调用登录逻辑
            onNoLogin();
        }
    }

    private void onRefreshSuccess(TokenSet newTokenSet) {
        Log.d("token_load", "refresh_token_success");

        authManager.getUserInfo(newTokenSet.accessToken)
                .thenAccept(this::onUserInfoSuccess)
                .exceptionally(e -> {
                    stopLoad();
                    LoginStates.loginState = LoginStates.LoginStateEnum.LoginExpired;
                    onLoginExpired();
                    Log.e("token_load", "获取用户信息失败，可能登录失效");
                    e.printStackTrace();
                    return null;
                });
    }

    private void onUserInfoSuccess(UserInfoSet userInfoSet) {

        if (userInfoSet == null) {
            stopLoad();
            Log.w("token_load", "userInfoSet is null");
            return;
        }
        LoginStates.loginState = LoginStates.LoginStateEnum.Login;
        ExecuteOperate.verifyConnection();
        String displayName = (userInfoSet.userNickName == null || userInfoSet.userNickName.isEmpty())
                ? userInfoSet.userName
                : userInfoSet.userNickName;

        String format = String.format(
                SettingsActivity.instance.getString(R.string.welcome_user),
                displayName
        );
        new Handler(Looper.getMainLooper()).post(() -> {
            PreferenceCategory authentication_title = settingsFragment.findPreference("authentication_title");
            if (authentication_title != null) {
                authentication_title.setTitle(format);
            }
            Preference username = settingsFragment.findPreference("username");
            if (username != null) {
                username.setSummary(userInfoSet.userName);
            }
            Preference logoutBtn = settingsFragment.findPreference("logoutBtn");
            if (logoutBtn != null) {
                logoutBtn.setVisible(true);
            }
            Preference authenticationBtn = settingsFragment.findPreference("authenticationBtn");
            if (authenticationBtn != null) {
                authenticationBtn.setTitle(R.string.authentication_title);
                authenticationBtn.setSummary(R.string.authentication_summary);
            }
            if(FloatWindowService.instance != null){
                FloatWindowService.instance.setText(format);
            }

        });
        //验证与题库服务器的连接

        stopLoad();

        Log.w("token_load", "用户信息加载成功，标题已更新");
    }

    /**
     * 用户未登录
     */
    private void onNoLogin() {
        new Handler(Looper.getMainLooper()).post(() -> {

            PreferenceCategory authentication_title = settingsFragment.findPreference("authentication_title");
            if (authentication_title != null) {
                authentication_title.setTitle(R.string.welcome_user_no_login);
            }
            Preference username = settingsFragment.findPreference("username");
            if (username != null) {
                username.setSummary(R.string.username_no_login_summary);
            }
            Preference logoutBtn = settingsFragment.findPreference("logoutBtn");
            if (logoutBtn != null) {
                logoutBtn.setVisible(false);
            }
            Preference authenticationBtn = settingsFragment.findPreference("authenticationBtn");
            if (authenticationBtn != null) {
                authenticationBtn.setTitle(R.string.authentication_no_login);
                authenticationBtn.setSummary(R.string.authentication_no_login_summary);
            }
            if(FloatWindowService.instance != null){
                FloatWindowService.instance.setText(this.getString(R.string.authentication_no_login));
            }

        });
    }

    /**
     * 登录失效
     */
    private void onLoginExpired() {
        new Handler(Looper.getMainLooper()).post(() -> {
            PreferenceCategory authentication_title = settingsFragment.findPreference("authentication_title");
            if (authentication_title != null) {
                authentication_title.setTitle(R.string.welcome_user_login_failure);
            }
            Preference logoutBtn = settingsFragment.findPreference("logoutBtn");
            if (logoutBtn != null) {
                logoutBtn.setVisible(false);
            }
            Preference authenticationBtn = settingsFragment.findPreference("authenticationBtn");
            if (authenticationBtn != null) {
                authenticationBtn.setTitle(R.string.authentication_no_login);
                authenticationBtn.setSummary(R.string.authentication_no_login_summary);
            }
            if(FloatWindowService.instance != null){
                FloatWindowService.instance.setText(this.getString(R.string.welcome_user_login_failure));
            }

        });
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

    public boolean isUse() {
        SwitchPreferenceCompat runModePref = settingsFragment.findPreference("feature_toggle");
        if (runModePref == null) {
            return false;
        }
        return runModePref.isChecked();
    }

    /**
     * 获取settingsFragment 中的指定 ListPreference的value
     *
     * @param key key  内容
     * @return 选中的值
     */
    public String getListPreferenceValue(String key) {
        if (settingsFragment == null) {
            return null;
        }
        ListPreference listPreference = settingsFragment.findPreference(key);
        if (listPreference == null) {
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
            new Handler(Looper.getMainLooper()).post(() -> {
                ImageView imageView = loadingDialog.findViewById(R.id.loading_image);
                imageView.setVisibility(View.GONE);
                imageView.clearAnimation();
                loadingDialog.dismiss();
            });
        }
    }

    public void showTips(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(SettingsActivity.instance, message, Toast.LENGTH_SHORT).show();

        });
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
        Throwable e = new Throwable("功能未完成");
        e.printStackTrace();
        return null;
//        EditTextPreference usernamePref = settingsFragment.findPreference("username");
//        EditTextPreference passwordPref = settingsFragment.findPreference("password");
//        if (usernamePref == null || passwordPref == null) {
//            return null;
//        }
//
//        String[] data = null;
//        if (usernamePref.getText() != null && !usernamePref.getText().isEmpty() && passwordPref.getText() != null && !passwordPref.getText().isEmpty()) {
//            data = new String[]{usernamePref.getText(), passwordPref.getText()};
//        }
//        return data;
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
                    } else {
                        if (FloatWindowService.instance != null) {
                            FloatWindowService.instance.setText("输入的地址无效" + newUrl);
                        }
                        Toast.makeText(SettingsActivity.instance, "输入的地址无效" + newUrl, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    return true;
                });
                if (serverBaseUrlPref.getText() != null && UrlUtil.isUrl(serverBaseUrlPref.getText())) {
                    Connected.setSetUrl(serverBaseUrlPref.getText());
                }
            }

            //验证按钮 点击事件
            Preference authenticationPref = findPreference("authenticationBtn");
            if (authenticationPref != null) {
                authenticationPref.setOnPreferenceClickListener(preference -> {
                    switch (LoginStates.loginState) {
                        case NoLogin://登录
                        case LoginExpired:
                            SettingsActivity.instance.showLoad();
                            SettingsActivity.instance.authManager.login(SettingsActivity.instance);
                            break;
                        case Login:
                            ExecuteOperate.verifyConnection();
                            break;

                    }
                    return true;

                });
            }
            Preference logoutPref = findPreference("logoutBtn");
            if (logoutPref != null) {

                logoutPref.setOnPreferenceClickListener(preference -> {
                    //弹窗确认
                    new AlertDialog.Builder(SettingsActivity.instance)
                            .setTitle("注销")
                            .setMessage("注销后，需要重新登录")
                            .setPositiveButton("确定", (dialog, which) -> {
                                SettingsActivity.instance.showLoad();
                                SettingsActivity.instance.authManager.logout(SettingsActivity.instance);
                            })
                            .setNegativeButton("取消", null)
                            .show();
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
                                return false;
                            }
                        } catch (IOException e) {
                            Toast.makeText(getContext(), "root权限检查失败！", Toast.LENGTH_SHORT).show();
                            return false;
                        } finally {
                            if (process != null) {
                                process.destroy();
                            }
                        }
                    } else {
                        runFunPref.setChecked(false);
                        ApplyForPermission.showApplyForScreenshotPermission(SettingsActivity.instance, SettingsActivity.instance);
                    }
                    return true;
                });
            }
            SwitchPreferenceCompat feature_toggle = findPreference("feature_toggle");
            if (feature_toggle != null) {
                feature_toggle.setOnPreferenceChangeListener((preference, newValue) -> {
                    if ((boolean) newValue && SettingsActivity.instance.checkRunMode() == ApplyForPermission.PrivilegeLevel.USER) {
                        //提示用户授权截取屏幕、无障碍服务
                        feature_toggle.setChecked(true);
                        ApplyForPermission.showApplyForScreenshotPermission(SettingsActivity.instance, SettingsActivity.instance);
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
            showApplyForScreenshotPermission(SettingsActivity.instance, SettingsActivity.instance);

        }
    }
}