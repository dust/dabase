package com.kmfrog.dabase.exception;

/**
 * Created by dust on 16-6-22.
 */
public class WebSocketException extends BaseException {

    private static final long serialVersionUID = 1L;

    public WebSocketException(String message) {
        super(message);
    }

    public WebSocketException(String message, Throwable t) {
        super(message, t);
    }
}
