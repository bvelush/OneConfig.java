package com.oneconfig.core;

public class Const {
    public static final int MAX_KEY_PARTS = 10; // max number of key parts: k1.k2.k3...
    public static final int MAX_KEY_LENGTH = 255; // max length of the full key: $storename.level1.level2...
    public static final int MAX_KEY_PART_LENGHT = 32; // max lenght of any part of the key (separated by dots)

    public static final String DEFAULT_INIT_SECTION = "INIT";
    public static final String DEFAULT_STORE_ROOT = "CONFIG_ROOT";
    public static final String DEFAULT_SENSOR_VALUE = "DEFAULT";

    public static final String INIT_STORES_SECTION = "stores";
    public static final String INIT_SENSORS_SECTION = "sensors";

}
