package com.oneconfig.utils.common;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Str {
    // full key consists of store, path and sensor. Only path in mandatory, store and sensor are not.
    // store example: $store.
    // path example: key1.key2.key3 or key
    // sensor example: ?sensor

    private static final String STR_CONFIGKEY = "(?<fullKey>(\\$(?<store>\\w+)\\.)?(?<path>\\w+(\\.\\w+)*)+)";
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

    /**
     * For each occurence of the @pattern in the @input string, 'transformMatch' function will be called, and found pattern
     * will be replaced with the function return. Function 'transformMatch' takes the parameter Matcher for the respective
     * pattern, and returns the string that will be used to replace the found pattern. replacePattern works until all
     * occurences of pattern will be replaced with return of transformMatch.
     *
     * Example: Pattern: '(?<number>[0-9]+)' Input: 'some 123 are 845 and some are not'.
     *
     * String result = Str.replacePattern("(?<number>[0-9]+)", "some 123 are 845 and some are not", (match) ->
     * match.group("number"))
     *
     * @param pattern        Pattern to be found. Supports groups, named groups and other advanced features of Regular
     *                       Expressions
     * @param input          input string
     * @param transformMatch function with signature 'String transformMatch(Matcher match)' that transforms the found match
     *                       to the string that will replace the occurence of the pattern
     * @return input with all occurences of pattern replaced with the return of transformMatch for each occurence
     */
    public static String replacePattern(Pattern pattern, String input, Function<Matcher, String> transformMatch) {
        StringBuffer result = new StringBuffer();

        Matcher m = pattern.matcher(input);
        while (m.find()) {
            String transformedMatch = transformMatch.apply(m);
            m.appendReplacement(result, Matcher.quoteReplacement(transformedMatch)); // .quoteReplacement processes '\' and '$' that have special
                                                                                     // meaning for any replace methods, including .appendReplacement
        }
        m.appendTail(result);

        return result.toString();
    }
}
