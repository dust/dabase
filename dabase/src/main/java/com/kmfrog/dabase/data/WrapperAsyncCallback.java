package com.kmfrog.dabase.data;

public abstract class WrapperAsyncCallback<D, W extends Wrapper<D>> implements AsyncCallback<W> {

    protected W w;

    @Override
    public void onSuccess(W result) {
        w=result;
        if(w.isWrapperErrState()) {
            onWrapperFailure(w, w.getWrapperErr());
        } else {
            onWrapperSuccess(result.getWrapperIn());
        }
    }

    @Override
    public void onFailure(Throwable e) {
        onWrapperFailure(w, e);
    }

    public abstract void onWrapperSuccess(D result);

    public abstract void onWrapperFailure(W w, Throwable ex);

}
