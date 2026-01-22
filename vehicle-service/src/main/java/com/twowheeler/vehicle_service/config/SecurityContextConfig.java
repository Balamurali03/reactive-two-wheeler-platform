package com.twowheeler.vehicle_service.config;



import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityContextConfig {

    public SecurityWebFilterChain filterChain(ServerHttpSecurity http){
        return
                http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex.anyExchange().permitAll())
                .build();
    }
}
