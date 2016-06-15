package com.kmfrog.dabase.data;

import com.kmfrog.dabase.exception.BaseException;

/**
 * 数据请求执行器。它代表处理、执行实际请求的具体实现。
 * @author dust@downjoy.com
 * @param <D> 数据请求需要获得和返回的数据类型,比如一个图片的网络请求,一般是Bitmap
 * @param <R> 请求得到的原生类型，比如网络请求一般得到的是字节流.(byte[])
 */
public abstract class RequestExecutor<D, R> {

    /**
     * 将原生数据解析为请求期望获得的数据的解析器。比如图片请求一般是将byte[]->bitmap
     */
    private final RawParser<D, R> mParser;

    /**
     * 缓存容器的封装
     */
    private final Cache mCache;

    /**
     * 用一个解析器构造一个数据请求的执行器。
     * @param parser 数据请求的解析器.
     */
    protected RequestExecutor(RawParser<D, R> parser, Cache cache) {
        mParser=parser;
        mCache=cache;
    }

    
    /**
     * 执行请求的处理方法。
     * @param request
     * @return
     * @throws BaseException
     */
    public abstract Response<D> exec(Request<D, R> request) throws BaseException;

    /**
     * 获得请求执行器的数据解析器。
     * @return
     */
    public RawParser<D, R> getParser() {
        return mParser;
    }

    protected void putCacheEntry(byte[] bytes, String cacheKey, String etag, long softTtl, long serverDateMillis, long ttl,
        String charset, String dataMeta) {

        Cache.Entry entry=new Cache.Entry();
        entry.data=bytes;
        entry.etag=etag;
        entry.softTtl=softTtl;
        entry.bornMillisTimes=serverDateMillis;
        entry.ttl=ttl;
        entry.charset=charset;
        entry.dataMeta = dataMeta;
        mCache.put(cacheKey, entry);
    }
}
