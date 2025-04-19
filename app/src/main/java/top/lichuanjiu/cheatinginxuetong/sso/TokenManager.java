package top.lichuanjiu.cheatinginxuetong.sso;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenManager {

    private static final String PREF_NAME = "secure_auth_tokens";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_ID_TOKEN = "id_token";

    private static SharedPreferences getEncryptedPreferences(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return EncryptedSharedPreferences.create(
                    PREF_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("无法创建加密 SharedPreferences", e);
        }
    }

    /**
     * 保存token
     * @param context 上下文
     * @param tokenSet TokenSet对象
     */
    public static void saveToken(Context context,TokenSet tokenSet){
        saveToken(context,tokenSet.accessToken,tokenSet.refreshToken,tokenSet.idToken);
    }

    /**
     * 保存Token
     * @param context 上下文对象
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     * @param idToken 用户Id令牌
     */
    public static void saveToken(Context context, String accessToken, String refreshToken, String idToken) {
        SharedPreferences.Editor editor = getEncryptedPreferences(context).edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putString(KEY_ID_TOKEN, idToken);
        editor.apply();
    }

    /**
     * 获取Token
     * @param context 上下文对象
     * @return TokenSet 对象
     */
    public static TokenSet getToken(Context context) {
        return new TokenSet(
                getAccessToken(context),
                getRefreshToken(context),
                getIdToken(context)
        );
    }

    /**
     * 获取访问令牌
     * @param context 上下文对象
     * @return 访问令牌
     */
    public static String getAccessToken(Context context) {
        return getEncryptedPreferences(context).getString(KEY_ACCESS_TOKEN, null);
    }

    /**
     * 获取刷新令牌
     * @param context 上下文对象
     * @return 刷新令牌
     */
    public static String getRefreshToken(Context context) {
        return getEncryptedPreferences(context).getString(KEY_REFRESH_TOKEN, null);
    }

    /**
     * 获取用户Id令牌
     * @param context 上下文对象
     * @return Id令牌
     */
    public static String getIdToken(Context context) {
        return getEncryptedPreferences(context).getString(KEY_ID_TOKEN, null);
    }

    /**
     * 清除保存的Token数据
     * @param context 上下文对象
     */
    public static void clearTokens(Context context) {
        SharedPreferences.Editor editor = getEncryptedPreferences(context).edit();
        editor.clear();
        editor.apply();
    }


}