package com.twowheeler.vehicle_service.globalExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import com.twowheeler.vehicle_service.customException.VehicleNotFoundException;
import com.twowheeler.vehicle_service.dto.ErrorResponse;

import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Component
@Order(-2)
public class GlobalExceptionHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Unexpected error";

        if (ex instanceof VehicleNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            message = ex.getMessage();
        }

        log.error("Request failed [{} {}] - {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath(),
                message,
                ex
        );

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value()
        );

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes = errorResponse.toString().getBytes();

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(bytes)));
    }
}
