package com.oneconfig.core;

import static org.junit.Assert.assertEquals;

import com.oneconfig.utils.common.ResourceLoader;

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


}
