package com.kmfrog.dabase.data.extra;

import java.util.Collections;
import java.util.Map;

import com.kmfrog.dabase.data.Response;
import com.kmfrog.dabase.exception.BaseException;





public class NetworkResponse<D> extends Response<D> {
    
    public final Map<String,String> mHeaders;
    
    public final boolean mNotModified;
    
    public final int mStatusCode;
    
    public NetworkResponse(int statusCode, D data, Map<String,String> headers, boolean notModified, boolean needCache){
        super(data,needCache);
        this.mHeaders = headers;
        this.mNotModified = notModified;
        this.mStatusCode = statusCode;
    }
    
    public NetworkResponse(BaseException ex){
        super(ex);
        mHeaders=null;
        mNotModified=false;
        mStatusCode=0;
    }

    protected NetworkResponse(D data, boolean needCache) {
        this(200, data,Collections.<String, String> emptyMap(), false, needCache);
    }

}
