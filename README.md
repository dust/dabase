[![Build Status](https://img.shields.io/travis/dust/dabase.svg?style=flat&branch=master)](https://travis-ci.org/dust/dabase)

dabase
=======

dabase是一个封装了网络请求的库，其解决的核心问题是结构化rest风格的API接口调用以及网络图片加载过程。它基于[okhttp](https://github.com/square/okhttp/)(3.4.1)和[universal-image-loader](https://github.com/nostra13/Android-Universal-Image-Loader)(1.9.5)等开源仓库。 也 **全局** 使用了 ` java.net.ResponseCache ` 规范的缓存机制


# 结构模型


    BaseRequest(Uri, Parser, AsyncObserver)
        |
        |
        okhttp3.Request
                |
                |
                Call=OkHttpClient.newCall(request)
                Call.enqueue(new ResponseCallback(baseRequest))
                                    |
                                    |
                                    ResponseCallback(onResponse|onFailure)
                                                    |
                                                    onResponse:Parser.parse(okhttp3.Response)
                                                    onFailure(BaseException)
                                                    |
                                                    DefaultDeliverer.postResponse(baseRequest, result, ex)
                                                                        |
                                                                        |
                                                                    AsyncObserver:
                                                                    onSuccess(result)
                                                                    onFailure(BaseException)
                                                                    onAppError(AppException)
                                                                        |
                                                                        |
                                                                        notifyDataChanged()


# ResponseCache

如果responseCacheSize大于0，则会开启 ` java.net.ResponseCache ` ， 它是 ** 全局 ** 性的，会影响所有HttpURLConnection/HttpsURLConnection等相关方法，但它是受控于服务端Response的header字段中Cache-Control的设置值。

``` java

    public static HttpRequestExecutor getInstance(Context ctx, final Handler handler, long responseCacheSize) {
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
                        DLog.wtf(ex, "AndroidShimResponseCache.create(%s, %d)", ctx.getCacheDir(), responseCacheSize);
                    }
                    sInstance = new HttpRequestExecutor(cache, new DefaultDeliverer(handler));
                }
            }
        }
        return sInstance;
    }

```

# universalimageloader



``` java

BitmapLoader.getInstance().bind(imageView, url, placeHolderResId, decorator, maxWidth, maxHeight);

public void bind(final ImageView imageView, final String url, final int placeHolderResId, BitmapDisplayer decorator,
                     int maxWidth, int maxHeight);

```

