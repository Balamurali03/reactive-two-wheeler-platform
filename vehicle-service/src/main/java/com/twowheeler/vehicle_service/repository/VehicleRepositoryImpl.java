package com.twowheeler.vehicle_service.repository;

import com.twowheeler.vehicle_service.model.Vehicles;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Slf4j
@Repository
public class VehicleRepositoryImpl implements VehicleRepository {

    private static final String TABLE_NAME = "vehicles";
    private static final String OWNER_GSI = "ownerid-index";

    private final DynamoDbTable<Vehicles> vehicleTable;
    private final DynamoDbIndex<Vehicles> ownerIndex;

    public VehicleRepositoryImpl(DynamoDbEnhancedClient enhancedClient) {
        this.vehicleTable = enhancedClient.table(
                TABLE_NAME,
                TableSchema.fromBean(Vehicles.class)
        );
        this.ownerIndex = vehicleTable.index(OWNER_GSI);
    }

    // ➕ Save vehicle
    @Override
    public Mono<Vehicles> save(Vehicles vehicle) {
        return Mono.fromRunnable(() -> {
                    log.debug("Saving vehicle vehicleId={}", vehicle.getVehicleId());
                    vehicleTable.putItem(vehicle);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .thenReturn(vehicle);
    }

    
    @Override
    public Mono<Vehicles> findById(String vehicleId) {
        return Mono.fromCallable(() -> {
                    log.debug("Fetching vehicle by id={}", vehicleId);
                    return vehicleTable.getItem(
                            r -> r.key(k -> k.partitionValue(vehicleId))
                    );
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(vehicle ->
                        vehicle == null ? Mono.empty() : Mono.just(vehicle)
                );
    }

    // 📄 Find vehicles owned by user (GSI query)
    @Override
public Flux<Vehicles> findOwnedVehicles(String ownerId) {

    return Flux.fromIterable(
            ownerIndex.query(
                    QueryConditional.keyEqualTo(
                            Key.builder()
                                    .partitionValue(ownerId)
                                    .build()
                    )
            )
    ).flatMap(page -> page.count() ==0 ? Flux.empty() : Flux.fromIterable(page.items()));
}


    // 📦 Find all vehicles (TABLE SCAN – ADMIN ONLY)
    @Override
    public Flux<Vehicles> findAllVehicles() {
        return Flux.defer(() -> {
                    log.warn("Scanning all vehicles table");

                    return Flux.fromIterable(
                            vehicleTable.scan()
                                        .items()
                                        .stream()
                                        .toList()
                    );
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
