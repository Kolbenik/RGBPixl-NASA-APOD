package de.kolbenik.exceptions;

public class SingletonViolationException extends RuntimeException {
    public SingletonViolationException(String message) {
        super(message);
    }

    public SingletonViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SingletonViolationException(Throwable cause) {
        super(cause);
    }
}