package com.kmfrog.dabase;

import com.kmfrog.dabase.exception.AppException;

/**
 * 异步观察者，当一个异步的任务完成后通知相应的实现类。
 *
 * @param <D>
 * @author dust@downjoy.com
 */
public interface AsyncObserver<D> {

    void onSuccess(D result);

    void onFailure(Throwable e);

    void onAppError(AppException ex);

}
