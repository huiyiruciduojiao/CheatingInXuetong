package top.lichuanjiu.cheatinginxuetong.sso.Exception;

public class SsoException extends RuntimeException {

    // 默认构造方法
    public SsoException() {
        super();
    }

    // 接收错误信息的构造方法
    public SsoException(String message) {
        super(message);
    }

    // 接收错误信息及异常原因的构造方法
    public SsoException(String message, Throwable cause) {
        super(message, cause);
    }

    // 接收异常原因的构造方法
    public SsoException(Throwable cause) {
        super(cause);
    }

    /**
     * 登录失效异常，当用户的登录状态已经失效时抛出该异常
     */
    public static class LoginExpiredException extends SsoException {
        public LoginExpiredException() {
            super("登录会话已过期，请重新登录！");
        }

        public LoginExpiredException(String message) {
            super(message);
        }

        public LoginExpiredException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 访问令牌失效异常，当访问令牌已经失效或不合法时抛出该异常
     */
    public static class TokenExpiredException extends SsoException {
        public TokenExpiredException() {
            super("访问令牌已失效，请重新获取令牌！");
        }

        public TokenExpiredException(String message) {
            super(message);
        }

        public TokenExpiredException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
