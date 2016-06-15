package com.kmfrog.dabase.data.extra;

import com.kmfrog.dabase.exception.BaseException;
import org.json.JSONObject;

import com.kmfrog.dabase.exception.ParserException;


public interface JsonParser<D> {
    
    String NO_CLZ = "NO_CLZ";

    String getDataType();

    D parseObject(JSONObject jsonObject) throws BaseException;
}
