package com.kmfrog.dabase.data;

import com.kmfrog.dabase.exception.BaseException;

public class DefaultRetryPolicy implements RetryPolicy {

	private final float mBackoffMultiplier;

	private int mCurrentRetryCount;

	private int mCurrentTimeoutMs;

	private final int mMaxNumRetries;

	public DefaultRetryPolicy() {
		// this(5000, 2, 1F);
		this(8000, 0, 0F);
	}

	public DefaultRetryPolicy(int timeoutMs, int maxNumRetries,
			float backoffMultiplier) {
		this.mCurrentTimeoutMs = timeoutMs;
		this.mMaxNumRetries = maxNumRetries;
		this.mBackoffMultiplier = backoffMultiplier;

	}

	@Override
	public int getCurrentRetryCount() {
		return mCurrentRetryCount;
	}

	@Override
	public int getCurrentTimeout() {
		return mCurrentTimeoutMs;
	}

	@Override
	public void retry(BaseException err) throws Throwable {
		mCurrentRetryCount = mCurrentRetryCount + 1;
		mCurrentTimeoutMs = (int) ((float) mCurrentTimeoutMs + (float) mCurrentTimeoutMs
				* mBackoffMultiplier);
		if (!hasAttemptRemaining()) {
			throw err;
		}
	}

	protected boolean hasAttemptRemaining() {
		return mCurrentRetryCount < mMaxNumRetries;
	}

}
