package com.twowheeler.auth_service.GlobalExceptionHandler;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.twowheeler.auth_service.CustomException.AccessDeniedException;
import com.twowheeler.auth_service.CustomException.DuplicateUsernameException;
import com.twowheeler.auth_service.CustomException.InvalidCredentialsException;
import com.twowheeler.auth_service.CustomException.TokenExpiredException;
import com.twowheeler.auth_service.CustomException.UserBlockedException;
import com.twowheeler.auth_service.Dto.Response.ErrorResponse;

import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Mono<ResponseEntity<ErrorResponse>> buildErrorResponse(HttpStatus status, String message) {
        ErrorResponse error = new ErrorResponse(
                status.value(),
                status.name(),
                message,
                Instant.now());
        return Mono.just(ResponseEntity.status(status).body(error));
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDuplicate(DuplicateUsernameException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidCreds(InvalidCredentialsException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(TokenExpiredException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleTokenExpired(TokenExpiredException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneric(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(ConditionalCheckFailedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUsernameExists(
            ConditionalCheckFailedException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Username already exists");

    }

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleRuntime(RuntimeException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAccessDenied(
            AccessDeniedException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(UserBlockedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUserBlockedException(
            UserBlockedException ex) {
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    }

}
