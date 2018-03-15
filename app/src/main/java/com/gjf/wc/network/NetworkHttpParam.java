package com.gjf.wc.network;

import android.support.v4.util.ArrayMap;

import java.util.Map;

/**
 * Created by guojunfu on 18/3/15.
 */

public class NetworkHttpParam {

    private Map<String, String> params;

    public NetworkHttpParam() {
        this.params = new ArrayMap();
    }

    public NetworkHttpParam(Map<String, String> mapString) {
        this.params = mapString;
    }

    public Map<String, String> getPara() {
        return this.params;
    }

    public void put(String key, String value) {
        this.params.put(key, value);
    }
}
