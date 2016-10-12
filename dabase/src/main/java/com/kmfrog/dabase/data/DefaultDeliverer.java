package com.kmfrog.dabase.data;

import android.os.Handler;
import com.kmfrog.dabase.exception.BaseException;

import java.util.concurrent.Executor;

/**
 * 数据请求回应传递器的实现类。它隐式创建的线程{@link #mResponsePoster}将response传递到主线程。后者被封装Runnable中，然后 Handler.post(runnable); 主线程中将执行
 *
 * @author dust@downjoy.com
 */
public class DefaultDeliverer implements Deliverer {

    private int mDiscardBefore;

    private final Executor mResponsePoster;

    @SuppressWarnings("rawtypes")
    private class ResponseDeliveryRunnable implements Runnable {

        private final BaseRequest mRequest;

        private final Object mResult;

        private final Throwable mEx;

        private final Runnable mRunnable;

        public ResponseDeliveryRunnable(BaseRequest request, Object result, Throwable ex, Runnable runnable) {
            mRequest = request;
            mResult = result;
            mEx = ex;
            mRunnable = runnable;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {


            if (mRequest.isCanceled()) {
                mRequest.finish("canceled-at-delivery");
                return;
            }
            try {
                mRequest.deliver(mResult, mEx);
            } finally {
//                if(mResponse.intermediate) {
//                    mRequest.addMarker("intermediate-response");
//                } else {
                mRequest.finish("done");
//                }
            }
            if (mRunnable != null) {
                mRunnable.run();
            }
        }
    }

    public DefaultDeliverer(final Handler handler) {
        mDiscardBefore = 0;
        mResponsePoster = new Executor() {

            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }

        };
    }

    @Override
    public <D, R> void postResponse(BaseRequest<D, R> request, D result, Throwable err) {
        postResponse(request, result, err, null);
    }

    @Override
    public <D, R> void postResponse(BaseRequest<D, R> request, D result, Throwable err, Runnable runnable) {
        request.markDelivered();
        request.addMarker("post-response");
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, result, err, runnable));
    }

}

