package top.lichuanjiu.cheatinginxuetong.sso;

public class LoginStates {
    public static LoginStateEnum loginState = LoginStateEnum.NoLogin;
    public enum LoginStateEnum {
        Login,
        NoLogin,
        LoginExpired

    }
}
