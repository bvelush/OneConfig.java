package com.oneconfig.core;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.oneconfig.core.sensors.ISensor;
import com.oneconfig.core.stores.IStore;
import com.oneconfig.core.stores.JsonStore;
import com.oneconfig.core.stores.StoreResult;
import com.oneconfig.utils.common.Json;
import com.oneconfig.utils.common.ResourceLoader;
import com.oneconfig.utils.common.Str;

public class OneConfig {
    private Map<String, IStore> stores = new HashMap<String, IStore>();
    private Map<String, ISensor> sensors = new HashMap<String, ISensor>();

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

    // region Constructors
    public OneConfig() {
        try {
            String rawCfg = ResourceLoader.getResourceAsString("config.json", OneConfig.class);
            init(rawCfg);
        }
        catch (Exception ex) {
            throw new OneConfigException("Can't process 'config.json'", ex);
        }
    }

    public OneConfig(String jsonConfig) {
        try {
            init(jsonConfig);
        }
        catch (Exception ex) {
            throw new OneConfigException("Can't process json string", ex);
        }
    }

    public OneConfig(Path configFilePath, Charset charset) {
        try {
            String rawCfg = new String(Files.readAllBytes(configFilePath), charset);
            init(rawCfg);
        }
        catch (Exception ex) {
            throw new OneConfigException("Can't process json string", ex);
        }
    }
    // endregion Constructors

    public Set<String> getSensorNames() {
        return sensors.keySet();
    }

    public String evaluateSensor(String sensorName) {
        try {
            return sensors.get(sensorName).evaluate();
        }
        catch (Exception ex) {
            throw new OneConfigException("Can't evaluate sensor '%s'. Make sure that sensor with this name is declared in the config", ex);
        }
    }

    public String get(String key) {
        if (key.length() > Const.MAX_KEY_LENGTH) {
            exOrDefault("The key '%s...' is too long. Max length allowed is '%d'", key.substring(0, 30), Const.MAX_KEY_LENGTH);
        }
        try {
            return internalGet(key, 1, key);
        }
        catch (OneConfigException ex) {
            return exOrDefault(ex);
        }
        catch (Exception ex) {
            return exOrDefault("Can't get the value for the key '%s'. See the inner exception for details", ex);
        }
    }

    private String internalGet(String key, int level, String origKey) {
        if (level > Const.MAX_KEY_LEVELS) {
            throw new OneConfigException("Too many nested levels while resolving the key '%s'", origKey);
        }
        String storeName = ""; // by default, using the default store (it's name is "")
        String path = key; // assuming that storeName is not specified
        if (key.startsWith("$")) { // key starting with $ will be understood as a store name
            storeName = Str.head(key).substring(1); // removing leading '$'
            path = Str.tail(key);
        }

        IStore store = stores.get(storeName);
        if (store == null) {
            exOrDefault("Can't find store '%s' from key '%s'", storeName, key);
        }

        StoreResult storeResult = store.resolvePath(path);
        String unexpandedReturn = resolveStoreResult(storeResult); // return could still contain other keys to be replaced

        String expandedReturn = Str.replacePattern(
            Str.RX_INLINE_CONFIGKEY,
            unexpandedReturn,
            (match) -> internalGet(match.group("fullKey"), level + 1, origKey)
        );

        return expandedReturn;
    }

    // region ----------- INIT ------------
    private void init(String rawCfg) throws Exception {
        JsonNode root = Json.parseJsonString(rawCfg);
        JsonNode initNode = root.get(Const.DEFAULT_INIT_SECTION);

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

    private void parseInit(JsonNode initNode) {
        // stores init
        JsonNode storesNode = initNode.get(Const.INIT_STORES_SECTION);
        if (storesNode != null) {
            Iterator<String> storeNames = storesNode.fieldNames();
            while (storeNames.hasNext()) {
                String storeName = storeNames.next();
                IStore store = initDynamicJsonClass(storeName, storesNode.get(storeName));
                stores.put(storeName, store);
            }
        }

        // sensors init
        JsonNode sensorsNode = initNode.get(Const.INIT_SENSORS_SECTION);
        if (sensorsNode != null) {
            Iterator<String> sensorNames = sensorsNode.fieldNames();
            while (sensorNames.hasNext()) {
                String sensorName = sensorNames.next();
                ISensor sensor = initDynamicJsonClass(sensorName, sensorsNode.get(sensorName));
                sensors.put(sensorName, sensor);
            }
        }

        // ... another init parameters, like cache config go here
    }

    private <T extends IInit> T initDynamicJsonClass(String name, JsonNode storeInit) {
        try {
            String dynClassName = Json.getMandatoryString(storeInit, "type");

            Class<?> dynClass = Class.forName(dynClassName);
            T instance = (T) dynClass.newInstance();

            Map<String, String> configObject = new HashMap<String, String>();
            Iterator<String> fieldNames = storeInit.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                configObject.put(fieldName, Json.getMandatoryString(storeInit, fieldName));
            }

            instance.init(name, configObject);
            return instance;
        }
        catch (Exception ex) {
            throw new OneConfigException(String.format("Can't initialize the class '%s'", name), ex);
        }
    }
    // endregion ----------- INIT ------------

    private String exOrDefault(String msg, Object... values) {
        if (exceptionWhenWrongKey) {
            throw new OneConfigException(String.format(msg, values));
        } else {
            return null;
        }
    }

    private String exOrDefault(OneConfigException ex) {
        if (exceptionWhenWrongKey) {
            throw ex;
        } else {
            return null;
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
}
