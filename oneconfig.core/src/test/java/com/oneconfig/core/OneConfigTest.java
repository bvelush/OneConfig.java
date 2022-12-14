package com.oneconfig.core;

import static org.junit.Assert.assertEquals;

import com.oneconfig.utils.common.ResourceLoader;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class OneConfigTest {

    @Before
    public void setEnvVar() {
        environmentVariables.set(
            "ONECFG_DEPLKEYPATH",
            ResourceLoader.urlToAbsolutePath(ResourceLoader.getResource("CertStores/DeploymentKeyStore.p12"))
        );

        environmentVariables.set("ONECFG_STOREPATH", ResourceLoader.urlToAbsolutePath(ResourceLoader.getResource("TestStores/testsecrets.jss")));

        environmentVariables.set("ONECFG_DEPLOYMENT", "TEST");
    }

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void testSmokeTest() throws Exception {
        OneConfig cfg = new OneConfig(ResourceLoader.getResourceAsString("TestStores/config.json"));
        String result;

        result = cfg.get("myapp.deployment");
        assertEquals("--TEST--", result);
        result = cfg.get("$vault.service1.sec1");
        assertEquals("--sec1--", result);
    }

    @Test
    public void testRecursiveReplaceSimple() {
        OneConfig cfg = new OneConfig(ResourceLoader.getResourceAsString("TestStores/config.json"));
        String result = cfg.get("db.pwd");

        assertEquals("--sec2--", result);
    }

    @Test
    public void testRecursiveReplaceFull() {
        OneConfig cfg = new OneConfig(ResourceLoader.getResourceAsString("TestStores/config.json"));
        String result = cfg.get("db.connectionString");

        assertEquals("server=--sec1--;vip=172.11.12.13;transactionSupport=true;pwd=--sec2--;OneConfig=is_cool;--root_sec1--", result);
    }
}
