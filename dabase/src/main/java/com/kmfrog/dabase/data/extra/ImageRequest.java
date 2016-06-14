package com.kmfrog.dabase.data.extra;

import android.graphics.Bitmap;
import android.net.Uri;

import com.kmfrog.dabase.AsyncObserver;
import com.kmfrog.dabase.data.Request;

public class ImageRequest extends Request<Bitmap, byte[]> {

    private final Bitmap.Config mDecodeConfig;

    private final int mMaxHeight;

    private final int mMaxWidth;

    public ImageRequest(Uri uri, AsyncObserver<Bitmap, Throwable> callback) {
        this(uri, callback, 0, 0, Bitmap.Config.RGB_565);
    }

    public ImageRequest(Uri uri, AsyncObserver<Bitmap, Throwable> callback, int width, int height, Bitmap.Config config) {
        super(uri, callback);
        mDecodeConfig=config == null ? Bitmap.Config.RGB_565 : config;
        mMaxHeight=height;
        mMaxWidth=width;
    }

    public Request.Priority getPriority() {
        return Request.Priority.LOW;
    }

    @Override
    public String getUrl() {
        return getUri().toString();
    }

    public int getMaxHeight() {
        return mMaxHeight;
    }

    public int getMaxWidth() {
        return mMaxWidth;
    }

    public Bitmap.Config getDecodeConfig() {
        return mDecodeConfig;
    }
    
    public String getBaseClazz(){
        return ImageRequest.class.getSimpleName();
    }

}
