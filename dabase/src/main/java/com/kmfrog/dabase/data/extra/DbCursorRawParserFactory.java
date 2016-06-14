package com.kmfrog.dabase.data.extra;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;

import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.exception.ParserException;

public class DbCursorRawParserFactory implements RawParser<Object, Cursor> {

    @SuppressWarnings("rawtypes")
    protected static Map<Class<? extends DbCursorParser>, DbCursorParser> map=
        new HashMap<Class<? extends DbCursorParser>, DbCursorParser>();

    private static final DbCursorRawParserFactory INSTANCE=new DbCursorRawParserFactory();

    private DbCursorRawParserFactory() {
        super();
    }

    @SuppressWarnings("rawtypes")
    private DbCursorRawParserFactory(List<DbCursorParser> dbCursorParserList) {
        super();
    }

    @SuppressWarnings("rawtypes")
    public void registerDbCursorParsers(List<DbCursorParser> dbCursorParsers) {
        for(DbCursorParser parser: dbCursorParsers) {
            map.put(parser.getClass(), parser);
        }
    }

    /**
     * 获得Cursor解析工厂类的实例。
     * @return
     */
    public synchronized static DbCursorRawParserFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 获得Cursor解析工厂类的实例。
     * @return
     */
    @SuppressWarnings("rawtypes")
    public synchronized static DbCursorRawParserFactory getInstance(List<DbCursorParser> dbCursorParsers) {
        if(map.size() == 0) {
            INSTANCE.registerDbCursorParsers(dbCursorParsers);
        }
        return INSTANCE;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object parse(Cursor raw, Map<String, Object> extra) throws ParserException {
        Class<? extends DbCursorParser> clz=(Class<? extends DbCursorParser>)extra.get("DbCursorParser");
        DbCursorParser parser=map.get(clz);
        if(parser == null) {
            if(DLog.DEBUG) {
                DLog.d(new StringBuilder().append("unexpect DbCursorParser:").append(clz.getName()).toString());
            }
            return null;
        }
        return parser.parseCursor(raw);
    }

}
