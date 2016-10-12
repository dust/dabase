package com.kmfrog.dabase.data;

import com.kmfrog.dabase.exception.BaseException;

import java.util.*;

/**
 * Created by dust on 16-9-30.
 */
public interface RawParser<D, R> {

    D parse(R raw, Map<String, Object> extra) throws BaseException;
}
