package com.auction.shared.model.entity;

import com.auction.shared.model.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminTest {

    @Test
    void testAdminCreation() {
        // Given
        String name = "testadmin";
        String password = "password789";
        String email = "admin@example.com";

        // When
        Admin admin = new Admin(name, password, email);

        // Then
        assertNotNull(admin);
        assertEquals(name, admin.getName());
        assertEquals(password, admin.getPassword());
        assertEquals(email, admin.getEmail());
        assertEquals(UserRole.ADMIN, admin.role);
        assertEquals("ADMIN", admin.getRole());
    }
}
