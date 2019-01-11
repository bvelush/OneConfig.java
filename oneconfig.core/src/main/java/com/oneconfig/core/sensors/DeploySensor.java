package com.oneconfig.core.sensors;

import java.util.Map;

import com.oneconfig.core.OneConfigException;

import org.apache.commons.lang3.StringUtils;

public class DeploySensor implements ISensor {
    private String name;
    private String envvar;

    public void init(String name, Map<String, String> configObject) {
        this.name = name;
        this.envvar = configObject.get("envvar");
        if (this.envvar == null) {
            throw new OneConfigException("DeploySensor requires 'envvar' to be defined in its configuration");
        }
    }

    public String getName() {
        return name;
    }

    public String evaluate() {
        String retVal = System.getenv(envvar);
        if (StringUtils.isEmpty(retVal)) {
            throw new OneConfigException("Environment variable '%s' is not defined", envvar);
        }
        return retVal;
    }
}
