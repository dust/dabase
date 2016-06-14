package com.kmfrog.dabase.data;

import java.util.concurrent.BlockingQueue;


import android.content.Context;

import com.kmfrog.dabase.AsyncObserver;
import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.exception.BaseException;

public class CacheDispatcher extends Thread {

//    private final Context mContext;

    private final Cache mCache;

    private final Deliverer mDeliverer;

    @SuppressWarnings("rawtypes")
    private final BlockingQueue<Request> mCacheQueue;

    @SuppressWarnings("rawtypes")
    private final BlockingQueue<Request> mGenericQueue;

    private volatile boolean mQuit;

    @SuppressWarnings("rawtypes")
    public CacheDispatcher(Context context, BlockingQueue<Request> cacheQueue, BlockingQueue<Request> genericQueue, Cache cache,
        Deliverer deliverer) {
//        mContext=context;
        mGenericQueue=genericQueue;
        mCacheQueue=cacheQueue;
        mCache=cache;
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
            DLog.v("start new CacheDispatcher %s", getName());
        }
        mCache.initialize();
        final String dispatcherClazz=getClass().getSimpleName();
        while(!mQuit) {
            Request request=null;
            try {
                request=mCacheQueue.take();
            } catch(InterruptedException ex) {
                if(mQuit) {
                    if(DLog.DEBUG) {
                        DLog.v("CacheDispatcher %s.quit!", getName());
                    }
                }
            }

            if(request != null) {
                request.addMarker("cache-queue-take");
                if(request.isCanceled()) {
                    request.finish("cache-discard-canceled");
                    continue;
                }

                String cacheKey=request.getCacheKey();
                Cache.Entry entry=mCache.get(cacheKey);
                try {
                    if(entry == null) {
                        request.addMarker("cache-miss");
                        mGenericQueue.put(request);
                    } else {
                        request.setCacheEntry(entry);

                        if(entry.isExpired(request.shouldIgnoreCacheExpiredTime())) {
                            request.addMarker("cache-hit-expired");
                            mGenericQueue.put(request);
                        } else {
                            request.addMarker("cache-hit");
                            Response response;
                            try {
                                RequestExecutor executor=
                                    RequestExecutorFactory.getInstance().getRequestExecutor(request, dispatcherClazz);
                                if(executor == null) {
                                    String msg=String.format("not found RequestExecutor for request: %s ", request.toString());
                                    if(DLog.DEBUG) {
                                        DLog.e(msg);
                                    }
                                    response=new Response(new BaseException(msg));
                                } else {
                                    response=executor.exec(request);
                                }
                                request.addMarker("req-cache-exec-complete");
                            } catch(Throwable ex) {
                                response=new Response(new BaseException(ex));
                            }

                            final AsyncObserver callback=request.getCallback();
                            if(callback instanceof AsyncCallback) {
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
                                DLog.d("request:%s ，callback is %s", request.toString(),
                                    callback == null ? "null" : callback.toString());
                            }
                        }
                    }
                } catch(InterruptedException ex) {
                    if(mQuit) {
                        if(DLog.DEBUG) {
                            DLog.v("CacheDispatcher %s.quit2!", getName());
                        }
                    }
                }
            }
        }
    }

}
