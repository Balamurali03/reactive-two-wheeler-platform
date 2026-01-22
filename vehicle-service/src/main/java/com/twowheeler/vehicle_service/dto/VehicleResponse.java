package com.twowheeler.vehicle_service.dto;

import com.twowheeler.vehicle_service.enums.VehicleStatus;
import com.twowheeler.vehicle_service.enums.VehicleType;

public record VehicleResponse(
        String vehicleId,
        String registrationNumber,
        VehicleType type,
        VehicleStatus status
) {

}
