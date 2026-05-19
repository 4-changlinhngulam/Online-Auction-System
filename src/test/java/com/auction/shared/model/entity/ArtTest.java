package com.auction.shared.model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArtTest {

    @Test
    void testArtCreation() {
        String id = "ART001";
        String name = "Mona Lisa";
        double startingPrice = 1000000.0;

        Art art = new Art(id, name, startingPrice);

        assertNotNull(art);
        assertEquals(id, art.getId());
        assertEquals(name, art.getName());
        assertEquals(startingPrice, art.getStartingPrice());
        assertEquals("PENDING", art.getStatus());
    }
}
1