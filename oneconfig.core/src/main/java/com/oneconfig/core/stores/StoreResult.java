package com.oneconfig.core.stores;

import java.util.Map;

public class StoreResult {
    private boolean isSensor;

    private String strValue;

    private Map<String, String> sensorValue;

    public boolean isSensor() {
        return isSensor;
    }

    public StoreResult(String value) {
        isSensor = false;
        strValue = value;
    }

    public StoreResult(Map<String, String> value) {
        isSensor = true;
        sensorValue = value;
    }

    public String getStrValue() {
        if (isSensor()) {
            throw new IllegalStateException("StoreResult is Sensor, but String value is requested");
        }
        return strValue;
    }

    public Map<String, String> getSensorValue() {
        if (!isSensor()) {
            throw new IllegalStateException("StoreResult is not Sensor, but Sensor value is requested");
        }
        return sensorValue;
    }
}
