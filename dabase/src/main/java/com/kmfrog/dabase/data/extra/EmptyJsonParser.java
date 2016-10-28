package com.kmfrog.dabase.data.extra;

import com.kmfrog.dabase.exception.ParserException;
import org.json.JSONObject;
import java.util.Map;

/**
 * author:Created by WangZhiQiang on 2016/10/27 0027.
 * 不解析Json,直接返回jsonObject
 */

public class EmptyJsonParser extends BaseObjectJsonParser<JSONObject> {
    @Override
    public JSONObject parseObject(JSONObject obj, Map extras) throws ParserException {
        return obj;
    }

}