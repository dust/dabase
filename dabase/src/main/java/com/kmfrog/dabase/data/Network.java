package com.kmfrog.dabase.data;

import org.apache.http.HttpResponse;

public interface Network {

    public <D> HttpResponse performRequest(Request<D, byte[]> request) throws Throwable;

}
