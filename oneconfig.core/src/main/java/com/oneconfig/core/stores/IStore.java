package com.oneconfig.core.stores;

public interface IStore {
    void init(String name, Object configObject);

    String getName();

    String resolveKey(String key);
}
