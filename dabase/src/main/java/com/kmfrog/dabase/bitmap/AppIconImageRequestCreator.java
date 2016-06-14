package com.kmfrog.dabase.bitmap;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.data.extra.PackageIconRequest;

public class AppIconImageRequestCreator implements ImageRequestCreator {

    private final Uri mUri;

    private final BitmapLoadedCallback mCallback;

    private final boolean mIgnoreCacheExpiredTime;

    public AppIconImageRequestCreator(String sheme, String iconKeyword, BitmapLoadedCallback callback, boolean ignoreCacheExpiredTime) {
        mCallback=callback;
        mIgnoreCacheExpiredTime=ignoreCacheExpiredTime;
        mUri=Uri.parse(new StringBuilder(sheme).append("://").append(iconKeyword).toString());
    }

    @Override
    public Request<Bitmap, Drawable> create() {
        PackageIconRequest request=new PackageIconRequest(mUri, mCallback);
        request.setIgnoreCacheExpiredTime(mIgnoreCacheExpiredTime);
        return request;
    }

}
