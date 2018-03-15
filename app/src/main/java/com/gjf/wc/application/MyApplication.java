package com.gjf.wc.application;

import android.app.Application;
import android.content.Context;

import com.gjf.wc.utils.GlobalConfig;

/**
 * Created by guojunfu on 18/3/15.
 */

public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // 缓存Context
        GlobalConfig.setAppContext(this);
    }
}
