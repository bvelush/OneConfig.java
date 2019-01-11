package com.oneconfig.utils.common;

import java.util.regex.Pattern;

public class Str {
    // full key consists of store, path and sensor. Only path in mandatory, store and sensor are not.
    // store example: $store.
    // path example: key1.key2.key3 or key
    // sensor example: ?sensor

    private static final String STR_CONFIGKEY = "(?<fullKey>(\\$(?<store>\\w+)\\.)?(?<path>\\w+(\\.\\w+)*)+(\\?(?<sensor>\\w+))?)";
    private static final String STR_INLINE_CONFIGKEY = "(\\{\\{\\{){1}" + STR_CONFIGKEY + "(\\}\\}\\}){1}";

    public static final Pattern RX_CONFIGKEY = Pattern.compile(STR_CONFIGKEY);
    public static final Pattern RX_INLINE_CONFIGKEY = Pattern.compile(STR_INLINE_CONFIGKEY);


    public static String head(String str) {
        int dot = str.indexOf('.');
        if (dot < 0) return str;
        return str.substring(0, dot);
    }

    public static String tail(String str) {
        int dot = str.indexOf('.');
        if (dot < 0) return "";
        return str.substring(dot + 1, str.length());
    }
}
