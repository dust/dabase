package com.kmfrog.dabase.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.kmfrog.dabase.DLog;


public abstract class BaseFragment extends Fragment {
    
    private TagListener mListener;
    
    private int mPosition;
    
    protected abstract void onUserVisible(boolean isVisibleToUser);

    @Override
    public final void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        onUserVisible(getUserVisibleHint());
    }
    
    public void setTagListener(TagListener listener){
        mListener=listener;
    }

    @Override
    public final void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String tag=super.getTag();
        if(DLog.DEBUG){
            DLog.d("%s(onViewCreated).mTag=%s", getClass().getSimpleName(),tag);
        }
        if(mListener!=null){
            mListener.notifyTag(this, mPosition, tag);
        }
    }

    public static BaseFragment createInstance(Context ctx, String clz, Bundle args, TagListener listener, int position) {
        BaseFragment f = (BaseFragment)Fragment.instantiate(ctx, clz, args);
        f.setTagListener(listener);
        f.mPosition=position;
        return f;
    }
    
    
    
    
}
