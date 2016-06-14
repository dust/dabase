package com.kmfrog.dabase.util;

import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.net.UrlQuerySanitizer.ParameterValuePair;
import android.net.UrlQuerySanitizer.ValueSanitizer;

public abstract class UriUtils {

    public static final String PARAM_PAGE_NO="pn";

    public static final String PARAM_PAGE_SIZE="ps";

    private static final UrlQuerySanitizer sUrlQueryParser=createUrlQueryParser();

    private UriUtils() {
    }

    /**
     * Creates a URL query parser by disabling all sanitizing features of a {@link android.net.UrlQuerySanitizer}.
     * <p>
     * {@link android.net.UrlQuerySanitizer} is the only API in the Android standard library that can enumerate the full set of query parameters
     * for a {@link android.net.Uri}.
     */
    private static UrlQuerySanitizer createUrlQueryParser() {
        UrlQuerySanitizer sanitizer=new UnicodeUrlQuerySanitizer();
        sanitizer.setAllowUnregisteredParamaters(true);
        ValueSanitizer valueSanitizer=UrlQuerySanitizer.getAllButNulLegal();
        sanitizer.setUnregisteredParameterValueSanitizer(valueSanitizer);
        return sanitizer;
    }

    public static Uri build(String scheme, String authority, String path) {
        Uri.Builder builder=new Uri.Builder();
        builder.scheme(scheme);
        builder.authority(authority);
        if(path != null) {
            builder.path(path);
        }
        return builder.build();
    }

    public static int getPageSize(Uri uri) {
        return getIntParam(uri, PARAM_PAGE_SIZE);
    }

    public static int getPageNo(Uri uri) {
        return getIntParam(uri, PARAM_PAGE_NO);
    }

    public static Uri setPageNo(Uri uri, int pageNo) {
        return replaceQueryParameter(uri, PARAM_PAGE_NO, String.valueOf(pageNo));
    }

    public static Uri replaceQueryParameter(Uri uri, String name, String value) {
        String url=uri.toString();
        Uri.Builder builder=uri.buildUpon();
        builder.query("");
        synchronized(sUrlQueryParser) {
            sUrlQueryParser.parseUrl(url);
            boolean exists=false;
            for(ParameterValuePair pair: sUrlQueryParser.getParameterList()) {
                if(!exists) {
                    exists=name.equals(pair.mParameter);
                }
                builder.appendQueryParameter(pair.mParameter, name.equals(pair.mParameter) ? value : pair.mValue);
            }
            if(!exists) {
                builder.appendQueryParameter(name, value);
            }
            return builder.build();
        }
    }

    public static int getIntParam(Uri uri, String paramName) {
        String ps=uri.getQueryParameter(paramName);
        if(ps == null || ps.length() == 0) {
            return 0;
        }
        return Integer.parseInt(ps);
    }

    private static class UnicodeUrlQuerySanitizer extends UrlQuerySanitizer {

        @Override
        public String unescape(String string) {
            // See: http://code.google.com/p/android/issues/detail?id=14437
            return Uri.decode(string);
        }
    }

}
