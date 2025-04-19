package top.lichuanjiu.cheatinginxuetong.sso;

public class TokenSet {
    public final String accessToken;
    public final String refreshToken;
    public final String idToken;

    public TokenSet(String accessToken, String refreshToken, String idToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.idToken = idToken;
    }
}
