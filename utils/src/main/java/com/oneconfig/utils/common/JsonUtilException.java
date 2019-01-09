package com.oneconfig.utils.common;

public class JsonUtilException extends RuntimeException {
    private static final long serialVersionUID = 01L; // to prevent possible InvalidClassException, see docs on java.io.Serializable

    public JsonUtilException(String message) {
        super(message);
    }

    public JsonUtilException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
