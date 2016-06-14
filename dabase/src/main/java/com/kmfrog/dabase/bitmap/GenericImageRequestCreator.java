package com.kmfrog.dabase.bitmap;

import android.graphics.Bitmap;
import android.net.Uri;

import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.data.extra.ImageRequest;

public class GenericImageRequestCreator implements ImageRequestCreator {

    private final Uri mUri;

    private final BitmapLoadedCallback mCallback;

    private final int mMaxWidth;

    private final int mMaxHeight;

    private final boolean mIgnoreCacheExpiredTime;

    public GenericImageRequestCreator(Uri uri, BitmapLoadedCallback callback, int maxWidth, int maxHeight,
        boolean ignoreCacheExpiredTime) {

        mCallback=callback;
        mMaxWidth=maxWidth;
        mMaxHeight=maxHeight;
        mIgnoreCacheExpiredTime=ignoreCacheExpiredTime;
        mUri=uri;
    }

    @Override
    public Request<Bitmap, byte[]> create() {
        ImageRequest request=new ImageRequest(mUri, mCallback, mMaxWidth, mMaxHeight, null);
        request.setIgnoreCacheExpiredTime(mIgnoreCacheExpiredTime);
        return request;

    }

}
