package com.kmfrog.dabase.data;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import com.kmfrog.dabase.AsyncObserver;
import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.exception.AppException;
import okhttp3.Call;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 代表一个数据请求。 TODO： //log request timeline(event_log),retryPolicy
 *
 * @param <D> 请求得到的数据类型
 */
public abstract class BaseRequest<D, R> {


    /**
     * 此请求是否使用缓存 *
     */
    private boolean mShouldCache;

    /**
     * 是否已经完成回调 *
     */
    private boolean mDelivered;

    /**
     * 请求产生时刻，不是对象构造的时刻 *
     */
    private long mBirthMillisTimes;

    /**
     * 当前请求是否被取消 *
     */
    private volatile boolean mCanceled;

    /**
     * 请求过程事件日志，用于调试 *
     */
    private final DLog.MarkerLog mEventLog;

    /**
     * 请求相应的uri *
     */
    private final Uri mUri;

    /**
     * 将response解析为想要的数据对象。
     */
    private final RawParser<D, R> mParser;

    /**
     * 将请求结果(数据对象或异常)回调通知到观察者。
     */
    private final AsyncObserver<D> mCallback;


    private Call mOkCall;

    /**
     * post请求时的参数值对。
     */
    private Map<String, String> mParams;

    /**
     * 当请求为上传文件类型时，file字段的参数名 *
     */
    private String mParamFileName;

    /**
     * 需要上传的文件对象 *
     */
    private File mFile;

    /**
     * 需要上传的文件的MediaType
     */
    private String mFileMediaType;

    protected BaseRequest(Uri uri, RawParser<D, R> parser, AsyncObserver<D> callback) {
        mUri = uri;
        mParser = parser;
        mCallback = callback;
        mShouldCache = true;
        mCanceled = false;
        mDelivered = false;
        mBirthMillisTimes = 0L;
        mParams = new HashMap<String, String>();
        mEventLog = DLog.MarkerLog.ENABLED ? new DLog.MarkerLog() : null;
    }

    /**
     * 增加一条日志标记。
     *
     * @param mark
     */
    public void addMarker(String mark) {
        if (DLog.MarkerLog.ENABLED) {
            if (mBirthMillisTimes == 0L) {
                mBirthMillisTimes = System.currentTimeMillis();
            }
            mEventLog.add(mark, Thread.currentThread().getId());
        }
    }

    public void setOkCall(Call call) {
        mOkCall = call;
    }

    public void deliver(D result, Throwable ex) {
        if (mCallback != null) {
            if (result != null) {
                mCallback.onSuccess(result);
            } else if (ex instanceof AppException) {
                mCallback.onAppError(((AppException) ex));
            } else {
                mCallback.onFailure(ex);
            }
        }
    }

    public void finish(final String tag) {
        if (!DLog.MarkerLog.ENABLED) {
            long l = System.currentTimeMillis() - mBirthMillisTimes;
            if (l >= 3000L) {
                Object[] objs = new Object[]{Long.valueOf(l), toString()};
                DLog.d("%d ms: %s", objs);
            }
        } else {
            final long threadId = Thread.currentThread().getId();
            if (Looper.myLooper() != Looper.getMainLooper()) {
                (new Handler(Looper.getMainLooper())).post(new Runnable() {

                    @Override
                    public void run() {
                        mEventLog.add(tag, threadId);
                        mEventLog.finish(((Object) this).toString());
                    }
                });
            } else {
                mEventLog.add(tag, threadId);
                mEventLog.finish(toString());
            }
        }
    }

    public boolean hasDelivered() {
        return mDelivered;
    }

    public void markDelivered() {
        mDelivered = true;
    }

    public final boolean shouldCache() {
        return mShouldCache;
    }

    public boolean isCanceled() {
        return mCanceled;
    }

    public void cancel() {
        mCanceled = true;
        if (mOkCall != null && !mOkCall.isCanceled()) {
            mOkCall.cancel();
        }
    }

    public final void setShouldCache(boolean flag) {
        mShouldCache = flag;
    }

    public Uri getUri() {
        return mUri;
    }

    public String getUrl() {
        return getUri().toString();
    }

    protected Map<String, String> getPostParams() {
        return null;
    }

    protected String getPostParamsEncoding() {
        return "UTF-8";
    }

    public byte[] getPostBody() {
        Map<String, String> map = getPostParams();
        if (map != null && map.size() > 0) {
            return encodePostParameters(map, getPostParamsEncoding());
        }
        return null;
    }

    public String getPostBodyContentType() {
        return (new StringBuilder())
                .append("application/x-www-form-urlencoded; charset=")
                .append(getPostParamsEncoding()).toString();
    }


    public void addPostParam(String key, String value) {
        mParams.put(key, value);
    }

    public void addPostParams(Map<String, String> map) {
        if (map != null) {
            mParams.putAll(map);
        }
    }

    protected Map<String, String> getHeaders() {
        return new HashMap<String, String>();
    }

    final RawParser<D, R> getParser() {
        return mParser;
    }


    public static byte[] encodePostParameters(Map<String, String> map,
                                              String enc) {
        StringBuilder sb = new StringBuilder();
        try {
            for (Iterator<Map.Entry<String, String>> iter = map.entrySet()
                    .iterator(); iter.hasNext(); sb.append('&')) {
                Map.Entry<String, String> entry = iter.next();
                DLog.d("postParams : %s = %s", entry.getKey(), entry.getValue());
                sb.append(entry.getKey()).append('=').append(entry.getValue());
                // sb.append(URLEncoder.encode(entry.getKey(),
                // enc)).append('=').append(URLEncoder.encode(entry.getValue(),
                // enc));
            }
            return sb.toString().getBytes(enc);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(String.format(
                    "encoding not supported:%s", enc), ex);
        }
    }

    public String getFileParamName() {
        return mParamFileName;
    }

    public File getFile() {
        return mFile;
    }

    public void setMultipartFile(String paramName, File file){
        setMultipartFile(paramName, file, "image/png; charset=UTF-8");
    }

    public void setMultipartFile(String paramName, File file, String mediaType) {
        if (file == null || !file.exists() || !file.canRead()) {
            throw new IllegalArgumentException("");
        }
        this.mParamFileName = paramName;
        this.mFile = file;
        this.mFileMediaType = mediaType;
    }

    public String getFileMediaType() {
        return mFileMediaType;
    }
}
