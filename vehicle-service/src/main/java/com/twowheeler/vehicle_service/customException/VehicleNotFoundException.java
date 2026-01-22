package com.twowheeler.vehicle_service.customException;

public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(String vehicleId) {
        super("Vehicle not found with id: " + vehicleId);
    }
}
