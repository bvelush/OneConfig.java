package com.oneconfig.core.sensors;

import java.util.Map;

import com.oneconfig.core.OneConfigException;

public class TestSensor implements ISensor {
    private String name;
    private String returnValue;

    public void init(String name, Map<String, String> configObject) {
        this.name = name;
        this.returnValue = configObject.get("retval");
        if (returnValue == null) {
            throw new OneConfigException("TestSensor requires 'retval' in its configuration");
        }
    }

    public String getName() {
        return name;
    }

    public String evaluate() {
        return returnValue;
    }
}
