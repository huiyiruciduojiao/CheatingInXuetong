package top.lichuanjiu.cheatinginxuetong.tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JSON {
    public static String createJsonData(Map<String,Object> data){
        JSONObject jsonObject = new JSONObject();
        for(String key:data.keySet()){
            try {
                jsonObject.put(key,data.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject.toString();

    }
    public static Map<String,Object> parseJsonData(String data){
        Map<String,Object> map = null;
        try {
            map = new HashMap<>();
            JSONObject jsonObject = new JSONObject(data);
            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                String key = it.next();
                map.put(key,jsonObject.get(key));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }
}
