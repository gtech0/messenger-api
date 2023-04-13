package com.labs.java_lab1.friends.exception;

public class FriendNotFoundException extends RuntimeException {
    public FriendNotFoundException(String message) {
        super(message);
    }
}
