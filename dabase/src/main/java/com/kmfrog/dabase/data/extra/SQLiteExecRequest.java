package com.kmfrog.dabase.data.extra;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.kmfrog.dabase.AsyncObserver;
import com.kmfrog.dabase.Contract;
import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.util.UriUtils;

public class SQLiteExecRequest<D> extends Request<D, Long> {

    private final ExecOp mOp;

    private final ContentValues mContentValues;

    private final String mWhere;

    private final String[] mWhereArgs;

    private final String mNullColumnHack;

    private final int mConflictAlgorithm;

    public enum ExecOp {
        INSERT, UPDATE, DELETE;
    }

    public SQLiteExecRequest(Uri uri, AsyncObserver<D, Throwable> callback, ExecOp op) {
        this(uri, callback, op, null, null, null, null, SQLiteDatabase.CONFLICT_NONE);
    }

    public SQLiteExecRequest(Uri uri, AsyncObserver<D, Throwable> callback, ExecOp op, ContentValues contentValues) {
        this(uri, callback, op, contentValues, null, null, null, SQLiteDatabase.CONFLICT_NONE);
    }

    public SQLiteExecRequest(Uri uri, AsyncObserver<D, Throwable> callback, ExecOp op, ContentValues contentValues,
        int conflictAlgorithm) {
        this(uri, callback, op, contentValues, null, null, null, conflictAlgorithm);
    }

    public SQLiteExecRequest(Uri uri, AsyncObserver<D, Throwable> callback, ExecOp op, ContentValues contentValues,
        String nullColumnHack, int conflictAlgorithm) {
        this(uri, callback, op, contentValues, null, null, nullColumnHack, conflictAlgorithm);
    }

    public SQLiteExecRequest(Uri uri, AsyncObserver<D, Throwable> callback, ExecOp op, ContentValues contentValues, String where,
        String[] whereArgs) {
        this(uri, callback, op, contentValues, where, whereArgs, null, SQLiteDatabase.CONFLICT_NONE);
    }

    public SQLiteExecRequest(Uri uri, AsyncObserver<D, Throwable> callback, ExecOp op, ContentValues contentValues, String where,
        String[] whereArgs, int conflictAlgorithm) {
        this(uri, callback, op, contentValues, where, whereArgs, null, conflictAlgorithm);
    }

    public SQLiteExecRequest(String sqliteOpenHelperClz, String tableName, AsyncObserver<D, Throwable> callback, ExecOp op) {
        this(UriUtils.build(Contract.URI_DB_SCHEME, sqliteOpenHelperClz, tableName), callback, op);
    }

    public SQLiteExecRequest(String sqliteOpenHelperClz, String tableName, AsyncObserver<D, Throwable> callback, ExecOp op,
        ContentValues contentValue) {
        this(UriUtils.build(Contract.URI_DB_SCHEME, sqliteOpenHelperClz, tableName), callback, op, contentValue,
            SQLiteDatabase.CONFLICT_NONE);
    }

    public SQLiteExecRequest(String sqliteOpenHelperClz, String tableName, AsyncObserver<D, Throwable> callback, ExecOp op,
        ContentValues contentValue, int conflictAlgorithm) {
        this(UriUtils.build(Contract.URI_DB_SCHEME, sqliteOpenHelperClz, tableName), callback, op, contentValue, conflictAlgorithm);
    }

    public SQLiteExecRequest(String sqliteOpenHelperClz, String tableName, AsyncObserver<D, Throwable> callback, ExecOp op,
        ContentValues contentValues, String where, String[] whereArgs) {
        this(UriUtils.build(Contract.URI_DB_SCHEME, sqliteOpenHelperClz, tableName), callback, op, contentValues, where, whereArgs,
            SQLiteDatabase.CONFLICT_NONE);
    }

    public SQLiteExecRequest(String sqliteOpenHelperClz, String tableName, AsyncObserver<D, Throwable> callback, ExecOp op,
        ContentValues contentValues, String where, String[] whereArgs, int conflictAlgorithm) {
        this(UriUtils.build(Contract.URI_DB_SCHEME, sqliteOpenHelperClz, tableName), callback, op, contentValues, where, whereArgs,
            conflictAlgorithm);
    }

    public SQLiteExecRequest(Uri uri, AsyncObserver<D, Throwable> callback, ExecOp op, ContentValues contentValues, String where,
        String[] whereArgs, String nullColumnHack, int conflictAlgorithm) {
        super(uri, callback);
        mOp=op;
        mContentValues=contentValues;
        mWhere=where;
        mWhereArgs=whereArgs;
        mNullColumnHack=nullColumnHack;
        mConflictAlgorithm=conflictAlgorithm;
        setShouldCache(false);
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

    @Override
    public String getUrl() {
        return null;
    }

    public ExecOp getOp() {
        return mOp;
    }

    public ContentValues getContentValues() {
        return mContentValues;
    }

    public String getWhere() {
        return mWhere;
    }

    public String[] getWhereArgs() {
        return mWhereArgs;
    }

    public String getNullColumnHack() {
        return mNullColumnHack;
    }

    public int getmConflictAlgorithm() {
        return mConflictAlgorithm;
    }

}
