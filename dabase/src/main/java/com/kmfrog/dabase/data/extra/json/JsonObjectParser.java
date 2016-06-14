package com.kmfrog.dabase.data.extra.json;

import org.json.JSONObject;

import com.kmfrog.dabase.data.extra.JsonParser;
import com.kmfrog.dabase.exception.ParserException;


public class JsonObjectParser implements JsonParser<JSONObject> {

    @Override
    public String getDataType() {
        return NO_CLZ;
    }

    @Override
    public JSONObject parseObject(JSONObject jsonObject) throws ParserException {
        return jsonObject;
    }

}
