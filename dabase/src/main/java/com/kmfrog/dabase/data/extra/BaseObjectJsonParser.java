package com.kmfrog.dabase.data.extra;


import com.kmfrog.dabase.exception.ParserException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public abstract class BaseObjectJsonParser<D> extends BaseJsonParser<D> {
    @Override
    public D parseArray(JSONArray array, Map<String, Object> extras) throws ParserException {
        throw new IllegalStateException(String.format("attempt parse JSONArray with %s!!!!", getClass()));
    }

    @Override
    public abstract D parseObject(JSONObject obj, Map<String, Object> extras) throws ParserException;
}
