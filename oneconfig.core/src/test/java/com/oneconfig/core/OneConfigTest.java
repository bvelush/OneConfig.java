package com.oneconfig.core;

import static org.junit.Assert.assertEquals;

import com.oneconfig.utils.common.ResourceLoader;

import org.junit.Test;

public class OneConfigTest {
    @Test
    public void testSmokeTest() {
        OneConfig cfg = new OneConfig(ResourceLoader.getResourceAsString("TestStore/1.json"));
        String result;

        result = cfg.get("myapp.deployment");
        assertEquals("--TEST--", result);
        result = cfg.get("$vault.service1.sec1");
        assertEquals("--sec1--", result);
    }

    @Test
    public void testRecursiveReplaceSimple() {
        OneConfig cfg = new OneConfig(ResourceLoader.getResourceAsString("TestStore/1.json"));
        String result = cfg.get("db.pwd");

        assertEquals("--sec2--", result);
    }

    @Test
    public void testRecursiveReplaceFull() {
        OneConfig cfg = new OneConfig(ResourceLoader.getResourceAsString("TestStore/1.json"));
        String result = cfg.get("db.connectionString");

        assertEquals("server=--sec1--;vip=172.11.12.13;transactionSupport=true;pwd=--sec2--;OneConfig=is_cool;--root_sec1--", result);
    }

}
