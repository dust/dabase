package com.kmfrog.dabase.data.extra;

import java.util.HashMap;
import java.util.Map;

import com.kmfrog.dabase.data.Cache;
import com.kmfrog.dabase.data.Cache.Entry;
import com.kmfrog.dabase.data.Network;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.data.Request;

public class BasedUriNetworkRequestExecutor extends NetworkRequestExecutor<Object> {

    public BasedUriNetworkRequestExecutor(Network network, RawParser<Object, byte[]> parser, Cache cache) {
        super(network, parser, cache);
    }

    @Override
    Map<String, Object> getExtras(Map<String, String> headers, Request<Object, byte[]> request, Entry entry) {
        Map<String, Object> extas=new HashMap<String, Object>();
        if(entry != null) {
            extas.put("charset", entry.charset);
        } else {
            extas.put("charset", parseCharset(headers));
        }
        extas.put(BasedUriRawParserFactory.DEF_IDENTITY_KEY, request.getUri());
        return extas;
    }

}
