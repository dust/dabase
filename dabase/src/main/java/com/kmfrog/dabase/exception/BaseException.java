package com.kmfrog.dabase.exception;


/**
 * @author dust@downjoy.com
 */
public class BaseException extends Exception {

    private static final long serialVersionUID = 1L;

    public BaseException() {

    }

    public BaseException(Throwable ex) {
        super(ex);
    }

    public BaseException(String msg) {
        super(msg);
    }

    public BaseException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
