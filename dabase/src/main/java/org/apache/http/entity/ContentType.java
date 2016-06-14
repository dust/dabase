package org.apache.http.entity;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;

import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

public class ContentType {

    public static final ContentType TEXT_PLAIN=create("text/plain", Charset.forName("UTF-8"));

    public static final ContentType APPLICATION_OCTET_STREAM=create("application/octet-stream", (Charset)null);

    // defaults
    public static final ContentType DEFAULT_TEXT=TEXT_PLAIN;

    private final String mimeType;

    private final Charset charset;

    private final NameValuePair[] params;

    public static final ContentType DEFAULT_BINARY=APPLICATION_OCTET_STREAM;

    public static ContentType create(final String mimeType, final Charset charset) {
        final String type=Args.notBlank(mimeType, "MIME type").toLowerCase(Locale.US);
        Args.check(valid(type), "MIME type may not contain reserved characters");
        return new ContentType(type, charset);
    }

    /**
     * @since 4.3
     */
    public String getParameter(final String name) {
        Args.notEmpty(name, "Parameter name");
        if(this.params == null) {
            return null;
        }
        for(final NameValuePair param: this.params) {
            if(param.getName().equalsIgnoreCase(name)) {
                return param.getValue();
            }
        }
        return null;
    }

    ContentType(final String mimeType, final Charset charset) {
        this.mimeType=mimeType;
        this.charset=charset;
        this.params=null;
    }

    ContentType(final String mimeType, final NameValuePair[] params) throws UnsupportedCharsetException {
        this.mimeType=mimeType;
        this.params=params;
        final String s=getParameter("charset");
        this.charset=s != null ? Charset.forName(s) : null;
    }

    private static boolean valid(final String s) {
        for(int i=0; i < s.length(); i++) {
            final char ch=s.charAt(i);
            if(ch == '"' || ch == ',' || ch == ';') {
                return false;
            }
        }
        return true;
    }

    public static ContentType parse(final String s) throws ParseException, UnsupportedCharsetException {
        Args.notNull(s, "Content type");
        final CharArrayBuffer buf=new CharArrayBuffer(s.length());
        buf.append(s);
        final ParserCursor cursor=new ParserCursor(0, s.length());
        final HeaderElement[] elements=BasicHeaderValueParser.INSTANCE.parseElements(buf, cursor);
        if(elements.length > 0) {
            return create(elements[0]);
        } else {
            throw new ParseException("Invalid content type: " + s);
        }
    }

    private static ContentType create(final HeaderElement helem) {
        final String mimeType=helem.getName();
        final NameValuePair[] params=helem.getParameters();
        return new ContentType(mimeType, params != null && params.length > 0 ? params : null);
    }

    public String getMimeType() {
        return mimeType;
    }

    public Charset getCharset() {
        return charset;
    }

    public NameValuePair[] getParams() {
        return params;
    }

    public static ContentType create(final String mimeType) {
        return new ContentType(mimeType, (Charset)null);
    }

    public static ContentType create(final String mimeType, final String charset) throws UnsupportedCharsetException {
        return create(mimeType, !Args.isBlank(charset) ? Charset.forName(charset) : null);
    }
}
