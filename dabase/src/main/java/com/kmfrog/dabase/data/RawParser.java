package com.kmfrog.dabase.data;

import java.util.Map;

import com.kmfrog.dabase.exception.ParserException;


public interface RawParser<D, R> {

    D parse(R raw, Map<String, Object> extra) throws ParserException;
}
