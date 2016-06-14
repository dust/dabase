package com.kmfrog.dabase.data.extra;

import java.util.HashMap;
import java.util.Map;

import com.kmfrog.dabase.data.Cache;
import com.kmfrog.dabase.data.Cache.Entry;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.data.Request;

import android.graphics.Bitmap;


public class BitmapCacheRequestExecutor extends CacheRequestExecutor<Bitmap> {

    public BitmapCacheRequestExecutor(RawParser<Bitmap, byte[]> parser, Cache cache) {
        super(parser, cache);
    }

    @Override
    Map<String, Object> getExtras(Entry entry, Request<Bitmap, byte[]> request) {
        Map<String,Object> extras=new HashMap<String,Object>();
        ImageRequest req = (ImageRequest)request;
        extras.put("mMaxHeight",req.getMaxHeight());
        extras.put("mMaxWidth", req.getMaxWidth());
        extras.put("mDecodeConfig", req.getDecodeConfig());
        return extras;
    }

}
