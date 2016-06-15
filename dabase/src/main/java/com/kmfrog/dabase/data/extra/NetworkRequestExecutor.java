package com.kmfrog.dabase.data.extra;

import android.text.TextUtils;
import android.util.Log;
import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.data.*;
import com.kmfrog.dabase.exception.AppException;
import com.kmfrog.dabase.exception.BaseException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * 其于网络的数据请求执行器。
 * 
 * @author dust@downjoy.com
 * @param <D>
 *            请求期望得到的数据类型。
 */
public abstract class NetworkRequestExecutor<D> extends
		RequestExecutor<D, byte[]> {

	private static final long SLOW_REQUEST_THRESHOLD_MS = 0xbb8;// 3000

	/**
	 * 网络资源的封装。类似于httpclient的实现。
	 */
	private Network mNetwork;

	/**
	 * 构造一个NetworkRequestExcutor,外部请使用
	 * {@link #(Network,RawParser,Cache)} 构造其实例。
	 * 
	 * @param parser
	 * @param cache
	 */
	protected NetworkRequestExecutor(RawParser<D, byte[]> parser, Cache cache) {
		super(parser, cache);
	}

	public NetworkRequestExecutor(Network network, RawParser<D, byte[]> parser,
			Cache cache) {
		this(parser, cache);
		this.mNetwork = network;
	}

	@Override
	public Response<D> exec(Request<D, byte[]> request) {
		D data = null;
		int responseCode = 200;
		boolean notModified = false;
		boolean needCache = request.shouldCache();
		long start = System.currentTimeMillis();
		try {
			HttpResponse response = mNetwork.performRequest(request);
			StatusLine statusLine = response.getStatusLine();
			Map<String, String> headers = convertHeaders(response
					.getAllHeaders());
			byte[] bytes = null;

			logSlowRequests(System.currentTimeMillis() - start, request, bytes,
					statusLine);

			if (DLog.DEBUG) {
				DLog.d("%s.exec.headers:%s", request.getUrl(),
						headers.toString());
			}

			request.addMarker("network-http-complete");
			Map<String, Object> extras = null;
			final int statusCode = statusLine.getStatusCode();
			if (statusCode == 304) {
				final Cache.Entry cacheEntry = request.getCacheEntry();
				bytes = cacheEntry.data;
				responseCode = 304;
				notModified = true;
				needCache = false;
				extras = getExtras(headers, request, cacheEntry);
				request.addMarker("not-modified");
			} else {
				if (statusCode != 200) {
					if (statusCode >= 500) {
						Log.i("error", "serverException===" + statusCode);
						throw new AppException("serverException",
								statusCode, headers);
					}
					throw new BaseException(new StringBuilder()
							.append("http status:").append(statusCode)
							.toString());
				}

				bytes = entityToBytes(response.getEntity(), isChunked(headers),
						isGzip(headers));

				if (request.shouldCache()) {
					putCache(bytes, headers, request.getCacheKey());
					request.addMarker("net-cache-written");
				}
				// charset=parseCharset(headers);
				extras = getExtras(headers, request, null);
			}

			// prestoreMap.put("charset", charset);

			final RawParser<D, byte[]> parser = getParser();
			data = parser.parse(bytes, extras);
			request.addMarker("data-parse-complete");
			return new NetworkResponse<D>(responseCode, data, headers,
					notModified, needCache);
		} catch (BaseException ex) {
			return new NetworkResponse<D>(ex);
		} catch (Throwable ex) {
			return new NetworkResponse<D>(new BaseException(ex));
		}
	}

	abstract Map<String, Object> getExtras(Map<String, String> headers,
			Request<D, byte[]> request, Cache.Entry entry);

	void putCache(byte[] bytes, Map<String, String> headers, String cacheKey) {
		long start = System.currentTimeMillis();
		long serverDateMillis = parseDateAsEpoch(headers.get("Date"));
		long expireMillis = 0L;
		long softTtl = 0L;
		String etag = null;
		String cacheControl = headers.get("Cache-Control");
		if (cacheControl == null) {
			expireMillis = parseDateAsEpoch(headers.get("Expires"));
			etag = headers.get("ETag");
			if (serverDateMillis > 0L && expireMillis >= serverDateMillis) {
				softTtl = start + (expireMillis - serverDateMillis);
			}
		} else {
			String[] arr = cacheControl.split(",");
			for (int i = 0; i < arr.length; i++) {
				String value = arr[i].trim();
				if (!value.equals("no-cache") && !value.equals("no-store")) {
					if (value.startsWith("max-age=")) {
						softTtl = Long.parseLong(value.substring("max-age="
								.length()));
					} else {
						if (value.equals("must-revalidate")
								|| value.equals("proxy-revalidate")) {
							softTtl = 0L;
						}
					}
				}
			}
			softTtl = start + 1000L * softTtl;
		}
		if (softTtl == 0L) {
			softTtl = 30000L;
		}

		String charset = parseCharset(headers);
		String dataMeta = headers.get(JsonRawParserFactory.DEF_DATA_META);
		putCacheEntry(bytes, cacheKey, etag, softTtl, serverDateMillis,
				softTtl * 2, charset, dataMeta);
	}

	static String parseCharset(Map<String, String> headers) {
		String defCharset = "ISO-8859-1";

		String value = headers.get("Content-Type");

		if (value == null) {
			return defCharset;
		}
		String[] values = value.split(";");
		if (values.length <= 1) {
			return defCharset;
		}
		for (int i = 1; i < values.length; i++) {
			String[] segments = values[i].trim().split("=");
			if (segments.length == 2 && segments[0].equals("charset")) {
				return segments[1];
			}
		}
		return defCharset;
	}

	static String parseDataMeta(Map<String, String> headers){
		return headers.get(JsonRawParserFactory.DEF_DATA_META);
	}

	static boolean isChunked(Map<String, String> headers) {
		String value = headers.get("Transfer-Encoding");
		if (value == null) {
			return false;
		}
		return value.equalsIgnoreCase("chunked");
	}

	static boolean isGzip(Map<String, String> headers) {
		String value = headers.get("Content-Encoding");
		if (value == null) {
			return false;
		}
		return value.equalsIgnoreCase("gzip");
	}

	static long parseDateAsEpoch(String s) {
		try {
			if (!TextUtils.isEmpty(s)) {
				return DateUtils.parseDate(s).getTime();
			}
		} catch (DateParseException ex) {
		}
		return 0L;
	}

	static <D, R> void logSlowRequests(long millis, Request<D, R> request,
			byte abyte0[], StatusLine statusline) {
		if (DLog.DEBUG && millis > SLOW_REQUEST_THRESHOLD_MS) {
			DLog.d("HTTP response for request=<%s> [lifetime=%d], [size=%s], [rc=%d], [retryCount=%s]",
					request.toString(), millis, abyte0 == null ? "null"
							: abyte0.length, statusline == null ? "null"
							: statusline.getStatusCode(), request
							.getRetryPolicy().getCurrentRetryCount());
		}
	}

	static byte[] entityToBytes(HttpEntity httpentity, boolean isChunked,
			boolean isGzip) throws IOException, AppException {
		InputStream is = null;
		try {

			is = httpentity.getContent();
			if (is == null) {
				throw new AppException();
			}
			if (isGzip) {
				is = new GZIPInputStream(is);
			}
			// if(isChunked) {
			// is=new ChunkedInputStream(is);
			// }
			ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
			byte[] bytes = new byte[1024];
			int i = 0;
			while ((i = is.read(bytes)) > -1) {
				byteArrayOut.write(bytes, 0, i);
			}
			return byteArrayOut.toByteArray();
		} catch (IOException ex) {
			DLog.e("entityToBytes", ex);
			throw ex;
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				httpentity.consumeContent();
			} catch (IOException ex) {
				DLog.v("Error occured when calling consumingContent");
			}
		}

	}

	private static Map<String, String> convertHeaders(Header aheader[]) {
		HashMap<String, String> headers = new HashMap<String, String>();
		for (int i = 0; i < aheader.length; i++) {
			headers.put(aheader[i].getName(), aheader[i].getValue());
		}

		return headers;
	}

}
