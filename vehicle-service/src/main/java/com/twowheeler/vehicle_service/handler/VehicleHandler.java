package com.twowheeler.vehicle_service.handler;


import com.twowheeler.vehicle_service.model.Vehicles;
import com.twowheeler.vehicle_service.service.VehicleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class VehicleHandler {

    private final VehicleService service;

    public VehicleHandler(VehicleService service) {
        this.service = service;
    }

    // ➕ Create vehicle
    public Mono<ServerResponse> createVehicle(ServerRequest request) {
        String ownerId = request.headers().firstHeader("X-User-Id");
        log.info("Create vehicle request received ownerId={}", ownerId);

        return request.bodyToMono(Vehicles.class)
                .flatMap(vehicle -> service.createVehicle(vehicle, ownerId))
                .flatMap(v -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(v)
                );
    }

    
    public Mono<ServerResponse> getVehicleById(ServerRequest request) {
        String vehicleId = request.pathVariable("vehicleId");
        log.info("Get vehicle request vehicleId={}", vehicleId);

        return service.getVehicleById(vehicleId)
                .flatMap(v -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(v)
                );
    }

    // 📄 Get owned vehicles
    public Mono<ServerResponse> getMyVehicles(ServerRequest request) {
        String ownerId = request.headers().firstHeader("X-User-Id");
        log.info("Get owned vehicles ownerId={}", ownerId);

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.getOwnedVehicles(ownerId), Vehicles.class);
    }

    // 📦 Admin – get all vehicles
    public Mono<ServerResponse> getAllVehicles(ServerRequest request) {
        log.warn("Admin request to fetch all vehicles");

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.getAllVehicles(), Vehicles.class);
    }
}
