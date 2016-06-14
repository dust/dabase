package com.kmfrog.dabase.data.extra;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.UriMatcher;
import android.net.Uri;


import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.exception.ParserException;

public class BasedUriRawParserFactory implements RawParser<Object, byte[]> {

    public static final String DEF_CHARSET="utf8";

    public static final String DEF_IDENTITY_KEY="URI";

    public static final int BASE_CODE=10000;

    protected static final UriMatcher sUriMatch=new UriMatcher(UriMatcher.NO_MATCH);

    @SuppressWarnings("rawtypes")
    protected static final Map<Integer, BasedUriParser> sCodeMap=new ConcurrentHashMap<Integer, BasedUriParser>();

    @SuppressWarnings("rawtypes")
    protected static final Map<Uri, BasedUriParser> sUriMap=new ConcurrentHashMap<Uri, BasedUriParser>();

    public static BasedUriRawParserFactory INSTANCE;

    private BasedUriRawParserFactory() {
        super();
    }

    @SuppressWarnings("rawtypes")
    private BasedUriRawParserFactory(List<BasedUriParser> parsers) {
        batchRegisterParser(parsers);
    }

    @SuppressWarnings("rawtypes")
    private static void batchRegisterParser(List<BasedUriParser> parsers) {
        if(parsers != null) {
            for(BasedUriParser p: parsers) {
                sUriMap.put(p.getIdenity(), p);
            }
        }
    }

    public synchronized static BasedUriRawParserFactory getInstance() {
        if(INSTANCE == null) {
            INSTANCE=new BasedUriRawParserFactory();
        }
        return INSTANCE;
    }

    @SuppressWarnings("rawtypes")
    public synchronized static BasedUriRawParserFactory getInstance(List<BasedUriParser> parsers) {
        if(INSTANCE == null) {
            INSTANCE=new BasedUriRawParserFactory(parsers);
        }
        if(sUriMap.size() == 0) {
            batchRegisterParser(parsers);
        }
        return INSTANCE;
    }

    public <D> void registerJsonParser(String authority, String path, int code, BasedUriParser<D> parser) {
        sUriMatch.addURI(authority, path, code);
        sCodeMap.put(code, parser);
    }

    @Override
    public Object parse(byte[] raw, Map<String, Object> extra) throws ParserException {
        String charset=(String)extra.get("charset");
        Uri uri=(Uri)extra.get(DEF_IDENTITY_KEY);
        try {
            String jsonText=new String(raw, charset == null ? DEF_CHARSET : charset);
//            if(DLog.DEBUG){
//                DLog.d("BasedUriResp:%s",jsonText);
//            }
            return rawParseObject(jsonText, uri);
        } catch(Throwable ex) {
            throw new ParserException(ex);
        }
    }

    @SuppressWarnings("rawtypes")
    private Object rawParseObject(String body, Uri uri) throws ParserException {
        BasedUriParser parser=getParserByUri(uri);
        if(parser == null) {
            if(DLog.DEBUG) {
                DLog.d(new StringBuilder().append("rawParseObject unexpect uri:").append(uri == null ? "null" : uri).toString());
            }
            return null;
        }
        return parser.parseObject(body);
    }

    @SuppressWarnings("rawtypes")
    public final BasedUriParser getParserByUri(Uri dataType) throws ParserException {
        int code=sUriMatch.match(dataType);
        BasedUriParser p=sCodeMap.get(code);
        if(p != null) {
            return p;
        }
        return sUriMap.get(dataType);
    }

}
