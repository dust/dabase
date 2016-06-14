package com.kmfrog.dabase.data;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.cookie.DateUtils;

import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.exception.BaseException;
import com.kmfrog.dabase.exception.NoConnectionException;
import com.kmfrog.dabase.exception.TimeoutException;

public class DefaultNetwork implements Network {

    protected final HttpStack mHttpStack;

    public DefaultNetwork(HttpStack httpstack) {
        mHttpStack=httpstack;
    }

    private void addCacheHeaders(Map<String, String> map, Cache.Entry entry) {
        if(entry != null) {
            if(entry.etag != null) {
                map.put("If-None-Match", entry.etag);
            }
            if(entry.bornMillisTimes > 0L) {
                map.put("If-Modified-Since", DateUtils.formatDate(new Date(entry.bornMillisTimes)));
            }
        }
    }

    private static <D, R> void attemptRetryOnException(String tag, Request<D, R> request, BaseException err)
        throws Throwable {
        RetryPolicy policy=request.getRetryPolicy();
        int timeoutMillis=request.getTimeoutMs();
        try {
            policy.retry(err);
        } catch(BaseException volleyerror1) {
            request.addMarker(String.format("%s-timeout-giveup [timeout=%s]", tag, timeoutMillis));
            throw volleyerror1;
        }
        request.addMarker(String.format("%s-retry [timeout=%s]", tag, timeoutMillis));
    }

    @Override
    public <D> HttpResponse performRequest(Request<D, byte[]> request) throws Throwable {
        addCacheHeaders(request.getHeaders(), request.getCacheEntry());
        HttpResponse httpResponse=null;
        while(httpResponse == null) {
            try {
                httpResponse=mHttpStack.performRequest(request, request.getHeaders());
            } catch(SocketTimeoutException ex) {
                if(DLog.DEBUG) {
                    DLog.d("DefNetwork.STE %s %s", ex.getMessage(), request.getUrl());
                }
                attemptRetryOnException("socket", request, new TimeoutException());
            } catch(ConnectTimeoutException ex) {
                if(DLog.DEBUG) {
                    DLog.d("DefNetwork.CTE %s %s", ex.getMessage(), request.getUrl());
                }
                attemptRetryOnException("socket", request, new TimeoutException());
            } catch(MalformedURLException ex) {
                if(DLog.DEBUG) {
                    DLog.d("DefNetwork.MalformedURLException %s %s", ex.getMessage(), request.getUrl());
                }
                throw new RuntimeException(new StringBuilder().append("Bad URL:").append(request.getUrl()).toString(), ex);
            } catch(Throwable ex) {
                if(DLog.DEBUG) {
                    DLog.e(String.format("DefNetwork.Throwable %s %s", ex.getMessage(), request.getUrl()), ex);
                }
                if(httpResponse == null) {
                    throw new NoConnectionException();
                }
                throw ex;
            }

        }
        return httpResponse;
    }
}
