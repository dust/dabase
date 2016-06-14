package com.kmfrog.dabase.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.kmfrog.dabase.app.BaseApp;

import android.content.Context;
import android.os.Environment;
import android.os.Process;

public class ParameterUtil {

    private static final String KEY_PREFERENCE_PARAMETERFILEUTIL_PREFERENCE_KES="KEY_PREFERENCE_PARAMETERFILEUTIL_PREFERENCE_KES";

    private static final String FILE_TYPE_NAME=".downjoy.parameter";

    private static final String FILE_DIR_NAME_PUBLIC="/downjoy/parameter/";

    private static final String sStartName_public="DOWNJOY_";

    private static final String sSpace=",";

    public static String getParameter(Context context, String key, String defaultString) {
        PreferenceUtil pu=PreferenceUtil.getInstance(context);
        initFromFile(context, false);
        return pu.getString(key, defaultString);
    }

    public static void removeParameter(final Context context, final String key) {
        new Thread() {

            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                try {
                    remove(context, key);
                } catch(Exception e) {
                }
            }
        }.start();
    }

    private synchronized static void remove(Context context, String key) {
        final PreferenceUtil pu=PreferenceUtil.getInstance(context);
        pu.remove(key);
        String keys=pu.getString(KEY_PREFERENCE_PARAMETERFILEUTIL_PREFERENCE_KES, null);
        if(keys == null) {
            return;
        }
        if(keys.contains("true_" + key)) {
            keys=keys.replace("true_" + key, "");
            getFile(key + FILE_TYPE_NAME, true, context.getPackageName()).delete();
            pu.saveString(KEY_PREFERENCE_PARAMETERFILEUTIL_PREFERENCE_KES, keys);
        } else if(keys.contains("false_" + key)) {
            keys=keys.replace("false_" + key, "");
            getFile(key + FILE_TYPE_NAME, false, context.getPackageName()).delete();
            pu.saveString(KEY_PREFERENCE_PARAMETERFILEUTIL_PREFERENCE_KES, keys);
        }
    }

    public static void saveParameter(Context context, final String key, final String value) {
        saveParameter(context, key, value, true);
    }

    public static void saveParameter(final Context context, final String key, final String value, final boolean isPrivate) {
        final PreferenceUtil pu=PreferenceUtil.getInstance(context);
        pu.saveString(key, value);
        final String keys=pu.getString(KEY_PREFERENCE_PARAMETERFILEUTIL_PREFERENCE_KES, null);
        String ss=isPrivate + "_" + key;
        if(keys == null) {
            pu.saveString(KEY_PREFERENCE_PARAMETERFILEUTIL_PREFERENCE_KES, ss);
        } else if(!keys.contains(ss)) {
            pu.saveString(KEY_PREFERENCE_PARAMETERFILEUTIL_PREFERENCE_KES, keys + sSpace + ss);
        }
        final String packageName=context.getPackageName();
        new Thread() {

            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                try {

                    savaParameterInFile(key, value, isPrivate, packageName);
                    if(keys != null) {
                        String[] arr=keys.split(sSpace);
                        for(String k: arr) {
                            String v=pu.getString(keys, null);
                            if(v != null) {
                                boolean is=k.startsWith("true_");
                                if(is) {
                                    k=k.substring(5);
                                } else {
                                    k=k.substring(6);
                                }
                                savaParameterInFile(k, v, is, packageName);
                            }
                        }
                    }
                } catch(Exception e) {
                }
            }
        }.start();
    }

    private static File getDirFile(boolean isPrivate, String pkg) {
        try {
            File dirFile;
            if(isPrivate) {
                dirFile=new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/downjoy/parameter/" + pkg + "/");
            } else {
                dirFile=new File(Environment.getExternalStorageDirectory().getAbsolutePath() + FILE_DIR_NAME_PUBLIC);
            }
            if(!dirFile.exists()) {
                dirFile.mkdirs();
            }
            return dirFile;
        } catch(Exception e) {
            return null;
        }
    }

    private static File getFile(String fileName, boolean isPrivate, String pkg) {
        File dirFile=getDirFile(isPrivate, pkg);
        if(dirFile == null) {
            return null;
        }
        if(isPrivate) {
            fileName=pkg + "_" + fileName;
        } else {
            fileName=sStartName_public + fileName;
        }
        return new File(dirFile, fileName);
    }

    private static synchronized boolean savaParameterInFile(String key, String value, boolean isPrivate, String pkg) {
        File file=getFile(key + FILE_TYPE_NAME, isPrivate, pkg);
        if(file == null) {
            return false;
        }
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch(IOException e) {
                return false;
            }
        }
        BufferedWriter writer=null;
        try {
            writer=new BufferedWriter(new FileWriter(file));
            writer.write(key + sSpace + value + "\n");
        } catch(Exception e) {
            return false;
        } finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch(IOException e) {
                }
            }
        }
        return true;
    }

    private static void initFromFile(Context context, boolean isPrivate) {
        PreferenceUtil pu=PreferenceUtil.getInstance(context);
        File file=getDirFile(isPrivate, context.getPackageName());
        if(file == null || !file.exists()) {
            return;
        }
        File[] fils=file.listFiles();
        if(fils == null) {
            return;
        }
        for(File f: fils) {
            String path=f.getAbsolutePath();
            if(path.endsWith(FILE_TYPE_NAME)) {
                FileReader fr=null;
                BufferedReader in=null;
                try {
                    fr=new FileReader(f);
                    in=new BufferedReader(fr);
                    while(in.ready()) {
                        String s=in.readLine();
                        String[] arr=s.split(sSpace);
                        pu.saveString(arr[0], arr[1]);
                    }
                } catch(Exception e) {
                } finally {
                    if(in != null) {
                        try {
                            in.close();
                        } catch(IOException e) {
                        }
                    }
                    if(fr != null) {
                        try {
                            fr.close();
                        } catch(IOException e) {
                        }
                    }
                }
            }
        }
    }
}
