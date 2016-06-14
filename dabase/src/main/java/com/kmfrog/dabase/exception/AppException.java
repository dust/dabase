package com.kmfrog.dabase.exception;

import java.util.HashMap;
import java.util.Map;

public class AppException extends BaseException {

    private static final long serialVersionUID=1L;

    private int mResponseCode;

    private int mErrCode;

    private String mErrMsg;

    private Map<String, String> mHeaders;

    private String mExtras;

    public AppException() {
        super();
    }

    public AppException(String msg) {
        super(msg);
    }

    public AppException(Throwable ex) {
        super(ex);
    }

    public AppException(String msg, int responseCode,int errCode, String errMsg, Map<String, String> responseHeaders, String extras) {
        super(msg);
        mHeaders=new HashMap<String, String>();
        mResponseCode=responseCode;
        mHeaders.putAll(responseHeaders);
        mExtras = extras;
        mErrCode = errCode;
        mErrMsg = errMsg;
    }

    public AppException(String msg, int responseCode, Map<String, String> responseHeaders) {
        super(msg);
        mHeaders=new HashMap<String, String>();
        mResponseCode=responseCode;
        mHeaders.putAll(responseHeaders);

    }

    public int getErrCode(){
        return mErrCode;
    }

    public String getErrMsg(){
        return mErrMsg;
    }

    public String getExtras(){
        return mExtras;
    }

    public int getResponseCode() {
        return mResponseCode;
    }

    public String getHeader(String fieldName) {
        if(mHeaders == null) {
            return null;
        }
        return mHeaders.get(fieldName);
    }

}
