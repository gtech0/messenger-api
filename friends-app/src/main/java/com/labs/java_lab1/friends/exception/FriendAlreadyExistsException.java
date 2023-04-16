package com.labs.java_lab1.friends.exception;

public class FriendAlreadyExistsException extends RuntimeException {
    public FriendAlreadyExistsException(String message) {
        super(message);
    }
}
