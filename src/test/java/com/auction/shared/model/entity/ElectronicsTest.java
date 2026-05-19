package com.auction.shared.model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElectronicsTest {

    @Test
    void testElectronicsCreation() {
        // Given
        String id = "ELEC001";
        String name = "MacBook Pro";
        double startingPrice = 1200.0;

        // When
        Electronics electronics = new Electronics(id, name, startingPrice);
        electronics.setWarrantyMonths(12);

        // Then
        assertNotNull(electronics);
        assertEquals(id, electronics.getId());
        assertEquals(name, electronics.getName());
        assertEquals(startingPrice, electronics.getStartingPrice());
        assertEquals("PENDING", electronics.getStatus());
        assertEquals(12, electronics.getWarrantyMonths());
    }
}
