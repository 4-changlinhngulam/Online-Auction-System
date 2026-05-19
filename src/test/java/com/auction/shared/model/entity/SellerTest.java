package com.auction.shared.model.entity;

import com.auction.shared.model.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SellerTest {

    @Test
    void testSellerCreation() {
        // Given
        String name = "testseller";
        String password = "password456";
        String email = "seller@example.com";

        // When
        Seller seller = new Seller(name, password, email);

        // Then
        assertNotNull(seller);
        assertEquals(name, seller.getName());
        assertEquals(password, seller.getPassword());
        assertEquals(email, seller.getEmail());
        assertEquals(UserRole.SELLER, seller.role);
        assertEquals("SELLER", seller.getRole());
    }
}
