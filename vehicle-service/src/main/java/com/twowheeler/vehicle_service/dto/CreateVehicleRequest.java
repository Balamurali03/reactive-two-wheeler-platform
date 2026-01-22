package com.twowheeler.vehicle_service.dto;

import com.twowheeler.vehicle_service.enums.VehicleType;

public record CreateVehicleRequest(
        String registrationNumber,
        VehicleType type
) {

}
