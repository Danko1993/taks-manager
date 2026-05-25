package com.example.taskmanager.exception;

public class AccountNotVerifiedException extends RuntimeException {
    public AccountNotVerifiedException() {
        super("Account not verified");
    }
}
