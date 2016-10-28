package com.kmfrog.dabase.ws;

/**
 * Created by dust on 16-6-22.
 */

import android.content.ContentValues;
import com.kmfrog.dabase.exception.WebSocketException;

/**
 * WebSockets message classes.
 * The master thread and the background reader/writer threads communicate using these messages
 * for WebSockets connections.
 */
public class WebSocketMessage {

    /// Base message class.
    public static class Message {
    }

    /// Quite background thread.
    public static class Quit extends Message {
    }

    /// Initial WebSockets handshake (client request).
    public static class ClientHandshake extends Message {

        public String mHost;
        public String mPath;
        public String mQuery;
        public String mOrigin;
        public String[] mSubprotocols;
        public ContentValues mHeaderList;

        ClientHandshake(String host) {
            mHost = host;
            mPath = "/";
            mOrigin = null;
            mSubprotocols = null;
            mHeaderList = null;
        }

        ClientHandshake(String host, String path, String origin) {
            mHost = host;
            mPath = path;
            mOrigin = origin;
            mSubprotocols = null;
        }

        ClientHandshake(String host, String path, String origin, String[] subprotocols) {
            mHost = host;
            mPath = path;
            mOrigin = origin;
            mSubprotocols = subprotocols;
        }
    }

    /// Initial WebSockets handshake (server response).
    public static class ServerHandshake extends Message {
        public boolean mSuccess;

        public ServerHandshake(boolean success) {
            mSuccess = success;
        }
    }

    /// WebSockets connection lost
    public static class ConnectionLost extends Message {
    }

    public static class ServerError extends Message {
        public int mStatusCode;
        public String mStatusMessage;

        public ServerError(int statusCode, String statusMessage) {
            mStatusCode = statusCode;
            mStatusMessage = statusMessage;
        }

    }

    /// WebSockets reader detected WS protocol violation.
    public static class ProtocolViolation extends Message {

        public WebSocketException mException;

        public ProtocolViolation(WebSocketException e) {
            mException = e;
        }
    }

    /// An exception occured in the WS reader or WS writer.
    public static class Error extends Message {

        public Exception mException;

        public Error(Exception e) {
            mException = e;
        }
    }

    /// WebSockets text message to send or received.
    public static class TextMessage extends Message {

        public String mPayload;

        TextMessage(String payload) {
            mPayload = payload;
        }
    }

    /// WebSockets raw (UTF-8) text message to send or received.
    public static class RawTextMessage extends Message {

        public byte[] mPayload;

        RawTextMessage(byte[] payload) {
            mPayload = payload;
        }
    }

    /// WebSockets binary message to send or received.
    public static class BinaryMessage extends Message {

        public byte[] mPayload;

        BinaryMessage(byte[] payload) {
            mPayload = payload;
        }
    }

    /// WebSockets close to send or received.
    public static class Close extends Message {

        public int mCode;
        public String mReason;

        Close() {
            mCode = -1;
            mReason = null;
        }

        Close(int code) {
            mCode = code;
            mReason = null;
        }

        Close(int code, String reason) {
            mCode = code;
            mReason = reason;
        }
    }

    /// WebSockets ping to send or received.
    public static class Ping extends Message {

        public byte[] mPayload;

        Ping() {
            mPayload = null;
        }

        Ping(byte[] payload) {
            mPayload = payload;
        }
    }

    /// WebSockets pong to send or received.
    public static class Pong extends Message {

        public byte[] mPayload;

        Pong() {
            mPayload = null;
        }

        Pong(byte[] payload) {
            mPayload = payload;
        }
    }

}
