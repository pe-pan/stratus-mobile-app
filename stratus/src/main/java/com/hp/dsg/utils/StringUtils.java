package com.hp.dsg.utils;

/**
 * Created by panuska on 7.1.2015.
 */
public class StringUtils {
    public static String emptifyNullString(String s) {
        return s == null ? "" : s;
    }

    public static String trimToEmpty(String s) { return s == null ? "" : s.trim(); }

    public static String nullifyNullObject(Object s) {
        return s == null ? "null" : s.toString();
    }

    public static String toJsonString(Object o) {
        if (o instanceof String) {
            return "\""+o.toString()+"\"";
        } else {
            return o.toString();
        }
    }
}
