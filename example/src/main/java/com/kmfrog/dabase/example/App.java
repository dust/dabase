package com.kmfrog.dabase.example;

import com.kmfrog.dabase.app.BaseApp;
import com.kmfrog.dabase.data.Cache;
import com.kmfrog.dabase.data.extra.BasedUriParser;
import com.kmfrog.dabase.data.extra.DbCursorParser;
import com.kmfrog.dabase.data.extra.DiskBasedCache;
import com.kmfrog.dabase.data.extra.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dust on 6/14/16.
 */
public class App extends BaseApp {

    static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static App get(){
        return sInstance;
    }

    @Override
    protected List<JsonParser> getJsonParsers() {
        List<JsonParser> list = new ArrayList<JsonParser>();
        list.add(new VersusJsonParser());
        return list;
    }

    @Override
    protected List<DbCursorParser> getDbCursorParsers() {
        return null;
    }

    @Override
    protected List<BasedUriParser> getBasedUriParsers() {
        return null;
    }

    @Override
    protected Cache newInstanceApiCache() {
        return new DiskBasedCache(getCacheDir("dat"), 1024*1024*2);
    }

    @Override
    protected Cache newInstanceBitmapCache() {
        return new DiskBasedCache(getCacheDir("images"), 1024*1024*5);
    }

    @Override
    protected String getChannelId() {
        return "11111";
    }

    @Override
    protected String getKey() {
        return "xxxxx";
    }
}
