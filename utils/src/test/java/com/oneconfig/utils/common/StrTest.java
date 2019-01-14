package com.oneconfig.utils.common;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class StrTest {
    @Test
    public void testHead() {
        Map<String, String> testCases = new HashMap<String, String>() {
            {
                put("", "");
                put("head.tail", "head");
                put("head.tail.tail", "head");
                put(".tail.tail", "");
            }
        };

        for (String input : testCases.keySet()) {

            String actualResult = Str.head(input);
            String expectedResult = testCases.get(input);
            assertEquals(String.format("'%s' => '%s'", input, expectedResult), expectedResult, actualResult);
        }
    }

    @Test
    public void testTail() {
        Map<String, String> testCases = new HashMap<String, String>() {
            {
                put("", "");
                put("head.tail", "tail");
                put("head.tail.tail", "tail.tail");
                put(".tail.tail", "tail.tail");
                put(".", "");
            }
        };

        for (String input : testCases.keySet()) {

            String actualResult = Str.tail(input);
            String expectedResult = testCases.get(input);
            assertEquals(String.format("'%s' => '%s'", input, expectedResult), expectedResult, actualResult);
        }
    }

    @Test
    public void testRxInlineMatcher() {
        // String result = Str.replacePattern(Pattern.compile("(?<number>[0-9]+)"), "some 123 are 845 and some are not", (match)
        // -> {
        // return String.format("!%s!", match.group("number"));
        // });

        Map<String, String> testCases = new HashMap<String, String>() {
            {
                put("", "");
                put("not replace", "not replace");
                put("{{{replace}}}", "replace");
                put("{{{replace}}}{{{replace}}}", "replacereplace");
                put("{{{replace}}}-{{{replace.a.b}}}", "replace-replace.a.b");
                put("{{{replace1}}}\n{{{replace2}}}", "replace1\nreplace2");
                put("{{{replace}}}\r{{{replace}}}", "replace\rreplace");
                put("{{{replace1}}}\n\r{{{replace2}}}", "replace1\n\rreplace2");
                put("{{{replace.a.b.c}}}\r\n{{{$s.replace.d.e.f}}}", "replace.a.b.c\r\n$s.replace.d.e.f");
                put("\r\n{{{a.b.c}}}\n\r", "\r\na.b.c\n\r");
                put("some text {{{$store.replace.a.b.c}}} some \n\r text", "some text $store.replace.a.b.c some \n\r text");
                put("some {{{$s.a.b.c}}} text {{{$ss.d.e.f}}} other", "some $s.a.b.c text $ss.d.e.f other");
            }
        };

        for (String input : testCases.keySet()) {
            String actualResult = Str.replacePattern(Str.RX_INLINE_CONFIGKEY, input, (match) -> match.group("fullKey"));
            String expectedResult = testCases.get(input);
            assertEquals(String.format("'%s' => '%s'", input, expectedResult), expectedResult, actualResult);
        }
    }
}
