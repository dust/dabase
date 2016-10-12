package com.kmfrog.dabase.data.model;

import android.net.Uri;
import com.kmfrog.dabase.app.BaseApp;
import com.kmfrog.dabase.data.BaseRequest;
import com.kmfrog.dabase.data.HttpRequestExecutor;
import com.kmfrog.dabase.data.RawParser;
import okhttp3.ResponseBody;

/**
 * Created by dust on 16-10-12.
 */
public abstract class BaseModel<D> {

    protected BaseApp mApp;
    protected Uri mUri;
    protected RawParser<D, ResponseBody> mParser;
    protected BaseRequest<D, ResponseBody> mCurrentRequest;
    private D mResult;
    private Throwable mEx;
    private final HttpRequestExecutor mHttpRequestExecutor;

    protected BaseModel(BaseApp app, Uri uri, RawParser<D, ResponseBody> parser) {
        this.mApp = app;
        mHttpRequestExecutor = app.getHttpExecutor();
        mParser = parser;
        mUri = uri;
    }

    protected abstract BaseRequest<D, ResponseBody> makeRequest();

    public final void start() {
        clearTransientState();
        mCurrentRequest = makeRequest();
        putQueue(mCurrentRequest);
    }

    public void putQueue(BaseRequest<D, ResponseBody> req) {
        mHttpRequestExecutor.put(req);
    }

    public final void retry() {
        if (inErrorState()) {
            clearErrors();
            start();
        }
    }

    public D getData() {
        return mResult;
    }

    public Throwable getError() {
        return mEx;
    }

    protected final void setData(D result) {
        mResult = result;
    }

    protected final void setError(Throwable error) {
        mEx = error;
    }


    public boolean inErrorState() {
        return mEx != null;
    }

    protected void clearTransientState() {

    }

    protected void clearErrors() {
        mEx = null;
    }

    public void destory() {
        mCurrentRequest = null;
        mResult = null;
        mEx = null;
        mUri = null;
    }


}
