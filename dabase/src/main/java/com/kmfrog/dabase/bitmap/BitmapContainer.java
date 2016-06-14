package com.kmfrog.dabase.bitmap;

import android.graphics.Bitmap;

import com.kmfrog.dabase.bitmap.BitmapLoader.RequestListenerWrapper;

public class BitmapContainer {

    /**
     * 
     */
    private final BitmapLoader mBitmapLoader;

    Bitmap mBitmap;

    BitmapLoadedCallback mBitmapLoaded;

    private String mCacheKey;

    private final String mRequestUrl;

    public void cancelRequest() {
        if(mBitmapLoaded != null) {
            RequestListenerWrapper requestlistenerwrapper=(RequestListenerWrapper)this.mBitmapLoader.mInFlightRequests.get(mCacheKey);
            if(requestlistenerwrapper != null) {
                if(requestlistenerwrapper.removeHandlerAndCancelIfNecessary(this))
                    this.mBitmapLoader.mInFlightRequests.remove(mCacheKey);
            } else {
                RequestListenerWrapper requestlistenerwrapper1=(RequestListenerWrapper)this.mBitmapLoader.mBatchedResponses.get(mCacheKey);
                if(requestlistenerwrapper1 != null) {
                    if(requestlistenerwrapper1.removeHandlerAndCancelIfNecessary(this)) {
                        this.mBitmapLoader.mBatchedResponses.remove(mCacheKey);
                    }
                }
            }
        }
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public String getRequestUrl() {
        return mRequestUrl;
    }

    public BitmapContainer(BitmapLoader bitmapLoader, Bitmap bitmap, String url, String cacheKey, BitmapLoadedCallback bitmapLoadedHandler) {
        mBitmapLoader=bitmapLoader;
        mBitmap=bitmap;
        mRequestUrl=url;
        mCacheKey=cacheKey;
        mBitmapLoaded=bitmapLoadedHandler;
    }
}