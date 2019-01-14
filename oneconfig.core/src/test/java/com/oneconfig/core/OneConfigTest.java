package com.oneconfig.core;

import static org.junit.Assert.assertEquals;

import java.util.regex.Matcher;

import com.oneconfig.utils.common.ResourceLoader;
import com.oneconfig.utils.common.Str;

import org.junit.Test;

public class OneConfigTest {
    @Test
    public void testSmokeTest() {
        OneConfig cfg = new OneConfig(ResourceLoader.getResourceAsString("TestStore/1.json", OneConfigTest.class));
        String result;

        result = cfg.get("myapp.deployment");
        assertEquals("--TEST--", result);
        result = cfg.get("$vault.service1.sec1");
        assertEquals("--sec1--", result);
    }

    @Test
    public void testRecursiveKeyResolution() {
        String input = "==={{{abc1}}}==={{{abc2}}}===";
        Matcher m = Str.RX_INLINE_CONFIGKEY.matcher(input);
        if (m.find()) {
            String result = m.replaceFirst(m.group("fullKey"));
            System.out.println(result);
        }
    }


}
