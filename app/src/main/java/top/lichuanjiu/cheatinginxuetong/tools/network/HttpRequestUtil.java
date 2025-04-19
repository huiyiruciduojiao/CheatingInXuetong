package top.lichuanjiu.cheatinginxuetong.tools.network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import top.lichuanjiu.cheatinginxuetong.tools.JSON;

public class HttpRequestUtil {
    private static final String BOUNDARY = "----WebKitFormBoundary" + System.currentTimeMillis();
    private static final String LINE_FEED = "\r\n";

    public static String sendGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return readResponse(connection);
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("status", StatusType.STATUS_ERROR);
            map.put("code", responseCode);
            map.put("option", OptionsType.GET_EDITION);
            return JSON.createJsonData(map);
        }
    }

    public static String sendPost(String urlStr, String urlParameters) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(5000);
        connection.setDoOutput(true);

        try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
            dos.writeBytes(urlParameters);
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> map = new HashMap<>();
            map.put("status", StatusType.STATUS_ERROR);
            map.put("code", e.getMessage());
            map.put("option", OptionsType.AUTHENTICATION);
            return JSON.createJsonData(map);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return readResponse(connection);
        } else {
            //构建map数据
            Map<String, Object> map = new HashMap<>();
            map.put("status", StatusType.STATUS_ERROR);
            map.put("code", responseCode);
            map.put("option", OptionsType.AUTHENTICATION);
            return JSON.createJsonData(map);

        }
    }

    public static String uploadImage(String requestURL, String filePath, String username, String password) throws Exception {
        File file = new File(filePath);
        String charset = "UTF-8";

        HttpURLConnection connection = (HttpURLConnection) new URL(requestURL).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(60000);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

        try (OutputStream outputStream = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true)) {

            // 添加用户名字段
            writer.append("--" + BOUNDARY).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"Token\"").append(LINE_FEED);
            writer.append(LINE_FEED).append(username).append(LINE_FEED).flush();

            // 添加密码字段
            writer.append("--" + BOUNDARY).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"Sub\"").append(LINE_FEED);
            writer.append(LINE_FEED).append(password).append(LINE_FEED).flush();

            // 添加文件部分
            writer.append("--" + BOUNDARY).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"" + file.getName() + "\"").append(LINE_FEED);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName())).append(LINE_FEED);
            writer.append(LINE_FEED).flush();

            // 读取文件并写入输出流
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                Map<String, Object> map = new HashMap<>();
                map.put("status", StatusType.STATUS_ERROR);
                map.put("result", e.getMessage());
                map.put("option", OptionsType.SOLVE);
                return JSON.createJsonData(map);
            }
            outputStream.flush();
            writer.append(LINE_FEED).flush();
            writer.append("--" + BOUNDARY + "--").append(LINE_FEED).flush();
        }catch (Exception e){
            Map<String, Object> map = new HashMap<>();
            map.put("status", StatusType.STATUS_ERROR);
            map.put("result", e.getMessage());
            map.put("option", OptionsType.SOLVE);
            return JSON.createJsonData(map);
        }

        // 获取响应
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return readResponse(connection);
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("status", StatusType.STATUS_ERROR);
            map.put("result", responseCode);
            map.put("option", OptionsType.AUTHENTICATION);
            return JSON.createJsonData(map);
        }
    }

    private static String readResponse(HttpURLConnection connection) throws Exception {
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        Log.d("Response: " , response.toString());
        return response.toString();
    }


    /**
     * 将Map转换为表单数据
     *
     * @param mapData map对象
     * @return 表单数据
     */
    public static String convertMapToFormData(Map<String, Object> mapData) {
        StringBuilder formData = new StringBuilder();
        for (String key : mapData.keySet()) {
            try {
                formData.append(key).append("=").append(URLEncoder.encode(mapData.get(key).toString(), "UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return formData.toString();

    }

}
