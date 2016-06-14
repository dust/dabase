package com.kmfrog.dabase;


/**
 * 异步观察者，当一个异步的任务完成后通知相应的实现类。
 * @author dust@downjoy.com
 *
 * @param <D>
 * @param <Err>
 */
public interface AsyncObserver<D, Err extends Throwable> {

    void onSuccess(D result);
    
    void onFailure(Err e);
    
    void onAppError(Err e);
}
