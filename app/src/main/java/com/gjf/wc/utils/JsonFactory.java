package com.gjf.wc.utils;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by guojunfu on 18/3/15.
 */

public class JsonFactory {

    private static Gson gson = new Gson();

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
