package com.kmfrog.dabase.example;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import com.kmfrog.dabase.data.HttpRequestExecutor;

/**
 * Created by dust on 16-10-10.
 */
public class App extends Application {

    public HttpRequestExecutor sHttpRequestExecutor;

    private static App sInstance;

    @Override
    public void onCreate() {
        sInstance = this;
        sHttpRequestExecutor = HttpRequestExecutor.getInstance(getApplicationContext(), new Handler(Looper.getMainLooper()), 0x640000);
        super.onCreate();
    }

    public static App get() {
        return sInstance;
    }
}
