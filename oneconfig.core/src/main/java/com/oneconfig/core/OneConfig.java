package com.oneconfig.core;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneconfig.core.stores.EncJsonStore;
import com.oneconfig.core.stores.IStore;
import com.oneconfig.core.stores.JsonStore;
import com.oneconfig.utils.common.ResourceLoader;

public class OneConfig {
    private Map<String, IStore> stores;

    public OneConfig() {
        try {
            String rawCfg = ResourceLoader.getResourceAsString("config.json", OneConfig.class);

            init(rawCfg);
        } catch (IOException ex) {
            throw new OneConfigException("Can't process 'config.json'", ex);
        }
    }

    public OneConfig(String jsonConfig) {
        try {
            init(jsonConfig);
        } catch (IOException ex) {
            throw new OneConfigException("Can't process json string", ex);
        }
    }

    public OneConfig(Path configFilePath, Charset charset) {
        try {
            String rawCfg = new String(Files.readAllBytes(configFilePath), charset);
            init(rawCfg);
        } catch (IOException ex) {
            throw new OneConfigException("Can't process json string", ex);
        }
    }

    private void init(String rawCfg) throws IOException {
        stores = new HashMap<String, IStore>();
        ObjectMapper om = new ObjectMapper();
        JsonNode root = om.readTree(rawCfg);
        JsonNode initNode = root.get("init");

        parseInit(initNode);
        if (stores.get("") == null) { // if init section does not have default store config, initialize it here
            IStore defaultStore = new JsonStore();
            defaultStore.init("", root);
            stores.put("", defaultStore);

            // this should be taken care in parseInit, but now taking a shortcut...
            IStore secretStore = new EncJsonStore();
            secretStore.init("safe", "MasterKey.p12");
            stores.put("safe", secretStore);
        }
    }

    public String get(String key) {
        IStore store = null;
        String storeName = "";
        int storePos = -1;
        if (key.charAt(0) == '$') { // key starts with store ID
            storePos = key.indexOf('.', 1);
            if (storePos == -1) {
                throw new OneConfigException("Store Name is not valid after '$' in the key: " + key);
            }
            storeName = key.substring(1, storePos - 1);
        }
        store = stores.get(storeName);
        if (store == null) {
            throw new OneConfigException(String.format("Store with name '%s' is not registered", storeName));
        }
        return store.resolveKey(key.substring(storePos + 1));
    }

    private void parseInit(JsonNode initNode) {
        parseStores(initNode.get("stores"));
        // ... another init parameters, like cache config goes here
    }

    private void parseStores(JsonNode stores) {
        Iterator<String> fields = stores.fieldNames();
        while (fields.hasNext()) {
            String name = fields.next();
            initStore(name, stores.get(name));
        }
    }

    private void initStore(String name, JsonNode storeInit) {
        System.out.println("Initializing store " + name);
    }
}
