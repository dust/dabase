package com.kmfrog.dabase.data.extra;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import com.kmfrog.dabase.data.Cache;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.data.RequestExecutor;
import com.kmfrog.dabase.data.Response;
import com.kmfrog.dabase.exception.BaseException;

public class SQLiteRequestExecutor extends RequestExecutor<Object, Cursor> {

    private final Map<String, SQLiteOpenHelper> sqlites=new ConcurrentHashMap<String, SQLiteOpenHelper>();

    protected SQLiteRequestExecutor(RawParser<Object, Cursor> parser, Cache cache) {
        super(parser, cache);
    }

    public SQLiteRequestExecutor(List<SQLiteOpenHelper> sqliteOpenHelpers, RawParser<Object, Cursor> parser, Cache cache) {
        this(parser, cache);
        for(SQLiteOpenHelper helper: sqliteOpenHelpers) {
            sqlites.put(helper.getClass().getName(), helper);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Response<Object> exec(Request<Object, Cursor> req) throws BaseException {
        SQLiteRequest request=(SQLiteRequest)req;
        String sqliteOpenHelperClz=request.getSQLiteOpenHelperClz();
        SQLiteOpenHelper sqlite=sqlites.get(sqliteOpenHelperClz);
        if(sqlite == null) {
            String msg=new StringBuilder().append("unexpect sqliteOpenHelperClz:").append(sqliteOpenHelperClz).toString();
            return new Response(new BaseException(msg));
        }
        Cursor cursor=
            sqlite.getReadableDatabase().query(request.getTableName(), request.getColumns(), request.getWhere(),
                request.getWhereArgs(), request.getGroupBy(), request.getHaving(), request.getOrderBy());
        try {
            Map<String, Object> extra=new HashMap<String, Object>();
            Class<? extends DbCursorParser> clz=request.getCursorParserClz();
            extra.put("DbCursorParser", clz);
            Object obj=getParser().parse(cursor, extra);
            return new Response<Object>(obj, false);
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
    }

}
