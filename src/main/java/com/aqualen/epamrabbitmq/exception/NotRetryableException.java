package com.aqualen.epamrabbitmq.exception;

public class NotRetryableException extends RuntimeException {
    public NotRetryableException(Throwable cause) {
        super(cause);
    }

    public NotRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
