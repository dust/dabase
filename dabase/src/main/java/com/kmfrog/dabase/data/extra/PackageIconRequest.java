package com.kmfrog.dabase.data.extra;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.kmfrog.dabase.AsyncObserver;
import com.kmfrog.dabase.data.Request;


public class PackageIconRequest extends Request<Bitmap, Drawable> {

    public PackageIconRequest(Uri uri, AsyncObserver<Bitmap, Throwable> callback) {
        super(uri, callback);
    }

    @Override
    public String getUrl() {
        return getUri().toString();
                
    }

}
