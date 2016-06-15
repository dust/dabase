package com.kmfrog.dabase.data.extra;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.kmfrog.dabase.data.Cache;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.data.RequestExecutor;
import com.kmfrog.dabase.data.Response;
import com.kmfrog.dabase.exception.BaseException;
import com.kmfrog.dabase.util.BitmapUtils;

public class AppIconRequestExecutor extends RequestExecutor<Bitmap, Drawable> {

    private Context mContext;

    protected AppIconRequestExecutor(RawParser<Bitmap, Drawable> parser, Cache cache) {
        super(parser, cache);
    }

    public AppIconRequestExecutor(Context context, RawParser<Bitmap, Drawable> parser, Cache cache) {
        this(parser,cache);
        this.mContext=context;
    }

    @Override
    public Response<Bitmap> exec(Request<Bitmap, Drawable> request) {
        Uri uri=request.getUri();// ie:app://com.google.maps
        String pkgName=uri.getHost();
        if(pkgName != null) {
            try {
                Drawable icon=mContext.getPackageManager().getApplicationIcon(pkgName);
                Bitmap bitmap=getParser().parse(icon, null);
                if(request.shouldCache()){
                    byte[] bytes = BitmapUtils.bitmap2ByteArray(bitmap);
                    long serverDateMillis=System.currentTimeMillis();
                    long softTtl = serverDateMillis + 86400L * 1000L * 7;
                    putCacheEntry(bytes, request.getCacheKey(), null, softTtl, serverDateMillis, softTtl, null, null);
                }
                return new Response<Bitmap>(bitmap, false);
            } catch(Throwable ex) {
                return new Response<Bitmap>(new BaseException(ex));
            }
        }
        return new Response<Bitmap>(new BaseException(new RuntimeException(new StringBuilder().append("request:")
            .append(request.toString()).append(" getUri(), pkgName is null").toString())));
    }
}
