package com.blazemeter.api.exception;

public class InterruptRuntimeException extends RuntimeException{

    public InterruptRuntimeException() {
    }

    public InterruptRuntimeException(String message) {
        super(message);
    }

    public InterruptRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
