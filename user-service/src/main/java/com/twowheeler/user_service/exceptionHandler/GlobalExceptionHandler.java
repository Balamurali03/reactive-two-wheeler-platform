package com.twowheeler.user_service.exceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twowheeler.user_service.customException.BadRequestException;
import com.twowheeler.user_service.customException.UnauthorizedException;
import com.twowheeler.user_service.customException.UserAlreadyExistsException;
import com.twowheeler.user_service.customException.UserNotFoundException;
import com.twowheeler.user_service.dto.ErrorResponse;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

@Configuration
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Unexpected error";

        if (ex instanceof UnauthorizedException ue) {
            status = ue.getStatus();
            message = ue.getMessage();
        } else if (ex instanceof UserNotFoundException ne) {
            status = ne.getStatus();
            message = ne.getMessage();
        } else if (ex instanceof BadRequestException bre) {
            status = bre.getStatus();
            message = bre.getMessage();
        } else if (ex instanceof UserAlreadyExistsException uae) {
            status = uae.getStatus();
            message = uae.getMessage();
        }

        ErrorResponse response = ErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                message);

        byte[] bytes;
        try {
            bytes = mapper.writeValueAsBytes(response);
        } catch (Exception e) {
            return Mono.error(e);
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(bytes)));
    }
}
