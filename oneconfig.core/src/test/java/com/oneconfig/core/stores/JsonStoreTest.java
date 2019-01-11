package com.oneconfig.core.stores;

import static org.junit.Assert.assertEquals;

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
}
