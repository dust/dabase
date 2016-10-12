package com.kmfrog.dabase.data.model;

import com.kmfrog.dabase.exception.BaseException;

/**
 * Created by dust on 16-10-12.
 */
public interface ChangedListener {

    void onChanged();

    void onError(Throwable ex);
}
