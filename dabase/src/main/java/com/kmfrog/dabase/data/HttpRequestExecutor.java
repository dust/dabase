package com.kmfrog.dabase.data;

import android.content.Context;
import android.os.Handler;
import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.data.cache.HttpResponseCache;
import com.kmfrog.dabase.exception.BaseException;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 其于网络的数据请求执行器。
 *
 * @author dust@downjoy.com
 *         请求期望得到的数据类型。
 */
public class HttpRequestExecutor {

    private final OkHttpClient mOkHttpClient;
    private final Deliverer mDeliverer;
    private static volatile HttpRequestExecutor sInstance;
    private static Object lock = new Object();
    private static final long SLOW_REQUEST_THRESHOLD_MS = 0xbb8;// 3000

    private HttpRequestExecutor(Cache cache, Deliverer deliverer) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10, TimeUnit.SECONDS).writeTimeout(20, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).cookieJar(CookieJar.NO_COOKIES);
        if (cache != null) {
            builder.cache(cache);
        }
        mOkHttpClient = builder.build();
        mDeliverer = deliverer;
    }

    public static HttpRequestExecutor getInstance(final Context ctx, final Handler handler, long responseCacheSize) {
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null) {
                    Cache cache = null;
                    try {
                        //AndroidShimResponseCache androidShimResponseCache = responseCacheSize > 0 ? AndroidShimResponseCache.create(ctx.getCacheDir(), responseCacheSize) : null;
                        if (responseCacheSize > 0L) {
                            HttpResponseCache.install(ctx.getCacheDir(), responseCacheSize);
                            HttpResponseCache responseCache = HttpResponseCache.getInstalled();
                            if (responseCache != null) {
                                cache = responseCache.getCache();
                            }
                        }
                    } catch (Throwable ex) {
                        DLog.wtf(ex, "AndroidShimResponseCache.create(%s, %d)", ctx.getCacheDir().getAbsolutePath(), responseCacheSize);
                    }
                    sInstance = new HttpRequestExecutor(cache, new DefaultDeliverer(handler));
                }
            }
        }
        return sInstance;
    }


    public <D> void put(BaseRequest<D, ResponseBody> baseRequest) {
        if (baseRequest.isCanceled()) {
            return;
        }

        baseRequest.addMarker("dispatch");
        Request.Builder builder = new Request.Builder();
        builder.url(baseRequest.getUrl());
        Map<String, String> reqHeaders = baseRequest.getHeaders();
        Set<String> keys = reqHeaders == null ? new HashSet<String>() : reqHeaders.keySet();
        for (final String name : keys) {
            builder.addHeader(name, reqHeaders.get(name));
        }
        if (!baseRequest.shouldCache()) {
            builder.cacheControl(CacheControl.FORCE_NETWORK);
        }

        Request req = builder.build();
        Call okCall = mOkHttpClient.newCall(req);
        baseRequest.setOkCall(okCall);
        baseRequest.addMarker("enqueue");
        okCall.enqueue(new ResponseCallback<D>(baseRequest));
//        try {
//            baseRequest.addMarker("exec");
//            Response response = okCall.execute();
//        } catch (IOException ex) {
//            mDeliverer.postResponse(baseRequest, null, ex);
//        } catch (Throwable ex) {
//            mDeliverer.postResponse(baseRequest, null, ex);
//        }

    }


    private class ResponseCallback<D> implements Callback {

        private RawParser<D, ResponseBody> rawParser;
        private final BaseRequest<D, ResponseBody> baseRequest;
        private D result;
        private BaseException e;

        ResponseCallback(BaseRequest<D, ResponseBody> baseRequest) {
            this.baseRequest = baseRequest;
            rawParser = this.baseRequest.getParser();
            result = null;
        }

        @Override
        public void onFailure(Call call, IOException ex) {
            e = new BaseException(ex.getMessage(), ex);
            mDeliverer.postResponse(baseRequest, result, ex);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            baseRequest.addMarker("exec-http-complete");
            int code = response.code();
            logSlowRequests(response.receivedResponseAtMillis() - response.sentRequestAtMillis(), baseRequest, code);
            if (code > 400) {
                e = new BaseException(String.format("Http Code:%d(%s)", code, baseRequest.getUrl()));
            } else {
                Map<String, Object> extras = convertHeaders(response.headers());
                extras.put("code", code);
                if (baseRequest instanceof ImageRequest) {
                    ImageRequest bmpReq = (ImageRequest) baseRequest;
                    extras.put("mMaxHeight", bmpReq.getMaxHeight());
                    extras.put("mMaxWidth", bmpReq.getMaxWidth());
                    extras.put("mDecodeConfig", bmpReq.getDecodeConfig());
                }
                try {
                    baseRequest.addMarker("exec-parse");
                    result = rawParser.parse(response.body(), extras);
                } catch (BaseException ex) {
                    e = ex;
                } catch (Throwable ex) {
                    e = new BaseException(ex.getMessage(), ex);
                }
            }
            delivery();
        }

        void delivery() {
            mDeliverer.postResponse(baseRequest, result, e);
        }

    }


    private static Map<String, Object> convertHeaders(Headers aHeaders) {
        HashMap<String, Object> headers = new HashMap<String, Object>();
        if (headers != null) {
            Set<String> names = aHeaders.names();
            for (String name : names) {
                headers.put(name, aHeaders.get(name));
            }
        }
        return headers;
    }

    static <D> void logSlowRequests(long millis, BaseRequest<D, ResponseBody> request, int code) {
        if (DLog.DEBUG && millis > SLOW_REQUEST_THRESHOLD_MS) {
            DLog.d("HTTP response for request=<%s> [lifetime=%d], [rc=%d], [retryCount=]",
                    request.toString(), millis, code);
        }
    }

}
