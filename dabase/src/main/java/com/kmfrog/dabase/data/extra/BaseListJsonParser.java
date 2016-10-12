package com.kmfrog.dabase.data.extra;

import com.kmfrog.dabase.exception.ParserException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BaseListJsonParser<D> extends BaseJsonParser<List<D>> {

    @Override
    public List<D> parseArray(JSONArray array, Map<String, Object> extras) throws ParserException {
        List<D> list = new ArrayList<D>();
        int size = array != null ? array.length() : 0;
        try {
            for (int i = 0; i < size; i++) {
                list.add(parseObjectFromJsonObject(array.getJSONObject(i), extras, i));
            }
        } catch (Exception ex) {
            throw new ParserException(ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public List<D> parseObject(JSONObject obj, Map<String, Object> extras) throws ParserException {
        return null;
    }

    public abstract D parseObjectFromJsonObject(JSONObject obj, Map<String, Object> extras, int index) throws ParserException;
}
