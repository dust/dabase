package com.kmfrog.dabase.bitmap;

import android.graphics.Bitmap;

public class BitmapLruCache extends LruCache<String, Bitmap> {

    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }

    protected int sizeOf(String key, Bitmap bitmap) {
        return bitmap.getRowBytes() * bitmap.getHeight();
    }


}
