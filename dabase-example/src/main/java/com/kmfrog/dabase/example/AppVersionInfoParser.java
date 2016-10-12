package com.kmfrog.dabase.example;

import com.kmfrog.dabase.data.extra.BaseJsonParser;
import com.kmfrog.dabase.exception.AppException;
import com.kmfrog.dabase.exception.BaseException;
import com.kmfrog.dabase.exception.ParserException;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by dust on 16-10-10.
 */
public class AppVersionInfoParser extends BaseJsonParser<AppVersionInfo> {

    public AppVersionInfoParser() {
        super();
    }

    public AppVersionInfoParser(String errCodeField, String errMsgField) {
        super(errCodeField, errMsgField);
    }

    @Override
    public AppVersionInfo parse(ResponseBody raw, Map<String, Object> extra) throws BaseException {
        return super.parse(raw, extra);
    }

    @Override
    public AppException parseAppException(String jsonBody, Map<String, Object> extras) throws BaseException {
        return super.parseAppException(jsonBody, extras);
    }


    public AppVersionInfo parseObject(JSONObject obj, Map<String, Object> extras) throws ParserException {
        AppVersionInfo v = new AppVersionInfo();
        v.client = obj.optString("client");
        v.createdDate = obj.optString("createdDate");
        v.memo = obj.optString("memo");
        v.miniNumSupported = obj.optInt("miniNumSupport");
        v.name = obj.optString("name");
        v.num = obj.optInt("num");
        return v;
    }

    public AppVersionInfo parseArray(JSONArray array, Map<String, Object> extras) throws ParserException {
        return null;
    }

}
