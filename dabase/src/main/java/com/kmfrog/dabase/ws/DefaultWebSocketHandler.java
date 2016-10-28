package com.kmfrog.dabase.ws;

import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.ws.handler.MessageHandler;

/**
 * Created by dust on 16-6-28.
 */
public class DefaultWebSocketHandler extends WebSocketConnectionHandler {

    private final MessageHandler rootHandler;

    public DefaultWebSocketHandler(MessageHandler rootHandler) {
        this.rootHandler = rootHandler;
    }

    @Override
    public void onBinaryMessage(byte[] payload) {
        rootHandler.onBinaryMessage(payload);
    }

    @Override
    public void onClose(int code, String reason) {
        rootHandler.onClose(code, reason);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        rootHandler.onOpen(webSocket);
    }

    @Override
    public void onRawTextMessage(byte[] payload) {
        rootHandler.onRawTextMessage(payload);
    }

    @Override
    public void onTextMessage(String payload) {
        try {
            rootHandler.onTextMessage(payload);
        }
        catch(Exception ex){
            DLog.e(ex.getMessage(), ex);
        }
    }
}
