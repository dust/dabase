package com.kmfrog.dabase.data;

/**
 * 数据请求响应的分发器。
 * @author dust@downjoy.com
 *
 */
public interface Deliverer {

    /**
     * 分发一个数据请求的执行结果。
     * @param request
     * @param result
     * @param ex
     */
    <D,R> void postResponse(BaseRequest<D, R> request, D result, Throwable ex);

    /**
     * 分发一个数据请求的执行结果。
     * @param request
     * @param result
     * @param ex
     * @param runnable 分发完成后，需要执行的附加任务(Runnable)。
     */
    <D,R> void postResponse(BaseRequest<D, R> request, D result, Throwable ex, Runnable runnable);

}