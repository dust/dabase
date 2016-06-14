package com.kmfrog.dabase.data.extra;

import java.util.Map;

import com.kmfrog.dabase.data.Cache;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.data.RequestExecutor;
import com.kmfrog.dabase.data.Response;
import com.kmfrog.dabase.exception.BaseException;


public abstract class CacheRequestExecutor<D> extends RequestExecutor<D, byte[]> {

    public CacheRequestExecutor(RawParser<D, byte[]> parser, Cache cache) {
        super(parser,cache);
    }

    @Override
    public Response<D> exec(Request<D, byte[]> request) throws BaseException{
        Cache.Entry entry=request.getCacheEntry();
        Map<String,Object> extras=getExtras(entry,request);
//        extras.put("charset",entry.charset);
        final RawParser<D,byte[]> parser=getParser();
        D d = parser.parse(entry.toBytes(), extras);
        return new Response<D>(d,false);
    }
    
    abstract Map<String,Object> getExtras(Cache.Entry entry, Request<D,byte[]> request);

}
