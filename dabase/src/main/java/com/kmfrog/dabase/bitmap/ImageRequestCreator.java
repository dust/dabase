package com.kmfrog.dabase.bitmap;

import com.kmfrog.dabase.data.Request;

public interface ImageRequestCreator {

    @SuppressWarnings("rawtypes")
    Request create();
}