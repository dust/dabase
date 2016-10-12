package com.kmfrog.dabase.data.model;

import android.net.Uri;
import com.kmfrog.dabase.app.BaseApp;
import com.kmfrog.dabase.data.BaseRequest;
import com.kmfrog.dabase.data.DataCallback;
import com.kmfrog.dabase.data.JsonRequest;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.exception.AppException;
import okhttp3.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dust on 16-10-12.
 */
public class UiModel<D> extends BaseModel<D> implements DataCallback<D> {

    private List<ChangedListener> mListeners;

    public UiModel(BaseApp app, Uri uri, RawParser<D, ResponseBody> parser) {
        super(app, uri, parser);
        mListeners = new ArrayList<ChangedListener>();
    }

    @Override
    public void onSuccess(D result) {
        clearErrors();
        setData(result);
        onDataBack(result, null);
        notifyDataChanged();
    }

    @Override
    public void onFailure(Throwable e) {
        setError(e);
        onDataBack(null, e);
        notifyError(e);
    }

    @Override
    public void onAppError(AppException ex) {

    }

    protected void onDataBack(D result, Throwable ex) throws ClassCastException {

    }

    public final void addChangedListener(ChangedListener listener) {
        mListeners.add(listener);
    }

    public final void removeChangedListener(ChangedListener listener) {
        mListeners.remove(listener);
    }

    protected void notifyDataChanged() {
        int size = mListeners.size();
        for (int i = 0; i < size; i++) {
            mListeners.get(i).onChanged();
        }
    }

    protected void notifyError(Throwable ex) {
        int size = mListeners.size();
        for (int i = 0; i < size; i++) {
            mListeners.get(i).onError(ex);
        }
    }

    protected BaseRequest<D, ResponseBody> makeRequest() {
        return new JsonRequest<D>(mUri, mParser, this);
    }

}
