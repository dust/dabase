package com.kmfrog.dabase.ws;

import android.content.ContentValues;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.exception.WebSocketException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SocketChannel;

/**
 * Created by dust on 16-6-22.
 */
public class WebSocketConnection implements WebSocket {

    private static final boolean DEBUG = true;
    private static final String TAG = WebSocketConnection.class.getName();

    protected Handler mMasterHandler;

    protected WebSocketReader mReader;
    protected WebSocketWriter mWriter;
    protected HandlerThread mWriterThread;

    protected SocketChannel mTransportChannel;

    private URI mWsUri;
    private String mWsScheme;
    private String mWsHost;
    private int mWsPort;
    private String mWsPath;
    private String mWsQuery;
    private String[] mWsSubprotocols;
    private ContentValues mWsHeaders;

    private ConnectionHandler mWsHandler;

    protected WebSocketOptions mOptions;

    private volatile boolean mActive;
    private boolean mPrevConnected;

    /**
     * Asynchronous socket connector.
     */
    private class WebSocketConnector extends Thread {

        public void run() {
            Thread.currentThread().setName("WebSocketConnector");

			/*
             * connect TCP socket
			 */
            try {
                mTransportChannel = SocketChannel.open();

                // the following will block until connection was established or
                // an error occurred!
                mTransportChannel.socket().connect(
                        new InetSocketAddress(mWsHost, mWsPort),
                        mOptions.getSocketConnectTimeout());

                // before doing any data transfer on the socket, set socket
                // options
                mTransportChannel.socket().setSoTimeout(
                        mOptions.getSocketReceiveTimeout());
                mTransportChannel.socket().setTcpNoDelay(
                        mOptions.getTcpNoDelay());

            } catch (IOException e) {
                onClose(WebSocketConnectionHandler.CLOSE_CANNOT_CONNECT,
                        e.getMessage());
                return;
            }

            if (mTransportChannel.isConnected()) {

                try {

                    // create & start WebSocket reader
                    createReader();

                    // create & start WebSocket writer
                    createWriter();

                    // start WebSockets handshake
                    WebSocketMessage.ClientHandshake hs = new WebSocketMessage.ClientHandshake(
                            mWsHost + ":" + mWsPort);
                    hs.mPath = mWsPath;
                    hs.mQuery = mWsQuery;
                    hs.mSubprotocols = mWsSubprotocols;
                    hs.mHeaderList = mWsHeaders;
                    mWriter.forward(hs);

                    mPrevConnected = true;

                } catch (Exception e) {
                    onClose(WebSocketConnectionHandler.CLOSE_INTERNAL_ERROR,
                            e.getMessage());
                    DLog.e(e, "%s", e.getMessage());
                    return;
                }
            } else {
                onClose(WebSocketConnectionHandler.CLOSE_CANNOT_CONNECT,
                        "Could not connect to WebSocket server");
                return;
            }
        }

    }

    public WebSocketConnection() {
        if (DEBUG) DLog.d(TAG, "created");

        // create WebSocket master handler
        createHandler();

        // set initial values
        mActive = false;
        mPrevConnected = false;
    }


    public void sendTextMessage(String payload) {
        DLog.d("writerThread:%s|%s|%b|%b|%s", mWriterThread.toString(), mWriterThread.getName(), mWriterThread.isAlive(), mWriterThread.isDaemon(), mWriterThread.getLooper()==null?"null":mWriterThread.getLooper().getThread());
        // add if by csj on 2016.08.23
        if (mWriterThread.isAlive()) {
            mWriter.forward(new WebSocketMessage.TextMessage(payload));
        } else {
            throw new NullPointerException("mWriterThread is dead!");
        }
    }


    public void sendRawTextMessage(byte[] payload) {
        mWriter.forward(new WebSocketMessage.RawTextMessage(payload));
    }


    public void sendBinaryMessage(byte[] payload) {
        mWriter.forward(new WebSocketMessage.BinaryMessage(payload));
    }


    public boolean isConnected() {
        return mTransportChannel != null && mTransportChannel.isConnected();
    }


    private void failConnection(int code, String reason) {

        if (DEBUG) DLog.d(TAG, "fail connection [code = " + code + ", reason = " + reason);

        if (mReader != null) {
            mReader.quit();
            try {
                mReader.join();
            } catch (InterruptedException e) {
                if (DEBUG) e.printStackTrace();
            }
            //mReader = null;
        } else {
            if (DEBUG) DLog.d(TAG, "mReader already NULL");
        }

        if (mWriter != null) {
//            mWriterThread.getLooper().quit();
            mWriter.forward(new WebSocketMessage.Quit());
            try {
                mWriterThread.join();
            } catch (InterruptedException e) {
                if (DEBUG) e.printStackTrace();
            }
//            mWriterThread = null;
        } else {
            if (DEBUG) DLog.d(TAG, "mWriter already NULL");
        }

        if (mTransportChannel != null) {
            try {
                mTransportChannel.close();
            } catch (IOException e) {
                if (DEBUG) e.printStackTrace();
            }
            //mTransportChannel = null;
        } else {
            if (DEBUG) DLog.d(TAG, "mTransportChannel already NULL");
        }

        onClose(code, reason);

        if (DEBUG) DLog.d(TAG, "worker threads stopped");
    }


    public void connect(String wsUri, ConnectionHandler wsHandler) throws WebSocketException {
        connect(wsUri, null, wsHandler, new WebSocketOptions(), null);
    }


    public void connect(String wsUri, ConnectionHandler wsHandler, WebSocketOptions options) throws WebSocketException {
        connect(wsUri, null, wsHandler, options, null);
    }


    public void connect(String wsUri, String[] wsSubprotocols, ConnectionHandler wsHandler, WebSocketOptions options, ContentValues headers) throws WebSocketException {

        // don't connect if already connected .. user needs to disconnect first
        //
        if (mTransportChannel != null && mTransportChannel.isConnected()) {
            throw new WebSocketException("already connected");
        }

        // parse WebSockets URI
        //
        try {
            mWsUri = new URI(wsUri);

            if (!mWsUri.getScheme().equals("ws") && !mWsUri.getScheme().equals("wss")) {
                throw new WebSocketException("unsupported scheme for WebSockets URI");
            }

            if (mWsUri.getScheme().equals("wss")) {
                throw new WebSocketException("secure WebSockets not implemented");
            }

            mWsScheme = mWsUri.getScheme();

            if (mWsUri.getPort() == -1) {
                if (mWsScheme.equals("ws")) {
                    mWsPort = 80;
                } else {
                    mWsPort = 443;
                }
            } else {
                mWsPort = mWsUri.getPort();
            }

            if (mWsUri.getHost() == null) {
                throw new WebSocketException("no host specified in WebSockets URI");
            } else {
                mWsHost = mWsUri.getHost();
            }

            if (mWsUri.getRawPath() == null || mWsUri.getRawPath().equals("")) {
                mWsPath = "/";
            } else {
                mWsPath = mWsUri.getRawPath();
            }

            if (mWsUri.getRawQuery() == null || mWsUri.getRawQuery().equals("")) {
                mWsQuery = null;
            } else {
                mWsQuery = mWsUri.getRawQuery();
            }

        } catch (URISyntaxException e) {

            throw new WebSocketException("invalid WebSockets URI");
        }

        mWsSubprotocols = wsSubprotocols;
        mWsHeaders = headers;
        mWsHandler = wsHandler;

        // make copy of options!
        mOptions = new WebSocketOptions(options);

        // set connection active
        mActive = true;

        // use asynch connector on short-lived background thread
        new WebSocketConnector().start();
    }


    public void disconnect() {
        if (mWriter != null) {
            mWriter.forward(new WebSocketMessage.Close(1000));
        } else {
            if (DEBUG) DLog.d(TAG, "could not send Close .. writer already NULL");
        }
        if (mReader != null) {
            mReader.quit();
        } else {
            if (DEBUG) DLog.d(TAG, "could not send Close .. reader already NULL");
        }
        mActive = false;
        mPrevConnected = false;
    }

    /**
     * Reconnect to the server with the latest options
     *
     * @return true if reconnection performed
     */
    public boolean reconnect() {
        if (!isConnected() && (mWsUri != null)) {
            new WebSocketConnector().start();
            return true;
        }
        return false;
    }

    /**
     * Perform reconnection
     *
     * @return true if reconnection was scheduled
     */
    protected boolean scheduleReconnect() {
        /**
         * Reconnect only if:
         *  - connection active (connected but not disconnected)
         *  - has previous success connections
         *  - reconnect interval is set
         */
        int interval = mOptions.getReconnectInterval();
        boolean need = mActive && mPrevConnected && (interval > 0);
        if (need) {
            if (DEBUG) DLog.d(TAG, "Reconnection scheduled");
            mMasterHandler.postDelayed(new Runnable() {

                public void run() {
                    if (DEBUG) DLog.d(TAG, "Reconnecting...");
                    reconnect();
                }
            }, interval);
        }
        return need;
    }

    /**
     * Common close handler
     *
     * @param code   Close code.
     * @param reason Close reason (human-readable).
     */
    private void onClose(int code, String reason) {
        boolean reconnecting = false;

        if ((code == ConnectionHandler.CLOSE_CANNOT_CONNECT) ||
                (code == ConnectionHandler.CLOSE_CONNECTION_LOST)) {
            reconnecting = scheduleReconnect();
        }


        if (mWsHandler != null) {
            try {
                if (reconnecting) {
                    mWsHandler.onClose(ConnectionHandler.CLOSE_RECONNECT, reason);
                } else {
                    mWsHandler.onClose(code, reason);
                }
            } catch (Exception e) {
                if (DEBUG) e.printStackTrace();
            }
            //mWsHandler = null;
        } else {
            if (DEBUG) DLog.d(TAG, "mWsHandler already NULL");
        }
    }


    /**
     * Create master message handler.
     */
    protected void createHandler() {

        mMasterHandler = new Handler(Looper.getMainLooper()) {

            public void handleMessage(Message msg) {

                if (msg.obj instanceof WebSocketMessage.TextMessage) {

                    WebSocketMessage.TextMessage textMessage = (WebSocketMessage.TextMessage) msg.obj;

                    if (mWsHandler != null) {
                        mWsHandler.onTextMessage(textMessage.mPayload);
                    } else {
                        if (DEBUG) DLog.d(TAG, "could not call onTextMessage() .. handler already NULL");
                    }

                } else if (msg.obj instanceof WebSocketMessage.RawTextMessage) {

                    WebSocketMessage.RawTextMessage rawTextMessage = (WebSocketMessage.RawTextMessage) msg.obj;

                    if (mWsHandler != null) {
                        mWsHandler.onRawTextMessage(rawTextMessage.mPayload);
                    } else {
                        if (DEBUG) DLog.d(TAG, "could not call onRawTextMessage() .. handler already NULL");
                    }

                } else if (msg.obj instanceof WebSocketMessage.BinaryMessage) {

                    WebSocketMessage.BinaryMessage binaryMessage = (WebSocketMessage.BinaryMessage) msg.obj;

                    if (mWsHandler != null) {
                        mWsHandler.onBinaryMessage(binaryMessage.mPayload);
                    } else {
                        if (DEBUG) DLog.d(TAG, "could not call onBinaryMessage() .. handler already NULL");
                    }

                } else if (msg.obj instanceof WebSocketMessage.Ping) {

                    WebSocketMessage.Ping ping = (WebSocketMessage.Ping) msg.obj;
                    if (DEBUG) DLog.d(TAG, "WebSockets Ping received");

                    // reply with Pong
                    WebSocketMessage.Pong pong = new WebSocketMessage.Pong();
                    pong.mPayload = ping.mPayload;
                    mWriter.forward(pong);

                } else if (msg.obj instanceof WebSocketMessage.Pong) {

                    @SuppressWarnings("unused")
                    WebSocketMessage.Pong pong = (WebSocketMessage.Pong) msg.obj;

                    if (DEBUG) DLog.d(TAG, "WebSockets Pong received");

                } else if (msg.obj instanceof WebSocketMessage.Close) {

                    WebSocketMessage.Close close = (WebSocketMessage.Close) msg.obj;

                    if (DEBUG) DLog.d(TAG, "WebSockets Close received (" + close.mCode + " - " + close.mReason + ")");

                    final int tavendoCloseCode = (close.mCode == 1000) ? ConnectionHandler.CLOSE_NORMAL : ConnectionHandler.CLOSE_CONNECTION_LOST;

                    if (mActive) {
                        mWriter.forward(new WebSocketMessage.Close(1000));
                    } else {
                        // we've initiated disconnect, so ready to close the channel
                        try {
                            mTransportChannel.close();
                        } catch (IOException e) {
                            if (DEBUG) e.printStackTrace();
                        }
                    }

                    onClose(tavendoCloseCode, close.mReason);

                } else if (msg.obj instanceof WebSocketMessage.ServerHandshake) {

                    WebSocketMessage.ServerHandshake serverHandshake = (WebSocketMessage.ServerHandshake) msg.obj;

                    if (DEBUG) DLog.d(TAG, "opening handshake received");

                    if (serverHandshake.mSuccess) {
                        if (mWsHandler != null) {
                            mWsHandler.onOpen(WebSocketConnection.this);
                        } else {
                            if (DEBUG) DLog.d(TAG, "could not call onOpen() .. handler already NULL");
                        }
                    }

                } else if (msg.obj instanceof WebSocketMessage.ConnectionLost) {

                    @SuppressWarnings("unused")
                    WebSocketMessage.ConnectionLost connnectionLost = (WebSocketMessage.ConnectionLost) msg.obj;
                    failConnection(WebSocketConnectionHandler.CLOSE_CONNECTION_LOST, "WebSockets connection lost");

                } else if (msg.obj instanceof WebSocketMessage.ProtocolViolation) {

                    @SuppressWarnings("unused")
                    WebSocketMessage.ProtocolViolation protocolViolation = (WebSocketMessage.ProtocolViolation) msg.obj;
                    failConnection(WebSocketConnectionHandler.CLOSE_PROTOCOL_ERROR, "WebSockets protocol violation");

                } else if (msg.obj instanceof WebSocketMessage.Error) {

                    WebSocketMessage.Error error = (WebSocketMessage.Error) msg.obj;
                    failConnection(WebSocketConnectionHandler.CLOSE_INTERNAL_ERROR, "WebSockets internal error (" + error.mException.toString() + ")");

                } else if (msg.obj instanceof WebSocketMessage.ServerError) {

                    WebSocketMessage.ServerError error = (WebSocketMessage.ServerError) msg.obj;
                    failConnection(WebSocketConnectionHandler.CLOSE_SERVER_ERROR, "Server error " + error.mStatusCode + " (" + error.mStatusMessage + ")");

                } else {

                    processAppMessage(msg.obj);

                }
            }
        };
    }


    protected void processAppMessage(Object message) {
    }


    /**
     * Create WebSockets background writer.
     */
    protected void createWriter() {

        mWriterThread = new HandlerThread("WebSocketWriter");
        mWriterThread.start();
        mWriter = new WebSocketWriter(mWriterThread.getLooper(), mMasterHandler, mTransportChannel, mOptions);

        if (DEBUG) DLog.d(TAG, "WS writer created and started");
    }


    /**
     * Create WebSockets background reader.
     */
    protected void createReader() {

        mReader = new WebSocketReader(mMasterHandler, mTransportChannel, mOptions, "WebSocketReader");
        mReader.start();

        if (DEBUG) DLog.d(TAG, "WS reader created and started");
    }
}
