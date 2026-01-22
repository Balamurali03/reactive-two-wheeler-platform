package com.twowheeler.vehicle_service.service;




import com.twowheeler.vehicle_service.customException.VehicleNotFoundException;
import com.twowheeler.vehicle_service.model.Vehicles;
import com.twowheeler.vehicle_service.repository.VehicleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class VehicleService {

    private final VehicleRepository repository;

    public VehicleService(VehicleRepository repository) {
        this.repository = repository;
    }

    public Mono<Vehicles> createVehicle(Vehicles vehicle, String ownerId) {
        log.info("Creating vehicle for ownerId={}", ownerId);

        vehicle.setVehicleId(UUID.randomUUID().toString());
        vehicle.setOwnerId(ownerId);
        vehicle.setCreatedAt(Instant.now());
        vehicle.setUpdatedAt(Instant.now());

        return repository.save(vehicle)
                .doOnSuccess(v -> log.info("Vehicle created vehicleId={}", v.getVehicleId()));
    }

    public Mono<Vehicles> getVehicleById(String vehicleId) {
        log.info("Fetching vehicle vehicleId={}", vehicleId);

        return repository.findById(vehicleId)
                .switchIfEmpty(Mono.error(new VehicleNotFoundException(vehicleId)));
    }

    public Flux<Vehicles> getOwnedVehicles(String ownerId) {
        log.info("Fetching vehicles for ownerId={}", ownerId);
        return repository.findOwnedVehicles(ownerId);
    }

    public Flux<Vehicles> getAllVehicles() {
        log.warn("Fetching ALL vehicles (admin use)");
        return repository.findAllVehicles();
    }
}

