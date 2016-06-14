package com.kmfrog.dabase.adapter;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.widget.BaseAdapter;

/**
 * 支持异步加载的adapter封装
 * @author dust@downjoy.com
 *
 * @param <D>
 */
public abstract class AsyncLoaderAdapter<D> extends BaseAdapter {

    protected AsyncLoader mLoader;

    protected Context mContext;

    protected int mCount=0;

    public AsyncLoaderAdapter(Context context) {
        this.mContext=context;
        mLoader=new AsyncLoader(context);
        mLoader.setUpdateThrottle(2000);// update at most every 2 seconds.
    }

    @Override
    public final int getCount() {
        return mCount;
    }
    
    /**
     * 开始加载数据
     */
    public void start() {
        if(mLoader.isStarted()) {
            mLoader.reset();
        }
        mLoader.startLoading();

    }

    /**
     * 显式停止加载数据
     */
    public void stop() {
        mLoader.stopLoading();
    }

    /**
     * 获得调试用tag,简单返回当前类名。
     * @return
     */
    public String getTAG() {
        return getClass().getSimpleName();
    }

    /**
     * 加载数据
     * @return 返回需要加载数据。
     */
    protected abstract D load();

    /**
     * 完成数据加载过程，获得需要的数据.
     * @param loader 数据加载器，{@link com.kmfrog.dabase.adapter.AsyncLoaderAdapter.AsyncLoader}
     * @param d 加载完成后，获得的数据
     * @return 返回adapter的count. ref:{@link #getCount()}
     */
    protected abstract int onLoadComplete(Loader<D> loader, D d);
    
    /**
     * Called if the task was canceled before it was completed. Gives the class a chance to properly dispose of the result.
     * @param d result.
     */
    protected void onCanceled(D d){
        
    }

    /**
     * 异步加载的执行器实现
     * @author dust@downjoy.com
     *
     */
    private class AsyncLoader extends AsyncTaskLoader<D> {

        public AsyncLoader(Context context) {
            super(context);
        }

        @Override
        public D loadInBackground() {
            return load();
        }

        @Override
        public void deliverResult(D data) {
            mCount=onLoadComplete(this, data);
            notifyDataSetChanged();
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
        
        @Override
        public void onCanceled(D data) {
            AsyncLoaderAdapter.this.onCanceled(data);
        }
        
        
    }
}
