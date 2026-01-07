package com.twowheeler.user_service.customException;

import org.springframework.http.HttpStatus;

public class BadRequestException extends RuntimeException {

    private final HttpStatus status = HttpStatus.BAD_REQUEST;

    public BadRequestException(String message) {
        super(message);
    }

    public HttpStatus getStatus() {
        return status;
    }
}