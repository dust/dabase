package com.kmfrog.dabase.adapter;


import android.net.Uri;
import android.widget.BaseAdapter;
import com.kmfrog.dabase.app.BaseApp;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.data.model.ChangedListener;
import com.kmfrog.dabase.data.model.ListLoader;
import okhttp3.ResponseBody;

import java.util.List;

public abstract class BaseListAdapter<D> extends BaseAdapter implements ChangedListener {

    protected ListLoader<D> mListLoader;

    public BaseListAdapter(BaseApp app, Uri uri, RawParser<List<D>, ResponseBody> parser, boolean autoLoadNextPage) {
        mListLoader = new ListLoader<D>(app, uri, parser, autoLoadNextPage, false);
        mListLoader.addChangedListener(this);
    }

    public BaseListAdapter(ListLoader listLoader) {
        mListLoader = listLoader;
        mListLoader.addChangedListener(this);
    }


    @Override
    public D getItem(int position) {
        if (position < mListLoader.getCount()) {
            return mListLoader.getItem(position);
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

    public void retryLoad() {
        mListLoader.retryLoadItems();
    }

    public void destory() {
        notifyDataSetInvalidated();
        if (mListLoader != null) {
            mListLoader.destory();
        }
    }

    public String getUriByPosition(int position) {
        if (mListLoader == null) {
            return null;
        }
        return mListLoader.getUriByPosition(position);
    }

}
