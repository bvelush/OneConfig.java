package com.oneconfig.core;

public class OneConfigException extends RuntimeException {

    private static final long serialVersionUID = 01L; // to prevent possible InvalidClassException, see docs on java.io.Serializable

    public OneConfigException(String message) {
        super(message);
    }

    public OneConfigException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
