package com.labs.java_lab1.user.exception;

public class UniqueConstraintViolationException extends RuntimeException {
    public UniqueConstraintViolationException (String message) {
        super(message);
    }
}
