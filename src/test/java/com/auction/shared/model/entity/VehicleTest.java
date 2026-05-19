package com.auction.shared.model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    @Test
    void testVehicleCreation() {
        // Given
        String id = "VEH001";
        String name = "Ford Mustang";
        double startingPrice = 25000.0;

        // When
        Vehicle vehicle = new Vehicle(id, name, startingPrice);
        vehicle.setMileage(50000);

        // Then
        assertNotNull(vehicle);
        assertEquals(id, vehicle.getId());
        assertEquals(name, vehicle.getName());
        assertEquals(startingPrice, vehicle.getStartingPrice());
        assertEquals("PENDING", vehicle.getStatus());
        assertEquals(50000, vehicle.getMileage());
    }
}
