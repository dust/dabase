/**
 * $Id: PreferenceUtil.java,v 1.1 2011/05/20 07:28:07 jian.hu Exp $
 */
package com.kmfrog.dabase.util;

import android.content.Context;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceUtil {

    private static final String PREFERENCE_NAME="PREFERENCE_DOWNJOY";

    private static PreferenceUtil mInstance;

    private SharedPreferences mSharedPreferences;

    private Editor mEditor;

    private Context mContext;

    private PreferenceUtil(Context context) {
        this.mContext=context;
        init(context);
    }

    public void init(Context context) {
        if(mSharedPreferences == null || mEditor == null) {
            try {
                mSharedPreferences=context.getSharedPreferences(PREFERENCE_NAME, 0);
                mEditor=mSharedPreferences.edit();
            } catch(Exception e) {
            }
        }
    }

    public static PreferenceUtil getInstance(Context context) {
        if(mInstance == null) {
            mInstance=new PreferenceUtil(context);
        }
        mInstance.init(context);
        return mInstance;
    }

    public void saveLong(String key, long l) {
        init(mContext);
        mEditor.putLong(key, l);
        mEditor.commit();
    }

    public long getLong(String key, long defaultLong) {
        init(mContext);
        return mSharedPreferences.getLong(key, defaultLong);
    }

    public void saveBoolean(String key, boolean value) {
        init(mContext);
        mEditor.putBoolean(key, value);
        mEditor.commit();
    }

    public boolean getBoolean(String key, boolean defaultBoolean) {
        init(mContext);
        return mSharedPreferences.getBoolean(key, defaultBoolean);
    }

    public void saveInt(String key, int value) {
        init(mContext);
        mEditor.putInt(key, value);
        mEditor.commit();
    }

    public int getInt(String key, int defaultInt) {
        init(mContext);
        return mSharedPreferences.getInt(key, defaultInt);
    }

    public void saveFloat(String key, float value) {
        init(mContext);
        saveString(key, Float.toString(value));
    }

    public float getFloat(String key, float defaultFloat) {
        try {
            String s=getString(key, null);
            if(s == null) {
                return defaultFloat;
            } else {
                return Float.parseFloat(s);
            }

        } catch(Exception e) {
            return defaultFloat;
        }
    }

    public void saveDouble(String key, double value) {
        init(mContext);
        saveString(key, Double.toString(value));
    }

    public double getDouble(String key, double defaultDouble) {
        try {
            String s=getString(key, null);
            if(s == null) {
                return defaultDouble;
            } else {
                return Double.parseDouble(s);
            }

        } catch(Exception e) {
            return defaultDouble;
        }
    }

    public void saveString(String key, String value) {
        init(mContext);
        mEditor.putString(key, value);
        mEditor.commit();
    }

    public String getString(String key, String defaultString) {
        init(mContext);
        return mSharedPreferences.getString(key, defaultString);
    }

    public void remove(String key) {
        init(mContext);
        mEditor.remove(key);
        mEditor.commit();
    }

    public void onDestroy() {
        mSharedPreferences=null;
        mEditor=null;
        mInstance=null;
    }

    public static void destroy() {
        if(mInstance != null) {
            mInstance.onDestroy();
            mInstance=null;
        }
    }
}
