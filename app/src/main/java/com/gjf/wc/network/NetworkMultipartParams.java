package com.gjf.wc.network;

import android.support.v4.util.ArrayMap;

import java.util.Map;

/**
 * Created by guojunfu on 18/3/15.
 */

public class NetworkMultipartParams {

    private Map<String, String> paramsStr = new ArrayMap();
    private Map<String, byte[]> paramsBytes = new ArrayMap();

    public NetworkMultipartParams() {
    }

    public void setParamsStr(Map<String, String> paramsStr) {
        this.paramsStr = paramsStr;
    }

    public void setParamsBytes(Map<String, byte[]> paramsBytes) {
        this.paramsBytes = paramsBytes;
    }

    public Map<String, String> getParamsStr() {
        return this.paramsStr;
    }

    public Map<String, byte[]> getParamsBytes() {
        return this.paramsBytes;
    }

    public void put(String key, String value) {
        this.paramsStr.put(key, value);
    }

    public void put(String key, byte[] value) {
        this.paramsBytes.put(key, value);
    }
}