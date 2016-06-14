package com.kmfrog.dabase.data.extra;

import java.util.HashMap;
import java.util.Map;

import android.net.Uri;

import com.kmfrog.dabase.AsyncObserver;
import com.kmfrog.dabase.data.Request;

public class BasedUriRequest<D> extends Request<D, byte[]> {

    private Map<String, String> params=new HashMap<String, String>();

    public BasedUriRequest(Uri uri, AsyncObserver<D, Throwable> callback) {
        super(uri, callback);
    }

    @Override
    public String getUrl() {
        return getUri().toString();
    }

    public void addPostParam(String key, String value) {
        params.put(key, value);
    }

    public void addPostParams(Map<String, String> map) {
        if(map != null) {
            params.putAll(map);
        }
    }

    @Override
    public Map<String, String> getPostParams() {
        return params;
    }

    public String getBaseClazz() {
        return BasedUriRequest.class.getSimpleName();
    }

}
