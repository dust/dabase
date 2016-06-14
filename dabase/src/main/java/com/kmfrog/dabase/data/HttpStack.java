package com.kmfrog.dabase.data;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;


public interface HttpStack {

    <D> HttpResponse performRequest(Request<D, byte[]> request, Map<String, String> headers)throws IOException;
}
