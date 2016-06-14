package com.kmfrog.dabase.data;

import com.kmfrog.dabase.AsyncObserver;


/**
 * 数据请求的回调接口，实现者的回调方法会借助{@link Deliverer}在主线程中被调用。
 * @author dust@downjoy.com
 *
 * @param <D> 数据请求的期望返回的数据。
 */
public interface DataCallback<D> extends AsyncObserver<D, Throwable> {

}
