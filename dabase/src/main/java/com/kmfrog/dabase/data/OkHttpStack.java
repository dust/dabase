package com.kmfrog.dabase.data;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import android.text.TextUtils;
import android.util.Log;

import com.kmfrog.dabase.util.ImageCompressionUtil;
import com.kmfrog.dabase.data.Request;


/**
 * OkHttpStack替代原有的HttpClientStack+AndroidHttpClient2<br>
 * 用OkHttp替代底层的HttpClient<br>
 * 1.提高请求效率<br>
 * 2.兼容6.0
 * 
 * @author fei.li
 * 
 */
public class OkHttpStack implements HttpStack {

	private final OkHttpClient mClient;

	public OkHttpStack(OkHttpClient client) {
		this.mClient = client;
	}

	@Override
	public <D> HttpResponse performRequest(Request<D, byte[]> request,
			Map<String, String> headers) throws IOException {
		// OkHttpClient client = mClient.clone();
		OkHttpClient client = mClient;
//		int timeoutMs = request.getTimeoutMs();
//		client.setConnectTimeout(timeoutMs, TimeUnit.MILLISECONDS);
//		client.setReadTimeout(timeoutMs, TimeUnit.MILLISECONDS);
//		client.setWriteTimeout(timeoutMs, TimeUnit.MILLISECONDS);


		okhttp3.Request.Builder okHttpRequestBuilder = new okhttp3.Request.Builder();
		okHttpRequestBuilder.url(request.getUrl());
		for (final String name : headers.keySet()) {
			okHttpRequestBuilder.addHeader(name, headers.get(name));
		}

		ImageCompressionUtil mImageCompressionUtil = setConnectionParametersForRequest(
				okHttpRequestBuilder, request, headers);

		okhttp3.Request okHttpRequest = okHttpRequestBuilder
				.build();
		okhttp3.Call okHttpCall = client.newCall(okHttpRequest);
		okhttp3.Response okHttpResponse = okHttpCall.execute();

		StatusLine responseStatus = new BasicStatusLine(
				parseProtocol(okHttpResponse.protocol()),
				okHttpResponse.code(), okHttpResponse.message());
		BasicHttpResponse response = new BasicHttpResponse(responseStatus);
		response.setEntity(entityFromOkHttpResponse(okHttpResponse));

		okhttp3.Headers responseHeaders = okHttpResponse.headers();
		for (int i = 0, len = responseHeaders.size(); i < len; i++) {
			final String name = responseHeaders.name(i), value = responseHeaders
					.value(i);
			if (name != null) {
				response.addHeader(new BasicHeader(name, value));
			}
		}
		if (mImageCompressionUtil != null) {
			mImageCompressionUtil.deleteZipFile();
		}

		return response;
	}

	private static HttpEntity entityFromOkHttpResponse(okhttp3.Response r)
			throws IOException {
		BasicHttpEntity entity = new BasicHttpEntity();

		ResponseBody body = r.body();

		entity.setContent(body.byteStream());
		entity.setContentLength(body.contentLength());
		entity.setContentEncoding(r.header("Content-Encoding"));

		if (body.contentType() != null) {
			entity.setContentType(body.contentType().type());
		}
		return entity;
	}

	@SuppressWarnings("deprecation")
	private ImageCompressionUtil setConnectionParametersForRequest(
			okhttp3.Request.Builder builder, Request request,
			Map<String, String> headers) throws IOException {
		byte[] bytes = request.getPostBody();
		ImageCompressionUtil mImageCompressionUtil = null;
		if (request.getFile() != null) {// 文件
			MultipartBody.Builder mBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
			// 遍历map中所有参数到builder
			Map<String, String> postParams = request.getPostParams();
			if (postParams != null) {
				for (String key : postParams.keySet()) {
					mBuilder.addFormDataPart(key, postParams.get(key));
				}
			}

			// 对图片进行压缩
			mImageCompressionUtil = new ImageCompressionUtil();
			File zipFile = mImageCompressionUtil.compressImageZipFile(
					request.getFile(), request.getFile().getName());

			if (zipFile != null) {
				// 服务器会检查文件名的后缀是否符合上传的类型，否则上传失败,此处的文件名必须使用request.getFile().getName()
				mBuilder.addFormDataPart(request.getFile().getName(), request
						.getFile().getName(), RequestBody.create(
						MediaType.parse("image/png; charset=UTF-8"), zipFile));
			}
			// 构建请求体
			RequestBody requestBody = mBuilder.build();
			builder.post(requestBody);// 添加请求体
		} else if (bytes != null) {// post
			builder.post(createRequestBody(request));
		} else {// get
			builder.get();
		}
		return mImageCompressionUtil;
	}

	private static ProtocolVersion parseProtocol(final Protocol p) {
		switch (p) {
		case HTTP_1_0:
			return new ProtocolVersion("HTTP", 1, 0);
		case HTTP_1_1:
			return new ProtocolVersion("HTTP", 1, 1);
		case SPDY_3:
			return new ProtocolVersion("SPDY", 3, 1);
		case HTTP_2:
			return new ProtocolVersion("HTTP", 2, 0);
		}
		throw new IllegalAccessError("Unkwown protocol");
	}

	private static RequestBody createRequestBody(Request r) {
		final byte[] body = r.getPostBody();
		if (body == null)
			return null;
		return RequestBody.create(MediaType.parse(r.getPostBodyContentType()),
				body);
	}

}