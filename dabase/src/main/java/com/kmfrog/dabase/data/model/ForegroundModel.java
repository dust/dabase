package com.kmfrog.dabase.data.model;

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;

import com.kmfrog.dabase.data.DataCallback;
import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.data.RequestQueue;
import com.kmfrog.dabase.data.extra.JsonRequest;

public class ForegroundModel<D> extends BaseModel<D> implements DataCallback<D> {

	private List<ChangedListener> mListeners;

	public ForegroundModel(RequestQueue requestQueue, Uri uri) {
		super(requestQueue, uri);
		mListeners = new ArrayList<ChangedListener>();
	}

	@Override
	public void onSuccess(D result) throws ClassCastException {
		clearErrors();
		setData(result);
		onDataBack(result, null);
		notifyDataChanged();
	}

	@Override
	public final void onFailure(Throwable e) {
		setError(e);
		onDataBack(null, e);
		notifyError(e);
	}

	@Override
	public void onAppError(Throwable e) {
		Throwable ee = new Throwable();
		setError(ee);
		notifyError(ee);
	}

	protected void onDataBack(D result, Throwable ex) throws ClassCastException {

	}

	public final void addChangedListener(ChangedListener listener) {
		mListeners.add(listener);
	}

	public final void removeChangedListener(ChangedListener listener) {
		mListeners.remove(listener);
	}

	protected void notifyDataChanged() {
		int size = mListeners.size();
		for (int i = 0; i < size; i++) {
			mListeners.get(i).onChanged();
		}
	}

	protected void notifyError(Throwable ex) {
		int size = mListeners.size();
		for (int i = 0; i < size; i++) {
			mListeners.get(i).onError(ex);
		}
	}

	protected Request<D, byte[]> makeRequest() {
		return new JsonRequest<D>(mUri, this);
	}

	@Override
	protected Request<D, byte[]> makeRequest(int timeoutMs, int maxNumRetries,
			float backoffMultiplier) {
		return new JsonRequest<D>(mUri, this, timeoutMs, maxNumRetries,
				backoffMultiplier);
	}

	@Override
	public void onDestory() {
		mListeners.clear();
		super.onDestory();
	}

}
