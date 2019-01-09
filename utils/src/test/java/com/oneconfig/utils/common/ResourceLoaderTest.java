package com.oneconfig.utils.common;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class ResourceLoaderTest {

    // https://stefanbirkner.github.io/system-rules/
    // https://stackoverflow.com/questions/8168884/how-to-test-code-dependent-on-environment-variables-using-junit
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void testRelativeRoot() {
        String resourceRelPath = "resourceloadertest1.txt";
        String content = ResourceLoader.getResourceAsString(resourceRelPath);
        content = content.trim();
        assertTrue(content.trim().equalsIgnoreCase(resourceRelPath));
    }

    @Test
    public void testRelativeNested() {
        String resourceRelPath = "test1/test2/resourceloadertest2.txt";
        String content = ResourceLoader.getResourceAsString(resourceRelPath);
        assertTrue(content.trim().equalsIgnoreCase(resourceRelPath));
    }

    @Test
    public void testAbsolutePath() throws Exception {
        String resourceRelPath = "resourceloadertest1.txt";
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceRelPath);
        File f = new File(url.toURI());
        String resourcePath = f.getAbsolutePath();

        String content = ResourceLoader.getResourceAsString(resourcePath); // testing the absolute path (resourcePath), not the relative
        content = content.trim();
        assertTrue(content.trim().equalsIgnoreCase(resourceRelPath));
    }

    @Test
    public void testEnvPath() throws Exception {
        String resourceRelPath = "resourceloadertest1.txt";
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceRelPath);
        File f = new File(url.toURI());
        String resourcePath = f.getAbsolutePath();
        environmentVariables.set("RESOURCE_PATH", resourcePath);

        String content = ResourceLoader.getResourceAsString("env:RESOURCE_PATH");
        content = content.trim();
        assertTrue(content.trim().equalsIgnoreCase(resourceRelPath));
    }
}
