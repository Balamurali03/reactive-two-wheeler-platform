package com.twowheeler.vehicle_service.model;

import java.time.Instant;

import com.twowheeler.vehicle_service.enums.VehicleStatus;
import com.twowheeler.vehicle_service.enums.VehicleType;

import io.github.balamurali03.dynamodb.annotation.DynamoEntity;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@DynamoEntity
@Data
public class Vehicles {

    private String vehicleId;
    private String ownerId;
    private String registrationNumber;
    private VehicleType type;
    private VehicleStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    @DynamoDbPartitionKey
    public String getVehicleId() {
        return vehicleId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "ownerid-index")
    public String getOwnerId() {
        return ownerId;
    }

    @DynamoDbSortKey
    public Instant getCreatedAt(){
        return createdAt;
    }
}
