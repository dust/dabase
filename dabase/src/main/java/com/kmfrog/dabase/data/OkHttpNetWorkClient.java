package com.kmfrog.dabase.data;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;

@SuppressLint("NewApi")
public class OkHttpNetWorkClient {

    private static OkHttpNetWorkClient sInstance = new OkHttpNetWorkClient();

    private OkHttpClient mOkHttpClient = new OkHttpClient();

    private OkHttpNetWorkClient() {
        /**
         * 设置连接的超时时间
         */

        mOkHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(20, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).cookieJar(CookieJar.NO_COOKIES).build();
//		mOkHttpClient.setConnectTimeout(10, TimeUnit.SECONDS);
//		/**
//		 * 设置响应的超时时间
//		 */
//		mOkHttpClient.setWriteTimeout(20, TimeUnit.SECONDS);
//		/**
//		 * 请求的超时时间
//		 */
//		mOkHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
//		mOkHttpClient.setCookieHandler(new java.net.CookieManager(null,
//				CookiePolicy.ACCEPT_NONE));
    }

    public static OkHttpNetWorkClient getInstance() {
        return sInstance;
    }

    /**
     * 通过单例模式构造对象
     *
     * @return NetWorkClient
     */
    public  OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

}
