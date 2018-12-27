package com.huawei.cloud.oneconfig.stores;

public interface IStore {
    void init(String name, Object configObject);

    String getName();

    String resolveKey(String key);
}
