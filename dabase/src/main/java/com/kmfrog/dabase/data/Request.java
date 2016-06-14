package com.kmfrog.dabase.data;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.kmfrog.dabase.AsyncObserver;
import com.kmfrog.dabase.DLog;

/**
 * 代表一个数据请求。 TODO： //log request timeline(event_log),retryPolicy
 * 
 * @param <D>
 *            请求得到的数据类型
 */
public abstract class Request<D, R> implements Comparable<Request<D, R>> {

	/** 数据请求对应的缓存项 **/
	private Cache.Entry mCacheEntry;


	/** 当前请求是否被取消 **/
	private boolean mCanceled;

	/** 是否被流放，一般因为整个队列都被流放 **/
	private boolean mDrainable;

	/** 请求产生时刻，不是对象构造的时刻 **/
	private long mBirthMillisTimes;

	/** 对请求队列的引用, 当前请求归属的队列 **/
	private RequestQueue mRequestQueue;

	/** 是否已经完成回调 **/
	private boolean mDelivered;

	/** 序列号 **/
	private Integer mSequence;

	/** 此请求是否使用缓存 **/
	private boolean mShouldCache;

	/** 此请求是否可能忽略缓存的过期时间，即始终使用缓存 **/
	private boolean mIgnoreCacheExpiredTime;

	/** 请求过程事件日志，用于调试 **/
	private final DLog.MarkerLog mEventLog;

	/** 请求相应的uri **/
	private final Uri mUri;

	/**
	 * 请求相应的回调，一般应该是{@link AsyncCallback}的实例(在后台纯程执行)，或者是{@link DataCallback}
	 * 的实例(在主线程执行)
	 **/
	private final AsyncObserver<D, Throwable> mCallback;

	private RetryPolicy mRetryPolicy;

	/** 当请求为上传文件类型时，file字段的参数名 **/
	private String mParamFileName;

	/** 需要上传的文件对象 **/
	private File mFile;

	/** 是否上传高清的图片,默认false。true:高清图片；false:低质量图片。 */
	private boolean mIsUploadHDImg = false;

	/**
	 * 请求在队列中优先级枚举。
	 * 
	 * @author dust@downjoy.com
	 */
	public static enum Priority {
		LOW, NORMAL, HIGH, IMMEDIATE;
	}

	protected Request(Uri uri, AsyncObserver<D, Throwable> callback) {
		mEventLog = DLog.MarkerLog.ENABLED ? new DLog.MarkerLog() : null;
		mUri = uri;
		mCallback = callback;
		mShouldCache = true;
		mIgnoreCacheExpiredTime = false;
		mCanceled = false;
		mDrainable = true;
		mDelivered = false;
		mBirthMillisTimes = 0L;
		setRetryPolicy(new DefaultRetryPolicy());
	}

	protected Request(Uri uri, AsyncObserver<D, Throwable> callback,
			int timeoutMs, int maxNumRetries, float backoffMultiplier) {
		mEventLog = DLog.MarkerLog.ENABLED ? new DLog.MarkerLog() : null;
		mUri = uri;
		mCallback = callback;
		mShouldCache = true;
		mIgnoreCacheExpiredTime = false;
		mCanceled = false;
		mDrainable = true;
		mDelivered = false;
		mBirthMillisTimes = 0L;
		setRetryPolicy(new DefaultRetryPolicy(timeoutMs, maxNumRetries,
				backoffMultiplier));
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

	public void cancel() {
		mCanceled = true;
	}

	public AsyncObserver<D, Throwable> getCallback() {
		return mCallback;
	}

	public void deliver(Response<D> response) {
		if (mCallback != null) {
			if (response.isSuccess() && response.mData != null) {
				JSONObject json = null;
				if (response.mData instanceof JSONObject) {
					try {
						json = (JSONObject) response.mData;
						/** 上传模块-单独处理 statusCode 为成功失败标示 */
						if (json.has("statusCode")) {
							int statusCode = json.optInt("statusCode");
							if (statusCode == 1) {
								mCallback.onSuccess(response.mData);
							} else {
								mCallback.onFailure(response.mError);
							}
							return;
						}
						/** 其他网络模块-resultCode 为成功失败标示 */
						int resultCode = json.optInt("resultCode");
						String resultMsg = json.optString("resultMsg");
						if (resultCode != 200 && TextUtils.isEmpty(resultMsg)) {
							Log.i("testError", "resultCode=" + resultCode
									+ "  resultMsg=" + resultMsg);
							mCallback.onAppError(response.mError);
							return;
						}
					} catch (Exception e) {
					}
				}
				mCallback.onSuccess(response.mData);
			} else if (response.mError != null) {
				mCallback.onFailure(response.mError);
			} else {
				mCallback.onAppError(response.mError);
			}
		}
	}

	public void finish(final String tag) {
		if (mRequestQueue != null) {
			mRequestQueue.finish(this);
		}
		if (!DLog.MarkerLog.ENABLED) {
			long l = System.currentTimeMillis() - mBirthMillisTimes;
			if (l >= 3000L) {
				Object[] objs = new Object[] { Long.valueOf(l), toString() };
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

	public void hasDispatched() {
		if (mBirthMillisTimes == 0L) {
			mBirthMillisTimes = System.currentTimeMillis();
		}
	}

	public long getSpendingMillis() {
		if (mBirthMillisTimes == 0L) {
			mBirthMillisTimes = System.currentTimeMillis();
			return 0L;
		}
		return System.currentTimeMillis() - mBirthMillisTimes;
	}

	protected Map<String, String> getPostParams() {
		return null;
	}

	protected String getPostParamsEncoding() {
		return "UTF-8";
	}

	public Cache.Entry getCacheEntry() {
		return mCacheEntry;
	}

	public String getCacheKey() {
		return mUri.toString();
	}

	public Map<String, String> getHeaders() {
		return new HashMap<String, String>();
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

	public Priority getPriority() {
		return Priority.NORMAL;
	}

	public RetryPolicy getRetryPolicy() {
		return mRetryPolicy;
	}

	public void setRetryPolicy(RetryPolicy policy) {
		this.mRetryPolicy = policy;
	}

	public final int getSequence() {
		if (mSequence == null) {
			throw new IllegalStateException(
					"getSequence called before setSequence");
		}
		return mSequence.intValue();
	}

	public final int getTimeoutMs() {
		return mRetryPolicy.getCurrentTimeout();
	}

	public Uri getUri() {
		return mUri;
	}

	public abstract String getUrl();

	public boolean hasDelivered() {
		return mDelivered;
	}

	public boolean isDrainable() {
		return mDrainable;
	}

	public void markDelivered() {
		mDelivered = true;
	}

	public void setCacheEntry(Cache.Entry entry) {
		mCacheEntry = entry;
	}

	public void setDrainable(boolean drainable) {
		mDrainable = drainable;
	}

	public void setRequestQueue(RequestQueue requestQueue) {
		mRequestQueue = requestQueue;
	}

	public final void setSequence(int i) {
		mSequence = Integer.valueOf(i);
	}

	public final void setShouldCache(boolean flag) {
		mShouldCache = flag;
	}

	public String getBaseClazz() {
		return getClass().getSimpleName();
	}

	public final void setIgnoreCacheExpiredTime(boolean ignoreCacheExpiredTime) {
		mIgnoreCacheExpiredTime = ignoreCacheExpiredTime;
	}

	public final boolean shouldIgnoreCacheExpiredTime() {
		return mIgnoreCacheExpiredTime;
	}

	public final boolean shouldCache() {
		return mShouldCache;
	}

	public boolean isCanceled() {
		return mCanceled;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.append(mCanceled ? "[X]" : "[ ]").append(mUri)
				.append(getPriority()).append(" ").append(mSequence).toString();
	}

	@Override
	public int compareTo(Request<D, R> request) {
		Priority priority = getPriority();
		Priority another = request.getPriority();
		return priority == another ? mSequence.intValue()
				- request.mSequence.intValue() : another.ordinal()
				- priority.ordinal();
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

	public boolean isUploadHDImg() {
		return mIsUploadHDImg;
	}

	public void setMultipartFile(String paramName, File file) {
		if (file == null || !file.exists() || !file.canRead()) {
			throw new IllegalArgumentException("");
		}
		this.mParamFileName = paramName;
		this.mFile = file;
	}

	public void setMultipartFile(String paramName, File file,
			boolean isUploadHDImg) {
		if (file == null || !file.exists() || !file.canRead()) {
			throw new IllegalArgumentException("");
		}
		this.mParamFileName = paramName;
		this.mFile = file;
		this.mIsUploadHDImg = isUploadHDImg;
	}
}
