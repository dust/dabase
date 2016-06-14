package com.kmfrog.dabase.data.extra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;

import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.data.extra.json.JsonObjectParser;
import com.kmfrog.dabase.exception.ParserException;

public class JsonRawParserFactory implements RawParser<Object, byte[]> {

    public static final String DEF_CHARSET="utf8";
    
    public static final String DEF_TYPE_IDENTITY="CLZ";

    @SuppressWarnings("rawtypes")
    protected static Map<String, JsonParser> map=new ConcurrentHashMap<String, JsonParser>();

    private static final JsonRawParserFactory INSTANCE=new JsonRawParserFactory();

    @SuppressWarnings("rawtypes")
    private JsonRawParserFactory(List<JsonParser> jsonParsers) {
        this();
        registerJsonParsers(jsonParsers);
    }

    private JsonRawParserFactory() {
        super();
    }

    @SuppressWarnings("rawtypes")
    public void registerJsonParsers(List<JsonParser> jsonParsers) {
        map.put(JsonParser.NO_CLZ, new JsonObjectParser());
        for(JsonParser parser: jsonParsers) {
            map.put(parser.getDataType(), parser);
        }
    }

    /**
     * 获得json解析工厂类的实例。
     * @return
     */
    public synchronized static JsonRawParserFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 获得json解析工厂类的实例。
     * @return
     */
    @SuppressWarnings("rawtypes")
    public synchronized static JsonRawParserFactory getInstance(List<JsonParser> jsonParsers) {
        if(JsonRawParserFactory.map.size() == 0) {
            INSTANCE.registerJsonParsers(jsonParsers);
        }
        return INSTANCE;
    }

    @Override
    public Object parse(byte[] raw, Map<String, Object> extra) throws ParserException {
        String charset=(String)extra.get("charset");
        try {
            String jsonText=new String(raw, charset == null ? DEF_CHARSET : charset);
            Object json=new JSONTokener(jsonText).nextValue();
            if(json instanceof JSONArray) {
                return parseArray((JSONArray)json);
            } else if(json instanceof JSONObject) {
                JSONObject obj=(JSONObject)json;
                return rawParseObject(obj, obj.optString(DEF_TYPE_IDENTITY));
            } else {
                String msg=new StringBuilder().append("unexpect json:").append(json == null ? "null" : json.toString()).toString();
                if(DLog.DEBUG) {
                    DLog.e("JsonRawParserFactory.parse: %s", msg);
                }
                return null;
                // throw new ParserException(new RuntimeException(new StringBuilder().append("unexpect json:")
                // .append(json == null ? "null" : json.toString()).toString()));
            }
        } catch(Throwable ex) {
            throw new ParserException(ex);
        }

    }

    private Object parseArray(JSONArray json) throws ParserException {
        final int size=json.length();
        List<Object> list=new ArrayList<Object>(size);
        for(int i=0; i < size; i++) {
            JSONObject obj=json.optJSONObject(i);
            if(obj == null) {
                list.add(null);
            } else {
                list.add(rawParseObject(obj, obj.optString(DEF_TYPE_IDENTITY)));
            }
        }
        return list;
    }

    @SuppressWarnings("rawtypes")
    private Object rawParseObject(JSONObject obj, String dataType) throws ParserException {
        JsonParser parser=getParserByDataType(dataType);
        if(parser == null) {
            if(DLog.DEBUG) {
                DLog.d(new StringBuilder().append("unexpect dataType:").append(dataType == null ? "null" : dataType).toString());
            }
            return null;
        }
        return parser.parseObject(obj);
    }

    @SuppressWarnings("rawtypes")
    public final JsonParser getParserByDataType(String dataType) throws ParserException {
        if(TextUtils.isEmpty(dataType)) {
            // throw new ParserException(new RuntimeException(new StringBuilder().append("unexpect dataType:")
            // .append(dataType == null ? "null" : dataType).toString()));
            if(DLog.DEBUG) {
                DLog.e(new StringBuilder().append("unexpect dataType:").append(dataType == null ? "null" : dataType).toString());
            }
            return map.get(JsonParser.NO_CLZ);
        }
        return map.get(dataType);
    }

}
