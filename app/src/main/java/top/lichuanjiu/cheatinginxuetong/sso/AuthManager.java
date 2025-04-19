package top.lichuanjiu.cheatinginxuetong.sso;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;


import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenResponse;

import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

import top.lichuanjiu.cheatinginxuetong.R;
import top.lichuanjiu.cheatinginxuetong.SettingsActivity;
import top.lichuanjiu.cheatinginxuetong.sso.Exception.SsoException;

/***
 * &#064;author:lichuanjiu
 * &#064;date:
 * &#064;description: 认证管理器
 */
public class AuthManager {
    private final Context context;
    private AuthorizationService authService;
    private TokenResponse tokenResponse;

    public AuthManager(Context context) {
        this.context = context;
        this.authService = new AuthorizationService(context);
    }

    /**
     * 开始登录调用
     *
     * @param activity 从哪来的上下文Activity
     */
    public void login(Activity activity) {

        AuthorizationServiceConfiguration serviceConfig = new AuthorizationServiceConfiguration(
                Uri.parse(AuthConfig.INSTANCE.authorizationEndpoint),
                Uri.parse(AuthConfig.INSTANCE.tokenEndpoint)
        );

        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                serviceConfig,
                AuthConfig.INSTANCE.clientId,
                ResponseTypeValues.CODE,
                AuthConfig.INSTANCE.redirectUri
        );

        builder.setScope(AuthConfig.INSTANCE.scope);
        AuthorizationRequest request = builder.build();

        Intent authIntent = authService.getAuthorizationRequestIntent(request);
        activity.startActivityForResult(authIntent, 1000);

    }

    /**
     * 认证中心回调处理函数
     *
     * @param intent 认证中心回调的Intent
     */
    public CompletableFuture<TokenSet> handleAuthorizationResponse(Intent intent) {
        if (intent == null) {
            throw new SsoException("intent is null");
        }
        CompletableFuture<TokenSet> future = new CompletableFuture<>();
        AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
        AuthorizationException exception = AuthorizationException.fromIntent(intent);


        if (response != null) {
            authService.performTokenRequest(
                    response.createTokenExchangeRequest(),
                    (tokenResponse, ex) -> {
                        if (tokenResponse != null) {
                            this.tokenResponse = tokenResponse;
                            saveTokens(tokenResponse);
                            future.complete(TokenManager.getToken(context));
                        } else {
                            future.completeExceptionally(new SsoException("token exchange failed"));
                            if (ex != null) {
                                Log.e("AuthRedirectActivity", "Token exchange failed: " + ex.getMessage());
                            } else {
                                Log.e("AuthRedirectActivity", "Token exchange failed: " + "Unknown error");
                            }
                        }
                    });
        }else {
            String errorStr = "Unknown error";
            if (exception != null) {
                future.completeExceptionally(new SsoException(exception));
                errorStr = exception.getMessage() != null ? exception.getMessage() : "Unknown error";
            }
            SettingsActivity.instance.stopLoad();
//            Log.e("AuthRedirectActivity", errorStr);
            SettingsActivity.instance.showTips(SettingsActivity.instance.getString(R.string.login_error_title)+errorStr);
        }
        return future;
    }

    /**
     * 保存token
     *
     * @param response token 对象
     */
    private void saveTokens(TokenResponse response) {
        TokenSet tokenSet = new TokenSet(response.accessToken, response.refreshToken, response.idToken);
        TokenManager.saveToken(context, tokenSet);

    }

    public void logout(Context context) {
        revokeToken(TokenManager.getRefreshToken(context), "refresh_token", context);

    }

    public void revokeToken(String token, String tokenTypeHint, Context context) {
        new Thread(() -> {
            try {
                Uri revokeUri = Uri.parse(AuthConfig.INSTANCE.revocationEndpoint);
                java.net.URL url = new java.net.URL(revokeUri.toString());
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Authorization", "Basic " + getBasicAuthHeader());

                String postData = "token=" + Uri.encode(token) +
                        "&token_type_hint=" + Uri.encode(tokenTypeHint) +
                        "&client_id=" + Uri.encode(AuthConfig.INSTANCE.clientId) +
                        "&client_secret=" + Uri.encode(AuthConfig.INSTANCE.clientSecret);

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    byte[] input = postData.getBytes("UTF-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Log.d("AuthManager", "Token revoked successfully");
                    //清理本地存储的 token
                    TokenManager.clearTokens(context);
                    SettingsActivity.instance.reLogin();
                    return;
                } else {
                    SettingsActivity.instance.showTips(SettingsActivity.instance.getString(R.string.logout_error_title)+"Failed to revoke token");
                    Log.e("AuthManager", "Failed to revoke token, HTTP code: " + responseCode);
                }

            } catch (Exception e) {
                String errorStr = "Unknown error";
                if( e.getMessage() != null){
                    errorStr = e.getMessage();
                }
                SettingsActivity.instance.showTips(SettingsActivity.instance.getString(R.string.logout_error_title)+errorStr);
            }
            SettingsActivity.instance.stopLoad();
        }).start();
    }

    private String getBasicAuthHeader() {
        String credentials = AuthConfig.INSTANCE.clientId + ":" + AuthConfig.INSTANCE.clientSecret;
        return android.util.Base64.encodeToString(credentials.getBytes(), android.util.Base64.NO_WRAP);
    }

    /**
     * 获取用户信息
     *
     * @param accessToken 访问令牌
     */
    public CompletableFuture<UserInfoSet> getUserInfo(String accessToken) {

        CompletableFuture<UserInfoSet> future = new CompletableFuture<>();
        new Thread(() -> {
            try {
                Uri userInfoUri = Uri.parse(AuthConfig.INSTANCE.userInfoEndpoint);
                java.net.URL url = new java.net.URL(userInfoUri.toString());
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);

                int responseCode = conn.getResponseCode();
                //判断响应状态码
                if (responseCode == 200) {
                    //获取输入流
                    java.io.InputStream inputStream = conn.getInputStream();
                    //读取响应数据
                    java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
                    //转换为字符串
                    String response = s.hasNext() ? s.next() : "";
                    Log.d("AuthManager", "UserInfo: " + response);
                    //解析响应数据
                    JSONObject jsonObject = new JSONObject(response);
                    //创建UserInfoSet对象
                    UserInfoSet userInfoSet = new UserInfoSet(jsonObject);
                    //完成Future
                    future.complete(userInfoSet);
                } else {
                    Log.e("AuthManager", "UserInfo failed: " + responseCode);
                    future.completeExceptionally(new SsoException.TokenExpiredException("UserInfo failed: " + responseCode));
                }
            } catch (Exception e) {
                Log.e("AuthManager", "Error fetching user info", e);
                future.completeExceptionally(e);
            }
        }).start();
        return future;
    }

    /**
     * 刷新Token，结果直接通过saveTokens保存
     *
     * @param refreshToken 刷新令牌
     */
    public CompletableFuture<TokenSet> refreshToken(String refreshToken) {

        CompletableFuture<TokenSet> future = new CompletableFuture<>();
        if(refreshToken == null){
            refreshToken = TokenManager.getRefreshToken(context);
        }
        if(refreshToken == null){
            future.completeExceptionally(new Exception("Token is Null"));
        }
        AuthorizationServiceConfiguration serviceConfig = new AuthorizationServiceConfiguration(
                Uri.parse(AuthConfig.INSTANCE.authorizationEndpoint),
                Uri.parse(AuthConfig.INSTANCE.tokenEndpoint)
        );

        net.openid.appauth.TokenRequest request = new net.openid.appauth.TokenRequest.Builder(
                serviceConfig,
                AuthConfig.INSTANCE.clientId
        )
                .setGrantType("refresh_token")
                .setRefreshToken(refreshToken)
                .build();

        authService.performTokenRequest(request, (response, ex) -> {
            if (response != null) {
                this.tokenResponse = response;
                saveTokens(response);
                future.complete(new TokenSet(response.accessToken, response.refreshToken, response.idToken));
                Log.d("AuthManager", "Token refreshed successfully");
            } else {
                Log.e("AuthManager", "Token refresh failed: " + (ex != null ? ex.errorDescription : "Unknown error"));
                future.completeExceptionally(new SsoException.LoginExpiredException(ex != null ? ex.errorDescription : "Unknown error"));

            }
        });
        return future;
    }
}
