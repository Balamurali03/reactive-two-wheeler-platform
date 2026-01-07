package com.twowheeler.user_service.customException;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends RuntimeException {

    private final HttpStatus status = HttpStatus.CONFLICT;

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public HttpStatus getStatus() {
        return status;
    }
}