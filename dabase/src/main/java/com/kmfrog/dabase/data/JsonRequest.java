package com.kmfrog.dabase.data;

import android.net.Uri;
import com.kmfrog.dabase.AsyncObserver;
import com.kmfrog.dabase.data.extra.BaseJsonParser;
import okhttp3.ResponseBody;

import java.util.Collections;
import java.util.Map;

/**
 * Created by dust on 16-10-10.
 */
public class JsonRequest<D> extends BaseRequest<D, ResponseBody> {

    public JsonRequest(Uri uri, RawParser<D, ResponseBody> parser, AsyncObserver<D> callback) {
        super(uri, parser, callback);
    }

    public Map<String, String> getHeaders() {
        return Collections.emptyMap();
    }

}
