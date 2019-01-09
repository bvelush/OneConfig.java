package com.oneconfig.core.stores;

import java.util.Map;

// store implementation must have no constructors (or empty default parameterless constructor)
// initialization of the class must be performed through 'init' call
public interface IStore {
    void init(String name, Map<String, String> configObject);

    String getName();

    String resolveKey(String key);
}
