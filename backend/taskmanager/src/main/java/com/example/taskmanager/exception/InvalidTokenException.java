package com.example.taskmanager.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("Inwalid or expired token");
    }
}
