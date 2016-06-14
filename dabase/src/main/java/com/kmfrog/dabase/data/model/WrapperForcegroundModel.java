package com.kmfrog.dabase.data.model;

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;

import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.data.RequestQueue;
import com.kmfrog.dabase.data.Wrapper;
import com.kmfrog.dabase.data.WrapperDataCallback;
import com.kmfrog.dabase.data.extra.BasedUriRequest;

public class WrapperForcegroundModel<D, W extends Wrapper<D>> extends WrapperDataCallback<D, W> {

    protected Throwable mError;

    private RequestQueue mQueue;

    protected Uri mUri;

    private D mData;

    protected Request<W, byte[]> mCurrentRequest;

    private final List<ChangedListener> mListeners;

    public WrapperForcegroundModel(RequestQueue requestQueue, Uri uri) {
        mUri=uri;
        mQueue=requestQueue;
        mListeners=new ArrayList<ChangedListener>();
    }

    @Override
    public void onWrapperSuccess(D result) {
        clearErrors();
        setData(result);
        onDataBack(result, w, null);
        notifyDataChanged();
    }

    @Override
    public void onWrapperFailure(W w, Throwable ex) {
        setError(ex);
        onDataBack(null, w, ex);
        notifyError(ex);
    }

    protected void onDataBack(D result, W w, Throwable ex) {

    }

    protected void clearErrors() {
        mError=null;
    }

    public Throwable getError() {
        return mError;
    }

    public boolean inErrorState() {
        return mError != null;
    }

    public D getData() {
        return mData;
    }

    protected final void setData(D result) {
        mData=result;
    }

    protected final void setError(Throwable error) {
        mError=error;
    }

    public final void startLoad() {
        clearTransientState();
        mCurrentRequest=makeRequest();
        mQueue.add(mCurrentRequest);
    }

    public Request<W, byte[]> makeRequest() {
        return new BasedUriRequest<W>(mUri, this);
    }

    public final void putQueue(Request<W, byte[]> req) {
        mQueue.add(req);
    }

    protected void setRequest() {

    }

    public final void retryLoad() {
        if(inErrorState()) {
            clearErrors();
            startLoad();
        }
    }

    protected void clearTransientState() {
    }

    public void onDestory() {
        mListeners.clear();
        mCurrentRequest=null;
        mData=null;
        mError=null;
    }

    public final void addChangedListener(ChangedListener listener) {
        mListeners.add(listener);
    }

    public final void removeChangedListener(ChangedListener listener) {
        mListeners.remove(listener);
    }

    protected void notifyDataChanged() {
        int size=mListeners.size();
        for(int i=0; i < size; i++) {
            mListeners.get(i).onChanged();
        }
    }

    protected void notifyError(Throwable ex) {
        int size=mListeners.size();
        for(int i=0; i < size; i++) {
            mListeners.get(i).onError(ex);
        }
    }

	@Override
	public void onAppError(Throwable e) {
	}

}
