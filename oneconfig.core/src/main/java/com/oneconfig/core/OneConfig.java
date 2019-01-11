package com.oneconfig.core;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.oneconfig.core.sensors.ISensor;
import com.oneconfig.core.stores.IStore;
import com.oneconfig.core.stores.JsonStore;
import com.oneconfig.core.stores.StoreResult;
import com.oneconfig.utils.common.Json;
import com.oneconfig.utils.common.ResourceLoader;
import com.oneconfig.utils.common.Str;

public class OneConfig {
    private Map<String, IStore> stores;
    private Map<String, ISensor> sensors;

    // region exceptionWhenWrongKey
    private boolean exceptionWhenWrongKey = true;

    public boolean getExceptionWhenKeyNotFound() {
        return exceptionWhenWrongKey;
    }

    public void setExceptionWhenKeyNotFound(boolean value) {
        exceptionWhenWrongKey = value;
    }
    // endregion exceptionWhenWrongKey

    // region allowDefaultSensorValue
    private boolean allowDefaultSensorValue;

    public boolean getAllowDefaultSensorValue() {
        return allowDefaultSensorValue;
    }

    public void setAllowDefaultSensorValue(boolean value) {
        allowDefaultSensorValue = value;
    }
    // endregion allowDefaultSensorValue

    public OneConfig() {
        try {
            String rawCfg = ResourceLoader.getResourceAsString("config.json", OneConfig.class);
            init(rawCfg);
        } catch (Exception ex) {
            throw new OneConfigException("Can't process 'config.json'", ex);
        }
    }

    public OneConfig(String jsonConfig) {
        try {
            init(jsonConfig);
        } catch (Exception ex) {
            throw new OneConfigException("Can't process json string", ex);
        }
    }

    public OneConfig(Path configFilePath, Charset charset) {
        try {
            String rawCfg = new String(Files.readAllBytes(configFilePath), charset);
            init(rawCfg);
        } catch (Exception ex) {
            throw new OneConfigException("Can't process json string", ex);
        }
    }

    private void init(String rawCfg) throws Exception {
        stores = new HashMap<String, IStore>();
        JsonNode root = Json.parseJsonString(rawCfg);
        JsonNode initNode = root.get("init");

        if (initNode != null) {
            parseInit(initNode);
        }
        if (stores.get("") == null) { // if init section does not have default store config, initialize it here
            IStore defaultStore = new JsonStore();
            Map<String, String> configObject = new HashMap<String, String>();
            String contentRoot = Json.getMandatoryNode(root, Const.DEFAULT_STORE_ROOT).toString();
            configObject.put(JsonStore.JSON_STORE_CONTENTSTR, contentRoot);
            defaultStore.init("", configObject);
            stores.put("", defaultStore);
        }
    }

    private String exOrDefault(String msg, Object... values) {
        if (exceptionWhenWrongKey) {
            throw new OneConfigException(String.format(msg, values));
        } else {
            return "";
        }
    }

    private String exOrDefault(OneConfigException ex) {
        if (exceptionWhenWrongKey) {
            throw ex;
        } else {
            return "";
        }
    }

    public String get(String key) {
        if (key.length() > Const.MAX_KEY_LENGTH) {
            exOrDefault("The key '%s...' is too long. Max length allowed is '%d'", key.subSequence(0, 30), Const.MAX_KEY_LENGTH);
        }
        try {
            return internalGet(key);
        } catch (OneConfigException ex) {
            return exOrDefault(ex);
        } catch (Exception ex) {
            return exOrDefault("Can't get the value for the key '%s'. See the inner exception for details", ex);
        }
    }

    private String resolveStoreResult(StoreResult result) {
        if (result.isSensor()) {
            String sensorName = result.getSensorName();
            ISensor sensor = sensors.get(sensorName);
            if (sensor == null) { // sensor with the name sensorName is not registered
                throw new OneConfigException("Sensor '%s' is not found", sensorName);
            }
            String sensorValue = sensor.evaluate();
            String matchingValue = result.getSensorCollection().get(sensorValue);
            if (matchingValue == null) { // can't match the return of the sensor with the StoreResult collection of sensor values
                if (allowDefaultSensorValue) { // if can't match the sensor return, try the default value
                    matchingValue = Const.DEFAULT_SENSOR_VALUE;
                } else {
                    throw new OneConfigException("Can't match the value '%s' for the sensor '%s'", sensorValue, sensorName);
                }
            }
            return matchingValue;
        } else {
            return result.getStrValue();
        }
    }

    private String internalGet(String key) {
        String storeName = ""; // by default, using the default store (it's name is "")
        if (key.startsWith("$")) { // the store is the first element of the key
            storeName = Str.head(key).substring(1, Const.MAX_KEY_PART_LENGHT);
        }

        // Matcher matcher = Str.RX_CONFIGKEY.matcher(key);
        // if (!matcher.matches()) {
        // exOrDefault("Key '%s' is in wrong format", key);
        // }

        // String storeName = matcher.group("store");
        // if (storeName == null) {
        // storeName = ""; // if no store name provided in the key, then it is default store
        // }
        IStore store = stores.get(storeName);
        if (store == null) {
            exOrDefault("Can't find store '%s' from key '%s'", storeName, key);
        }

        StoreResult storeResult = store.resolvePath(Str.tail(key));
        String unexpandedReturn = resolveStoreResult(storeResult);

        // TODO: parse sensors, then recursive RX_INLINE matching
        return unexpandedReturn; // storeResult.getStrValue();
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
        try {
            String storeType = Json.getMandatoryString(storeInit, "type");

            Class<?> storeClass = Class.forName(storeType);
            IStore store = (IStore) storeClass.newInstance();

            Map<String, String> configObject = new HashMap<String, String>();
            Iterator<String> fieldNames = storeInit.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                configObject.put(fieldName, Json.getMandatoryString(storeInit, fieldName));
            }

            store.init(name, configObject);

        } catch (Exception ex) {
            throw new OneConfigException(String.format("Can't initialize the store '%s'", name), ex);
        }
    }


}
