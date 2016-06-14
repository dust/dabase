package com.kmfrog.dabase.data;

import java.util.concurrent.Executor;

import android.os.Handler;

/**
 * 数据请求回应传递器的实现类。它隐式创建的线程{@link #mResponsePoster}将response传递到主线程。后者被封装Runnable中，然后 Handler.post(runnable); 主线程中将执行
 * {@link Request#deliverResponse(Object)}.
 * @author dust@downjoy.com
 */
public class DefaultDeliverer implements Deliverer {

    private int mDiscardBefore;

    private final Executor mResponsePoster;

    @SuppressWarnings("rawtypes")
    private class ResponseDeliveryRunnable implements Runnable {

        private final Request mRequest;

        private final Response mResponse;

        private final Runnable mRunnable;

        public ResponseDeliveryRunnable(Request request, Response response, Runnable runnable) {
            mRequest=request;
            mResponse=response;
            mRunnable=runnable;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            boolean flag=mRequest.isDrainable() && mRequest.getSequence() < mDiscardBefore;

            if(flag || mRequest.isCanceled()) {
                mRequest.finish("canceled-at-delivery");
                return;
            }
            try {
                mRequest.deliver(mResponse);
            } finally {
                if(mResponse.intermediate) {
                    mRequest.addMarker("intermediate-response");
                } else {
                    mRequest.finish("done");
                }
            }
            if(mRunnable != null) {
                mRunnable.run();
            }
        }
    }

    public DefaultDeliverer(final Handler handler) {
        mDiscardBefore=0;
        mResponsePoster=new Executor() {

            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }

        };
    }

    @Override
    public void discardBefore(int i) {
        mDiscardBefore=i;
    }

    @Override
    public <D, R> void postResponse(Request<D, R> request, Response<D> response) {
        postResponse(request, response, null);
    }

    @Override
    public <D, R> void postResponse(Request<D, R> request, Response<D> response, Runnable runnable) {
        request.markDelivered();
        request.addMarker("post-response");
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, response, runnable));
    }

}
