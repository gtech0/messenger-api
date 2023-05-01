package com.labs.java_lab1.chat.exception;

public class ChatUserNotFoundException extends RuntimeException {
    public ChatUserNotFoundException(String message) {
        super(message);
    }
}
