package com.authentication.errorhandling;

public class UserExistException extends RuntimeException {
    public UserExistException(String message) {
        super(message);
    }
}
