package com.twowheeler.user_service.router;

import com.twowheeler.user_service.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;


@Configuration
public class UserRouter {

    @Bean
    public RouterFunction<ServerResponse> routes(UserHandler handler) {
        return RouterFunctions.route()
                .POST("/users", handler::updateProfile)
                .GET("/users/me", handler::getMe)
                .GET("/users", handler ::getAllUsers)
                .build();
    }
}