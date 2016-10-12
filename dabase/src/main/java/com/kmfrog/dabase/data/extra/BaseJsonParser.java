package com.kmfrog.dabase.data.extra;

import android.text.TextUtils;
import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.exception.AppException;
import com.kmfrog.dabase.exception.BaseException;
import com.kmfrog.dabase.exception.ParserException;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * Created by dust on 16-10-8.
 */
public abstract class BaseJsonParser<D> implements RawParser<D, ResponseBody> {

//    public static final String DEF_DATA_META = "Data-Meta";

//    public static final String DEF_CHARSET = "utf8";

    public static final String DEF_JSON_ERR_CODE_FILED = "errCode";

    public static final String DEF_JSON_ERR_MSG_FIELD = "errMsg";

    private final String mErrCodeField;
    private final String mErrMsgField;

    public BaseJsonParser() {
        this(null, null);
    }

    public BaseJsonParser(String errCodeField, String errMsgField) {
        mErrCodeField = TextUtils.isEmpty(errCodeField) ? DEF_JSON_ERR_CODE_FILED : errCodeField;
        mErrMsgField = TextUtils.isEmpty(errMsgField) ? DEF_JSON_ERR_MSG_FIELD : errMsgField;
    }

    @Override
    public D parse(ResponseBody raw, Map<String, Object> extra) throws BaseException {
        try {
            String jsonBody = raw.string();
            AppException appEx = parseAppException(jsonBody, extra);
            if (appEx != null) {
                throw appEx;
            }

//            String charset = (String) extra.get("charset");
            try {
//                String jsonText=new String(raw, charset == null ? DEF_CHARSET : charset);
                Object json = new JSONTokener(jsonBody).nextValue();
                if (json instanceof JSONArray) {
                    return parseArray((JSONArray) json, extra);
                } else if (json instanceof JSONObject) {
                    JSONObject obj = (JSONObject) json;
                    return parseObject(obj, extra);
                } else {
                    String msg = new StringBuilder().append("unexpect json:").append(json == null ? "null" : json.toString()).toString();
                    if (DLog.DEBUG) {
                        DLog.e("BaseJsonParser.parse: %s", msg);
                    }
                    throw new BaseException(msg);
                    // throw new ParserException(new RuntimeException(new StringBuilder().append("unexpect json:")
                    // .append(json == null ? "null" : json.toString()).toString()));
                }
            } catch (ParserException ex) {
                if (DLog.DEBUG) {
                    DLog.d(ex.getMessage(), ex);
                }
                throw ex;
            } catch (Throwable ex) {
                if (DLog.DEBUG) {
                    DLog.d(ex.getMessage(), ex);
                }
                throw new ParserException(ex);
            }

        } catch (IOException ex) {
            if (DLog.DEBUG) {
                DLog.d(ex.getMessage(), ex);
            }
            throw new BaseException(ex.getMessage(), ex);
        }
        finally {
            raw.close();
        }
    }

    protected AppException parseAppException(String jsonBody, Map<String, Object> extras) throws BaseException {
        if (!TextUtils.isEmpty(jsonBody)) {
            String json = jsonBody.trim();
            try {
                if (json.startsWith("{") && json.indexOf(mErrCodeField) > 0) {
                    Object js = new JSONTokener(json).nextValue();
                    JSONObject jsObj = (JSONObject) js;
                    int code = jsObj.getInt(mErrCodeField);
                    String msg = jsObj.getString(mErrMsgField);
                    int responseCode = extras.get("code") == null ? -1 : Integer.valueOf(extras.get("code").toString());
                    return new AppException("AppException", responseCode, code, msg, extras);
                }
            } catch (Throwable ex) {
                throw new ParserException(ex);
            }
        } else {
            String errCode = (String) extras.get(mErrCodeField);
            if (!TextUtils.isEmpty(errCode)) {
                String msg = (String) extras.get(mErrMsgField);
                int responseCode = extras.get("code") == null ? -1 : Integer.valueOf(extras.get("code").toString());
                return new AppException("AppException", responseCode, Integer.valueOf(errCode), msg, extras);
            }
        }
        return null;
    }

    public abstract D parseArray(JSONArray array, Map<String, Object> extras) throws ParserException;

    public abstract D parseObject(JSONObject obj, Map<String, Object> extras) throws ParserException;

}
