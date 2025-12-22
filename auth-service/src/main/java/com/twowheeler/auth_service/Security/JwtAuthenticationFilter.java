package com.twowheeler.auth_service.Security;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Autowired
    private JwtUtil util;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String authHeader = exchange.getRequest()
        .getHeaders()
        .getFirst(HttpHeaders.AUTHORIZATION);

        if(authHeader ==null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }
        String token = authHeader.substring(7);
        if(!util.validateToken(token)) {
            return chain.filter(exchange);
        }

        String userName = util.getUsername(token);

        var authorities = util.getRoles(token).stream()
        .map(SimpleGrantedAuthority :: new)
        .collect(Collectors.toList());

        var authentication =
                new UsernamePasswordAuthenticationToken(userName, null, authorities);


        SecurityContext context = new SecurityContextImpl(authentication);

        return chain.filter(exchange)
        .contextWrite(
            ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context))
        );

}

    

}
