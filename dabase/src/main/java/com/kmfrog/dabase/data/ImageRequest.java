package com.kmfrog.dabase.data;

import android.graphics.Bitmap;
import android.net.Uri;
import com.kmfrog.dabase.AsyncObserver;
import com.kmfrog.dabase.data.extra.BitmapRawParser;
import okhttp3.ResponseBody;


public class ImageRequest extends BaseRequest<Bitmap, ResponseBody> {
    private final Bitmap.Config mDecodeConfig;

    private final int mMaxHeight;

    private final int mMaxWidth;

    public ImageRequest(Uri uri, AsyncObserver<Bitmap> callback) {
        this(uri, callback, 0, 0, Bitmap.Config.RGB_565);
    }

    public ImageRequest(Uri uri, AsyncObserver<Bitmap> callback, int width, int height, Bitmap.Config config) {
        super(uri, new BitmapRawParser(), callback);
        mDecodeConfig = config == null ? Bitmap.Config.RGB_565 : config;
        mMaxHeight = height;
        mMaxWidth = width;
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


}
