package com.kmfrog.dabase.bitmap;

import android.graphics.Bitmap;

import com.kmfrog.dabase.data.DataCallback;

public abstract class BitmapLoadedCallback implements DataCallback<Bitmap> {

    private String mCacheKey;

    private String mUrl;

    private final BitmapLoader mBitmapLoader;

    private final BitmapDecorator mBitmapDecorator;

    private final boolean mUsingMemCache;

    public BitmapLoadedCallback(BitmapLoader loader, String cacheKey, String url, BitmapDecorator bitmapDecorator) {
        this(loader, cacheKey, url, bitmapDecorator, true);
    }

    public BitmapLoadedCallback(BitmapLoader loader, String cacheKey, String url, BitmapDecorator bitmapDecorator,
        boolean usingMemCache) {
        mUrl=url;
        mCacheKey=cacheKey;
        mBitmapLoader=loader;
        mBitmapDecorator=bitmapDecorator;
        mUsingMemCache=usingMemCache;
    }

    public BitmapLoadedCallback(BitmapLoader loader, String url, BitmapDecorator bitmapDecorator, boolean usingMemCache) {
        mUrl=url;
        mBitmapLoader=loader;
        mBitmapDecorator=bitmapDecorator;
        mUsingMemCache=usingMemCache;
    }

    public BitmapLoadedCallback(BitmapLoader loader, String url, BitmapDecorator bitmapDecorator) {
        this(loader, url, bitmapDecorator, true);
    }

    @Override
    public void onSuccess(Bitmap result) {
        if(mBitmapDecorator != null) {
            Bitmap bmp=mBitmapDecorator.decorate(result);
            result.recycle();
            result=null;
            if(bmp == null) {
                onFailure(new RuntimeException(new StringBuilder().append("url:").append(mUrl).append(" decorate == null")
                    .toString()));
                return;
            }
            result=bmp;
        }
        mBitmapLoader.onGetImageSuccess(mUsingMemCache, mUrl, mCacheKey, result);// TODO:是url，还是cacheKey?
        // onResponse(new BitmapContainer(mBitmapLoader,result,mUrl,mCacheKey,this));
    }

    @Override
    public void onFailure(Throwable e) {
        mBitmapLoader.onGetImageError(mUrl);
        // onResponse(new BitmapContainer(mBitmapLoader,null,mUrl,mCacheKey,this));
    }

    public abstract void onResponse(BitmapContainer bitmapContainer);

    public void setCacheKey(String cacheKey) {
        mCacheKey=cacheKey;
    }

    public boolean isUsingMemCache() {
        return mUsingMemCache;
    }
}