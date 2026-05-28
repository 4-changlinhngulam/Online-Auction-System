package com.auction.shared.model.entity;

import com.auction.shared.model.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BidderTest {

    @Test
    void testBidderCreation() {
        String name = "testbidder";
        String password = "password123";
        String email = "bidder@example.com";

        Bidder bidder = new Bidder(name, password, email);

        assertNotNull(bidder);
        assertEquals(name, bidder.getName());
        assertEquals(password, bidder.getPassword());
        assertEquals(email, bidder.getEmail());
        assertEquals(UserRole.BIDDER, bidder.role);
        assertEquals("BIDDER", bidder.getRole());
    }
}
