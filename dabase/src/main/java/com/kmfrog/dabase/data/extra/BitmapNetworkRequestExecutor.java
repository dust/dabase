package com.kmfrog.dabase.data.extra;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;

import com.kmfrog.dabase.data.Cache;
import com.kmfrog.dabase.data.Cache.Entry;
import com.kmfrog.dabase.data.Network;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.data.Request;


public class BitmapNetworkRequestExecutor extends NetworkRequestExecutor<Bitmap> {

    public BitmapNetworkRequestExecutor(Network network, RawParser<Bitmap, byte[]> parser, Cache cache) {
        super(network, parser, cache);
    }

    @Override
    Map<String, Object> getExtras(Map<String, String> headers, Request<Bitmap, byte[]> request, Entry entry) {
        Map<String,Object> extras=new HashMap<String,Object>();
        ImageRequest req = (ImageRequest)request;
        extras.put("mMaxHeight",req.getMaxHeight());
        extras.put("mMaxWidth", req.getMaxWidth());
        extras.put("mDecodeConfig", req.getDecodeConfig());
        return extras;
    }

}
