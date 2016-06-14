package com.kmfrog.dabase.data.extra;

import com.kmfrog.dabase.exception.ParserException;


public interface CommandOutputParser<D> {
    
    String NO_CLZ = "NO_CLZ";

    D parseObject(String output) throws ParserException;

}
