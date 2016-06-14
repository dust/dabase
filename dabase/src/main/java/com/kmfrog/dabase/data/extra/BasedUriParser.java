package com.kmfrog.dabase.data.extra;

import android.net.Uri;

import com.kmfrog.dabase.exception.ParserException;



public interface BasedUriParser<D>{
    
    Uri getIdenity();

    D parseObject(String responseBody) throws ParserException;
}
