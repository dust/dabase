package com.kmfrog.dabase.exception;


public class ParserException extends BaseException {

    private static final long serialVersionUID=1L;

    public ParserException() {
        super();
    }

    public ParserException(Throwable ex) {
        super(ex);
    }

    public ParserException(String msg) {
        super(msg);
    }

    public ParserException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
