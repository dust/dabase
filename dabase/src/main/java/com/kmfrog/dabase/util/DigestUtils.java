package com.kmfrog.dabase.util;

import java.security.MessageDigest;

import com.kmfrog.dabase.DLog;

public abstract class DigestUtils {

    private static MessageDigest md5;

    private static Object md5Lock=new Object();

    private DigestUtils() {
    }

    public static String getMd5Hex(String input) {
        byte[] digest=null;
        try {
            synchronized(md5Lock) {
                if(md5 == null) {
                    md5=MessageDigest.getInstance("MD5");
                }
                digest=md5.digest(input.getBytes("UTF-8"));
            }
            StringBuilder hex=new StringBuilder();
            for(int i=0; i < digest.length; i++) {
                int v=(digest[i] & 0xFF);
                if(v < 0x10) {
                    hex.append('0');
                }
                hex.append(Integer.toHexString(v));
            }
            return hex.toString();
        } catch(Exception ex) {
            if(DLog.DEBUG) {
                DLog.e(ex.getMessage(), ex);
            }
        }
        return null;
    }

}
