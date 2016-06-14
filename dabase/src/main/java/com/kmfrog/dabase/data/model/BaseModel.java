package com.kmfrog.dabase.data.model;

import android.net.Uri;

import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.data.RequestQueue;

public abstract class BaseModel<D> {

	protected Throwable mError;

	private RequestQueue mQueue;

	protected Uri mUri;

	private D mData;

	protected Request<D, byte[]> mCurrentRequest;

	protected BaseModel(RequestQueue requestQueue, Uri uri) {
		mUri = uri;
		mQueue = requestQueue;
	}

	protected void clearErrors() {
		mError = null;
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
		mData = result;
	}

	protected final void setError(Throwable error) {
		mError = error;
	}

	public final void startLoad() {
		clearTransientState();
		mCurrentRequest = makeRequest();
		mQueue.add(mCurrentRequest);
	}

	public final void startLoad(int timeoutMs, int maxNumRetries,
			float backoffMultiplier) {
		clearTransientState();
		mCurrentRequest = makeRequest(timeoutMs, maxNumRetries,
				backoffMultiplier);
		mQueue.add(mCurrentRequest);
	}

	public final void putQueue(Request<D, byte[]> req) {
		mQueue.add(req);
	}

	protected void setRequest() {

	}

	public final void retryLoad() {
		if (inErrorState()) {
			clearErrors();
			startLoad();
		}
	}

	protected abstract Request<D, byte[]> makeRequest();

	protected abstract Request<D, byte[]> makeRequest(int timeoutMs,
			int maxNumRetries, float backoffMultiplier);

	protected void clearTransientState() {

	}

	public void onDestory() {
		mCurrentRequest = null;
		mData = null;
		mError = null;
	}

}
