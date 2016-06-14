package com.kmfrog.dabase.data;

import com.kmfrog.dabase.AsyncObserver;


/**
 * 数据请求的回调接口，实现者的回调方法会运行在后台线程中。
 * @author dust@downjoy.com
 *
 * @param <D> 数据请求的期望返回的数据。
 */
public interface AsyncCallback<D> extends AsyncObserver<D, Throwable> {


    
}
