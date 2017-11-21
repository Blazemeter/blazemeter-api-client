package com.blazemeter.api.exception;

public class UnexpectedResponseException extends RuntimeException {

    public UnexpectedResponseException() {
    }

    public UnexpectedResponseException(String message) {
        super(message);
    }

    public UnexpectedResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
