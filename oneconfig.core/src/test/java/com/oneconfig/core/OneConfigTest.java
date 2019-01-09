package com.oneconfig.core;

import com.oneconfig.utils.common.ResourceLoader;

import org.junit.Test;

public class OneConfigTest {
    @Test
    public void testOc() {
        // Security.addProvider(new BouncyCastleProvider());
        OneConfig cfg = new OneConfig(ResourceLoader.getResourceAsString("TestStore/1.json", OneConfigTest.class));

        // cfg.get("web.db.server");

    }
}
