package com.kmfrog.dabase.data;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import com.kmfrog.dabase.Contract;
import com.kmfrog.dabase.data.extra.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请求执行器工厂类。主要业务方法通过不同的{@link Request}获得对应的{@link RequestExecutor} <br/>
 * 
 * @author dust@downjoy.com
 */
public final class RequestExecutorFactory implements Contract {

	private final Context mContext;

	private final Cache mCache;

	private final Cache mBitmapCache;

	private static RequestExecutorFactory instance = null;

	@SuppressWarnings("rawtypes")
	private final Map<String, RequestExecutor> mExecutorMap;

	// private AndroidHttpClient2 mAndroidHttpClient;

	@SuppressWarnings("rawtypes")
	private RequestExecutorFactory(Context context, Cache cache,
			Cache bitmapCache, List<SQLiteOpenHelper> sqliteOpenHelpers,
			InputStream trustSvrKeyStoreInputStream,
			char[] trustSvrKeyStorePassword) {
		mContext = context;
		mCache = cache;
		mBitmapCache = bitmapCache;
		mExecutorMap = new HashMap<String, RequestExecutor>();
		registerRequestExecutor(trustSvrKeyStoreInputStream,
				trustSvrKeyStorePassword);
		if (sqliteOpenHelpers != null && sqliteOpenHelpers.size() > 0) {
			this.registerSQLiteRequestExecutor(sqliteOpenHelpers);
		}
	}

	@SuppressWarnings("unchecked")
	public <D, R> RequestExecutor<D, R> getRequestExecutor(
			Request<D, R> request, String dispatcherClazz) {
		Uri uri = request.getUri();
		String clazz = request.getBaseClazz();// request.getClass().getSimpleName();
		return mExecutorMap
				.get(genKey(uri.getScheme(), clazz, dispatcherClazz));
	}

	public synchronized static RequestExecutorFactory getInstance(
			Context context, Cache cache, Cache bitmapCache) {
		return getInstance(context, cache, bitmapCache, null);
	}

	public synchronized static RequestExecutorFactory getInstance(
			Context context, Cache cache, Cache bitmapCache,
			List<SQLiteOpenHelper> sqliteOpenHelpers) {
		return getInstance(context, cache, bitmapCache, sqliteOpenHelpers,
				null, null);
	}

	public synchronized static RequestExecutorFactory getInstance(
			Context context, Cache cache, Cache bitmapCache,
			List<SQLiteOpenHelper> sqliteOpenHelpers,
			InputStream trustSvrKeyStoreInputStream,
			char[] trustSvrKeyStorePassword) {
		if (instance == null) {
			Log.i("testExc", "RequestExecutorFactory=instance==");
			instance = new RequestExecutorFactory(context, cache, bitmapCache,
					sqliteOpenHelpers, trustSvrKeyStoreInputStream,
					trustSvrKeyStorePassword);
		}
		return instance;
	}

	static RequestExecutorFactory getInstance() {
		return instance;
	}

	private void registerRequestExecutor(InputStream is, char[] password) {
		final String dispatchClz = Dispatcher.class.getSimpleName();
		final String cacheDispatchClz = CacheDispatcher.class.getSimpleName();

		final String imgReqClz = ImageRequest.class.getSimpleName();
		final String iconReqClz = PackageIconRequest.class.getSimpleName();
		final String apiReqClz = JsonRequest.class.getSimpleName();
		final String basedUriReqClz = BasedUriRequest.class.getSimpleName();
		final String cmdReqClz = CommandRequest.class.getSimpleName();

		RawParser<Bitmap, byte[]> bitmapRawParser = new BitmapRawParser();
		RawParser<Bitmap, Drawable> drawableRawParser = new DrawableBitmapParser();
		RawParser<Object, byte[]> jsonRawParser = JsonRawParserFactory
				.getInstance();
		RawParser<Object, byte[]> basedUriRawParser = BasedUriRawParserFactory
				.getInstance();
		RawParser<Object, byte[]> cmdRawParser = CommandOutputRawParserFactory
				.getInstance();

		// AndroidHttpClient2 mAndroidHttpClient;
		// try {
		// mAndroidHttpClient = AndroidHttpClient2.newInstance(
		// System.getProperty("User-Agent"), mContext, is, password);
		// } catch (Throwable ex) {
		// throw new RuntimeException(ex);
		// }
		// Network network = new DefaultNetwork(new HttpClientStack(
		// mAndroidHttpClient));
		Log.i("testExc", "OkHttpStack====");
		Network network = new DefaultNetwork(new OkHttpStack(
				OkHttpNetWorkClient.getInstance().getOkHttpClient()));

		// Network based ImageRequest
		NetworkRequestExecutor<Bitmap> networkBmpExec = new BitmapNetworkRequestExecutor(
				network, bitmapRawParser, mBitmapCache);
		// Cache based ImageRequest
		CacheRequestExecutor<Bitmap> cacheBmpExec = new BitmapCacheRequestExecutor(
				bitmapRawParser, mBitmapCache);
		// PackageManager(Context) based ImageRequest --> app://com.diguayouxi
		AppIconRequestExecutor appIconExec = new AppIconRequestExecutor(
				mContext, drawableRawParser, mBitmapCache);
		// Network based BasedUriRequest
		NetworkRequestExecutor<Object> basedUriExec = new BasedUriNetworkRequestExecutor(
				network, basedUriRawParser, mCache);
		// Network based ApiRequest
		NetworkRequestExecutor<Object> networkJsonExec = new JsonNetworkRequestExecutor(
				network, jsonRawParser, mCache);
		// Cache based ApiRequest
		CacheRequestExecutor<Object> cacheJsonExec = new JsonCacheRequestExecutor(
				jsonRawParser, mCache);
		// command request
		CommandRequestExecutor cmdExec = new CommandRequestExecutor(
				cmdRawParser, null);

		mExecutorMap.put(genKey(URI_NET_SCHEME, basedUriReqClz, dispatchClz),
				basedUriExec);
		mExecutorMap.put(genKey(null, basedUriReqClz, dispatchClz),
				basedUriExec);
		mExecutorMap.put(genKey(URI_NET_SCHEME, imgReqClz, dispatchClz),
				networkBmpExec);
		mExecutorMap.put(genKey(null, imgReqClz, dispatchClz), networkBmpExec);
		mExecutorMap.put(genKey(URI_APP_ICON_SCHEME, iconReqClz, dispatchClz),
				appIconExec);
		mExecutorMap.put(genKey(URI_NET_SCHEME, apiReqClz, dispatchClz),
				networkJsonExec);
		mExecutorMap.put(genKey(null, apiReqClz, dispatchClz), networkJsonExec);
		mExecutorMap.put(genKey(URI_CMD_SCHEME, cmdReqClz, dispatchClz),
				cmdExec);

		mExecutorMap.put(genKey(URI_NET_SCHEME, imgReqClz, cacheDispatchClz),
				cacheBmpExec);
		mExecutorMap.put(
				genKey(URI_APP_ICON_SCHEME, imgReqClz, cacheDispatchClz),
				cacheBmpExec);
		mExecutorMap.put(genKey(URI_NET_SCHEME, apiReqClz, cacheDispatchClz),
				cacheJsonExec);
		mExecutorMap.put(genKey(null, apiReqClz, cacheDispatchClz),
				cacheJsonExec);

		// ...
	}

	private void registerSQLiteRequestExecutor(
			List<SQLiteOpenHelper> sqliteOpenHelpers) {
		DbCursorRawParserFactory dbRawParser = DbCursorRawParserFactory
				.getInstance();
		final String dispatchClz = Dispatcher.class.getSimpleName();
		final String sqliteReqClz = SQLiteRequest.class.getSimpleName();
		SQLiteRequestExecutor sqliteQueryExec = new SQLiteRequestExecutor(
				sqliteOpenHelpers, dbRawParser, mCache);

		final String sqliteExecReqClz = SQLiteExecRequest.class.getSimpleName();
		SQLiteExecRequestExecutor sqliteExecExec = new SQLiteExecRequestExecutor(
				sqliteOpenHelpers, null, mCache);

		mExecutorMap.put(genKey(URI_DB_SCHEME, sqliteReqClz, dispatchClz),
				sqliteQueryExec);
		mExecutorMap.put(genKey(URI_DB_SCHEME, sqliteExecReqClz, dispatchClz),
				sqliteExecExec);
	}

	private static String genKey(String uriScheme, String requestClazz,
			String dispatcherClazz) {
		return new StringBuilder().append(uriScheme).append("://")
				.append(dispatcherClazz).append("/").append(requestClazz)
				.toString();
	}

	// public AndroidHttpClient2 getHttpClient(){
	// return mAndroidHttpClient;
	// }
	//
	// public HttpContext getHttpContext(){
	// return mAndroidHttpClient.getHttpContext();
	// }

}
