package com.twowheeler.auth_service.FunctionalRouter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.twowheeler.auth_service.FunctionalHandler.AuthHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
//import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> authRoutes(AuthHandler handler) {

        return RouterFunctions
                .route(POST("/auth/register"), handler::register)
                .andRoute(POST("/auth/login"), handler::login)
                .andRoute(POST("/auth/refresh"), handler::refresh)
                .andRoute(POST("/welcome/auth"),handler::welcome);
    }

}
