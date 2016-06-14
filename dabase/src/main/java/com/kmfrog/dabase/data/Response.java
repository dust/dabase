package com.kmfrog.dabase.data;

import com.kmfrog.dabase.exception.BaseException;


/**
 * 数据请求回应的包装。
 * @author dust@downjoy.com
 * @param <D> 请求得到的数据类型
 */
public class Response<D> {

    /** 请求得到的数据 */
    public final D mData;

    /** 是一个中间/后台过程，不需要调用回调方法，比如仅用于更新一下缓存 **/
    public boolean intermediate;

    public final Throwable mError;
    
    public final boolean mNeedCache;

    public Response(BaseException ex) {
        intermediate=false;
        mError=ex;
        mData=null;
        mNeedCache=false;
    }

    public Response(D data, boolean needCache) {
        intermediate=false;
        mError=null;
        mData=data;
        mNeedCache=needCache;
    }

    public boolean isSuccess() {
        return mError == null;
    }

}
