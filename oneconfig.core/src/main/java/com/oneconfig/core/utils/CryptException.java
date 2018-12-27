package com.oneconfig.core.utils;

public class CryptException extends RuntimeException {

    private static final long serialVersionUID = 01L; // to prevent possible InvalidClassException, see docs on java.io.Serializable

    public CryptException(String message) {
        super(message);
    }

    public CryptException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
