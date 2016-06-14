package com.kmfrog.dabase.bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.data.RequestQueue;

public class BitmapLoader {

    class RequestListenerWrapper {

        public void addHandler(BitmapContainer bitmapContainer) {
            handlers.add(bitmapContainer);
        }

        public boolean removeHandlerAndCancelIfNecessary(BitmapContainer bitmapContainer) {
            handlers.remove(bitmapContainer);
            if(handlers.size() == 0) {
                request.cancel();
                return true;
            }
            return false;
        }

        @SuppressWarnings("rawtypes")
        public RequestListenerWrapper(Request request, BitmapContainer bitmapContrainer) {
            handlers=new ArrayList<BitmapContainer>();
            this.request=request;
            handlers.add(bitmapContrainer);
        }

        private List<BitmapContainer> handlers;

        @SuppressWarnings("rawtypes")
        private Request request;

        private Bitmap responseBitmap;

    }

    public BitmapLoader(RequestQueue requestQueue) {
        mCachedRemoteImages=new BitmapLruCache(MAX_CACHE_SIZE_IN_BYTES);
        mRequestQueue=requestQueue;
    }

    private void batchResponse(String s, RequestListenerWrapper requestListenerWrapper) {
        mBatchedResponses.put(s, requestListenerWrapper);
        if(mRunnable == null) {
            mRunnable=new Runnable() {

                public void run() {
                    for(Iterator<RequestListenerWrapper> iterator=mBatchedResponses.values().iterator(); iterator.hasNext();) {
                        RequestListenerWrapper reqListenerWrapper=iterator.next();
                        Iterator<BitmapContainer> subIterator=reqListenerWrapper.handlers.iterator();
                        while(subIterator.hasNext()) {
                            BitmapContainer bitmapcontainer=subIterator.next();
                            bitmapcontainer.mBitmap=reqListenerWrapper.responseBitmap;
                            bitmapcontainer.mBitmapLoaded.onResponse(bitmapcontainer);
                        }
                    }

                    mBatchedResponses.clear();
                    mRunnable=null;
                }
            };
            mHandler.postDelayed(mRunnable, 100L);
        }
    }

    @SuppressWarnings("rawtypes")
    private BitmapContainer get(String url, boolean flag, String cacheKey, Bitmap placeholder,
        ImageRequestCreator remoterequestcreator, BitmapLoadedCallback bitmaploadedhandler) {
        BitmapContainer bitmapcontainer;
        if(!flag && TextUtils.isEmpty(url)) {
            bitmapcontainer=new BitmapContainer(this, placeholder, null, null, null);
        } else {
            Bitmap bitmap1=(Bitmap)mCachedRemoteImages.get(cacheKey);
            if(bitmap1 != null && !bitmap1.isRecycled()) {
                bitmapcontainer=new BitmapContainer(this, bitmap1, url, null, null);
            } else {
                bitmapcontainer=new BitmapContainer(this, placeholder, url, cacheKey, bitmaploadedhandler);
                RequestListenerWrapper requestlistenerwrapper=(RequestListenerWrapper)mInFlightRequests.get(url);
                if(requestlistenerwrapper != null) {
                    requestlistenerwrapper.addHandler(bitmapcontainer);
                } else {
                    Request request=remoterequestcreator.create();
                    mRequestQueue.add(request);
                    mInFlightRequests.put(url, new RequestListenerWrapper(request, bitmapcontainer));
                }
            }
        }
        return bitmapcontainer;
    }

    private static String getCacheKey(String s, int i, int j, String subCacheKey) {
        StringBuilder sb=new StringBuilder();
        sb.append("#W").append(i).append("#H").append(j).append(s);
        if(subCacheKey != null) {
            sb.append("#").append(subCacheKey);
        }
        return sb.toString();
    }

    void onGetImageError(String url) {
        RequestListenerWrapper requestlistenerwrapper=(RequestListenerWrapper)mInFlightRequests.remove(url);
        if(requestlistenerwrapper != null) {
            batchResponse(url, requestlistenerwrapper);
            if(DLog.DEBUG) {
                DLog.e("Bitmap error %s", requestlistenerwrapper.request.getUrl());
            }
        }
    }

    void onGetImageSuccess(String url, String cacheKey, Bitmap bitmap) {
        onGetImageSuccess(true, url, cacheKey, bitmap);
    }

    void onGetImageSuccess(boolean usingMemCache, String url, String cacheKey, Bitmap bitmap) {
        if(usingMemCache && bitmap.getHeight() * bitmap.getRowBytes() <= 0x7d000) {
            mCachedRemoteImages.put(cacheKey, bitmap);
        }
        RequestListenerWrapper requestlistenerwrapper=(RequestListenerWrapper)mInFlightRequests.remove(url);
        if(requestlistenerwrapper != null) {
            requestlistenerwrapper.responseBitmap=bitmap;
            batchResponse(url, requestlistenerwrapper);
            if(DLog.DEBUG) {
                DLog.d("Loaded bitmap %s", requestlistenerwrapper.request.getUrl());
            }
        }
    }

    public void drain(int i) {
        mRequestQueue.drain(i);
        ArrayList<String> keys=new ArrayList<String>();
        for(Iterator<String> iterator=mInFlightRequests.keySet().iterator(); iterator.hasNext();) {
            String key=iterator.next();
            RequestListenerWrapper listenerWrapper=mInFlightRequests.get(key);
            if(listenerWrapper.request == null || listenerWrapper.request.getSequence() < i) {
                keys.add(key);
            }
        }

        for(Iterator<String> iter=keys.iterator(); iter.hasNext();) {
            mInFlightRequests.remove(iter.next());
        }

    }

    public void dropoutFromCache(String url, int maxWidth, int maxHeight) {
        dropoutFromCache(url, maxWidth, maxHeight, null);
    }

    public void dropoutFromCache(String url, int maxWidth, int maxHeight, String subCacheKey) {
        String cacheKey=getCacheKey(url, maxWidth, maxHeight, subCacheKey);
        mCachedRemoteImages.remove(cacheKey);
    }

    public void dropoutFromCache(String url) {
        dropoutFromCache(url, 0, 0);
    }

    public BitmapContainer get(String url, Bitmap bitmap, BitmapLoadedCallback bitmapLoadedHandler) {
        return get(url, bitmap, bitmapLoadedHandler, 0, 0, null, false);
    }

    public BitmapContainer get(String url, Bitmap bitmap, BitmapLoadedCallback bitmapLoadedHandler,
        ImageRequestCreator imgReqCreator) {
        return get(url, bitmap, bitmapLoadedHandler, 0, 0, null, false, imgReqCreator);
    }

    public BitmapContainer get(String url, Bitmap bitmap, BitmapLoadedCallback bitmapLoadedHandler, String subCacheKey) {
        return get(url, bitmap, bitmapLoadedHandler, 0, 0, subCacheKey, false);
    }

    public BitmapContainer get(String url, Bitmap bitmap, BitmapLoadedCallback bitmapLoadedHandler, String subCacheKey,
        ImageRequestCreator imgReqCreator) {
        return get(url, bitmap, bitmapLoadedHandler, 0, 0, subCacheKey, false, imgReqCreator);
    }

    public BitmapContainer get(String url, Bitmap bitmap, BitmapLoadedCallback bitmapLoadedHandler, boolean ignoreCacheExpiredTime) {
        return get(url, bitmap, bitmapLoadedHandler, 0, 0, null, ignoreCacheExpiredTime);
    }

    public BitmapContainer get(String url, Bitmap bitmap, BitmapLoadedCallback bitmapLoadedHandler, boolean ignoreCacheExpiredTime,
        ImageRequestCreator imgReqCreator) {
        return get(url, bitmap, bitmapLoadedHandler, 0, 0, null, ignoreCacheExpiredTime, imgReqCreator);
    }

    public BitmapContainer get(final String requestUrl, Bitmap bitmap, BitmapLoadedCallback bitmapLoadedHandler,
        final int maxWidth, final int maxHeight, String subCacheKey, final boolean ignoreCacheExpiredTime) {
        final String cacheKey=getCacheKey(requestUrl, maxWidth, maxHeight, subCacheKey);
        bitmapLoadedHandler.setCacheKey(cacheKey);
        return get(requestUrl, false, cacheKey, bitmap, new GenericImageRequestCreator(Uri.parse(requestUrl), bitmapLoadedHandler,
            maxWidth, maxHeight, ignoreCacheExpiredTime), bitmapLoadedHandler);
    }

    public BitmapContainer get(final String requestUrl, Bitmap bitmap, BitmapLoadedCallback bitmapLoadedHandler,
        final int maxWidth, final int maxHeight, String subCacheKey, final boolean ignoreCacheExpiredTime,
        ImageRequestCreator imgReqCreator) {
        final String cacheKey=getCacheKey(requestUrl, maxWidth, maxHeight, subCacheKey);
        bitmapLoadedHandler.setCacheKey(cacheKey);
        return get(requestUrl, false, cacheKey, bitmap, imgReqCreator, bitmapLoadedHandler);
    }

    public void clean() {
        if(mCachedRemoteImages != null) {
            mCachedRemoteImages.clear();
        }
    }

    private static int MAX_CACHE_SIZE_IN_BYTES=0x300000;

    final HashMap<String, RequestListenerWrapper> mBatchedResponses=new HashMap<String, RequestListenerWrapper>();

    private final BitmapLruCache mCachedRemoteImages;

    private final Handler mHandler=new Handler(Looper.getMainLooper());

    final HashMap<String, RequestListenerWrapper> mInFlightRequests=new HashMap<String, RequestListenerWrapper>();

    private final RequestQueue mRequestQueue;

    private Runnable mRunnable;

}
