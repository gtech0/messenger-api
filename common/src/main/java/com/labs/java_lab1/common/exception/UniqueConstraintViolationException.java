package com.labs.java_lab1.common.exception;

public class UniqueConstraintViolationException extends RuntimeException {
    public UniqueConstraintViolationException (String message) {
        super(message);
    }
}
