package com.kmfrog.dabase.ws.handler;

import com.kmfrog.dabase.exception.BaseException;
import com.kmfrog.dabase.ws.WebSocket;

/**
 * Created by dust on 16-6-28.
 */
public interface MessageHandler {

    boolean onTextMessage(String payload) throws BaseException;

    boolean onBinaryMessage(byte[] payload);

    boolean onRawTextMessage(byte[] payload);

    void onOpen(WebSocket webSocket);

    void onClose(int code, String reason);

    String getIdentifier();

    void add(MessageHandler messageHandler);

}
