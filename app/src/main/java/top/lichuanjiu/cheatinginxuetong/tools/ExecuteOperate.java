package top.lichuanjiu.cheatinginxuetong.tools;

import java.util.HashMap;
import java.util.Map;

import top.lichuanjiu.cheatinginxuetong.SettingsActivity;
import top.lichuanjiu.cheatinginxuetong.sso.TokenManager;
import top.lichuanjiu.cheatinginxuetong.sso.UserInfoSet;
import top.lichuanjiu.cheatinginxuetong.tools.network.Connected;
import top.lichuanjiu.cheatinginxuetong.tools.network.HttpRequestUtil;
import top.lichuanjiu.cheatinginxuetong.tools.network.OptionsType;

public class ExecuteOperate {
    public static void verifyConnection() {
        SettingsActivity.instance.showLoad();
        //获取用户信息
        SettingsActivity.instance.authManager.refreshToken(
                        TokenManager.getRefreshToken(SettingsActivity.instance))
                .thenAccept(tokenSet -> {
                    Map<String, Object> mapData = new HashMap<>();
                    mapData.put("Token", tokenSet.accessToken);
                    mapData.put("Sub", UserInfoSet.sub == null ? "" : UserInfoSet.sub);
                    String formData = HttpRequestUtil.convertMapToFormData(mapData);
                    new Connected().execute(OptionsType.AUTHENTICATION, formData);
                }).exceptionally(e -> {
                    SettingsActivity.instance.reLogin();
                    return null;
                });
    }
}
