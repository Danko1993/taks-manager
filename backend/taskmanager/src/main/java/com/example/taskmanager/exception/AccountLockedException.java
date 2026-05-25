package com.example.taskmanager.exception;


public class AccountLockedException extends RuntimeException {
    public AccountLockedException() {
        super("Account is locked");
    }
}
