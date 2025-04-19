package top.lichuanjiu.cheatinginxuetong.sso;

import android.net.Uri;

import androidx.collection.ArraySet;

public class AuthConfig {
    public static final AuthConfig INSTANCE = new AuthConfig();

    public final String clientId;
    public final Uri redirectUri;
    public final String scope;
    public final Uri discoveryUri;
    public final String tokenEndpoint = "https://sso.ysit.top/realms/ysit/protocol/openid-connect/token";
    public final String authorizationEndpoint = "https://sso.ysit.top/realms/ysit/protocol/openid-connect/auth";
    public final String revocationEndpoint  = "https://sso.ysit.top/realms/ysit/protocol/openid-connect/revoke";
    public final String userInfoEndpoint = "https://sso.ysit.top/realms/ysit/protocol/openid-connect/userinfo";
    public final String clientSecret = "";
    //访问令牌失效后最大刷新次数
    public static int maxRefreshCount = 3;
    //获取用户信息失败后最大尝试次数
    public static int maxGetUserInfoCount = 3;

    private AuthConfig() {
        // 从 JSON 文件加载配置（实际开发中需要实现）
        clientId = "xxtAI-Mobile";
        redirectUri = Uri.parse("ysitxxtcheatinginxuetong://ysit:80/sso/callback");
        scope = "openid email profile offline_access";
        discoveryUri = Uri.parse("https://sso.ysit.top/realms/ysit/.well-known/openid-configuration");

    }
}
