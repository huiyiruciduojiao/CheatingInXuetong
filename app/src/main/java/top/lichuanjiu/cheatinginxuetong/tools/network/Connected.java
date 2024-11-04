package top.lichuanjiu.cheatinginxuetong.tools.network;


import android.os.AsyncTask;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import top.lichuanjiu.cheatinginxuetong.R;
import top.lichuanjiu.cheatinginxuetong.SettingsActivity;
import top.lichuanjiu.cheatinginxuetong.service.FloatWindowService;
import top.lichuanjiu.cheatinginxuetong.tools.JSON;

public class Connected extends AsyncTask<String, Void, String> {
    private static String baseUrl = "http://10.1.254.139/";
    private static String apiUrl = baseUrl + "/api";

    // 验证
    public static String verification = apiUrl + "/verification.php";
    //提交图片
    public static String submitImage = apiUrl + "/submitImage.php";
    //提交问题
    public static String submitQuestion = apiUrl + "/submitQuestion";


    private static String sendVerification(String data) {
        //发送https请求
        String result = null;
        try {
            result = HttpRequestUtil.sendPost(verification, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }
    public static void setSetUrl(String Url){
        baseUrl = Url;
        apiUrl = baseUrl + "/api";
        verification = apiUrl + "/verification.php";
        submitImage = apiUrl + "/submitImage.php";
        submitQuestion = apiUrl + "/submitQuestion";

    }

    private static String sendSubmitImage(String filePath, String username, String password) {
        try {
            return HttpRequestUtil.uploadImage(submitImage, filePath, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void init() {


    }

    @Override
    protected String doInBackground(String... strings) {
        switch (strings[0]) {
            case OptionsType.AUTHENTICATION:
                return sendVerification(strings[1]);
            case OptionsType.SOLVE:
                return sendSubmitImage(strings[1], strings[2], strings[3]);
            default:
                return null;
        }

    }

    @Override
    protected void onPostExecute(String s) {
        SettingsActivity.instance.stopLoad();
        if (s == null) return;

        //解析json 数据
        Map<String, Object> data = JSON.parseJsonData(s);
        //获取status
        if (data.get("option") == null) {
            return;
        }

        String options = data.get("option").toString();
        switch (options) {
            case OptionsType.AUTHENTICATION:
                onAuthentication(data);
                break;
            case OptionsType.SOLVE:
                onSolve(data);
                break;
            default:
                break;

        }


        super.onPostExecute(s);
    }

    /**
     * 认证回调
     *
     * @param data json数据
     */

    private void onAuthentication(Map<String, Object> data) {
        Preference authenticationBtn = SettingsActivity.settingsFragment.findPreference("authenticationBtn");
        PreferenceCategory authentication_title = SettingsActivity.settingsFragment.findPreference("authentication_title");

        String status = data.get("status").toString();

        if (status.equals(StatusType.STATUS_ERROR)) {
            Toast.makeText(SettingsActivity.instance, "验证失败:" + data.get("code"), Toast.LENGTH_SHORT).show();
            FloatWindowService.instance.setText("验证失败:" + data.get("code"));
            authenticationBtn.setSummary(R.string.authentication_summary);
            authentication_title.setTitle(R.string.authentication_parameters);
            return;
        }

        if (!status.equals(StatusType.STATUS_SUCCESS)) {
            authenticationBtn.setSummary(R.string.authentication_summary);
            authentication_title.setTitle(R.string.authentication_parameters);
            Toast.makeText(SettingsActivity.instance, "验证失败:" + data.get("message").toString(), Toast.LENGTH_SHORT).show();
            FloatWindowService.instance.setText("验证失败:" + data.get("message").toString());
            return;

        }
        JSONObject dataBoy = (JSONObject) data.get("data");

        String format = null;
        Toast.makeText(SettingsActivity.instance, "验证成功", Toast.LENGTH_SHORT).show();
        try {
            format = String.format(SettingsActivity.instance.getString(R.string.welcome_user), dataBoy.get("nickname"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        authentication_title.setTitle(format);
        FloatWindowService.instance.setText(format);

        String authenticationSuccess = SettingsActivity.instance.getString(R.string.authentication_success);

        try {
            format = String.format(authenticationSuccess, dataBoy.get("remainingtimes"), dataBoy.get("usedtimes"), dataBoy.get("totaltimes"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //修改验证按钮字符串
        assert authenticationBtn != null;
        authenticationBtn.setSummary(format);
    }

    private void onSolve(Map<String, Object> data) {
        Preference authenticationBtn = SettingsActivity.settingsFragment.findPreference("authenticationBtn");

        //获取状态
        String status = data.get("status").toString();
        if (status.equals(StatusType.STATUS_ERROR)) {
            Toast.makeText(SettingsActivity.instance, "提交失败:" + data.get("result").toString(), Toast.LENGTH_SHORT).show();
            FloatWindowService.instance.setText("提交失败:" + data.get("result").toString());
            return;
        }
        //获取使用数据
        JSONObject dataPacket = (JSONObject) data.get("data");
        JSONObject dataBoy = null;
        try {
            dataBoy = (JSONObject) dataPacket.get("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String authenticationSuccess = SettingsActivity.instance.getString(R.string.authentication_success);
        String format = null;
        try {
            format = String.format(authenticationSuccess, Integer.parseInt(dataBoy.get("remainingtimes").toString())-1+"", Integer.parseInt(dataBoy.get("usedtimes").toString())-1+"", dataBoy.get("totaltimes"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonObject = new JSONObject(data.get("result").toString());
            JSONObject output = jsonObject.getJSONObject("output");
            JSONArray choices = output.getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            JSONArray content = message.getJSONArray("content");
            String contentText = content.getJSONObject(0).getString("text");
            FloatWindowService.instance.setText(contentText);
        } catch (JSONException e) {
            FloatWindowService.instance.setText("解析数据失败："+e.getMessage());
            e.printStackTrace();
        }
        authenticationBtn.setSummary(format);

    }
}
