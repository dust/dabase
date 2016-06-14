package com.kmfrog.dabase.data.extra;

import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;

public abstract class DbCursorParser<D> {

    // private String[] mColumns;
    private final Map<String, Integer> mColumnIndexs;

    public DbCursorParser(String[] columns) {
        mColumnIndexs=new HashMap<String, Integer>(columns.length);
        for(int i=0; i < columns.length; i++) {
            mColumnIndexs.put(columns[i], i);
        }
    }

    protected abstract D parseCursor(Cursor cursor);

    public int getIndex(String column) {
        Integer index=mColumnIndexs.get(column);
        if(index == null) {
            throw new RuntimeException(new StringBuilder().append("column :").append(column).append(" not found in ")
                .append(mColumnIndexs.toString()).toString());
        }
        return index.intValue();
    }
    
    /**
     * 将一个int值转为Boolean实例。
     *  负数返回null
     *  1返回Boolean.True
     *  0返回Boolean.False
     * @param value
     * @return
     */
    public Boolean intToBoolean(int value){
        if(value<0){
            return null;
        }
        return (value==1)?Boolean.TRUE:Boolean.FALSE;
    }

}
