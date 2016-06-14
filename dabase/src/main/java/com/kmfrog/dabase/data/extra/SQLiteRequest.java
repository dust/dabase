package com.kmfrog.dabase.data.extra;

import android.database.Cursor;
import android.net.Uri;

import com.kmfrog.dabase.AsyncObserver;
import com.kmfrog.dabase.Contract;
import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.util.UriUtils;

public class SQLiteRequest<D> extends Request<D, Cursor> {

    private final String[] mColumns;

    private final String mWhere;

    private final String[] mWhereArgs;

    private final Class<? extends DbCursorParser<D>> mClz;

    private final String mOrderBy;

    private final String mGroupBy;

    private final String mHaving;

    public SQLiteRequest(Uri uri, AsyncObserver<D, Throwable> callback, Class<? extends DbCursorParser<D>> clz) {
        this(uri, callback, null, null, null, clz);
    }

    public SQLiteRequest(Uri uri, AsyncObserver<D, Throwable> callback, String[] columns, Class<? extends DbCursorParser<D>> clz) {
        this(uri, callback, columns, null, null, clz);
    }

    public SQLiteRequest(Uri uri, AsyncObserver<D, Throwable> callback, String[] columns, String where, String[] whereArgs,
        Class<? extends DbCursorParser<D>> clz) {
        this(uri, callback, columns, where, whereArgs, null, null, null, clz);
    }

    public SQLiteRequest(Uri uri, AsyncObserver<D, Throwable> callback, String[] columns, String where, String[] whereArgs,
        String groupBy, String having, String orderBy, Class<? extends DbCursorParser<D>> clz) {
        super(uri, callback);
        mColumns=columns;
        mWhere=where;
        mWhereArgs=whereArgs;
        mGroupBy=groupBy;
        mHaving=having;
        mOrderBy=orderBy;
        mClz=clz;
        setShouldCache(false);
    }

    public SQLiteRequest(String sqliteOpenHelperClz, String tableName, AsyncObserver<D, Throwable> callback, String[] columns,
        String where, String[] whereArgs, Class<? extends DbCursorParser<D>> clz) {
        this(UriUtils.build(Contract.URI_DB_SCHEME, sqliteOpenHelperClz, tableName), callback, columns, where, whereArgs, clz);
    }

    public SQLiteRequest(String sqliteOpenHelperClz, String tableName, AsyncObserver<D, Throwable> callback, String[] columns,
        String where, String[] whereArgs, String groupBy, String having, String orderBy, Class<? extends DbCursorParser<D>> clz) {
        this(UriUtils.build(Contract.URI_DB_SCHEME, sqliteOpenHelperClz, tableName), callback, columns, where, whereArgs, groupBy,
            having, orderBy, clz);
    }

    /**
     * 获得相应的SQLiteOpenHelperClz
     * @return Class<? extends SQLiteOpenHelper>.getName()
     */
    public String getSQLiteOpenHelperClz() {
        Uri uri=getUri();
        return uri.getHost();
    }

    public String getTableName() {
        Uri uri=getUri();
        final String tableName=uri.getPath();
        if(tableName.startsWith("/")) {
            return tableName.substring(1);// exclude "/", "/table_name"
        }
        return tableName;
    }

    public String[] getColumns() {
        return mColumns;
    }

    public String getWhere() {
        return mWhere;
    }

    public String[] getWhereArgs() {
        return mWhereArgs;
    }

    public String getOrderBy() {
        return mOrderBy;
    }

    public String getGroupBy() {
        return mGroupBy;
    }

    public String getHaving() {
        return mHaving;
    }

    public Class<? extends DbCursorParser<D>> getCursorParserClz() {
        return mClz;
    }

    @Override
    public String getUrl() {
        return null;
    }

}
