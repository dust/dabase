package com.kmfrog.dabase.example;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import com.kmfrog.dabase.app.BaseApp;
import com.kmfrog.dabase.data.HttpRequestExecutor;

/**
 * Created by dust on 16-10-10.
 */
public class App extends BaseApp {

    @Override
    protected int getApiCacheSize() {
        return 0x640000; //10M
    }

    @Override
    protected String getChannelId() {
        return "1";
    }

    @Override
    protected String getKey() {
        return "xxx";
    }

    @Override
    protected int getImageCacheSize() {
        return 0x6400000; //100M
    }

    private static App sInstance;

    @Override
    public void onCreate() {
        sInstance = this;
        super.onCreate();
    }

    public static App get() {
        return sInstance;
    }
}
