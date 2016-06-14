package com.kmfrog.dabase.data;

import java.util.concurrent.BlockingQueue;


import android.content.Context;

import com.kmfrog.dabase.AsyncObserver;
import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.data.extra.SQLiteExecRequest;
import com.kmfrog.dabase.exception.BaseException;

public class Dispatcher extends Thread {

    // private final Context mContext;
    //
    // private final Cache mCache;

    private final Deliverer mDeliverer;

    @SuppressWarnings("rawtypes")
    private final BlockingQueue<Request> mQueue;

    private volatile boolean mQuit;
    
    private static volatile long sLastReqMillisTimes;

    @SuppressWarnings("rawtypes")
    public Dispatcher(Context context, BlockingQueue<Request> queue, Cache cache, Deliverer deliverer) {
        // mContext=context;
        // mCache=cache;
        mQueue=queue;
        mDeliverer=deliverer;
        mQuit=false;
    }

    public void quit() {
        mQuit=true;
        interrupt();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void run() {
        if(DLog.DEBUG) {
            DLog.v("start new Dispatcher %s", getName());
        }

        final String dispatcherClazz=getClass().getSimpleName();
        while(!mQuit) {
            Request request=null;
            try {
                request=mQueue.take();

            } catch(InterruptedException ex) {
                if(mQuit) {
                    if(DLog.DEBUG) {
                        DLog.v("Dispatcher %s, quit!", getName());
                    }
                }
            }

            if(request != null) {
                request.addMarker("dispatcher-queue-take");
                if(request.isCanceled()) {
                    request.finish("dispatcher-discard-canceled");
                    continue;
                }

                Response response;
                try {
                    RequestExecutor executor=RequestExecutorFactory.getInstance().getRequestExecutor(request, dispatcherClazz);
                    if(executor == null) {
                        String msg=String.format("not found RequestExecutor for request: %s ", request.toString());
                        if(DLog.DEBUG) {
                            DLog.e(msg);
                        }
                        response=new Response(new BaseException(msg));
                    } else {
                        response=executor.exec(request);
                        sLastReqMillisTimes=System.currentTimeMillis();
                    }

                    request.addMarker("req-exec-complete");
                } catch(Throwable ex) {
                    response=new Response(new BaseException(ex));
                }

                final AsyncObserver callback=request.getCallback();
                if(callback instanceof AsyncCallback) {
                    // if(response.isSuccess()) {
                    // callback.onSuccess(response.mData);
                    // } else {
                    // callback.onFailure(response.mError);
                    // }
                    try {
                        request.markDelivered();
                        request.deliver(response);
                    } finally {
                        request.finish("done");
                    }

                } else if(callback instanceof DataCallback) {
                    mDeliverer.postResponse(request, response);
                } else {
                    request.finish("done");
                    if(DLog.DEBUG) {
                        if(!(request instanceof SQLiteExecRequest)) {
                            DLog.d("request:%s ，callback is %s", request.toString(), callback == null ? "null" : callback
                                .getClass().getName());
                        }
                    }
                }

            } // end if(request==null)
        }// end while(!mQuit)

        if(DLog.DEBUG) {
            DLog.d("Dispatcher.run.quit %s", getName());
        }
    }
    
    public static long getLastReqMillisTimes(){
        return sLastReqMillisTimes;
    }

}
