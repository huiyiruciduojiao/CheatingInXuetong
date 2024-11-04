package top.lichuanjiu.cheatinginxuetong.tools.network;

public class UrlUtil {
    public static boolean isUrl(String url){
        return url.matches("^(https|http|ftp)\\:\\/\\/[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,}(:[0-9]{1,5})?(\\/[\\S]*)?$");
    }
}
