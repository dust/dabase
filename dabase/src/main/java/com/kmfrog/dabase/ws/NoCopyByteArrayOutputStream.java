package com.kmfrog.dabase.ws;

/**
 * Created by dust on 16-6-22.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * OutputStream backed by a byte array. This class provides copyless access
 * to byte array backing the ByteArrayOutputStream
 */
public class NoCopyByteArrayOutputStream extends ByteArrayOutputStream {

    /**
     * Create new OutputStream backed by byte array.
     */
    public NoCopyByteArrayOutputStream() {
        super();
    }

    /**
     * Create new OutputStream backed by byte array.
     *
     * @param size    Initial size of underlying byte array.
     */
    public NoCopyByteArrayOutputStream(int size) {
        super(size);
    }

    /**
     * Wraps the underyling byte array into an InputStream.
     *
     * @return     New InputStream wrapping byte buffer underlying this stream.
     */
    public InputStream getInputStream() {
        return new ByteArrayInputStream(buf, 0, count);
    }

    /**
     * Get byte array underlying this OutputStream. This
     * does not copy any data, but return reference to the
     * underlying byte array.
     *
     * @return     Underlying byte array by reference.
     */
    public byte[] getByteArray() {
        return buf;
    }
}
