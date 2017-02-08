package com.foune.tracklinedemo;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * descreption:
 * company: foune.com
 * Created by xuyanliang on 2017/2/7 0007.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
    }
}
