package com.kmfrog.dabase.data.extra;

import java.util.HashMap;
import java.util.Map;

import com.kmfrog.dabase.data.Cache;
import com.kmfrog.dabase.data.Cache.Entry;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.data.Request;

public class JsonCacheRequestExecutor extends CacheRequestExecutor<Object> {

    public JsonCacheRequestExecutor(RawParser<Object, byte[]> parser, Cache cache) {
        super(parser, cache);

    }

    @Override
    Map<String, Object> getExtras(Entry entry, Request<Object, byte[]> request) {
        Map<String, Object> extas=new HashMap<String, Object>();

        extas.put("charset", entry.charset);

        return extas;
    }

}
