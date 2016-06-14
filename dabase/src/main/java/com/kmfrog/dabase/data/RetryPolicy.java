package com.kmfrog.dabase.data;

import com.kmfrog.dabase.exception.BaseException;

public interface RetryPolicy {

    int getCurrentRetryCount();

    int getCurrentTimeout();

    void retry(BaseException volleyerror) throws Throwable;
}
