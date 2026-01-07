package com.twowheeler.user_service.customException;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends RuntimeException {

    private final HttpStatus status = HttpStatus.NOT_FOUND;

    public UserNotFoundException(String message) {
        super(message);
    }

    public HttpStatus getStatus() {
        return status;
    }
}