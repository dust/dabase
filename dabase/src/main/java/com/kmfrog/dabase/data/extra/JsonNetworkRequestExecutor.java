package com.kmfrog.dabase.data.extra;

import java.util.HashMap;
import java.util.Map;


import com.kmfrog.dabase.data.Cache;
import com.kmfrog.dabase.data.Network;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.data.Request;


public class JsonNetworkRequestExecutor extends NetworkRequestExecutor<Object> {

    public JsonNetworkRequestExecutor(RawParser<Object, byte[]> parser, Cache cache) {
        super(parser, cache);
    }

    public JsonNetworkRequestExecutor(Network network, RawParser<Object, byte[]> parser, Cache cache) {
        super(network, parser, cache);
    }

    @Override
    Map<String, Object> getExtras(Map<String, String> headers, Request<Object, byte[]> request, Cache.Entry entry) {
        Map<String,Object> extas=new HashMap<String,Object>();
        if(entry!=null){
            extas.put("charset", entry.charset);
            extas.put(JsonRawParserFactory.DEF_DATA_META, entry.dataMeta);
        }
        else{
            extas.put("charset", parseCharset(headers));
            extas.put(JsonRawParserFactory.DEF_DATA_META, parseDataMeta(headers));
        }
        return extas;
        
    }

}
