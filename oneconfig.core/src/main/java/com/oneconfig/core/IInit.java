package com.oneconfig.core;

import java.util.Map;

/**
 * Core interface of OneConfig. Both Sensors and Stores are inherited from it.
 *
 */
public interface IInit {
    void init(String name, Map<String, String> configObject);
}
