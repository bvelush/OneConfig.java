package com.oneconfig.core.stores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class JsonStoreTest {
    //@formatter:off
    private static final String JS_SIMPLETRAVERSE =
        "{                                                                      \n" +
            "'l1val': '--l1val--',                                              \n" +
            "'l1': {                                                            \n" +
                "'l2val': '--l1.l2val--',                                       \n" +
                "'l2': {                                                        \n" +
                    "'l3val': '--l1.l2.l3val--',                                \n" +
                    "'l3': {                                                    \n" +
                        "'l4val': '--l1.l2.l3.l4val--'                          \n" +
                    "}                                                          \n" +
                "}                                                              \n" +
            "}                                                                  \n" +
        "}                                                                      \n";
    //@formatter:on

    //@formatter:off
    private static final String JS_SENSORS =
        "{                                                                      \n" +
            "'l1val': {                                                         \n" +
                "'?': 'Sensor1',                                                \n" +
                "'A': '--A--',                                                  \n" +
                "'B': '--B--',                                                  \n" +
                "'C': '--C--',                                                  \n" +
                "'DEFAULT': '--DEFAULT--'                                       \n" +
            "},                                                                 \n" +
            "'l1': {                                                            \n" +
                "'l2val': {                                                     \n" +
                    "'?': 'Sensor2',                                            \n" +
                    "'1': '--A--',                                              \n" +
                    "'2': '--B--',                                              \n" +
                    "'3': '--C--'                                               \n" +
                "}                                                              \n" +
            "}                                                                  \n" +
        "}                                                                      \n";
    //@formatter:on

    @Test
    public void testSimpleTraverse() {
        JsonStore jstore = new JsonStore();
        Map<String, String> configObj = new HashMap<String, String>();
        configObj.put(JsonStore.JSON_STORE_CONTENTSTR, JS_SIMPLETRAVERSE.replaceAll("\'", "\""));
        jstore.init("", configObj);

        String[] paths = {"l1val", "l1.l2val", "l1.l2.l3val", "l1.l2.l3.l4val"};

        StoreResult result;
        for (String path : paths) {
            result = jstore.resolvePath(path);
            assertEquals(String.format("--%s--", path), result.getStrValue());
        }
    }

    @Test
    public void testReturnSensor() {
        JsonStore jstore = new JsonStore();
        Map<String, String> configObj = new HashMap<String, String>();
        configObj.put(JsonStore.JSON_STORE_CONTENTSTR, JS_SENSORS.replaceAll("\'", "\""));
        jstore.init("name", configObj);

        StoreResult result;

        result = jstore.resolvePath("l1val");
        assertTrue(result.isSensor());
        assertEquals("Sensor1", result.getSensorCollection().get("?"));
        assertTrue(result.getSensorCollection().size() == 5);

        result = jstore.resolvePath("l1.l2val");
        assertTrue(result.isSensor());
        assertEquals("Sensor2", result.getSensorCollection().get("?"));
        assertTrue(result.getSensorCollection().size() == 4);
    }
}
