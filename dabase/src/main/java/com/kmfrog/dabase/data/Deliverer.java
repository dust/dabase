package com.kmfrog.dabase.data;


/**
 * 数据请求响应的分发器。
 * @author dust@downjoy.com
 *
 */
public interface Deliverer {

    void discardBefore(int i);
    
    /**
     * 分发一个数据请求的执行结果。
     * @param request
     * @param response
     */
    <D,R> void postResponse(Request<D, R> request, Response<D> response);
    
    /**
     * 分发一个数据请求的执行结果。
     * @param request
     * @param response
     * @param runnable 分发完成后，需要执行的附加任务(Runnable)。
     */
    <D,R> void postResponse(Request<D, R> request, Response<D> response, Runnable runnable);
    
}
