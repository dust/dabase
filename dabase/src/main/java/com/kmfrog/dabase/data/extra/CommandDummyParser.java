package com.kmfrog.dabase.data.extra;

import com.kmfrog.dabase.exception.ParserException;

public class CommandDummyParser implements CommandOutputParser<String> {

    public CommandDummyParser() {
    }
    
    @Override
    public String parseObject(String output) throws ParserException {
        return output;
    }

}
