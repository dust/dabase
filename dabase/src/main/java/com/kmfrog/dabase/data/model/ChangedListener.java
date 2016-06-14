package com.kmfrog.dabase.data.model;



public interface ChangedListener {

    void onChanged();
    
    void onError(Throwable ex);
    
}
