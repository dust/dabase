package com.kmfrog.dabase.example;

import com.kmfrog.dabase.data.extra.BaseListJsonParser;
import com.kmfrog.dabase.exception.ParserException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by dust on 16-10-12.
 */
public class VersusParser extends BaseListJsonParser<Versus> {


    @Override
    public Versus parseObjectFromJsonObject(JSONObject obj, Map<String, Object> extras, int index) throws ParserException {
        Versus v = new Versus();
        v.createdDate = obj.optString("createdDate");
        return v;
    }
}
