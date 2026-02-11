package com.twowheeler.vehicle_service.Filter;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdWebFilter implements WebFilter {

    public static final String CORRELATION_ID = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String correlationId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(CORRELATION_ID);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        String finalCorrelationId = correlationId;

        // Add to response headers
        exchange.getResponse()
                .getHeaders()
                .add(CORRELATION_ID, finalCorrelationId);

        return chain.filter(exchange)
                // put into MDC for logging
                .doOnEach(signal -> MDC.put(CORRELATION_ID, finalCorrelationId))
                .doFinally(signalType -> MDC.remove(CORRELATION_ID));
    }
}
