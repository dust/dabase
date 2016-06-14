package com.kmfrog.dabase.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.kmfrog.dabase.DLog;

public class RequestQueue {

    private static AtomicInteger sSequenceGenerator=new AtomicInteger();

    private final Cache mCache;

    @SuppressWarnings("rawtypes")
    private final PriorityBlockingQueue<Request> mCacheQueue;

    @SuppressWarnings("rawtypes")
    private final PriorityBlockingQueue<Request> mGenericQueue;

    private final Deliverer mDeliverer;

    @SuppressWarnings("rawtypes")
    private final Map<String, Queue<Request>> mWaitingRequests;

    private CacheDispatcher mCacheDispatcher;

    private Dispatcher[] mDispatchers;

    private Context mContext;

    public RequestQueue(Context context, Cache cache) {
        this(context, cache, 4);
    }

    public RequestQueue(Context context, Cache cache, int threadCnt) {
        this(context, cache, threadCnt, (new DefaultDeliverer(new Handler(Looper.getMainLooper()))));
    }

    @SuppressWarnings("rawtypes")
    public RequestQueue(Context context, Cache cache, int threadCnt, Deliverer deliverer) {
        mCacheQueue=new PriorityBlockingQueue<Request>();
        mGenericQueue=new PriorityBlockingQueue<Request>();
        mCache=cache;
        mDeliverer=deliverer;
        mDispatchers=new Dispatcher[threadCnt];
        mWaitingRequests=new HashMap<String, Queue<Request>>();
        mContext=context;
    }

    @SuppressWarnings("rawtypes")
    private static void cancelDrainable(PriorityBlockingQueue<Request> queue, int i) {
        ArrayList<Request> arraylist=new ArrayList<Request>();
        queue.drainTo(arraylist);
        Request request;
        for(Iterator<Request> iterator=arraylist.iterator(); iterator.hasNext(); queue.add(request)) {
            request=iterator.next();
            if(request.isDrainable() && request.getSequence() < i) {
                request.cancel();
            }
        }

    }

    /**
     * 添加一个数据请求。
     * @param request
     * @return
     */
    @SuppressWarnings("rawtypes")
    public Request add(Request request) {
        request.setRequestQueue(this);
        request.setSequence(sSequenceGenerator.incrementAndGet());
        request.addMarker("add-to-queue");
        if(!request.shouldCache()) {
            mGenericQueue.add(request);
        } else {
            synchronized(mWaitingRequests) {
                String key=request.getCacheKey();
                if(!mWaitingRequests.containsKey(key)) {
                    mWaitingRequests.put(key, null);
                    mCacheQueue.add(request);
                } else {
                    Queue<Request> obj=mWaitingRequests.get(key);
                    if(obj == null) {
                        obj=new LinkedList<Request>();
                    }
                    obj.add(request);
                    mWaitingRequests.put(key, obj);
                    if(DLog.DEBUG) {
                        DLog.v("Request for cacheKey=%s is in flight, putting on hold.", key);
                    }
                }
            }

        }
        return request;
    }

    public void drain() {
        drain(getSequenceNumber());
    }

    public void drain(int i) {
        cancelDrainable(mCacheQueue, i);
        cancelDrainable(mGenericQueue, i);
        mDeliverer.discardBefore(i);
        if(DLog.DEBUG) {
            DLog.v("Draining requests with sequence number below %s", i);
        }
    }

    @SuppressWarnings("rawtypes")
    <D, R> void finish(Request<D, R> request) {
        if(request.shouldCache()) {
            synchronized(mWaitingRequests) {
                String key=request.getCacheKey();
                Queue<Request> queue=mWaitingRequests.remove(key);
                if(queue != null) {
                    if(DLog.DEBUG) {
                        DLog.d("ReqQueue.2Releasing %d waiting requests for cacheKey=%s", queue.size(), key);
                    }
                    mCacheQueue.addAll(queue);
                }
            }
        }
    }

    public static int getSequenceNumber() {
        return sSequenceGenerator.incrementAndGet();
    }

    public void start() {
        stop();
        mCacheDispatcher=new CacheDispatcher(mContext, mCacheQueue, mGenericQueue, mCache, mDeliverer);
        mCacheDispatcher.start();
        for(int i=0; i < mDispatchers.length; i++) {
            mDispatchers[i]=new Dispatcher(mContext, mGenericQueue, mCache, mDeliverer);
            mDispatchers[i].start();
        }
    }

    public void stop() {
        if(mCacheDispatcher != null) {
            mCacheDispatcher.quit();
            mCacheDispatcher=null;
        }
        if(mDispatchers != null) {
            for(int i=0; i < mDispatchers.length; i++) {
                if(mDispatchers[i] != null) {
                    mDispatchers[i].quit();
                    mDispatchers[i]=null;
                }
            }
        }
    }
    
    public long getLastReqMillisTimes(){
        return Dispatcher.getLastReqMillisTimes();
    }

}
