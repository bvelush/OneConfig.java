package com.oneconfig.core;

public class Const {
    public static final int MAX_KEY_LEVELS = 10; // max number of key parts: k1.k2.k3...
    public static final int MAX_KEY_LENGTH = 255; // max length of the full key: $storename.level1.level2...
    public static final int MAX_KEY_PART_LENGHT = 32; // max lenght of any part of the key (separated by dots)

    public static final String DEFAULT_INIT_SECTION = "INIT"; // init section of the default config file
    public static final String DEFAULT_STORE_ROOT = "CONFIG_ROOT"; // config section of the default config file
    public static final String DEFAULT_SENSOR_VALUE = "DEFAULT"; // in case default sensor value is enabled, this is the default section of the sensor
                                                                 // declaration

    public static final String INIT_STORES_SECTION = "stores"; // stores declaration in the init section
    public static final String INIT_SENSORS_SECTION = "sensors"; // sensors declaration in the init section

}
