package com.kmfrog.dabase.adapter;

import android.net.Uri;
import android.widget.BaseAdapter;

import com.kmfrog.dabase.data.RequestQueue;
import com.kmfrog.dabase.data.model.ChangedListener;
import com.kmfrog.dabase.data.model.IListLoader;
import com.kmfrog.dabase.data.model.ListLoader;

public abstract class BaseListAdapter<D> extends BaseAdapter implements ChangedListener {

    protected IListLoader mListLoader;

    public BaseListAdapter(RequestQueue requestQueue, Uri uri, boolean autoLoadNextPage) {
        mListLoader=new ListLoader<D>(requestQueue, uri, autoLoadNextPage);
        mListLoader.addChangedListener(this);
    }

    public BaseListAdapter(IListLoader listLoader) {
        mListLoader=listLoader;
        mListLoader.addChangedListener(this);
    }

    @Override
    public int getCount() {
        return mListLoader.getCount();
    }

    @SuppressWarnings("unchecked")
    @Override
    public D getItem(int position) {
        if(position < mListLoader.getCount()) {
            return (D)mListLoader.getItem(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onChanged() {
        notifyDataSetChanged();
    }

    @Override
    public void onError(Throwable ex) {
        notifyDataSetChanged();
    }

    public void start() {
        mListLoader.startLoadItems();
    }

    public void retryLoadingItems() {
        mListLoader.retryLoadItems();
    }

    public void onDestroy() {
        notifyDataSetInvalidated();
        if(mListLoader != null) {
            mListLoader.onDestory();
        }
    }

    public String getUriByPosition(int position) {
        if(mListLoader == null) {
            return null;
        }
        return mListLoader.getUriByPosition(position);
    }
}
