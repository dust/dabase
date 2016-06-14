package com.kmfrog.dabase.util;

import java.lang.reflect.Method;

import android.content.SharedPreferences.Editor;

import com.kmfrog.dabase.DLog;

public class EditorUtils {

    private static Method sApplyMethod;

    private static EditorUtils sInstance;

    private EditorUtils() {
        try {
            sApplyMethod=Editor.class.getMethod("apply", new Class<?>[]{});
            sApplyMethod.setAccessible(true);
        } catch(Exception ex) {
            if(DLog.DEBUG) {
                DLog.e("EditorUtils", "not found apply() method.", ex);
            }
        }
    }

    public static synchronized EditorUtils getInstance() {
        if(sInstance == null) {
            sInstance=new EditorUtils();
        }
        return sInstance;
    }

    public void applyOrCommit(Editor editor) {
        try {
            if(sApplyMethod != null) {
                sApplyMethod.invoke(editor, new Object[]{});
                return;
            }
        } catch(Exception ex) {
            if(DLog.DEBUG) {
                DLog.e("EditorUtils", ".apply(editor)", ex);
            }
        }
        editor.commit();
    }

}
