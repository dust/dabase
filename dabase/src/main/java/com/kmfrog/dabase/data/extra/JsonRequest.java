package com.kmfrog.dabase.data.extra;

import java.util.HashMap;
import java.util.Map;

import android.net.Uri;
import android.text.TextUtils;

import com.kmfrog.dabase.AsyncObserver;
import com.kmfrog.dabase.data.Request;

public class JsonRequest<D> extends Request<D, byte[]> {

	private Map<String, String> params = new HashMap<String, String>();

	public JsonRequest(Uri uri, AsyncObserver<D, Throwable> callback) {
		super(uri, callback);
	}

	public JsonRequest(Uri uri, AsyncObserver<D, Throwable> callback,
			int timeoutMs, int maxNumRetries, float backoffMultiplier) {
		super(uri, callback, timeoutMs, maxNumRetries, backoffMultiplier);
	}

	@Override
	public String getUrl() {
		Uri uri = getUri();
		if (TextUtils.isEmpty(uri.getScheme())) {
			return Uri.withAppendedPath(Uri.parse("http://api.digua.d.cn/"),
					uri.toString()).toString();
		}
		return uri.toString();
	}

	@Override
	public Map<String, String> getHeaders() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(
				"HEAD",
				"{\"resolutionWidth\":540,\"resolutionHeight\":960,\"osName\":\"4.0.4\",\"version\":\"6.0.1\",\"clientChannelId\":\"100003\",\"device\":\"XT912\",\"imei\":\"99000084260963\",\"hasRoot\":\"true\",\"num\":\"4600310234\",\"sdk\":15,\"ss\":2,\"sswdp\":360,\"dd\":240,\"it\":\"2\",\"verifyCode\":\"282f681c8860b0ff6d201d56aaebdb80\"}");
		return headers;
	}

	public void addPostParam(String key, String value) {
		params.put(key, value);
	}

	public void addPostParams(Map<String, String> map) {
		if (map != null) {
			params.putAll(map);
		}
	}

	@Override
	public Map<String, String> getPostParams() {
		return params;
	}

	public String getBaseClazz() {
		return JsonRequest.class.getSimpleName();
	}

}
