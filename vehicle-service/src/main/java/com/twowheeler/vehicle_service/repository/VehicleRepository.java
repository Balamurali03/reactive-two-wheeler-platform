package com.twowheeler.vehicle_service.repository;

import com.twowheeler.vehicle_service.model.Vehicles;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VehicleRepository {

    public Mono<Vehicles> save(Vehicles vehicle);
    public Mono<Vehicles> findById(String vehicleId);
    public Flux<Vehicles> findOwnedVehicles(String ownerId);
    public Flux<Vehicles> findAllVehicles();
}
