package com.kmfrog.dabase.data.extra;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kmfrog.dabase.data.Cache;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.data.RequestExecutor;
import com.kmfrog.dabase.data.Response;
import com.kmfrog.dabase.exception.BaseException;

public class SQLiteExecRequestExecutor extends RequestExecutor<Object, Long> {

    private final Map<String, SQLiteOpenHelper> sqlites=new ConcurrentHashMap<String, SQLiteOpenHelper>();

    protected SQLiteExecRequestExecutor(RawParser<Object, Long> parser, Cache cache) {
        super(parser, cache);
    }

    public SQLiteExecRequestExecutor(List<SQLiteOpenHelper> sqliteOpenHelpers, RawParser<Object, Long> parser, Cache cache) {
        this(parser, cache);
        for(SQLiteOpenHelper helper: sqliteOpenHelpers) {
            sqlites.put(helper.getClass().getName(), helper);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Response<Object> exec(Request<Object, Long> req) throws BaseException {
        SQLiteExecRequest request=(SQLiteExecRequest)req;
        String sqliteOpenHelperClz=request.getSQLiteOpenHelperClz();
        SQLiteOpenHelper sqlite=sqlites.get(sqliteOpenHelperClz);
        if(sqlite == null) {
            String msg=new StringBuilder().append("unexpect sqliteOpenHelperClz:").append(sqliteOpenHelperClz).toString();
            return new Response<Object>(new BaseException(msg));
        }
        SQLiteDatabase db=sqlite.getWritableDatabase();
        long ret=-1L;
        final int conflictAlgorithm=request.getmConflictAlgorithm();
        switch(request.getOp()) {
            case INSERT:
                if(SQLiteDatabase.CONFLICT_NONE == conflictAlgorithm) {
                    ret=db.insert(request.getTableName(), request.getNullColumnHack(), request.getContentValues());
                } else {
                    ret=
                        db.insertWithOnConflict(request.getTableName(), request.getNullColumnHack(), request.getContentValues(),
                            conflictAlgorithm);
                }
                break;
            case UPDATE:
                if(SQLiteDatabase.CONFLICT_NONE == conflictAlgorithm) {
                    ret=db.update(request.getTableName(), request.getContentValues(), request.getWhere(), request.getWhereArgs());
                } else {
                    ret=
                        db.updateWithOnConflict(request.getTableName(), request.getContentValues(), request.getWhere(),
                            request.getWhereArgs(), conflictAlgorithm);
                }
                break;
            case DELETE:
                ret=db.delete(request.getTableName(), request.getWhere(), request.getWhereArgs());
                break;
        }
        return new Response<Object>(Long.valueOf(ret), false);

    }
}
