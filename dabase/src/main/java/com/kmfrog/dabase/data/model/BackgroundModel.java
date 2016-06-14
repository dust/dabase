package com.kmfrog.dabase.data.model;

import android.net.Uri;

import com.kmfrog.dabase.data.AsyncCallback;
import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.data.RequestQueue;
import com.kmfrog.dabase.data.extra.JsonRequest;

public class BackgroundModel<D> extends BaseModel<D> {

	protected AsyncCallback<D> mCallback;

	public BackgroundModel(RequestQueue requestQueue, Uri uri,
			AsyncCallback<D> callback) {
		super(requestQueue, uri);
		mCallback = callback;
	}

	@Override
	protected JsonRequest<D> makeRequest() {
		return new JsonRequest<D>(mUri, mCallback);
	}

	public AsyncCallback<D> getCallback() {
		return mCallback;
	}

	@Override
	protected Request<D, byte[]> makeRequest(int timeoutMs, int maxNumRetries,
			float backoffMultiplier) {
		return new JsonRequest<D>(mUri, mCallback, timeoutMs, maxNumRetries,
				backoffMultiplier);
	}

}
