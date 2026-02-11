package com.twowheeler.vehicle_service.router;


import com.twowheeler.vehicle_service.handler.VehicleHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class VehicleRouter {

    @Bean
    public RouterFunction<ServerResponse> vehicleRoutes(VehicleHandler handler) {
        return RouterFunctions.route()
        .POST("/welcome/vehi", handler::welcome)
                .path("/vehicles", builder -> builder
                        .POST("", handler::createVehicle)
                        .GET("/my", handler::getMyVehicles)
                        .GET("/{vehicleId}", handler::getVehicleById)
                        .GET("", handler::getAllVehicles)
                )
                .build();
    }
}
