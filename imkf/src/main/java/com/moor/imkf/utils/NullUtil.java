package com.moor.imkf.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 检测值是否为空
 * Created by longwei on 2015/10/30.
 */
public class NullUtil {

    /**
     * 检测字符串是否是空的，若是空的则返回空字符串，避免空指针错误
     * @param str
     * @return
     */
    public static String checkNull(String str) {
            if(str != null && !"".equals(str)) {
                return str;
            }else {
                return "";
            }
    }

    public static Integer checkNull(Integer i) {
        if(i != null) {
            return i;
        }else {
            return -1;
        }
    }

    public static long checkNull(Long i) {
        if(i != null) {
            return i;
        }else {
            return -1;
        }
    }
    public static String checkJsonStringNull(JSONObject jb, String key) {
        String value = "";
        try{
            value = jb.getString(key);
        }catch(JSONException e) {

        }
        return value;
    }

    public static JSONArray checkJsonArrayNull(JSONObject jb, String key) {
        JSONArray value = new JSONArray();
        try{
            value = jb.getJSONArray(key);
        }catch(JSONException e) {

        }
        return value;
    }
    public static JSONObject checkJsonObjectNull(JSONObject jb, String key) {
        JSONObject value = new JSONObject();
        try{
            value = jb.getJSONObject(key);
        }catch(JSONException e) {

        }
        return value;
    }
}
