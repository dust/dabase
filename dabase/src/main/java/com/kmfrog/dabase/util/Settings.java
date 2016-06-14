package com.kmfrog.dabase.util;

import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.text.TextUtils;

public class Settings {

    private SharedPreferences mPrefs;

    public Settings(Context context, String name) {
        if(Build.VERSION.SDK_INT > 10) {
            mPrefs=context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
        } else {
            mPrefs=context.getSharedPreferences(name, Context.MODE_PRIVATE);
        }
    }

    public int getInt(String key, int def) {
        return mPrefs.getInt(key, def);
    }

    public boolean getBoolean(String key, boolean def) {
        return mPrefs.getBoolean(key, def);
    }

    public long getLong(String key, long def) {
        return mPrefs.getLong(key, def);
    }

    public String getString(String key, String def) {
        return mPrefs.getString(key, def);
    }

    public String[] getStringArray(String key, String[] def) {
        return getStringArray(key, def, "[|]");
    }
    
    public Set<String> getStringSet(String key, Set<String> def){
        return mPrefs.getStringSet(key, def);
    }

    public String[] getStringArray(String key, String[] def, String expression) {
        String tmp=mPrefs.getString(key, null);
        if(tmp == null) {
            return def;
        }
        return TextUtils.split(tmp, expression);
    }

    public double getDouble(String key, double d) {
        String str=getString(key, null);
        if(TextUtils.isEmpty(str)) {
            return d;
        }
        try {
            return Double.parseDouble(str);
        } catch(NumberFormatException ex) {
            return d;
        }
    }

    public void setString(String key, String value) {
        final Editor edit=mPrefs.edit();
        edit.putString(key, value);
        EditorUtils.getInstance().applyOrCommit(edit);
    }

    public void setLong(String key, long v) {
        final Editor edit=mPrefs.edit();
        edit.putLong(key, v);
        EditorUtils.getInstance().applyOrCommit(edit);
    }

    public void setInt(String key, int v) {
        final Editor edit=mPrefs.edit();
        edit.putInt(key, v);
        EditorUtils.getInstance().applyOrCommit(edit);
    }

    public void putDouble(String key, double v) {
        final Editor edit=mPrefs.edit();
        edit.putFloat(key, (float)v);
        EditorUtils.getInstance().applyOrCommit(edit);
    }

    public void setBoolean(String key, boolean v) {
        final Editor edit=mPrefs.edit();
        edit.putBoolean(key, v);
        EditorUtils.getInstance().applyOrCommit(edit);
    }

    public void setStringArray(String key, String[] array) {
        setStringArray(key, array, "|");
    }

    public void setStringArray(String key, String[] array, CharSequence delimiter) {
        final Editor edit=mPrefs.edit();
        edit.putString(key, TextUtils.join(delimiter, array));
        EditorUtils.getInstance().applyOrCommit(edit);
    }
    
    public void setStringSet(String key, Set<String> set){
        final Editor edit=mPrefs.edit();
        edit.putStringSet(key, set);
        EditorUtils.getInstance().applyOrCommit(edit);
    }
    
    public void remove(String key){
        final Editor edit=mPrefs.edit();
        edit.remove(key);
        EditorUtils.getInstance().applyOrCommit(edit);
    }

    public void commit() {
        mPrefs.edit().commit();
    }
}
