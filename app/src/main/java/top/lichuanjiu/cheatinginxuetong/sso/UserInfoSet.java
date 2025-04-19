package top.lichuanjiu.cheatinginxuetong.sso;

import org.json.JSONObject;

public class UserInfoSet {
    public String userName = null;
    public String userNickName = null;
    public String userEmail = null;
    public String userSub = null;
    public static String sub = null;

    public UserInfoSet(String userName, String userNickName, String userEmail, String userSub) {
        this.userName = userName;
        this.userNickName = userNickName;
        this.userEmail = userEmail;
        this.userSub = userSub;
    }

    public UserInfoSet(String accessToken) {

    }
    public UserInfoSet(JSONObject jsonObject){
        this.userName = jsonObject.optString("preferred_username");
        this.userNickName = jsonObject.optString("nickname");
        this.userEmail = jsonObject.optString("email");
        this.userSub = jsonObject.optString("sub");
        UserInfoSet.sub = userSub;

    }

}
